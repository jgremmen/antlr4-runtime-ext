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
 * Builder interface for constructing and reporting syntax errors with detailed location information.
 * <p>
 * This interface provides a fluent API for specifying the location of a syntax error using tokens or syntax tree
 * nodes. It allows you to define the error's start and stop positions, attach a root cause exception, and then report
 * the error. This makes it easy to construct informative error messages that pinpoint exactly where problems occur
 * in parsed input.
 * <p>
 * The builder supports both single-point errors (using {@link #with(Token)}) and range-based errors
 * (using {@link #withStart(Token)} and {@link #withStop(Token)}). You can work with either raw tokens or syntax tree
 * nodes, depending on what's available in your parsing context.
 * <p>
 * Typical usage patterns:
 * <pre>
 *   // For a single-token error
 *   builder.with(token)
 *          .report();
 *
 *   // For a multi-token error range
 *   builder.withStart(startToken)
 *          .withStop(stopToken)
 *          .report();
 *
 *   // With a root cause exception
 *   builder.with(token)
 *          .withCause(exception)
 *          .report();
 * </pre>
 *
 * @since 0.6.0
 *
 * @see SyntaxErrorException
 * @see SyntaxErrorFormatter
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
  default @NotNull SyntaxErrorBuilder with(@NotNull Token token) {
    return withStart(token).withStop(token);
  }


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
   * Builds and throws a syntax error exception with all configured information.
   * <p>
   * This method constructs a {@link SyntaxErrorException} using the start and stop tokens, optional cause, and
   * generates a formatted error message showing the error location. Both start and stop tokens must be set before
   * calling this method.
   * <p>
   * <b>Note:</b> This method always throws an exception and never returns normally. No validation is performed on
   * token positions (e.g., whether start comes before stop), so ensure tokens are set correctly to get meaningful
   * error messages.
   *
   * @throws NullPointerException  if start or stop token has not been set
   * @throws SyntaxErrorException  always thrown with the constructed error details
   */
  @Contract("-> fail")
  void report();
}
