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
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Interval;
import org.jetbrains.annotations.NotNull;


/**
 * Minimal {@link Token} implementation that provides location information only.
 * <p>
 * This class is primarily intended for use with syntax error formatting, where a lightweight token is needed to
 * convey the position of an error in the source input. It does not carry parser-related metadata such as token type,
 * channel, or token index, which are returned as constant default values.
 *
 * @author Jeroen Gremmen
 * @since 0.5.3
 *
 * @see de.sayayi.lib.antlr4.syntax.SyntaxErrorFormatter
 */
public class LocationToken implements Token
{
  private final CharStream inputStream;
  private final int line;
  private final int charPositionInLine;
  private final int startIndex;
  private final int stopIndex;


  /**
   * Creates a new location token from the given parser rule context.
   * <p>
   * The token's position is derived from the start token of the context, while the stop index
   * is taken from the context's stop token.
   *
   * @param ctx  the parser rule context to extract location information from, not {@code null}
   *
   * @since 0.7.1
   */
  public LocationToken(@NotNull ParserRuleContext ctx)
  {
    final var start = ctx.getStart();

    this.inputStream = start.getInputStream();
    this.line = start.getLine();
    this.charPositionInLine = start.getCharPositionInLine();
    this.startIndex = start.getStartIndex();
    this.stopIndex = ctx.getStop().getStopIndex();
  }


  /**
   * Creates a new location token with the given source position.
   *
   * @param inputStream         the character stream that this token originates from, not {@code null}
   * @param line                the 1-based line number of the token
   * @param charPositionInLine  the 0-based character offset within the line
   * @param startIndex          the start index of the token in the input stream
   * @param stopIndex           the stop index of the token in the input stream (inclusive)
   */
  public LocationToken(@NotNull CharStream inputStream, int line, int charPositionInLine,
                       int startIndex, int stopIndex)
  {
    this.inputStream = inputStream;
    this.line = line;
    this.charPositionInLine = charPositionInLine;
    this.startIndex = startIndex;
    this.stopIndex = stopIndex;
  }


  /**
   * {@inheritDoc}
   *
   * @return  the character stream this token originates from, never {@code null}
   */
  @Override
  public CharStream getInputStream() {
    return inputStream;
  }


  /**
   * {@inheritDoc}
   *
   * @return  the 1-based line number of this token
   */
  @Override
  public int getLine() {
    return line;
  }


  /**
   * {@inheritDoc}
   *
   * @return  the 0-based character offset within the line
   */
  @Override
  public int getCharPositionInLine() {
    return charPositionInLine;
  }


  /**
   * {@inheritDoc}
   *
   * @return  the start index of this token in the input stream
   */
  @Override
  public int getStartIndex() {
    return startIndex;
  }


  /**
   * {@inheritDoc}
   *
   * @return  the stop index (inclusive) of this token in the input stream
   */
  @Override
  public int getStopIndex() {
    return stopIndex;
  }


  /**
   * {@inheritDoc}
   * <p>
   * Returns the text covered by this token's index range from the underlying input stream.
   *
   * @return  the text spanned by this token
   */
  @Override
  public String getText() {
    return inputStream.getText(new Interval(startIndex, stopIndex));
  }


  /**
   * {@inheritDoc}
   *
   * @return  {@link #INVALID_TYPE}
   */
  @Override
  public int getType() {
    return INVALID_TYPE;
  }


  /**
   * {@inheritDoc}
   *
   * @return  {@link #DEFAULT_CHANNEL}
   */
  @Override
  public int getChannel() {
    return DEFAULT_CHANNEL;
  }


  /**
   * {@inheritDoc}
   *
   * @return  {@code -1}
   */
  @Override
  public int getTokenIndex() {
    return -1;
  }


  /**
   * {@inheritDoc}
   *
   * @return  {@code null}
   */
  @Override
  public TokenSource getTokenSource() {
    return null;
  }


  /**
   * Returns a string representation of this token, formatted similarly to ANTLR's {@code CommonToken}.
   *
   * @return  a string containing the token's index range, text, line, and character position
   */
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
