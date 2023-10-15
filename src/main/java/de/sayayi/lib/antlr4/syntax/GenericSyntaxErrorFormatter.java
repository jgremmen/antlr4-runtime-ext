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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static java.lang.Character.isSpaceChar;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.fill;
import static java.util.Objects.requireNonNull;
import static org.antlr.v4.runtime.Token.EOF;


/**
 * @author Jeroen Gremmen
 * @since 0.3.0
 */
public class GenericSyntaxErrorFormatter implements SyntaxErrorFormatter
{
  private final int tabSize;
  private final int showLinesBefore;
  private final int showLinesAfter;
  private final String prefix;


  public GenericSyntaxErrorFormatter(int tabSize, int showLinesBefore, int showLinesAfter, int indent) {
    this(tabSize, showLinesBefore, showLinesAfter, prefixFromIndent(indent));
  }


  public GenericSyntaxErrorFormatter(int tabSize, int showLinesBefore, int showLinesAfter,
                                     @NotNull String prefix)
  {
    if (tabSize < 1)
      throw new IllegalArgumentException("tabSize must be at least 1");

    this.tabSize = tabSize;
    this.showLinesBefore = min(max(showLinesBefore, 0), 0x3fff_ffff);
    this.showLinesAfter = min(max(showLinesAfter, 0), 0x3fff_ffff);
    this.prefix = requireNonNull(prefix, "prefix must not be null");
  }


  @Override
  public @NotNull String format(@NotNull Token startToken, @NotNull Token stopToken,
                                Exception cause)
  {
    final CharStream inputStream = startToken.getInputStream();
    final Location[] startStopLocation = getStartStopLocation(startToken, stopToken);

    if (startStopLocation == null || inputStream == null)
      return formatForMissingTokenLocation(cause);

    final Location startLocation = startStopLocation[0];
    final Location stopLocation = startStopLocation[1];
    final int startLine0Based = startLocation.line - 1;
    final int stopLine0Based = stopLocation.line - 1;

    final String[] lines = inputStream
        .getText(Interval.of(0, inputStream.size() - 1))
        .split("\r?\n");
    final int formatStopLine0Based = min(stopLine0Based + showLinesAfter, lines.length - 1);

    final String lineNumberFormat = getLineNumberFormat(lines.length, formatStopLine0Based + 1);
    final int lineNumberFormatLength = String.format(lineNumberFormat, 1).length();

    final StringBuilder text = new StringBuilder();

    for(int l = max(startLine0Based - showLinesBefore, 0); l <= formatStopLine0Based; l++)
    {
      final String line = lines[l];
      final char[] lineChars = getLineCharacters(line);
      int lineLength = lineChars.length;

      text.append(prefix).append(String.format(lineNumberFormat, l + 1)).append(lineChars).append('\n');

      if (l >= startLine0Based && l <= stopLine0Based &&
          !(l > startLine0Based && l < stopLine0Based && lineLength == 0))
      {
        text.append(prefix);

        if (startLine0Based == l)
          lineLength = max(adjustLocation(lineChars, startLocation.charPositionInLine) + 1, lineLength);
        if (stopLine0Based == l)
          lineLength = max(adjustLocation(lineChars, stopLocation.charPositionInLine) + 1, lineLength);

        boolean printMarker = false;
        final char marker = getMarker();

        for(int c = -lineNumberFormatLength;
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
    final StringBuilder s = new StringBuilder();

    line = trimRight(line);

    if (line.indexOf('\t') == -1)
      s.append(line);
    else
    {
      int p = 0;
      final char[] spaces = new char[tabSize];
      fill(spaces, ' ');

      for(char ch: line.toCharArray())
        if (ch == '\t')
        {
          final int spacesToAdd = tabSize - (p % tabSize);
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


  @Contract(pure = true)
  protected @NotNull String formatForMissingTokenLocation(Exception ex)
  {
    if (ex instanceof LexerNoViableAltException)
      return ex.toString();

    return "";
  }


  @Contract(pure = true)
  protected @NotNull String getLineNumberFormat(int lines, int stopLine)
  {
    if (lines > 1)
    {
      int digits = 1;

      for(int upperLimit = 10; stopLine >= upperLimit && digits <= 9; digits++)
        upperLimit *= 10;

      return "%0" + digits + "d: ";
    }
    else
      return "";
  }


  @Contract(pure = true)
  protected int adjustLocation(char[] line, int charPositionInLine)
  {
    int p = 0;

    for(int n = 0; n < line.length && n < charPositionInLine; n++)
      if (line[n] == '\t')
        p = ((p / tabSize) + 1) * tabSize;
      else
        p++;

    return max(p, charPositionInLine);
  }


  @Contract(pure = true)
  private Location[] getStartStopLocation(@NotNull Token startToken, @NotNull Token stopToken)
  {
    Location startLocation = getStartLocation(startToken);
    Location stopLocation = getStopLocation(stopToken);

    if (!startLocation.isValid() && !stopLocation.isValid())
      return null;

    if (startLocation.isValid() && !stopLocation.isValid())
      stopLocation = startLocation;
    else if (!startLocation.isValid() || stopLocation.compareTo(startLocation) < 0)
      startLocation = stopLocation;

    return new Location[] { startLocation, stopLocation };
  }


  @Contract(pure = true)
  protected @NotNull Location getStartLocation(@NotNull Token startToken) {
    return new Location(startToken);
  }


  @Contract(pure = true)
  protected @NotNull Location getStopLocation(@NotNull Token stopToken)
  {
    final Location endLocation = new Location(stopToken);

    if (stopToken.getType() != EOF)
    {
      final String text = stopToken
          .getInputStream()
          .getText(new Interval(stopToken.getStartIndex(), stopToken.getStopIndex()));

      if (!text.isEmpty())
      {
        final char[] chars = text.toCharArray();

        for(int n = 0, l = chars.length - 1; n < l; n++)
        {
          final char c = chars[n];
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


  @Contract(pure = true)
  protected char getMarker() {
    return '^';
  }


  @Contract(pure = true)
  private @NotNull String trimRight(@NotNull String s)
  {
    final char[] chars = s.toCharArray();
    int len = chars.length;

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

    final char[] spaces = new char[indent];
    fill(spaces, ' ');

    return new String(spaces);
  }




  protected static final class Location implements Comparable<Location>
  {
    private int line;
    private int charPositionInLine;


    private Location(@NotNull Token token)
    {
      line = token.getLine();
      charPositionInLine = token.getCharPositionInLine();
    }


    boolean isValid() {
      return line >= 1 && charPositionInLine >= 0;
    }


    @Override
    public int compareTo(@NotNull Location location)
    {
      return line < location.line ? -1 : line > location.line ? 1 :
          Integer.compare(charPositionInLine, location.charPositionInLine);
    }


    @Override
    public String toString()
    {
      if (isValid())
        return "Location(line=" + line + ",pos=" + (charPositionInLine + 1) + ')';
      else
        return "Location(<invalid>)";
    }
  }
}
