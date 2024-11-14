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
import org.antlr.v4.runtime.misc.Interval;
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


  protected AbstractAntlr4Parser() {
    this(new GenericSyntaxErrorFormatter(8, 0, 0, " "));
  }


  protected AbstractAntlr4Parser(@NotNull SyntaxErrorFormatter syntaxErrorFormatter) {
    this.syntaxErrorFormatter = syntaxErrorFormatter;
  }


  @Contract(mutates = "param1")
  protected <L extends TokenSource,P extends Parser,C extends ParserRuleContext,R>
      R parse(@NotNull L lexer, @NotNull Function<L,P> parserSupplier, @NotNull Function<P,C> ruleExecutor,
              @NotNull ParseTreeListener listener, @NotNull Function<C,R> contextResultExtractor) {
    return contextResultExtractor.apply(walk(listener, parse(lexer, parserSupplier, ruleExecutor)));
  }


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
          var lexer = (Lexer)recognizer;
          var inputStream = lexer.getInputStream();

          offendingSymbol = new LexerPositionToken(line, charPositionInLine,
              lexer._tokenStartCharIndex, inputStream.index(), inputStream);
        }

        AbstractAntlr4Parser.this.syntaxError(analyseStartStopToken((Token)offendingSymbol, ex), msg, ex);
      }
    };

    if (lexer instanceof Lexer)
    {
      var antlr4Lexer = (Lexer)lexer;

      antlr4Lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);  // console polluter
      antlr4Lexer.addErrorListener(errorListener);
    }

    var parser = parserSupplier.apply(lexer);

    parser.removeErrorListener(ConsoleErrorListener.INSTANCE);  // console polluter
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


  @Contract("_, _ -> fail")
  protected void syntaxError(@NotNull ParserRuleContext ctx, @NotNull String errorMsg) {
    syntaxError(ctx, errorMsg, null);
  }


  @Contract("_, _, _ -> fail")
  protected void syntaxError(@NotNull ParserRuleContext ctx, @NotNull String errorMsg, Exception cause) {
    syntaxError(new Token[] { ctx.getStart(), ctx.getStop() }, errorMsg, cause);
  }


  @Contract("_, _ -> fail")
  protected void syntaxError(@NotNull TerminalNode terminalNode, @NotNull String errorMsg) {
    syntaxError(terminalNode, errorMsg, null);
  }


  @Contract("_, _, _ -> fail")
  protected void syntaxError(@NotNull TerminalNode terminalNode, @NotNull String errorMsg, Exception cause) {
    syntaxError(terminalNode.getSymbol(), errorMsg, cause);
  }


  @Contract("_, _ -> fail")
  protected void syntaxError(@NotNull Token token, @NotNull String errorMsg) {
    syntaxError(token, errorMsg, null);
  }


  @Contract("_, _, _ -> fail")
  protected void syntaxError(@NotNull Token token, @NotNull String errorMsg, Exception cause) {
    syntaxError(new Token[] { token, token }, errorMsg, cause);
  }


  @Contract("_, _, _ -> fail")
  private void syntaxError(@NotNull Token[] startStopToken, @NotNull String errorMsg, Exception cause)
  {
    var startToken = startStopToken[0];
    var stopToken = startStopToken[1];

    throw createException(startToken, stopToken,
        syntaxErrorFormatter.format(startToken, stopToken, cause), errorMsg, cause);
  }


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




  private static final class LexerPositionToken implements Token
  {
    private final int line;
    private final int charPositionInLine;
    private final int startIndex;
    private final int stopIndex;
    private final CharStream inputStream;


    public LexerPositionToken(int line, int charPositionInLine, int startIndex, int stopIndex,
                              @NotNull CharStream inputStream)
    {
      this.line = line;
      this.charPositionInLine = charPositionInLine;
      this.startIndex = startIndex;
      this.stopIndex = stopIndex;
      this.inputStream = inputStream;
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
    public CharStream getInputStream() {
      return inputStream;
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
  }
}
