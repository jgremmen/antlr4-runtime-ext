package de.sayayi.lib.antlr4;

import de.sayayi.lib.antlr4.syntax.SyntaxErrorException;
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
    var exception = assertThrows(SyntaxErrorException.class, () ->
        compiler.parseJson("{ \"test\": true, antlr:5 }"));

    assertEquals("  { \"test\": true, antlr:5 }\n                  ~\n", exception.getFormattedMessage());
    assertEquals("token recognition error at: 'a'", exception.getErrorMessage());
    assertInstanceOf(LexerNoViableAltException.class, exception.getCause());
  }


  @Test
  @DisplayName("JSON lexer error (multiple lines)")
  void lexerErrorMultipleLines()
  {
    var exception = assertThrows(SyntaxErrorException.class, () ->
        compiler.parseJson("{\n  \"data\": 4.5e-3,\n  \"test\": ull,\n  \"more\": true  \n}"));

    assertEquals("  2:   \"data\": 4.5e-3,\n  3:   \"test\": ull,\n               ~\n  4:   \"more\": true\n",
        exception.getFormattedMessage());
    assertEquals("token recognition error at: 'u'", exception.getErrorMessage());
    assertInstanceOf(LexerNoViableAltException.class, exception.getCause());
  }


  @Test
  @DisplayName("JSON parser error (object size)")
  void parserErrorObjectSize()
  {
    var exception = assertThrows(SyntaxErrorException.class, () ->
        compiler.parseJson("{\n  \"a\": 1,\n  \"b\": 2,\n  \"c\": 3,\n  \"d\": 4,\n  \"e\": 5  \n}"));

    assertEquals(
        "  4:   \"c\": 3,\n  5:   \"d\": 4,\n       ~~~~~~~\n  6:   \"e\": 5\n       ~~~~~~\n  7: }\n",
        exception.getFormattedMessage());
    assertEquals("object size must be <= 3", exception.getErrorMessage());
    assertNull(exception.getCause());
  }


  @Test
  @DisplayName("No viable alternative")
  void parserErrorNoViableAlternative()
  {
    var exception = assertThrows(SyntaxErrorException.class, () ->
        compiler.parseJson("{:}"));

    assertEquals("  {:}\n   ~\n", exception.getFormattedMessage());
    assertEquals("no viable alternative at input '{:'", exception.getErrorMessage());
    assertInstanceOf(NoViableAltException.class, exception.getCause());
  }


  @Test
  @DisplayName("Unwanted token")
  void parserErrorUnwantedToken()
  {
    var exception = assertThrows(SyntaxErrorException.class, () ->
        compiler.parseJson("{\"test\":3}3"));

    assertEquals("  {\"test\":3}3\n            ~\n", exception.getFormattedMessage());
    assertEquals("extraneous input '3' expecting <EOF>", exception.getErrorMessage());
    assertNull(exception.getCause());
  }


  @Test
  @DisplayName("Mismatched input")
  void parserMismatchedInput()
  {
    var exception = assertThrows(SyntaxErrorException.class, () ->
        compiler.parseJson("{\"test\""));

    assertEquals("  {\"test\"\n         ~\n", exception.getFormattedMessage());
    assertEquals("mismatched input <EOF> expecting ':'", exception.getErrorMessage());
    assertInstanceOf(InputMismatchException.class, exception.getCause());
  }


  @Test
  @DisplayName("Token recognition error")
  void parserTokenRecognitionError()
  {
    var exception = assertThrows(SyntaxErrorException.class, () ->
        compiler.parseJson("."));

    assertEquals("  .\n  ~\n", exception.getFormattedMessage());
    assertEquals("token recognition error at: '.'", exception.getErrorMessage());
    assertInstanceOf(LexerNoViableAltException.class, exception.getCause());
  }
}
