# Version [0.5.3](https://github.com/jgremmen/antlr4-runtime-ext/releases/tag/0.5.3) (2025-05-27)

## Breaking Changes

### Deprecated syntaxError() methods

The following `syntaxError()` methods in `AbstractAntlr4Parser` have been deprecated for removal. Use the new
builder-based `syntaxError(String)` method instead.

```java
// deprecated methods
syntaxError(ParserRuleContext ctx, String errorMsg)
syntaxError(ParserRuleContext ctx, String errorMsg, Exception cause)
syntaxError(TerminalNode terminalNode, String errorMsg)
syntaxError(TerminalNode terminalNode, String errorMsg, Exception cause)
syntaxError(Token token, String errorMsg)
syntaxError(Token token, String errorMsg, Exception cause)
```

Replace calls with the builder pattern:

```java
// before
syntaxError(ctx, "unexpected value");

// after
syntaxError("unexpected value").with(ctx).report();

// before (with cause)
syntaxError(token, "invalid input", cause);

// after
syntaxError("invalid input").with(token).withCause(cause).report();
```

The builder provides more flexibility by allowing independent control over start and stop tokens:

```java
syntaxError("range is invalid")
    .withStart(startCtx)
    .withStop(stopCtx)
    .report();
```

## New Features

### SyntaxErrorBuilder interface

A new `SyntaxErrorBuilder` interface has been added (as an inner type of `AbstractAntlr4Parser`) for constructing
syntax errors using the builder pattern. It is obtained via the new `syntaxError(String)` method:

```java
protected SyntaxErrorBuilder syntaxError(String errorMessage)
```

The builder provides methods for specifying start and stop tokens individually (`withStart()`, `withStop()`),
or setting both at once via `with(Token)` or `with(SyntaxTree)`. The `report()` method triggers exception creation
and always throws.

### LocationToken class

A new public `LocationToken` class has been added to the `de.sayayi.lib.antlr4` package. It is a minimal `Token`
implementation that carries only location information (line, column, start/stop index, and input stream). It is
useful for constructing syntax error tokens in situations where a full `CommonToken` is not available, such as lexer
error recovery.

### Constructor with keepConsoleErrorListeners option

A new constructor has been added to `AbstractAntlr4Parser`:

```java
protected AbstractAntlr4Parser(
    SyntaxErrorFormatter syntaxErrorFormatter,
    boolean keepConsoleErrorListeners)
```

By default, the `ConsoleErrorListener` instances added by ANTLR4 to lexer and parser instances are removed. Set
`keepConsoleErrorListeners` to `true` to retain them.

## Bug Fixes

_No bug fixes in this release._
