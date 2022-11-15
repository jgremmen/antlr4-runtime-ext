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

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;
import lombok.var;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static java.lang.Character.isSpaceChar;
import static java.lang.Math.*;
import static java.util.Arrays.fill;
import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 0.3.0
 */
public class GenericSyntaxErrorFormatter implements SyntaxErrorFormatter
{
  private final int tabSize;
  private final int showLinesBefore;
  private final int showLinesAfter;


  public GenericSyntaxErrorFormatter(int tabSize, int showLinesBefore, int showLinesAfter)
  {
    if (tabSize < 1)
      throw new IllegalArgumentException("tabSize must be at least 1");

    this.tabSize = tabSize;
    this.showLinesBefore = max(showLinesBefore, 0);
    this.showLinesAfter = max(showLinesAfter, 0);
  }


  @Override
  public @NotNull String format(@NotNull Token startToken, @NotNull Token stopToken,
                                @NotNull String errorMsg, RecognitionException ex)
  {
    val inputStream = startToken.getInputStream();
    val startStopLocation = getStartStopLocation(startToken, stopToken);

    if (startStopLocation == null || inputStream == null)
      return formatForMissingTokenLocation(errorMsg, ex);

    val startLocation = startStopLocation[0];
    var stopLocation = startStopLocation[1];
    val startLocationLine0 = startLocation.line - 1;
    val stopLocationLine0 = stopLocation.line - 1;

    val lines = inputStream.getText(Interval.of(0, inputStream.size() - 1)).split("\r?\n");
    val stopLine = min(stopLocationLine0 + showLinesAfter, lines.length - 1);

    val lineFormat = getLineFormat(lines.length, stopLine + 1);
    val lineFormatLength = String.format(lineFormat, 1).length();

    val text = new StringBuilder(errorMsg).append(":\n");

    for(int l = max(startLocationLine0 - showLinesBefore, 0); l <= stopLine; l++)
    {
      val line = lines[l];
      val lineChars = getLineCharacters(line);
      var lineLength = lineChars.length;

      text.append(String.format(lineFormat, l + 1)).append(lineChars).append('\n');

      if (l >= startLocationLine0 && l <= stopLocationLine0 &&
          !(l > startLocationLine0 && l < stopLocationLine0 && lineLength == 0))
      {
        if (startLocationLine0 == l)
          lineLength = max(adjustLocation(lineChars, startLocation.charPositionInLine) + 1, lineLength);
        if (stopLocationLine0 == l)
          lineLength = max(adjustLocation(lineChars, stopLocation.charPositionInLine) + 1, lineLength);

        var printMarker = false;
        val marker = getMarker();

        for(int c = -lineFormatLength;
            c < lineLength && !(stopLocationLine0 == l && c > stopLocation.charPositionInLine);
            c++)
        {
          if (c < 0 || (startLocationLine0 == l && c < startLocation.charPositionInLine))
            text.append(' ');
          else
            text.append((printMarker |= c >= lineChars.length || !isSpaceChar(line.charAt(c))) ? marker : ' ');
        }

        if (l < stopLocationLine0)
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
    val s = new StringBuilder();

    line = trimRight(line);

    if (line.indexOf('\t') == -1)
      s.append(line);
    else
    {
      var p = 0;
      val spaces = new char[tabSize];
      fill(spaces, ' ');

      for(val ch: line.toCharArray())
        if (ch == '\t')
        {
          val spacesToAdd = tabSize - (p % tabSize);
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
  protected @NotNull String formatForMissingTokenLocation(@NotNull String errorMsg,
                                                          @SuppressWarnings("unused") RecognitionException ex) {
    return errorMsg;
  }


  @Contract(pure = true)
  protected @NotNull String getLineFormat(int lines, int stopLine) {
    return lines == 1 ? " " : (" %0" + (int)ceil(log10(stopLine + 1.0)) + "d: ");
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


  @Contract(pure = true)
  protected @NotNull Location getStartLocation(@NotNull Token startToken) {
    return new Location(startToken);
  }


  @Contract(pure = true)
  protected @NotNull Location getStopLocation(@NotNull Token stopToken)
  {
    val endLocation = new Location(stopToken);

    if (stopToken.getType() != Token.EOF)
    {
      val text = stopToken.getInputStream()
          .getText(new Interval(stopToken.getStartIndex(), stopToken.getStopIndex()));

      if (!text.isEmpty())
      {
        val chars = text.toCharArray();

        for(int n = 0, l = chars.length - 1; n < l; n++)
        {
          val c = chars[n];
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
    val chars = s.toCharArray();
    int len = chars.length;

    while(len > 0 && chars[len - 1] <= ' ')
      len--;

    return len < chars.length ? new String(chars, 0, len) : s;
  }




  @ToString(doNotUseGetters = true)
  @NoArgsConstructor(access = PRIVATE)
  private static final class Location implements Comparable<Location>
  {
    int line;
    @ToString.Include(name = "pos")
    int charPositionInLine;


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
  }
}