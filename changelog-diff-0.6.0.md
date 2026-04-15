# Version [0.6.0](https://github.com/jgremmen/antlr4-runtime-ext/releases/tag/0.6.0) (2025-06-07)

## Breaking Changes

### Deprecated syntaxError() methods removed

All `syntaxError()` methods deprecated in 0.5.3 have been removed:

```java
syntaxError(ParserRuleContext ctx, String errorMsg)
syntaxError(ParserRuleContext ctx, String errorMsg, Exception cause)
syntaxError(TerminalNode terminalNode, String errorMsg)
syntaxError(TerminalNode terminalNode, String errorMsg, Exception cause)
syntaxError(Token token, String errorMsg)
syntaxError(Token token, String errorMsg, Exception cause)
```

Use the builder-based `syntaxError(String)` method instead. See the 0.5.3 changelog for migration examples.

### Recursive walker variants removed

The following recursive walker variants have been removed from the `Walker` enum:

- `WALK_FULL_RECURSIVE`
- `WALK_EXIT_RULES_RECURSIVE`
- `WALK_ENTER_AND_EXIT_RULES_RECURSIVE`

The heap-based (iterative) variants remain and should be used instead. The following table shows the migration path:

| Removed | Replacement |
|---|---|
| `WALK_FULL_RECURSIVE` | `WALK_FULL_HEAP` |
| `WALK_EXIT_RULES_RECURSIVE` | `WALK_EXIT_RULES_HEAP` |
| `WALK_ENTER_AND_EXIT_RULES_RECURSIVE` | `WALK_ENTER_AND_EXIT_RULES_HEAP` |

### Default walker changed to WALK_FULL_HEAP

The default walker used by `AbstractAntlr4Parser.walk()` and the default implementation of
`WalkerSupplier.getWalker()` has changed from `WALK_FULL_RECURSIVE` to `WALK_FULL_HEAP`. This affects all
listeners that do not explicitly specify a walker.

### analyseStartStopToken() method removed

The protected method `analyseStartStopToken(Token, RecognitionException)` has been removed from
`AbstractAntlr4Parser`. The parser now handles token analysis internally. If you relied on this method in a
subclass, use the `SyntaxErrorBuilder` methods `withStart()` and `withStop()` to set tokens explicitly.

### createException() is no longer abstract

The `createException()` method now has a default implementation that returns a `SyntaxErrorException`. Subclasses
that override this method are not affected. Subclasses that previously had to implement this method can now remove
the override if the default `SyntaxErrorException` is sufficient.

### SyntaxErrorBuilder moved to syntax package

The `SyntaxErrorBuilder` interface has been extracted from `AbstractAntlr4Parser` and moved to the
`de.sayayi.lib.antlr4.syntax` package as a top-level interface. Update imports accordingly:

```java
// before
import de.sayayi.lib.antlr4.AbstractAntlr4Parser.SyntaxErrorBuilder;

// after
import de.sayayi.lib.antlr4.syntax.SyntaxErrorBuilder;
```

The `SyntaxErrorBuilder.with(Token)` method is now a default method that delegates to `withStart(token).withStop(token)`.

### AbstractVocabulary is now sealed after construction

The vocabulary can no longer be modified after the constructor returns. Calling `add()` outside of the `addTokens()`
method now throws an `IllegalStateException`.

## New Features

### SyntaxErrorException

A new `SyntaxErrorException` class has been added to the `de.sayayi.lib.antlr4.syntax` package. It is the default
exception thrown by `createException()`. The exception provides access to:

- `getStartToken()` / `getStopToken()` - the token range where the error occurred
- `getErrorMessage()` - the short error message
- `getFormattedMessage()` - the visual error representation with source context and markers
- `getMessage()` - combines the error message and formatted message

### Custom error message methods

Several new protected methods have been added to `AbstractAntlr4Parser` to allow customization of parser error
messages. Override these methods to produce error messages tailored to your grammar:

#### createTokenRecognitionMessage

Called when the lexer fails to determine the next token:

```java
protected String createTokenRecognitionMessage(
    Lexer lexer, String text, boolean hasEOF)
```

#### createInputMismatchMessage

Called when the parser encounters a token that does not match the expected input:

```java
protected String createInputMismatchMessage(
    Parser parser, IntervalSet expectedTokens,
    Token mismatchLocationNearToken)
```

#### createMissingTokenMessage

Called when the parser detects a missing token:

```java
protected String createMissingTokenMessage(
    Parser parser, IntervalSet expectedTokens,
    Token missingLocationNearToken)
```

#### createUnwantedTokenMessage

Called when the parser encounters an extraneous token:

```java
protected String createUnwantedTokenMessage(
    Parser parser, Token unwantedToken,
    IntervalSet expectedTokens)
```

#### createNoViableAlternativeMessage

Called when the parser cannot decide which of two or more paths to take:

```java
protected String createNoViableAlternativeMessage(
    Parser parser, Token startToken,
    Token offendingToken)
```

### Token display helper methods

New protected methods for consistent token display in error messages:

- `isEOFToken(Token)` - returns `true` if the token is of type `EOF`
- `getEOFTokenDisplayText()` - returns the display text for the EOF token (default: `<EOF>`)
- `getTokenDisplayText(Parser, Token)` - returns the display text for a token using the parser's vocabulary
- `getQuotedDisplayText(String)` - returns the text enclosed in quotes with `\n`, `\r`, `\t` escaped

## Bug Fixes

- Fixed `getTokenDisplayText()` returning raw token text instead of the vocabulary display name.
- Fixed `getQuotedDisplayText()` not properly handling text containing quote characters.
