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
 * Generic syntax error exception.
 *
 * @see de.sayayi.lib.antlr4.AbstractAntlr4Parser#createException(Token, Token, String, String, Exception)
 *      AbstractAntlr4Parser#createException(Token, Token, String, String, Exception)
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


  public SyntaxErrorException(@NotNull Token startToken, @NotNull Token stopToken, @NotNull String formattedMessage,
                              @NotNull String errorMessage, Exception cause)
  {
    super(errorMessage, cause);

    this.startToken = startToken;
    this.stopToken = stopToken;
    this.formattedMessage = formattedMessage;
  }


  @Contract(pure = true)
  public String getErrorMessage() {
    return super.getMessage();
  }


  @Override
  public String getMessage() {
    return getErrorMessage() + "\n\n" + formattedMessage;
  }


  @Contract(pure = true)
  public @NotNull Token getStartToken() {
    return startToken;
  }


  @Contract(pure = true)
  public @NotNull Token getStopToken() {
    return stopToken;
  }


  /**
   * Returns a visual representation detailing the exact location where the syntax error occurred. E.g.:
   * <pre>
   *   { "test" : 12, bool: true }
   *                  ~~~~
   * </pre>
   *
   * @return  formatted message, never {@code null}
   *
   * @see SyntaxErrorFormatter
   */
  @Contract(pure = true)
  public @NotNull String getFormattedMessage() {
    return formattedMessage;
  }
}
