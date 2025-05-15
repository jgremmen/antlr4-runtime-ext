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
      R parse(@NotNull L lexer, @NotNull Function<L,P> parserSupplier, @NotNull Function<P,C> ruleExecutor,
              @NotNull ParseTreeListener listener, @NotNull Function<C,R> contextResultExtractor) {
    return contextResultExtractor.apply(walk(listener, parse(lexer, parserSupplier, ruleExecutor)));
  }


  /**
   * Parse the input using the given lexer and parser supplier.
   * <p>
   * This method will use the {@code parserSupplier} to create a parser instance based on the provided {@code lexer}
   * and invokes the {@code ruleExecutor}. The result from the rule executor is returned.
   * <p>
   * Syntax errors are handled appropriately and always result in an exception being thrown.
   * <p>
   * By default, the {@link ConsoleErrorListener}s automatically added by Antlr4 to the lexer and parser instances
   * are removed as they pollute the console output and do not add any value in complex application scenarios.
   * If these console error listeners should be kept, set the {@code keepConsoleErrorListeners} constructor parameter
   * to {@code true}.
   *
   * @param lexer           lexer instance, not {@code null}
   * @param parserSupplier  parser supplier, not {@code null}
   * @param ruleExecutor    rule executor, not {@code null}
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
      @NotNull C parse(@NotNull L lexer, @NotNull Function<L,P> parserSupplier, @NotNull Function<P,C> ruleExecutor)
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

        AbstractAntlr4Parser.this.syntaxError(analyseStartStopToken((Token)offendingSymbol, ex), msg, ex);
      }
    };

    if (lexer instanceof Lexer)
    {
      final var antlr4Lexer = (Lexer)lexer;

      if (!keepConsoleErrorListeners)
        antlr4Lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

      antlr4Lexer.addErrorListener(errorListener);
    }

    final var parser = parserSupplier.apply(lexer);

    if (!keepConsoleErrorListeners)
      parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

    parser.addErrorListener(errorListener);

    return requireNonNull(ruleExecutor.apply(parser));
  }


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
  protected void syntaxError(@NotNull ParserRuleContext ctx, @NotNull String errorMsg, Exception cause) {
    syntaxError(new Token[] { ctx.getStart(), ctx.getStop() }, errorMsg, cause);
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
  protected void syntaxError(@NotNull TerminalNode terminalNode, @NotNull String errorMsg, Exception cause) {
    syntaxError(terminalNode.getSymbol(), errorMsg, cause);
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
  protected void syntaxError(@NotNull Token token, @NotNull String errorMsg, Exception cause) {
    syntaxError(new Token[] { token, token }, errorMsg, cause);
  }


  @Contract("_, _, _ -> fail")
  private void syntaxError(@NotNull Token[] startStopToken, @NotNull String errorMsg, Exception cause)
  {
    final var startToken = startStopToken[0];
    final var stopToken = startStopToken[1];

    throw createException(startToken, stopToken,
        syntaxErrorFormatter.format(startToken, stopToken, cause), errorMsg, cause);
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
}
