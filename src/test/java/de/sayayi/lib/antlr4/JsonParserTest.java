package de.sayayi.lib.antlr4;

import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.NoViableAltException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Jeroen Gremmen
 * @since 0.5.3
 */
@DisplayName("JSON parser")
class JsonParserTest
{
  private final JsonTestCompiler compiler = new JsonTestCompiler();


  @Test
  @DisplayName("JSON lexer error (single line)")
  void lexerErrorSingleLine()
  {
    var exception = assertThrows(IllegalArgumentException.class, () ->
        compiler.parseJson("{ \"test\": true, antlr:5 }"));

    assertEquals("  { \"test\": true, antlr:5 }\n                  ~\n", exception.getMessage());
  }


  @Test
  @DisplayName("JSON lexer error (multiple lines)")
  void lexerErrorMultipleLines()
  {
    var exception = assertThrows(IllegalArgumentException.class, () ->
        compiler.parseJson("{\n  \"data\": 4.5e-3,\n  \"test\": ull,\n  \"more\": true  \n}"));

    assertEquals("  2:   \"data\": 4.5e-3,\n  3:   \"test\": ull,\n               ~\n  4:   \"more\": true\n",
        exception.getMessage());
  }


  @Test
  @DisplayName("JSON parser error (object size)")
  void parserErrorObjectSize()
  {
    var exception = assertThrows(IllegalArgumentException.class, () ->
        compiler.parseJson("{\n  \"a\": 1,\n  \"b\": 2,\n  \"c\": 3,\n  \"d\": 4,\n  \"e\": 5  \n}"));

    assertEquals(
        "  4:   \"c\": 3,\n  5:   \"d\": 4,\n       ~~~~~~~\n  6:   \"e\": 5\n       ~~~~~~\n  7: }\n",
        exception.getMessage());
  }


  @Test
  @DisplayName("No viable alternative")
  void parserErrorNoViableAlternative()
  {
    var exception = assertThrows(IllegalArgumentException.class, () ->
        compiler.parseJson("{:}"));

    assertEquals("  {:}\n   ~\n", exception.getMessage());
    assertInstanceOf(NoViableAltException.class, exception.getCause());
  }


  @Test
  @DisplayName("Unwanted token")
  void parserErrorUnwantedToken()
  {
    var exception = assertThrows(IllegalArgumentException.class, () ->
        compiler.parseJson("{\"test\":3}3"));

    assertEquals("  {\"test\":3}3\n            ~\n", exception.getMessage());
    assertNull(exception.getCause());
  }


  @Test
  @DisplayName("Mismatched input")
  void parserMismatchedInput()
  {
    var exception = assertThrows(IllegalArgumentException.class, () ->
        compiler.parseJson("{\"test\""));

    assertEquals("  {\"test\"\n         ~\n", exception.getMessage());
    assertInstanceOf(InputMismatchException.class, exception.getCause());
  }


  @Test
  @DisplayName("Token recognition error")
  void parserTokenRecognitionError()
  {
    var exception = assertThrows(IllegalArgumentException.class, () ->
        compiler.parseJson("."));

    assertEquals("  .\n  ~\n", exception.getMessage());
    assertInstanceOf(LexerNoViableAltException.class, exception.getCause());
  }
}
