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
package de.sayayi.lib.antlr4;

import de.sayayi.lib.antlr4.syntax.GenericSyntaxErrorFormatter;
import de.sayayi.lib.antlr4.syntax.SyntaxErrorFormatter;
import de.sayayi.lib.antlr4.walker.Walker;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.SyntaxTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static de.sayayi.lib.antlr4.walker.Walker.WALK_FULL_RECURSIVE;
import static java.util.Objects.requireNonNull;


/**
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@SuppressWarnings({"SameParameterValue", "unused"})
public abstract class AbstractAntlr4Parser
{
  private final SyntaxErrorFormatter syntaxErrorFormatter;
  private final boolean keepConsoleErrorListeners;


  protected AbstractAntlr4Parser() {
    this(new GenericSyntaxErrorFormatter(8, 0, 0, " "));
  }


  protected AbstractAntlr4Parser(@NotNull SyntaxErrorFormatter syntaxErrorFormatter) {
    this(syntaxErrorFormatter, false);
  }


  /**
   * @since 0.5.3
   */
  protected AbstractAntlr4Parser(@NotNull SyntaxErrorFormatter syntaxErrorFormatter,
                                 boolean keepConsoleErrorListeners)
  {
    this.syntaxErrorFormatter = syntaxErrorFormatter;
    this.keepConsoleErrorListeners = keepConsoleErrorListeners;
  }


  @Contract(mutates = "param1")
  protected <L extends TokenSource,P extends Parser,C extends ParserRuleContext,R>
      R parse(@NotNull L lexer, @NotNull Function<L,P> parserInstantiator, @NotNull Function<P,C> ruleExecutor,
              @NotNull ParseTreeListener listener, @NotNull Function<C,R> contextResultExtractor) {
    return contextResultExtractor.apply(walk(listener, parse(lexer, parserInstantiator, ruleExecutor)));
  }


  /**
   * Parse the input using the given lexer and parser supplier.
   * <p>
   * This method uses the {@code parserSupplier} to create a parser instance based on the provided {@code lexer}
   * and invokes the {@code ruleExecutor}. The result from the rule executor is returned.
   * <p>
   * Syntax errors are handled appropriately and always result in an exception being thrown.
   * <p>
   * By default, the {@link ConsoleErrorListener}s automatically added by Antlr4 to the lexer and parser instances
   * are removed as they pollute the console output and do not add any value in complex application scenarios.
   * If these console error listeners should be kept, set the {@code keepConsoleErrorListeners} constructor parameter
   * to {@code true}.
   *
   * @param lexer               lexer instance, not {@code null}
   * @param parserInstantiator  parser instantiator, not {@code null}
   * @param ruleExecutor        rule executor, not {@code null}
   *
   * @return  parser rule context returned by the executed rule, never {@code null}
   *
   * @param <L>  TokenSource type, usually the generated {@code Lexer} type
   * @param <P>  Parser type, usually the generated {@code Parser} type
   * @param <C>  ParserRuleContext type returned by the executed rule
   *
   * @see #AbstractAntlr4Parser(SyntaxErrorFormatter, boolean)
   * @see #createException(Token, Token, String, String, Exception)
   */
  @Contract(value = "_, _, _ -> new", mutates = "param1")
  protected <L extends TokenSource,P extends Parser,C extends ParserRuleContext>
      @NotNull C parse(@NotNull L lexer, @NotNull Function<L,P> parserInstantiator, @NotNull Function<P,C> ruleExecutor)
  {
    var errorListener = new BaseErrorListener() {
      @Override
      public void syntaxError(@NotNull Recognizer<?,?> recognizer, Object offendingSymbol, int line,
                              int charPositionInLine, String msg, RecognitionException ex)
      {
        if (offendingSymbol == null && recognizer instanceof Lexer)
        {
          final var lexer = (Lexer)recognizer;
          final var inputStream = lexer.getInputStream();

          offendingSymbol =
              new LocationToken(inputStream, line, charPositionInLine, lexer._tokenStartCharIndex, inputStream.index());
        }

        final var boundTokens = analyseStartStopToken((Token)offendingSymbol, ex);

        AbstractAntlr4Parser.this.syntaxError(msg)
            .withStart(boundTokens[0])
            .withStop(boundTokens[1])
            .withCause(ex)
            .report();
      }
    };

    if (lexer instanceof Lexer)
    {
      final var antlr4Lexer = (Lexer)lexer;

      if (!keepConsoleErrorListeners)
        antlr4Lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

      antlr4Lexer.addErrorListener(errorListener);
    }

    final var parser = parserInstantiator.apply(lexer);

    if (!keepConsoleErrorListeners)
      parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

    parser.addErrorListener(errorListener);

    return requireNonNull(ruleExecutor.apply(parser));
  }


  /**
   * Walk the parser rule context using the given {@code listener}.
   * <p>
   * By default, the walker used to walk the parser rule context is {@link Walker#WALK_FULL_RECURSIVE}. This works
   * in all cases but may not be the ideal choice. If the listener is an instance of
   * {@link WalkerSupplier WalkerSupplier}, the {@link WalkerSupplier#getWalker() WalkerSupplier.getWalker()} method
   * is used to retrieve the walker.
   *
   * @param listener           listener to be used for walking the parser rule context, not {@code null}
   * @param parserRuleContext  parser rule context to be walked, not {@code null}
   *
   * @return  parser rule context, never {@code null}
   *
   * @param <C>  parser rule context type
   *
   * @see Walker
   */
  @Contract(value = "_, _ -> param2", mutates = "param2")
  protected <C extends ParserRuleContext>
      @NotNull C walk(@NotNull ParseTreeListener listener, @NotNull C parserRuleContext)
  {
    (listener instanceof WalkerSupplier
        ? ((WalkerSupplier)listener).getWalker()
        : WALK_FULL_RECURSIVE)
        .walk(listener, parserRuleContext);

    return parserRuleContext;
  }


  @Contract(value = "null, null -> fail", pure = true)
  protected @NotNull Token[] analyseStartStopToken(Token offendingSymbol, RecognitionException ex)
  {
    if (ex != null)
    {
      var offendingToken = ex.getOffendingToken();
      if (offendingToken != null)
        return new Token[] { offendingToken, offendingToken };

      var ctx = ex.getCtx();
      if (ctx instanceof ParserRuleContext)
      {
        var parserRuleContext = (ParserRuleContext)ctx;
        return new Token[] { parserRuleContext.getStart(), parserRuleContext.getStop() };
      }
    }

    return new Token[] { requireNonNull(offendingSymbol), offendingSymbol };
  }


  /**
   * Prepare the syntax error builder for the given error message.
   * <p>
   * @param errorMessage  error message describing the problem, not {@code null}
   *
   * @return  new syntax error builder instance, never {@code null}
   *
   * @since 0.5.3
   */
  @Contract(value = "_ -> new", pure = true)
  protected @NotNull SyntaxErrorBuilder syntaxError(@NotNull String errorMessage) {
    return new Builder(errorMessage);
  }


  /**
   * Report a syntax error for the given parser rule context.
   * <p>
   * The parser rule context and error message are used to create a syntax error message using the configured
   * {@link SyntaxErrorFormatter}.
   * <p>
   * This method will always result in an exception being thrown.
   *
   * @param ctx       parser rule context where the syntax error occurred, not {@code null}
   * @param errorMsg  error message describing the problem, not {@code null}
   *
   * @see #createException(Token, Token, String, String, Exception)
   */
  @Contract("_, _ -> fail")
  @Deprecated(since = "0.5.3", forRemoval = true)
  protected void syntaxError(@NotNull ParserRuleContext ctx, @NotNull String errorMsg) {
    syntaxError(ctx, errorMsg, null);
  }


  /**
   * Report a syntax error for the given parser rule context.
   * <p>
   * The parser rule context and error message are used to create a syntax error message using the configured
   * {@link SyntaxErrorFormatter}.
   * <p>
   * This method will always result in an exception being thrown.
   *
   * @param ctx       parser rule context where the syntax error occurred, not {@code null}
   * @param errorMsg  error message describing the problem, not {@code null}
   * @param cause     optional exception cause
   *
   * @see #createException(Token, Token, String, String, Exception)
   */
  @Contract("_, _, _ -> fail")
  @Deprecated(since = "0.5.3", forRemoval = true)
  protected void syntaxError(@NotNull ParserRuleContext ctx, @NotNull String errorMsg, Exception cause) {
    syntaxError(errorMsg).with(ctx).withCause(cause).report();
  }


  /**
   * Report a syntax error for the given terminal node.
   * <p>
   * The terminal node and error message are used to create a syntax error message using the configured
   * {@link SyntaxErrorFormatter}.
   * <p>
   * This method will always result in an exception being thrown.
   *
   * @param terminalNode  token where the syntax error occurred, not {@code null}
   * @param errorMsg      error message describing the problem, not {@code null}
   *
   * @see #createException(Token, Token, String, String, Exception)
   */
  @Contract("_, _ -> fail")
  @Deprecated(since = "0.5.3", forRemoval = true)
  protected void syntaxError(@NotNull TerminalNode terminalNode, @NotNull String errorMsg) {
    syntaxError(terminalNode, errorMsg, null);
  }


  /**
   * Report a syntax error for the given terminal node.
   * <p>
   * The terminal node and error message are used to create a syntax error message using the configured
   * {@link SyntaxErrorFormatter}.
   * <p>
   * This method will always result in an exception being thrown.
   *
   * @param terminalNode  token where the syntax error occurred, not {@code null}
   * @param errorMsg      error message describing the problem, not {@code null}
   * @param cause        optional exception cause
   *
   * @see #createException(Token, Token, String, String, Exception)
   */
  @Contract("_, _, _ -> fail")
  @Deprecated(since = "0.5.3", forRemoval = true)
  protected void syntaxError(@NotNull TerminalNode terminalNode, @NotNull String errorMsg, Exception cause) {
    syntaxError(errorMsg).with(terminalNode).withCause(cause).report();
  }


  /**
   * Report a syntax error for the given token.
   * <p>
   * The token and error message are used to create a syntax error message using the configured
   * {@link SyntaxErrorFormatter}.
   * <p>
   * This method will always result in an exception being thrown.
   *
   * @param token     token where the syntax error occurred, not {@code null}
   * @param errorMsg  error message describing the problem, not {@code null}
   *
   * @see #createException(Token, Token, String, String, Exception)
   */
  @Contract("_, _ -> fail")
  @Deprecated(since = "0.5.3", forRemoval = true)
  protected void syntaxError(@NotNull Token token, @NotNull String errorMsg) {
    syntaxError(token, errorMsg, null);
  }


  /**
   * Report a syntax error for the given token.
   * <p>
   * The token and error message are used to create a syntax error message using the configured
   * {@link SyntaxErrorFormatter}.
   * <p>
   * This method will always result in an exception being thrown.
   *
   * @param token     token where the syntax error occurred, not {@code null}
   * @param errorMsg  error message describing the problem, not {@code null}
   * @param cause     optional exception cause
   *
   * @see #createException(Token, Token, String, String, Exception)
   */
  @Contract("_, _, _ -> fail")
  @Deprecated(since = "0.5.3", forRemoval = true)
  protected void syntaxError(@NotNull Token token, @NotNull String errorMsg, Exception cause) {
    syntaxError(errorMsg).with(token).withCause(cause).report();
  }


  /**
   * Create a new exception instance for the given start and stop token.
   * <p>
   * It is advisable, yet not required, that the created exception uses {@code cause} as its cause method argument.
   *
   * @param startToken        first token in the syntax error, not {@code null}
   * @param stopToken         last token in the syntax error, not {@code null}
   * @param formattedMessage  formatted syntax error message, not {@code null}
   * @param errorMsg          message describing the problem, not {@code null}
   * @param cause             optional exception cause
   *
   * @return  new exception instance, never {@code null}
   */
  @Contract("_, _, _, _, _ -> new")
  protected abstract @NotNull RuntimeException createException(
      @NotNull Token startToken, @NotNull Token stopToken, @NotNull String formattedMessage,
      @NotNull String errorMsg, Exception cause);




  public interface WalkerSupplier extends ParseTreeListener
  {
    @Contract(pure = true)
    default @NotNull Walker getWalker() {
      return WALK_FULL_RECURSIVE;
    }
  }




  /**
   * An interface for building a syntax error.
   *
   * @since 0.5.3
   */
  @SuppressWarnings("UnusedReturnValue")
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




  private final class Builder implements SyntaxErrorBuilder
  {
    private final String errorMessage;
    private Token startToken;
    private Token stopToken;
    private Exception cause;


    private Builder(@NotNull String errorMessage) {
      this.errorMessage = errorMessage;
    }


    @Override
    public @NotNull SyntaxErrorBuilder withStart(@NotNull Token token)
    {
      startToken = token;

      return this;
    }


    @Override
    public @NotNull SyntaxErrorBuilder withStart(@NotNull SyntaxTree syntaxTree)
    {
      if (syntaxTree instanceof ParserRuleContext)
        startToken = ((ParserRuleContext)syntaxTree).getStart();
      else if (syntaxTree instanceof TerminalNode)
        startToken = ((TerminalNode)syntaxTree).getSymbol();
      else
        throw new IllegalArgumentException("unsupported syntax tree type: " + syntaxTree.getClass().getName());

      return this;
    }


    @Override
    public @NotNull SyntaxErrorBuilder withStop(@NotNull Token token)
    {
      stopToken = token;

      return this;
    }


    @Override
    public @NotNull SyntaxErrorBuilder withStop(@NotNull SyntaxTree syntaxTree)
    {
      if (syntaxTree instanceof ParserRuleContext)
        stopToken = ((ParserRuleContext)syntaxTree).getStop();
      else if (syntaxTree instanceof TerminalNode)
        stopToken = ((TerminalNode)syntaxTree).getSymbol();
      else
        throw new IllegalArgumentException("unsupported syntax tree type: " + syntaxTree.getClass().getName());

      return this;
    }


    @Override
    public @NotNull SyntaxErrorBuilder with(@NotNull Token token)
    {
      startToken = token;
      stopToken = token;

      return this;
    }


    @Override
    public @NotNull SyntaxErrorBuilder with(@NotNull SyntaxTree syntaxTree)
    {
      if (syntaxTree instanceof ParserRuleContext)
      {
        final var parserRuleContext = (ParserRuleContext)syntaxTree;

        startToken = parserRuleContext.getStart();
        stopToken = parserRuleContext.getStart();
      }
      else if (syntaxTree instanceof TerminalNode)
      {
        final var terminalNode = (TerminalNode)syntaxTree;

        startToken = terminalNode.getSymbol();
        stopToken = terminalNode.getSymbol();
      }
      else
        throw new IllegalArgumentException("unsupported syntax tree type: " + syntaxTree.getClass().getName());

      return this;
    }


    @Override
    public @NotNull SyntaxErrorBuilder withCause(@NotNull Exception cause)
    {
      this.cause = cause;

      return this;
    }


    @Override
    public void report()
    {
      final var formattedMessage = syntaxErrorFormatter.format(
          requireNonNull(startToken, "start token must be specified"),
          requireNonNull(stopToken, "stop token must be specified"),
          cause);

      throw createException(startToken, stopToken, formattedMessage, errorMessage, cause);
    }
  }
}
