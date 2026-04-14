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
package de.sayayi.lib.antlr4.syntax;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * Exception thrown when a syntax error is detected during parsing.
 * <p>
 * This exception encapsulates detailed information about the syntax error, including the tokens marking the error
 * location, a plain error message, and a formatted visual representation showing exactly where in the source the error
 * occurred. This makes it easier for users to identify and fix syntax problems in their input.
 * <p>
 * The exception provides two types of messages:
 * <ul>
 *   <li>A plain error message describing what went wrong</li>
 *   <li>A formatted message showing the error location in context with visual markers</li>
 * </ul>
 *
 * @see de.sayayi.lib.antlr4.AbstractAntlr4Parser#createException(Token, Token, String, String, Exception)
 *      AbstractAntlr4Parser#createException(Token, Token, String, String, Exception)
 * @see SyntaxErrorFormatter
 *
 * @author Jeroen Gremmen
 * @since 0.6.0
 */
@SuppressWarnings("JavadocReference")
public class SyntaxErrorException extends RuntimeException
{
  private final Token startToken;
  private final Token stopToken;
  private final String formattedMessage;


  /**
   * Creates a new syntax error exception with detailed error information.
   * <p>
   * This constructor is typically called by parser implementations when a syntax error is detected.
   * It captures both human-readable error descriptions and the tokens that mark where the error occurred.
   *
   * @param startToken        the token marking the beginning of the syntax error, not {@code null}
   * @param stopToken         the token marking the end of the syntax error, not {@code null}
   * @param formattedMessage  a visual representation showing the error location in context, not {@code null}
   * @param errorMessage      a plain text description of the error, not {@code null}
   * @param cause             the underlying exception that caused this syntax error, may be {@code null}
   */
  public SyntaxErrorException(@NotNull Token startToken, @NotNull Token stopToken, @NotNull String formattedMessage,
                              @NotNull String errorMessage, Exception cause)
  {
    super(errorMessage, cause);

    this.startToken = startToken;
    this.stopToken = stopToken;
    this.formattedMessage = formattedMessage;
  }


  /**
   * Returns the plain text error message describing what went wrong.
   * <p>
   * This message provides a concise description of the syntax error without any visual formatting or location markers.
   * Use {@link #getMessage()} to get the complete error message including the formatted visual representation.
   *
   * @return the plain error message, never {@code null}
   */
  @Contract(pure = true)
  public String getErrorMessage() {
    return super.getMessage();
  }


  /**
   * Returns the complete error message including both the description and formatted location.
   * <p>
   * This method combines the plain error message with the visual representation of where the error occurred in the
   * source, separated by blank lines. This provides the most complete error information for displaying to users.
   *
   * @return  the complete error message with visual error location, never {@code null}
   */
  @Override
  public String getMessage() {
    return getErrorMessage() + "\n\n" + formattedMessage;
  }


  /**
   * Returns the token marking the beginning of the syntax error.
   * <p>
   * This token indicates where the error starts in the source. If the error is at a single position, this will
   * typically be the same as the stop token.
   *
   * @return  the start token, never {@code null}
   */
  @Contract(pure = true)
  public @NotNull Token getStartToken() {
    return startToken;
  }


  /**
   * Returns the token marking the end of the syntax error.
   * <p>
   * This token indicates where the error ends in the source. If the error is at a single position, this will
   * typically be the same as the start token.
   *
   * @return  the stop token, never {@code null}
   */
  @Contract(pure = true)
  public @NotNull Token getStopToken() {
    return stopToken;
  }


  /**
   * Returns a visual representation showing exactly where the syntax error occurred in the source.
   * <p>
   * This formatted message includes a snippet of the source code with visual markers (like tildes) highlighting the
   * exact position or range of the error. This makes it easy for users to locate and understand the problem.
   * For example:
   * <pre>
   *   { "test" : 12, bool: true }
   *                  ~~~~
   * </pre>
   *
   * @return  the formatted error location message, never {@code null}
   *
   * @see SyntaxErrorFormatter
   */
  @Contract(pure = true)
  public @NotNull String getFormattedMessage() {
    return formattedMessage;
  }
}
