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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.IterativeParseTreeWalker;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static de.sayayi.lib.antlr4.ParseTreeWalker.walkEnterAndExitsOnly;
import static de.sayayi.lib.antlr4.ParseTreeWalker.walkExitsOnly;
import static java.lang.Character.isSpaceChar;
import static java.util.Arrays.fill;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;


/**
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@RequiredArgsConstructor(access = PROTECTED)
public abstract class AbstractAntlr4Parser
{
  private final SyntaxErrorFormatter syntaxErrorFormatter;


  protected AbstractAntlr4Parser() {
    this(DefaultSyntaxErrorFormatter.INSTANCE);
  }


  protected <L extends Lexer & ParserInputSupplier,P extends Parser,C extends ParserRuleContext,R>
      R parse(@NotNull L lexer, @NotNull Function<L,P> parserSupplier, @NotNull Function<P,C> ruleExecutor,
              @NotNull ParseTreeListener listener, @NotNull Function<C,R> contextResultExtractor)
  {
    val parserRuleContext = parse(lexer, parserSupplier, ruleExecutor);

    walk(listener, parserRuleContext);

    return contextResultExtractor.apply(parserRuleContext);
  }


  @NotNull
  protected <L extends Lexer & ParserInputSupplier,P extends Parser,C extends ParserRuleContext>
      C parse(@NotNull L lexer, @NotNull Function<L,P> parserSupplier, @NotNull Function<P,C> ruleExecutor)
  {
    val parserInput = lexer.getParserInput();
    val errorListener = new BaseErrorListener() {
      @Override
      public void syntaxError(@NotNull Recognizer<?,?> recognizer, Object offendingSymbol, int line,
                              int charPositionInLine, String msg, RecognitionException ex)
      {
        AbstractAntlr4Parser.this.syntaxError(parserInput, recognizer, (Token)offendingSymbol,
            line, charPositionInLine, msg, ex);
      }
    };

    lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);  // console polluter
    lexer.addErrorListener(errorListener);

    val parser = parserSupplier.apply(lexer);

    parser.removeErrorListener(ConsoleErrorListener.INSTANCE);  // console polluter
    parser.addErrorListener(errorListener);

    return ruleExecutor.apply(parser);
  }


  private void syntaxError(@NotNull String parserInput, @NotNull Recognizer<?,?> recognizer,
                           Token offendingSymbol, int line, int charPositionInLine,
                           String msg, RecognitionException ex)
  {
    if (offendingSymbol == null && recognizer instanceof Lexer)
    {
      val inputStream = ((Lexer)recognizer).getInputStream();
      offendingSymbol = new PositionToken(line, charPositionInLine, inputStream.index(), inputStream);
    }

    syntaxError(parserInput, analyseStartStopToken(offendingSymbol, ex), msg, ex);
  }


  @Contract(value = "null, null -> fail", pure = true)
  protected @NotNull Token[] analyseStartStopToken(Token offendingSymbol, RecognitionException ex)
  {
    if (ex != null)
    {
      val offendingToken = ex.getOffendingToken();
      if (offendingToken != null)
        return new Token[] { offendingToken, offendingToken };

      val ctx = ex.getCtx();
      if (ctx instanceof ParserRuleContext)
      {
        val parserRuleContext = (ParserRuleContext)ctx;
        return new Token[] { parserRuleContext.getStart(), parserRuleContext.getStop() };
      }
    }

    return new Token[] { requireNonNull(offendingSymbol), offendingSymbol };
  }


  @Contract(mutates = "param2")
  private void walk(@NotNull ParseTreeListener listener, @NotNull ParseTree parseTree)
  {
    if (listener instanceof WalkerSupplier)
      switch(((WalkerSupplier)listener).getWalker())
      {
        case WALK_EXIT_RULES_ONLY:
          walkExitsOnly(listener, parseTree);
          return;

        case WALK_ENTER_AND_EXIT_RULES_ONLY:
          walkEnterAndExitsOnly(listener, parseTree);
          return;

        case WALK_FULL_HEAP:
          new IterativeParseTreeWalker().walk(listener, parseTree);
          return;
      }

    // default walker
    ParseTreeWalker.DEFAULT.walk(listener, parseTree);
  }


  @Contract("_, _, _ -> fail")
  protected void syntaxError(@NotNull String parserInput, @NotNull ParserRuleContext ctx,
                             @NotNull String errorMsg) {
    syntaxError(parserInput, new Token[] { ctx.getStart(), ctx.getStop() }, errorMsg, null);
  }


  @Contract("_, _, _ -> fail")
  protected void syntaxError(@NotNull String parserInput, @NotNull Token token, @NotNull String errorMsg) {
    syntaxError(parserInput, new Token[] { token, token }, errorMsg, null);
  }


  @Contract("_, _, _, _ -> fail")
  private void syntaxError(@NotNull String parserInput, @NotNull Token[] startStopToken,
                           @NotNull String errorMsg, RecognitionException ex)
  {
    val startToken = startStopToken[0];
    val stopToken = startStopToken[1];

    throw createException(parserInput, startToken, stopToken,
        syntaxErrorFormatter.format(parserInput, startToken, stopToken, errorMsg, ex), errorMsg, ex);
  }


  protected abstract @NotNull RuntimeException createException(
      @NotNull String parserInput, @NotNull Token startToken, @NotNull Token stopToken,
      @NotNull String formattedMessage, @NotNull String errorMsg, RecognitionException ex);




  public interface ParserInputSupplier
  {
    @Contract(pure = true)
    @NotNull String getParserInput();
  }




  public interface WalkerSupplier
  {
    @Contract(pure = true)
    @NotNull Walker getWalker();
  }




  public enum Walker
  {
    WALK_FULL_RECURSIVE,
    WALK_FULL_HEAP,
    WALK_EXIT_RULES_ONLY,
    WALK_ENTER_AND_EXIT_RULES_ONLY
  }




  @RequiredArgsConstructor(access = PRIVATE)
  @Getter
  private static final class PositionToken implements Token
  {
    private final int line;
    private final int startIndex;
    private final int stopIndex;
    private final CharStream inputStream;


    @Override
    public String getText() {
      return null;
    }


    @Override
    public int getType() {
      return 0;
    }


    @Override
    public int getCharPositionInLine() {
      return startIndex;
    }


    @Override
    public int getChannel() {
      return Token.DEFAULT_CHANNEL;
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




  private enum DefaultSyntaxErrorFormatter implements SyntaxErrorFormatter
  {
    INSTANCE;


    @Override
    public @NotNull String format(@NotNull String parserInput, @NotNull Token startToken,
                                  @NotNull Token stopToken, @NotNull String errorMsg, RecognitionException ex)
    {
      val lines = parserInput.split("\r?\n");

      return lines.length == 1
          ? syntaxErrorSingleLine(errorMsg, parserInput, startToken, stopToken)
          : syntaxErrorMultiLine(errorMsg, lines, startToken, stopToken);
    }


    @Contract(pure = true)
    private @NotNull String syntaxErrorSingleLine(@NotNull String errorMsg, @NotNull String compilerInput,
                                                  @NotNull Token startToken, @NotNull Token stopToken)
    {
      val startIndex = startToken.getStartIndex();
      val stopIndex = stopToken.getStopIndex();
      val marker = new char[stopIndex + 1];

      fill(marker, 0, startIndex, ' ');  // leading spaces
      fill(marker, startIndex, stopIndex + 1, '^');  // marker

      return errorMsg + ":\n" + compilerInput + '\n' + trimRight(String.valueOf(marker));
    }


    @Contract(pure = true)
    private @NotNull String syntaxErrorMultiLine(@NotNull String errorMsg, @NotNull String[] lines,
                                                 @NotNull Token startToken, @NotNull Token stopToken)
    {
      val text = new StringBuilder(errorMsg).append(":\n");

      val lineStart = startToken.getLine();
      val colStart = startToken.getStartIndex();
      val lineStop = stopToken.getLine();
      val colStop = stopToken.getStopIndex();

      val lineCount = lines.length;
      val format = lineCount >= 100 ? " %03d: " : lineCount >= 10 ? " %02d: " : " %1d: ";

      for(int n = 1; n <= lineCount; n++)
      {
        val line = trimRight(lines[n - 1]);
        val lineLength = line.length();
        val lineNumber = String.format(format, n);

        text.append(lineNumber).append(line).append('\n');

        if (n >= lineStart && n <= lineStop && lineLength > 0)
        {
          var nonSpace = false;

          for(int c = -lineNumber.length(); c < lineLength; c++)
          {
            if ((lineStart == n && c < colStart) || (lineStop == n && c > colStop) || c < 0)
              text.append(' ');
            else
            {
              nonSpace |= !isSpaceChar(line.charAt(c));

              text.append(nonSpace ? '^' : ' ');
            }
          }

          text.append('\n');
        }
      }

      return trimRight(text.toString());
    }


    @Contract(pure = true)
    private @NotNull String trimRight(@NotNull String s)
    {
      val chars = s.toCharArray();
      int len = chars.length;

      while(len > 0 && chars[len - 1] <= ' ')
        len--;

      return len < chars.length ? new String(chars, 0, len) : s;
    }
  }
}