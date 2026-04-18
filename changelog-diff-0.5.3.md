# Version [0.5.3](https://github.com/jgremmen/antlr4-runtime-ext/tree/0.5.3) (2025-05-27)

## Breaking Changes

### Deprecation of `syntaxError` methods in `AbstractAntlr4Parser`

The following methods have been deprecated for removal:

- `syntaxError(ParserRuleContext, String)`
- `syntaxError(ParserRuleContext, String, Exception)`
- `syntaxError(TerminalNode, String)`
- `syntaxError(TerminalNode, String, Exception)`
- `syntaxError(Token, String)`
- `syntaxError(Token, String, Exception)`

These methods are replaced by the new `SyntaxErrorBuilder` API, accessible via `syntaxError(String)`. Instead of calling:

```java
syntaxError(ctx, "unexpected token");
```

use the builder pattern:

```java
syntaxError("unexpected token")
    .with(ctx)
    .report();
```

If a cause exception needs to be provided:

```java
syntaxError("unexpected token")
    .with(ctx)
    .withCause(cause)
    .report();
```

The builder allows separate control over start and stop tokens using `withStart(...)` and `withStop(...)`.

### Renamed `parserSupplier` parameter to `parserInstantiator`

The `parse` method parameter previously named `parserSupplier` has been renamed to `parserInstantiator`. This is a source-compatible change unless the parameter name was referenced explicitly (e.g. in documentation or reflection-based code).

## New Features

### `SyntaxErrorBuilder` interface

A new public interface `AbstractAntlr4Parser.SyntaxErrorBuilder` has been introduced. It provides a fluent API for constructing and reporting syntax errors. The builder supports the following methods:

- `withStart(Token)` / `withStart(SyntaxTree)` - set the start location of the error
- `withStop(Token)` / `withStop(SyntaxTree)` - set the stop location of the error
- `with(Token)` / `with(SyntaxTree)` - set both start and stop to the same location
- `withCause(Exception)` - attach a root cause
- `report()` - format and throw the exception

The builder is obtained by calling the new `syntaxError(String)` method on `AbstractAntlr4Parser`.

### `LocationToken` class

The previously private inner class `LexerPositionToken` has been extracted into a public class `LocationToken`. It implements the `Token` interface and provides location information (line, column, start/stop index) without being tied to a specific lexer token. This is useful for constructing synthetic tokens for error reporting.

```java
var token = new LocationToken(inputStream, line, charPositionInLine, startIndex, stopIndex);
```

### Constructor option to keep console error listeners

A new constructor `AbstractAntlr4Parser(SyntaxErrorFormatter, boolean)` has been added. The second parameter `keepConsoleErrorListeners` controls whether the `ConsoleErrorListener` instances automatically added by ANTLR4 to the lexer and parser are removed. The default behavior (removing them) is unchanged. Set the parameter to `true` to retain console error output.

### `WalkerSupplier` support in `walk` method

The `walk` method now checks if the provided `ParseTreeListener` implements `WalkerSupplier`. If it does, the walker returned by `WalkerSupplier.getWalker()` is used instead of the default `Walker.WALK_FULL_RECURSIVE`.

### Protected access for `GenericSyntaxErrorFormatter.Location` constructor

The `Location` inner class constructor has been changed from private to protected, allowing subclasses of `GenericSyntaxErrorFormatter` to create `Location` instances.

## Bug Fixes

_No bug fixes in this release._
