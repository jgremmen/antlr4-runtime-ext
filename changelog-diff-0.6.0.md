# Version [0.6.0](https://github.com/jgremmen/antlr4-runtime-ext/tree/0.6.0) (2025-06-07)

## Breaking Changes

### Removed deprecated syntaxError() methods

The deprecated `syntaxError()` overloads that accepted `ParserRuleContext`, `TerminalNode`, or `Token` parameters
directly have been removed. These methods were deprecated in version 0.5.3.

The following methods no longer exist:

```java
syntaxError(ParserRuleContext ctx, String errorMsg)
syntaxError(ParserRuleContext ctx, String errorMsg, Exception cause)
syntaxError(TerminalNode terminalNode, String errorMsg)
syntaxError(TerminalNode terminalNode, String errorMsg, Exception cause)
syntaxError(Token token, String errorMsg)
syntaxError(Token token, String errorMsg, Exception cause)
```

Use the builder-based approach introduced in 0.5.3 instead:

```java
// single token
syntaxError("invalid input").with(token).withCause(cause).report();

// parser rule context
syntaxError("unexpected value").with(ctx).report();

// terminal node
syntaxError("unexpected symbol").with(terminalNode).report();

// explicit start/stop range
syntaxError("range is invalid")
    .withStart(startCtx)
    .withStop(stopCtx)
    .report();
```

### Removed analyseStartStopToken() method

The protected method `analyseStartStopToken(Token, RecognitionException)` has been removed. This method was used
internally to determine start and stop tokens from an offending symbol or recognition exception. The logic has been
replaced by the new `lexerSyntaxError()` and `parserSyntaxError()` methods, which handle token resolution
internally.

If you were overriding `analyseStartStopToken()`, replace it by overriding `createTokenRecognitionMessage()` for
lexer errors, or by overriding one of the `createInputMismatchMessage()`, `createMissingTokenMessage()`,
`createUnwantedTokenMessage()`, or `createNoViableAlternativeMessage()` methods for parser errors.

### Removed recursive Walker enum constants

The following `Walker` enum constants have been removed:

- `WALK_FULL_RECURSIVE`
- `WALK_EXIT_RULES_RECURSIVE`
- `WALK_ENTER_AND_EXIT_RULES_RECURSIVE`

These walkers used recursion to traverse the parse tree, which could cause `StackOverflowError` on deeply nested
input. Use the heap-based alternatives instead:

| Removed constant | Replacement |
|---|---|
| `WALK_FULL_RECURSIVE` | `WALK_FULL_HEAP` |
| `WALK_EXIT_RULES_RECURSIVE` | `WALK_EXIT_RULES_HEAP` |
| `WALK_ENTER_AND_EXIT_RULES_RECURSIVE` | `WALK_ENTER_AND_EXIT_RULES_HEAP` |

The default walker used by `AbstractAntlr4Parser.walkParserRule()` has changed from `WALK_FULL_RECURSIVE` to
`WALK_FULL_HEAP`. The same applies to `WalkerSupplier.getWalker()`.

### createException() is no longer abstract

The `createException(Token, Token, String, String, Exception)` method in `AbstractAntlr4Parser` is no longer
abstract. It now has a default implementation that returns a `SyntaxErrorException` instance. Subclasses that
previously had to implement this method can remove their implementation if the default behavior is sufficient.

### SyntaxErrorBuilder moved to separate class

The `SyntaxErrorBuilder` interface, previously an inner type of `AbstractAntlr4Parser`, has been extracted into its
own top-level class at `de.sayayi.lib.antlr4.syntax.SyntaxErrorBuilder`. Update import statements accordingly:

```java
// before
import de.sayayi.lib.antlr4.AbstractAntlr4Parser.SyntaxErrorBuilder;

// after
import de.sayayi.lib.antlr4.syntax.SyntaxErrorBuilder;
```

The `SyntaxErrorBuilder.with(Token)` method has been changed from an abstract method to a default method that
delegates to `withStart(token).withStop(token)`.

### Error handling pipeline replaced

The internal error handling in `AbstractAntlr4Parser` has been restructured. Instead of using a shared
`BaseErrorListener` for both lexer and parser, separate listeners are now installed. The parser additionally uses a
custom `DefaultErrorStrategy` subclass (`ParserErrorHandler`) that delegates error message creation to overridable
methods in `AbstractAntlr4Parser`.

This change means that all parser error messages (input mismatch, missing token, unwanted token, no viable
alternative) are now constructed through dedicated protected methods rather than being passed through as raw ANTLR
messages. If you relied on the exact format of ANTLR's default error messages, the output may differ.

### AbstractVocabulary is sealed after construction

`AbstractVocabulary` now prevents modifications after the constructor completes. Calling `add()` outside of
`addTokens()` throws an `IllegalStateException`. This ensures vocabulary consistency after initialization.

## New Features

### SyntaxErrorException

A new `SyntaxErrorException` class has been added to the `de.sayayi.lib.antlr4.syntax` package. It is a
`RuntimeException` that carries the start and stop tokens, the formatted error message (with visual location
marker), and the original error message.

```java
try {
  compiler.compile(input);
} catch(SyntaxErrorException ex) {
  System.err.println(ex.getErrorMessage());
  System.err.println(ex.getFormattedMessage());
  Token start = ex.getStartToken();
  Token stop = ex.getStopToken();
}
```

The `createException()` method in `AbstractAntlr4Parser` returns a `SyntaxErrorException` by default. Override it
to return a different exception type.

### Customizable error message methods

Several new protected methods have been added to `AbstractAntlr4Parser` for customizing syntax error messages.
Override these methods to control the wording of error messages produced during parsing.

#### createTokenRecognitionMessage()

Called when the lexer fails to match the input to any token rule.

```java
@Override
protected String createTokenRecognitionMessage(
    Lexer lexer, String text, boolean hasEOF) {
  return "unrecognized input: " + getQuotedDisplayText(text);
}
```

#### createInputMismatchMessage()

Called when the parser encounters a token that does not match any expected alternative.

```java
@Override
protected String createInputMismatchMessage(
    Parser parser, IntervalSet expectedTokens,
    Token mismatchLocationNearToken) {
  return "expected " + expectedTokens.toString(parser.getVocabulary());
}
```

#### createMissingTokenMessage()

Called when the parser determines a required token is missing.

#### createUnwantedTokenMessage()

Called when the parser encounters an extraneous token that should not be present.

#### createNoViableAlternativeMessage()

Called when the parser cannot decide which path to take based on the remaining input.

### Token display text methods

Several new protected methods have been added for controlling how tokens are displayed in error messages:

#### getTokenDisplayText()

Returns a human-readable representation of a token. Tokens with text are quoted using `getQuotedDisplayText()`.
Tokens without text fall back to the vocabulary display name or a type-based placeholder.

#### getEOFTokenDisplayText()

Returns the display text for the EOF token. Defaults to `<EOF>`. Override this to change how end-of-input is
displayed in error messages.

#### getQuotedDisplayText()

Quotes and escapes a text string for display in error messages. The method escapes `\n`, `\r` and `\t` characters
and selects a quote character that does not conflict with the text content.

### isEOFToken() method

A new protected method `isEOFToken(Token)` has been added to `AbstractAntlr4Parser`. It returns `true` if the
given token is of type `Token.EOF`.

### Default EOF entry in AbstractVocabulary

`AbstractVocabulary` now automatically registers a literal `<EOF>` mapping for the `Token.EOF` token type. This
entry can be overwritten by calling `add(Token.EOF, ...)` in `addTokens()`.

## Bug Fixes

- Fixed `getQuotedDisplayText()` not properly handling text that contains common quote characters. The method now
  cycles through multiple quote styles to find one not present in the text.
- Fixed `getTokenDisplayText()` returning unquoted text for tokens that are not EOF and not enclosed in angle
  brackets.
