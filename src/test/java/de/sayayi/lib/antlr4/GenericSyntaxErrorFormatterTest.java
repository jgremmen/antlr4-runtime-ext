package de.sayayi.lib.antlr4;

import de.sayayi.lib.antlr4.syntax.GenericSyntaxErrorFormatter;
import lombok.val;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.antlr.v4.runtime.CharStreams.fromStream;
import static org.antlr.v4.runtime.Token.DEFAULT_CHANNEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;


@TestInstance(PER_CLASS)
class GenericSyntaxErrorFormatterTest
{
  private CharStream inputStream;
  private String parserInput;


  @BeforeAll
  void init() throws IOException
  {
    inputStream = fromStream(requireNonNull(
        getClass().getResourceAsStream("/de/sayayi/lib/antlr4/lorem-ipsum.txt")));
    parserInput = inputStream.toString();
  }


  @Test
  void format1LineNoContext()
  {
    val formatter = new GenericSyntaxErrorFormatter(8, 0, 0);
    val msg = formatter.format(
        createTokenWithLocation(99, 104, 2, 3),
        createTokenWithLocation(122, 129, 2, 26), "Error", null);

    assertEquals("Error:\n" +
        " 2: ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo\n" +
        "       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n", msg);
  }


  @Test
  void format1LineWithContext()
  {
    val formatter = new GenericSyntaxErrorFormatter(8, 2, 1);
    val msg = formatter.format(
        createTokenWithLocation(99, 104, 2, 3),
        createTokenWithLocation(122, 129, 2, 26), "Error", null);

    assertEquals("Error:\n" +
        " 1: Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt\n" +
        " 2: ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo\n" +
        "       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        " 3: dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor\n", msg);
  }


  @Contract(pure = true)
  private @NotNull Token createTokenWithLocation(int startIndex, int stopIndex, int line, int charPositionInLine)
  {
    return CommonTokenFactory.DEFAULT.create(new Pair<>(null, inputStream), 0, null,
        DEFAULT_CHANNEL, startIndex, stopIndex, line, charPositionInLine);
  }
}