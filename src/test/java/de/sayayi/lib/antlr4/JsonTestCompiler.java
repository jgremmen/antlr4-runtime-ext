package de.sayayi.lib.antlr4;

import de.sayayi.lib.antlr4.syntax.GenericSyntaxErrorFormatter;
import de.sayayi.lib.antlr4.syntax.SyntaxErrorFormatter;
import de.sayayi.lib.antlr4.walker.Walker;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static de.sayayi.lib.antlr4.JSONParser.*;
import static de.sayayi.lib.antlr4.walker.Walker.WALK_EXIT_RULES_HEAP;


/**
 * @author Jeroen Gremmen
 * @since 0.5.3
 */
final class JsonTestCompiler extends AbstractAntlr4Parser
{
  private static final Vocabulary VOCABULARY = new AbstractVocabulary() {
    @Override
    protected void addTokens()
    {
      add(EOF, "<EOF>", "EOF");
      add(STRING, "<string>", "STRING");
      add(NUMBER, "<number>", "NUMBER");
      add(WS, "<whitespace>", "WS");
    }
  };

  private static final SyntaxErrorFormatter SYNTAX_ERROR_FORMATTER =
      new GenericSyntaxErrorFormatter(2, 1, 1 ,2) {
        @Override
        protected char getMarker() {
          return '~';
        }
      };


  public JsonTestCompiler() {
    super(SYNTAX_ERROR_FORMATTER);
  }


  @Contract(pure = true)
  public void parseJson(@NotNull String text) {
    parse(new Lexer(text), Parser::new, JSONParser::json, new Listener(), ctx -> null);
  }


  @Override
  protected @NotNull RuntimeException createException(@NotNull Token startToken, @NotNull Token stopToken,
                                                      @NotNull String formattedMessage, @NotNull String errorMsg,
                                                      Exception cause) {
    return new IllegalArgumentException(formattedMessage, cause);
  }




  private static final class Lexer extends JSONLexer
  {
    public Lexer(@NotNull String message) {
      super(CharStreams.fromString(message));
    }


    @Override
    public Vocabulary getVocabulary() {
      return JsonTestCompiler.VOCABULARY;
    }
  }




  private static final class Parser extends JSONParser
  {
    public Parser(@NotNull Lexer lexer) {
      super(new BufferedTokenStream(lexer));
    }


    @Override
    public Vocabulary getVocabulary() {
      return JsonTestCompiler.VOCABULARY;
    }
  }




  private final class Listener extends JSONBaseListener implements WalkerSupplier
  {
    @Override
    public @NotNull Walker getWalker() {
      return WALK_EXIT_RULES_HEAP;
    }


    @Override
    public void exitObj(ObjContext ctx)
    {
      final var pairs = ctx.pair();

      if (pairs.size() > 3)
      {
        syntaxError("object size must be <= 3")
            .withStartToken(pairs.get(3).getStart())
            .withStopToken(pairs.get(pairs.size() - 1).getStop())
            .report();
      }
    }


    @Override
    public void exitPair(PairContext ctx)
    {
      if (ctx.STRING().getText().length() > 4)
      {
        syntaxError("string too long")
            .withContext(ctx)
            .report();
      }
    }
  }
}
