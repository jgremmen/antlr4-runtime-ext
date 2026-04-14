/*
 * Copyright 2022 Jeroen Gremmen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sayayi.lib.antlr4.syntax;

import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import static java.lang.Character.isSpaceChar;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.*;
import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;
import static java.util.Objects.requireNonNull;
import static org.antlr.v4.runtime.Token.EOF;


/**
 * A configurable syntax error formatter that produces human-readable error messages with visual context.
 * <p>
 * This formatter generates error messages that include surrounding source code lines with line numbers and visual
 * markers (typically carets) pointing to the exact location of the syntax error. The output helps users quickly
 * identify and understand what went wrong in their input.
 * <p>
 * The formatter is highly configurable, allowing control over:
 * <ul>
 *   <li>Tab size for proper alignment when displaying source code</li>
 *   <li>Number of context lines to show before and after the error</li>
 *   <li>Indentation or prefix for the formatted output</li>
 *   <li>Line number formatting and marker characters</li>
 * </ul>
 * <p>
 * Example output:
 * <pre>
 * 5: { "test" : 12, bool: true }
 *                   ^^^^
 * </pre>
 *
 * @author Jeroen Gremmen
 * @since 0.3.0
 *
 * @see SyntaxErrorFormatter
 */
public class GenericSyntaxErrorFormatter implements SyntaxErrorFormatter
{
  private final int tabSize;
  private final int showLinesBefore;
  private final int showLinesAfter;
  private final String prefix;


  /**
   * Creates a formatter with the specified configuration using an indentation level.
   * <p>
   * The indent parameter specifies how many spaces to prepend before each line of the formatted output.
   *
   * @param tabSize          the number of spaces to use when expanding tab characters, must be at least 1
   * @param showLinesBefore  the number of context lines to show before the error line
   * @param showLinesAfter   the number of context lines to show after the error line
   * @param indent           the number of spaces to indent the formatted output, must be non-negative
   *
   * @throws IllegalArgumentException  if {@code tabSize} is less than 1 or {@code indent} is negative
   */
  public GenericSyntaxErrorFormatter(int tabSize, int showLinesBefore, int showLinesAfter, int indent) {
    this(tabSize, showLinesBefore, showLinesAfter, prefixFromIndent(indent));
  }


  /**
   * Creates a formatter with the specified configuration using a custom prefix string.
   * <p>
   * The prefix is prepended to each line of the formatted output, allowing for custom indentation or decorative
   * elements.
   *
   * @param tabSize          the number of spaces to use when expanding tab characters, must be at least 1
   * @param showLinesBefore  the number of context lines to show before the error line
   * @param showLinesAfter   the number of context lines to show after the error line
   * @param prefix           the string to prepend to each output line, not {@code null}
   *
   * @throws IllegalArgumentException  if {@code tabSize} is less than 1
   * @throws NullPointerException      if {@code prefix} is {@code null}
   */
  public GenericSyntaxErrorFormatter(int tabSize, int showLinesBefore, int showLinesAfter, @NotNull String prefix)
  {
    if (tabSize < 1)
      throw new IllegalArgumentException("tabSize must be at least 1");

    this.tabSize = tabSize;
    this.showLinesBefore = clamp(showLinesBefore, 0, 0x3fff_ffff);
    this.showLinesAfter = clamp(showLinesAfter, 0, 0x3fff_ffff);
    this.prefix = requireNonNull(prefix, "prefix must not be null");
  }


  /**
   * {@inheritDoc}
   * <p>
   * This implementation generates a multi-line message showing the relevant source code lines with line numbers and
   * visual markers indicating where the error occurred. The error can span a single token or multiple tokens across
   * lines.
   */
  @Override
  public @NotNull String format(@NotNull Token startToken, @NotNull Token stopToken, Exception cause)
  {
    final var inputStream = startToken.getInputStream();
    final var startStopLocation = getStartStopLocation(startToken, stopToken);

    if (startStopLocation == null || inputStream == null)
      return formatForMissingTokenLocation(cause);

    final var startLocation = startStopLocation[0];
    final var stopLocation = startStopLocation[1];
    final var startLine0Based = startLocation.line - 1;
    final var stopLine0Based = stopLocation.line - 1;

    final var lines = inputStream
        .getText(Interval.of(0, inputStream.size() - 1))
        .split("\r?\n");
    final var formatStopLine0Based = min(stopLine0Based + showLinesAfter, lines.length - 1);

    final var lineNumberFormatter = getLineNumberFormatter(lines.length, formatStopLine0Based + 1);
    final var lineNumberFormatLength = lineNumberFormatter.format(1, false).length();
    final var text = new StringBuilder();

    for(var l = max(startLine0Based - showLinesBefore, 0); l <= formatStopLine0Based; l++)
    {
      final var line = lines[l];
      final var lineChars = getLineCharacters(line);
      final var markedLine = l >= startLine0Based && l <= stopLine0Based;
      var lineLength = lineChars.length;

      text.append(prefix).append(lineNumberFormatter.format(l + 1, markedLine)).append(lineChars).append('\n');

      if (markedLine && !(l > startLine0Based && l < stopLine0Based && lineLength == 0))
      {
        text.append(prefix);

        if (startLine0Based == l)
          lineLength = max(adjustLocation(lineChars, startLocation.charPositionInLine) + 1, lineLength);
        if (stopLine0Based == l)
          lineLength = max(adjustLocation(lineChars, stopLocation.charPositionInLine) + 1, lineLength);

        final var marker = getMarker();
        var printMarker = false;

        for(var c = -lineNumberFormatLength;
            c < lineLength && !(stopLine0Based == l && c > stopLocation.charPositionInLine);
            c++)
        {
          if (c < 0 || (startLine0Based == l && c < startLocation.charPositionInLine))
            text.append(' ');
          else
            text.append((printMarker |= c >= lineChars.length || !isSpaceChar(line.charAt(c))) ? marker : ' ');
        }

        if (l < stopLine0Based)
          while(text.charAt((lineLength = text.length()) - 1) == ' ')
            text.delete(lineLength - 1, lineLength);

        text.append('\n');
      }
    }

    return text.toString();
  }


  @Contract(pure = true)
  private char[] getLineCharacters(@NotNull String line)
  {
    final var s = new StringBuilder();

    line = trimRight(line);

    if (line.indexOf('\t') == -1)
      s.append(line);
    else
    {
      var p = 0;
      var spaces = new char[tabSize];
      fill(spaces, ' ');

      for(var ch: line.toCharArray())
        if (ch == '\t')
        {
          var spacesToAdd = tabSize - (p % tabSize);
          s.append(spaces, 0, spacesToAdd);
          p += spacesToAdd;
        }
        else
        {
          s.append(ch);
          p++;
        }
    }

    return s.toString().toCharArray();
  }


  /**
   * Provides a fallback error message when token location information is not available.
   * <p>
   * This method is called when the tokens don't have valid location information. The default implementation returns
   * the exception's string representation for lexer exceptions, or an empty string otherwise. Subclasses can override
   * this to provide custom fallback formatting.
   *
   * @param ex  the exception that occurred, may be {@code null}
   *
   * @return  a fallback error message, never {@code null}
   */
  @Contract(pure = true)
  protected @NotNull String formatForMissingTokenLocation(Exception ex)
  {
    if (ex instanceof LexerNoViableAltException)
      return ex.toString();

    return "";
  }


  /**
   * Creates a line number formatter.
   * <p>
   * The default implementation returns a formatter that produces "{@code <line>: }", where {@code <line>} is the
   * zero-padded line number, e.g. "{@code 06: }".
   *
   * @param lines     the number of lines that were supposed to be parsed ({@code 0}..{@code n})
   * @param stopLine  the highest line number to be displayed by the syntax error formatter ({@code 1}..{@code n})
   *
   * @return  line number formatter instance, never {@code null}
   *
   * @since 0.5.2
   */
  @Contract(pure = true)
  protected @NotNull LineNumberFormatter getLineNumberFormatter(int lines, int stopLine)
  {
    if (lines <= 1 || stopLine <= 1)
      return (lineNumber, markedLine) -> "";

    // digits = floor(1 + log10(stopLine))
    var digits = 1;
    for(var upperLimit = 10; stopLine >= upperLimit; digits++)
      upperLimit *= 10;

    return new DefaultLineNumberFormatter(digits, '0', null, ": ");
  }


  /**
   * Adjusts a character position to account for tab expansion.
   * <p>
   * This method calculates the actual display column of a character when tabs are expanded to spaces based on the
   * configured tab size. This ensures that error markers align correctly with the displayed source code.
   *
   * @param line                the line characters (after tab expansion), not {@code null}
   * @param charPositionInLine  the original character position in the source line
   *
   * @return  the adjusted display column position
   */
  @Contract(pure = true)
  protected int adjustLocation(char[] line, int charPositionInLine)
  {
    var p = 0;

    for(var n = 0; n < line.length && n < charPositionInLine; n++)
      if (line[n] == '\t')
        p = ((p / tabSize) + 1) * tabSize;
      else
        p++;

    return max(p, charPositionInLine);
  }


  @Contract(pure = true)
  private Location[] getStartStopLocation(@NotNull Token startToken, @NotNull Token stopToken)
  {
    var startLocation = getStartLocation(startToken);
    var stopLocation = getStopLocation(stopToken);

    if (!startLocation.isValid() && !stopLocation.isValid())
      return null;

    if (startLocation.isValid() && !stopLocation.isValid())
      stopLocation = startLocation;
    else if (!startLocation.isValid() || stopLocation.compareTo(startLocation) < 0)
      startLocation = stopLocation;

    return new Location[] { startLocation, stopLocation };
  }


  /**
   * Extracts the starting location from a token.
   * <p>
   * Subclasses can override this to customize how start locations are determined.
   *
   * @param startToken  the token marking the error start, not {@code null}
   *
   * @return  the start location, never {@code null}
   */
  @Contract(pure = true)
  protected @NotNull Location getStartLocation(@NotNull Token startToken) {
    return new Location(startToken);
  }


  /**
   * Extracts the stopping location from a token.
   * <p>
   * For multi-character and multi-line tokens, this method adjusts the position to point to the last character of
   * the token. Subclasses can override this to customize how stop locations are determined.
   *
   * @param stopToken  the token marking the error end, not {@code null}
   *
   * @return  the stop location, never {@code null}
   */
  @Contract(pure = true)
  protected @NotNull Location getStopLocation(@NotNull Token stopToken)
  {
    final var endLocation = new Location(stopToken);

    if (stopToken.getType() != EOF)
    {
      final var text = stopToken
          .getInputStream()
          .getText(new Interval(stopToken.getStartIndex(), stopToken.getStopIndex()));

      if (!text.isEmpty())
      {
        final var chars = text.toCharArray();

        for(int n = 0, l = chars.length - 1; n < l; n++)
        {
          final var c = chars[n];
          if (c != '\r')
          {
            if (c == '\n')
            {
              endLocation.line++;
              endLocation.charPositionInLine = 0;
            }
            else
              endLocation.charPositionInLine++;
          }
        }
      }
    }

    return endLocation;
  }


  /**
   * Returns the character used for marking error locations in the formatted output.
   * <p>
   * The default implementation returns {@code ^}. Subclasses can override this to use a different marker character.
   *
   * @return  the marker character
   */
  @Contract(pure = true)
  protected char getMarker() {
    return '^';
  }


  @Contract(pure = true)
  private @NotNull String trimRight(@NotNull String s)
  {
    final var chars = s.toCharArray();
    var len = chars.length;

    while(len > 0 && chars[len - 1] <= ' ')
      len--;

    return len < chars.length ? new String(chars, 0, len) : s;
  }


  @Contract(pure = true)
  private static @NotNull String prefixFromIndent(int indent)
  {
    if (indent < 0)
      throw new IllegalArgumentException("indent must be at least 0");

    if (indent == 0)
      return "";

    final var spaces = new char[indent];
    fill(spaces, ' ');

    return new String(spaces);
  }




  /**
   * Represents a position in the source code defined by a line number and character offset.
   * <p>
   * Instances are used internally by the formatter to track where errors start and end, and to correctly position
   * visual markers in the formatted output.
   */
  protected static final class Location implements Comparable<Location>
  {
    private int line;
    private int charPositionInLine;


    /**
     * Creates a location from a token's line and character position.
     *
     * @param token  the token to extract position information from, not {@code null}
     */
    protected Location(@NotNull Token token)
    {
      line = token.getLine();
      charPositionInLine = token.getCharPositionInLine();
    }


    /**
     * Checks whether this location has valid line and position values.
     *
     * @return  {@code true} if the line number is at least 1 and the character position is non-negative
     */
    boolean isValid() {
      return line >= 1 && charPositionInLine >= 0;
    }


    /**
     * Compares this location to another, ordering by line number first and then by character position.
     *
     * @param location  the location to compare to, not {@code null}
     *
     * @return  a negative value if this location precedes {@code location}, a positive value if it follows,
     *          or zero if both locations are equal
     */
    @Override
    public int compareTo(@NotNull Location location)
    {
      return line < location.line ? -1 : line > location.line ? 1 :
          Integer.compare(charPositionInLine, location.charPositionInLine);
    }


    /**
     * Returns a string representation of this location for debugging purposes.
     *
     * @return  a string describing the line and position, or an indication that the location is invalid
     */
    @Override
    public String toString()
    {
      if (isValid())
        return "Location(line=" + line + ",pos=" + (charPositionInLine + 1) + ')';
      else
        return "Location(<invalid>)";
    }
  }




  /**
   * Strategy interface for formatting line numbers in error output.
   * <p>
   * Implementations control how line numbers are displayed in the formatted error messages. The {@code format}
   * method must always return a string of fixed length to ensure proper alignment of all output lines.
   *
   * @author Jeroen Gremmen
   * @since 0.5.2
   *
   * @see GenericSyntaxErrorFormatter#getLineNumberFormatter(int, int)
   */
  @FunctionalInterface
  public interface LineNumberFormatter
  {
    /**
     * Format the given {@code lineNumber}. This method must return a string of a fixed length, regardless of the
     * line number being formatted. 
     * <p>
     * Note: the highest line number must be known to create a suitable implementation.
     * 
     * @param lineNumber  line number to format ({@code 1}..{@code n})
     * @param markedLine  indicates whether the line contains an error marker
     * 
     * @return  the formatted line number, never {@code null}
     *
     * @see GenericSyntaxErrorFormatter#getLineNumberFormatter(int, int) 
     */
    @Contract(pure = true)
    @NotNull String format(@Range(from = 1, to = MAX_VALUE) int lineNumber, boolean markedLine);
  }




  /**
   * Default implementation of line number formatting with configurable padding and decorations.
   * <p>
   * This formatter produces line numbers with zero-padding (or other padding characters), optional prefix and suffix
   * strings. For example, it can format line numbers as "001: ", " 42: ", etc., with consistent width for proper
   * alignment.
   *
   * @author Jeroen Gremmen
   * @since 0.5.2
   */
  public static class DefaultLineNumberFormatter implements LineNumberFormatter
  {
    protected final int prefixLength;
    protected final char[] chars;
    protected final int lineNumberWidth;
    protected final char paddingChar;


    /**
     * Creates a line number formatter with the specified configuration.
     * <p>
     * The formatter pads line numbers to a fixed width using the padding character, and optionally adds prefix and
     * suffix strings.
     *
     * @param lineNumberWidth  the width (number of digits) for the line number field, 0 to 10
     * @param paddingChar      the character to use for padding (typically '0' or ' ')
     * @param prefix           optional string to prepend before the line number, may be {@code null}
     * @param suffix           optional string to append after the line number, may be {@code null}
     */
    public DefaultLineNumberFormatter(@Range(from = 0, to = 10) int lineNumberWidth, char paddingChar,
                                      String prefix, String suffix)
    {
      this.lineNumberWidth = lineNumberWidth;
      this.paddingChar = paddingChar;

      var suffixLength = suffix == null ? 0 : suffix.length();

      prefixLength = prefix == null ? 0 : prefix.length();
      chars = new char[prefixLength + lineNumberWidth + suffixLength];

      if (prefixLength > 0)
        arraycopy(prefix.toCharArray(), 0, chars, 0, prefixLength);
      if (suffixLength > 0)
        arraycopy(suffix.toCharArray(), 0, chars, prefixLength + lineNumberWidth, suffixLength);
    }


    /**
     * Formats a line number with padding, prefix, and suffix.
     * <p>
     * The line number is padded to the configured width and combined with the prefix and suffix to produce a
     * fixed-length string. The {@code markedLine} parameter is not used by this default implementation but is
     * available for subclasses to override.
     *
     * @param lineNumber  the line number to format, must be positive
     * @param markedLine  {@code true} if this line contains an error marker (not used in default implementation)
     *
     * @return  the formatted line number with fixed length, never {@code null}
     */
    @Override
    public @NotNull String format(@Range(from = 1, to = MAX_VALUE) int lineNumber, boolean markedLine)
    {
      for(var n = lineNumberWidth; n-- > 0; lineNumber /= 10)
      {
        chars[prefixLength + n] = n < lineNumberWidth - 1 && lineNumber == 0
            ? paddingChar
            : (char)((lineNumber % 10) + '0');
      }

      return new String(chars);
    }
  }
}
