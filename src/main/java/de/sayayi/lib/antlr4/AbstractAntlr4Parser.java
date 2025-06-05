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
import de.sayayi.lib.antlr4.syntax.SyntaxErrorBuilder;
import de.sayayi.lib.antlr4.syntax.SyntaxErrorException;
import de.sayayi.lib.antlr4.syntax.SyntaxErrorFormatter;
import de.sayayi.lib.antlr4.walker.Walker;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.SyntaxTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static de.sayayi.lib.antlr4.walker.Walker.WALK_FULL_HEAP;
import static java.util.Objects.requireNonNull;
import static org.antlr.v4.runtime.Token.EOF;


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
  @SuppressWarnings("UnusedReturnValue")
  protected <L extends TokenSource,P extends Parser,C extends ParserRuleContext,R>
      R parse(@NotNull L lexer, @NotNull Function<L,P> parserInstantiator, @NotNull Function<P,C> ruleExecutor,
              @NotNull ParseTreeListener listener, @NotNull Function<C,R> contextResultExtractor) {
    return contextResultExtractor.apply(walk(listener, parse(lexer, parserInstantiator, ruleExecutor)));
  }


  /**
   * Parse the input using the given lexer and parser instantiator.
   * <p>
   * This method uses the {@code parserInstantiator} to create a parser instance based on the provided {@code lexer}
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
    // configure lexer
    if (lexer instanceof Lexer)
    {
      final var antlr4Lexer = (Lexer)lexer;

      if (!keepConsoleErrorListeners)
        antlr4Lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

      antlr4Lexer.addErrorListener(new BaseErrorListener() {
        @Override
        public void syntaxError(@NotNull Recognizer<?,?> recognizer, Object offendingSymbol, int line,
                                int charPositionInLine, String msg, RecognitionException ex) {
          lexerSyntaxError((Lexer)recognizer, line, charPositionInLine, ex);
        }
      });
    }

    // create and configure parser
    final var parser = parserInstantiator.apply(lexer);

    if (!keepConsoleErrorListeners)
      parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

    parser.setErrorHandler(new ParserErrorHandler(parser));
    parser.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(@NotNull Recognizer<?,?> recognizer, Object offendingSymbol, int line,
                              int charPositionInLine, String msg, RecognitionException ex) {
        parserSyntaxError((Token)offendingSymbol, msg, ex);
      }
    });

    return requireNonNull(ruleExecutor.apply(parser));
  }


  @Contract("_, _, _, _ -> fail")
  private void lexerSyntaxError(@NotNull Lexer lexer, int line, int charPositionInLine, RecognitionException ex)
  {
    final var inputStream = lexer.getInputStream();
    final var tokenStartCharIndex = lexer._tokenStartCharIndex;

    var text = inputStream.getText(Interval.of(tokenStartCharIndex, inputStream.index()));
    final var length = text.length();
    final var hasEOF = length > 0 && text.codePointAt(length - 1) == EOF;

    if (hasEOF)
      text = text.substring(0, length - 1);

    syntaxError(createTokenRecognitionMessage(lexer, text, length == 0 || hasEOF))
        .with(new LocationToken(inputStream, line, charPositionInLine, tokenStartCharIndex, inputStream.index()))
        .withCause(ex)
        .report();
  }


  @Contract("_, _, _ -> fail")
  private void parserSyntaxError(Token token, String msg, RecognitionException ex)
  {
    final var syntaxError = syntaxError(msg).withCause(ex);

    if (ex != null)
    {
      if ((token = ex.getOffendingToken()) != null)
        syntaxError.with(token);
      else
        syntaxError.with(ex.getCtx());
    }
    else if (token != null)
      syntaxError.with(token);

    syntaxError.report();
  }


  /**
   * Walk the parser rule context using the given {@code listener}.
   * <p>
   * By default, the walker used to walk the parser rule context is {@link Walker#WALK_FULL_HEAP}. This works
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
        : WALK_FULL_HEAP)
        .walk(listener, parserRuleContext);

    return parserRuleContext;
  }


  /**
   * Tells if the given {@code token} is of type {@link Token#EOF EOF}.
   *
   * @param token  token
   *
   * @return  {@code true} if the token is of type {@link Token#EOF EOF}, {@code false} otherwise
   *
   * @since 0.6.0
   */
  @Contract(pure = true)
  protected boolean isEOFToken(Token token) {
    return token != null && token.getType() == EOF;
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
   * Create a new exception instance for the given start and stop token.
   * <p>
   * The default implementation returns a {@link SyntaxErrorException} instance.
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
  protected @NotNull RuntimeException createException(
      @NotNull Token startToken, @NotNull Token stopToken, @NotNull String formattedMessage,
      @NotNull String errorMsg, Exception cause) {
    return new SyntaxErrorException(startToken, stopToken, formattedMessage, errorMsg, cause);
  }


  /**
   * Create a message for the case where the lexer failed to determine the next token.
   *
   * @param lexer   lexer instance, not {@code null}
   * @param text    input where the lexer failed to determine the next token, not {@code null}
   * @param hasEOF  if EOF was reached
   *
   * @return  message, describing the problem, never {@code null}
   *
   * @since 0.6.0
   */
  @Contract(pure = true)
  protected @NotNull String createTokenRecognitionMessage(@NotNull Lexer lexer, @NotNull String text, boolean hasEOF) {
    return "token recognition error at: " + (hasEOF ? getEOFTokenDisplayText() : getQuotedDisplayText(text));
  }


  /**
   * @since 0.6.0
   */
  @Contract(pure = true)
  protected @NotNull String createInputMismatchMessage(@NotNull Parser parser, @NotNull IntervalSet expectedTokens,
                                                       Token mismatchLocationNearToken)
  {
    return "mismatched input " + getTokenDisplayText(parser, mismatchLocationNearToken) +
        " expecting " + expectedTokens.toString(parser.getVocabulary());
  }


  /**
   * @since 0.6.0
   */
  @Contract(pure = true)
  protected @NotNull String createMissingTokenMessage(@NotNull Parser parser, @NotNull IntervalSet expectedTokens,
                                                      Token missingLocationNearToken)
  {
    return "missing " + expectedTokens.toString(parser.getVocabulary()) + " at " +
        getTokenDisplayText(parser, missingLocationNearToken);
  }


  /**
   * @since 0.6.0
   */
  @Contract(pure = true)
  protected @NotNull String createUnwantedTokenMessage(@NotNull Parser parser, @NotNull Token unwantedToken,
                                                       @NotNull IntervalSet expectedTokens)
  {
    return "extraneous input " + getTokenDisplayText(parser, unwantedToken) + " expecting " +
        expectedTokens.toString(parser.getVocabulary());
  }


  /**
   * Create a message for the case where the parser could not decide which of two or more paths to take based
   * upon the remaining input.
   *
   * @param parser          parser instance, not {@code null}
   * @param startToken      starting token of the offending input, not {@code null}
   * @param offendingToken  token where the parser failed to decide which path to take, not {@code null}
   *
   * @return  message, describing the problem, never {@code null}
   *
   * @since 0.6.0
   */
  @Contract(pure = true)
  protected @NotNull String createNoViableAlternativeMessage(@NotNull Parser parser, @NotNull Token startToken,
                                                             @NotNull Token offendingToken)
  {
    final var input = isEOFToken(startToken)
        ? "<EOF>"
        : getQuotedDisplayText(parser.getInputStream().getText(startToken, offendingToken));

    return "no viable alternative at input " + input;
  }


  /**
   * Returns the display text for the {@code Token#EOF EOF} token.
   * <p>
   * This method is used primarily for a lexer "no viable alternative" situation when reaching the end of the input.
   * For parser errors the provided tokens are translated using the {@link Vocabulary} but may fall back to this
   * method if it fails to translate {@code Token#EOF EOF}.
   *
   * @return  display text for the {@code Token#EOF EOF} token, never {@code null}
   *
   * @since 0.6.0
   */
  @Contract(pure = true)
  protected @NotNull String getEOFTokenDisplayText() {
    return "<EOF>";
  }


  /**
   * Get the display text for the given token. This method is used to display the token in syntax error messages.
   *
   * @param parser  parser instance, not {@code null}
   * @param token   token to display, may be {@code null}
   *
   * @return  token display text, never {@code null}
   *
   * @since 0.6.0
   */
  @Contract(pure = true)
  protected @NotNull String getTokenDisplayText(@NotNull Parser parser, Token token)
  {
    if (token == null)
      return "<no token>";

    final var tokenType = token.getType();

    var text = parser.getVocabulary().getDisplayName(tokenType);
    if (text != null)
      return text;

    if (isEOFToken(token))
      return getEOFTokenDisplayText();
    else if ((text = token.getText()) == null)
      return "<" + tokenType + '>';

    return getQuotedDisplayText(text);
  }


  /**
   * Returns the quoted and escaped {@code text}.
   * <p>
   * This method only escapes the \n, \r and \t characters.
   *
   * @param text  text to quote and escape, not {@code null}
   *
   * @return  quoted and escaped {@code text}
   *
   * @since 0.6.0
   */
  @Contract(pure = true)
  protected @NotNull String getQuotedDisplayText(@NotNull String text)
  {
    return '\'' + text
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t") +
        '\'';
  }




  public interface WalkerSupplier extends ParseTreeListener
  {
    @Contract(pure = true)
    default @NotNull Walker getWalker() {
      return WALK_FULL_HEAP;
    }
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
    public @NotNull SyntaxErrorBuilder with(@NotNull SyntaxTree syntaxTree)
    {
      if (syntaxTree instanceof ParserRuleContext)
      {
        final var parserRuleContext = (ParserRuleContext)syntaxTree;

        startToken = parserRuleContext.getStart();
        stopToken = parserRuleContext.getStop();
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




  private final class ParserErrorHandler extends DefaultErrorStrategy
  {
    private final Parser parser;


    private ParserErrorHandler(@NotNull Parser parser) {
      this.parser = parser;
    }


    @Override
    protected void reportInputMismatch(Parser parser, InputMismatchException ex)
    {
      final var mismatchLocationNearToken = ex.getOffendingToken();

      parser.notifyErrorListeners(mismatchLocationNearToken,
          createInputMismatchMessage(parser, ex.getExpectedTokens(), mismatchLocationNearToken), ex);
    }


    @Override
    protected void reportMissingToken(Parser parser)
    {
      if (!inErrorRecoveryMode(parser))
      {
        beginErrorCondition(parser);

        final var missingLocationNearToken = parser.getCurrentToken();

        parser.notifyErrorListeners(missingLocationNearToken,
            createMissingTokenMessage(parser, getExpectedTokens(parser), missingLocationNearToken), null);
      }
    }


    @Override
    protected void reportUnwantedToken(Parser parser)
    {
      if (!inErrorRecoveryMode(parser))
      {
        beginErrorCondition(parser);

        final var unwantedToken = parser.getCurrentToken();

        parser.notifyErrorListeners(unwantedToken,
            createUnwantedTokenMessage(parser, unwantedToken, getExpectedTokens(parser)), null);
      }
    }


    @Override
    protected void reportNoViableAlternative(Parser parser, NoViableAltException ex)
    {
      final var offendingToken = ex.getOffendingToken();

      parser.notifyErrorListeners(offendingToken,
          createNoViableAlternativeMessage(parser, ex.getStartToken(), offendingToken), ex);
    }


    @Override
    protected String getTokenErrorDisplay(Token token) {
      return getTokenDisplayText(parser, token);
    }


    @Override
    protected String escapeWSAndQuote(String s) {
      return getQuotedDisplayText(s);
    }
  }
}
