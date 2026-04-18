# Version [0.5.1](https://github.com/jgremmen/antlr4-runtime-ext/tree/0.5.1) (2024-11-14)

## Breaking Changes

### Java Module System support

A `module-info.java` has been added, turning this library into a named Java module (`de.sayayi.lib.antlr`). Classes in packages not listed in the module descriptor are no longer accessible to consumers. If your project uses the Java Module System, you must add a `requires de.sayayi.lib.antlr;` directive to your own `module-info.java`.

### `antlr4-runtime` dependency scope changed from `api` to `implementation`

The `org.antlr:antlr4-runtime` dependency has been changed from `api` to `implementation`. This means it is no longer transitively available on the compile classpath of consumers. If your project directly uses classes from `antlr4-runtime`, you must now declare the dependency explicitly in your own build script:

```groovy
implementation "org.antlr:antlr4-runtime:4.13.2"
```

### `jetbrains:annotations` dependency scope changed from `compileOnly` to `compileOnlyApi`

The `org.jetbrains:annotations` dependency scope has been changed from `compileOnly` to `compileOnlyApi`, and the version range has been widened from `25.0.+` to `[24.0,26.1)`. This makes the annotations transitively visible at compile time for consumers. No action is required unless you have pinned a specific version of `org.jetbrains:annotations` outside the new range.

### Dependency changes

| Dependency | Scope | 0.5.0 | 0.5.1 |
|---|---|---|---|
| `org.antlr:antlr4-runtime` | compile | `4.13.2` (api) | `4.13.2` (implementation) |
| `org.jetbrains:annotations` | compile | `25.0.+` (compileOnly) | `[24.0,26.1)` (compileOnlyApi) |

## New Features

### `walk` method is now `protected`

The `AbstractAntlr4Parser.walk(ParseTreeListener, ParserRuleContext)` method has been changed from `private` to `protected`. Previously, tree walking was only performed internally as part of the `parse` method. With this change, subclasses can walk a `ParserRuleContext` with a different listener after the initial parse has completed, or re-walk the same context multiple times.

The method selects the walking strategy based on whether the listener implements `WalkerSupplier`. If it does, the walker returned by `WalkerSupplier.getWalker()` is used; otherwise, the default `WALK_FULL_RECURSIVE` strategy is applied.

```java
// In a subclass of AbstractAntlr4Parser:
var context = parse(lexer, MyParser::new, MyParser::myRule);
walk(new FirstPassListener(), context);
walk(new SecondPassListener(), context);
```

### Internal token class renamed to `LexerPositionToken`

The internal token class `PositionToken` (used to represent the position of a lexer error) has been renamed to `LexerPositionToken`. Its `getType()` method now returns `Token.INVALID_TYPE` instead of `0`, which correctly represents an invalid token type. This change does not affect the public API.

## Bug Fixes

_No bug fixes in this release._

