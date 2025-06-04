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
import org.antlr.v4.runtime.tree.SyntaxTree;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * An interface for building a syntax error.
 *
 * @since 0.6.0
 */
public interface SyntaxErrorBuilder
{
  /**
   * Provide the start token for the syntax error. This method can be used to either set the start token or
   * modify the start token if it had previously been set.
   *
   * @param token  start token where the syntax error starts, not {@code null}
   *
   * @return  this builder instance, never {@code null}
   *
   * @see #with(Token)
   * @see #with(SyntaxTree)
   */
  @Contract(value = "_ -> this", mutates = "this")
  @NotNull SyntaxErrorBuilder withStart(@NotNull Token token);


  /**
   * Provide the start syntax tree for the syntax error. This method can be used to either set the start token or
   * modify the start token if it had previously been set.
   *
   * @param syntaxTree  start token where the syntax error starts, not {@code null}
   *
   * @return  this builder instance, never {@code null}
   *
   * @see #with(Token)
   * @see #with(SyntaxTree)
   */
  @Contract(value = "_ -> this", mutates = "this")
  @NotNull SyntaxErrorBuilder withStart(@NotNull SyntaxTree syntaxTree);


  /**
   * Provide the stop token for the syntax error. This method can be used to either set the stop token or
   * modify the stop token if it had previously been set.
   *
   * @param token  stop token where the syntax error ends, not {@code null}
   *
   * @return  this builder instance, never {@code null}
   *
   * @see #with(Token)
   * @see #with(SyntaxTree)
   */
  @Contract(value = "_ -> this", mutates = "this")
  @NotNull SyntaxErrorBuilder withStop(@NotNull Token token);


  /**
   * Provide the stop token for the syntax error. This method can be used to either set the stop token or
   * modify the stop token if it had previously been set.
   *
   * @param syntaxTree  stop token where the syntax error ends, not {@code null}
   *
   * @return  this builder instance, never {@code null}
   *
   * @see #with(Token)
   * @see #with(SyntaxTree)
   */
  @Contract(value = "_ -> this", mutates = "this")
  @NotNull SyntaxErrorBuilder withStop(@NotNull SyntaxTree syntaxTree);


  /**
   * Provide the token for the syntax error. This method assumes the provided token is the exact location where
   * the syntax error occurred.
   *
   * @param token  token where the syntax error occurred, not {@code null}
   *
   * @return  this builder instance, never {@code null}
   */
  @Contract(value = "_ -> this", mutates = "this")
  @NotNull SyntaxErrorBuilder with(@NotNull Token token);


  /**
   * Provide the syntax tree node for the syntax error.
   *
   * @param syntaxTree  syntax tree node where the syntax error occurred, not {@code null}
   *
   * @return  this builder instance, never {@code null}
   */
  @Contract(value = "_ -> this", mutates = "this")
  @NotNull SyntaxErrorBuilder with(@NotNull SyntaxTree syntaxTree);


  /**
   * Provide a root cause for the syntax error.
   *
   * @param cause  root cause exception
   *
   * @return  this builder instance, never {@code null}
   */
  @Contract(value = "_ -> this", mutates = "this")
  @NotNull SyntaxErrorBuilder withCause(Exception cause);


  /**
   * Report a syntax error based on the error message and start and stop tokens.
   * <p>
   * In order for this method to succeed, it is required that the start and stop tokens have been set.
   * Please note that no sanity checks regarding the start and stop tokens are performed. E.g., if the
   * start token comes after the stop token, expect the unexpected.
   * <p>
   * This method will always result in an exception being thrown.
   *
   * @throws NullPointerException  if start or stop token is not set
   */
  @Contract("-> fail")
  void report();
}
