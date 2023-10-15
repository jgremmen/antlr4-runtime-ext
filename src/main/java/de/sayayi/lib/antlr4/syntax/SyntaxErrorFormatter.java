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

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * Interface for formatting syntax error messages based on detailed lexer/parser token, location
 * and context information.
 *
 * @see GenericSyntaxErrorFormatter
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@FunctionalInterface
public interface SyntaxErrorFormatter
{
  /**
   * Format the syntax error.
   *
   * @param startToken  start token representing the beginning of the syntax error, not {@code null}
   * @param stopToken   stop token representing the end of the syntax error, not {@code null}
   * @param cause       optional {@code Exception} instance providing additional information
   *                    on where the syntax error occurred
   *
   * @return  formatted syntax error message, never {@code null}
   */
  @Contract(pure = true)
  @NotNull String format(@NotNull Token startToken, @NotNull Token stopToken, Exception cause);
}
