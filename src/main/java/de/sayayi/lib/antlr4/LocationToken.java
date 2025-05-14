/*
 * Copyright 2025 Jeroen Gremmen
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
package de.sayayi.lib.antlr4;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Interval;
import org.jetbrains.annotations.NotNull;


/**
 * Minimal token implementation to be used for syntax error formatting, providing location information only.
 *
 * @author Jeroen Gremmen
 * @since 0.5.3
 */
public class LocationToken implements Token
{
  private final CharStream inputStream;
  private final int line;
  private final int charPositionInLine;
  private final int startIndex;
  private final int stopIndex;


  public LocationToken(@NotNull CharStream inputStream, int line, int charPositionInLine,
                       int startIndex, int stopIndex)
  {
    this.inputStream = inputStream;
    this.line = line;
    this.charPositionInLine = charPositionInLine;
    this.startIndex = startIndex;
    this.stopIndex = stopIndex;
  }


  @Override
  public CharStream getInputStream() {
    return inputStream;
  }


  @Override
  public int getLine() {
    return line;
  }


  @Override
  public int getCharPositionInLine() {
    return charPositionInLine;
  }


  @Override
  public int getStartIndex() {
    return startIndex;
  }


  @Override
  public int getStopIndex() {
    return stopIndex;
  }


  @Override
  public String getText() {
    return inputStream.getText(new Interval(startIndex, stopIndex));
  }


  @Override
  public int getType() {
    return INVALID_TYPE;
  }


  @Override
  public int getChannel() {
    return DEFAULT_CHANNEL;
  }


  @Override
  public int getTokenIndex() {
    return -1;
  }


  @Override
  public TokenSource getTokenSource() {
    return null;
  }


  @Override
  public String toString()
  {
    var text = getText();
    if (text != null)
    {
      text = '\'' + text
          .replace("\n","\\n")
          .replace("\r","\\r")
          .replace("\t","\\t") + '\'';
    }
    else
      text = "<no text>";

    // similar formatting as in CommonToken
    return "[@-1," + startIndex + ':' + stopIndex + '=' + text + ',' + line + ':' + charPositionInLine + ']';
  }
}
