# Version [0.5.1](https://github.com/jgremmen/antlr4-runtime-ext/releases/tag/0.5.1) (2024-11-14)

## Breaking Changes

### Java module system support (module-info.java)

A `module-info.java` has been added, declaring the module `de.sayayi.lib.antlr`. The module exports the following
packages:

- `de.sayayi.lib.antlr4`
- `de.sayayi.lib.antlr4.walker`
- `de.sayayi.lib.antlr4.syntax`

Internal implementation classes in non-exported packages are no longer accessible when running on the module path.
Projects using the class path are not affected by this change.

### Dependency scope changes

| Dependency | Type | Old scope | New scope |
|---|---|---|---|
| org.antlr:antlr4-runtime 4.13.2 | runtime | compile (transitive) | compile (non-transitive) |
| org.jetbrains:annotations | compile | compile (non-transitive) | compile (transitive) |

The `antlr4-runtime` dependency scope changed from `api` (transitive) to `implementation` (non-transitive). Projects
that rely on `antlr4-runtime` classes must now declare the dependency explicitly.

The JetBrains annotations dependency scope changed from `compileOnly` (non-transitive) to `compileOnlyApi`
(transitive). The accepted version range widened from `25.0.+` to `[24.0,26.1)`.

## New Features

### walk() method is now protected

The `walk()` method in `AbstractAntlr4Parser` has been changed from `private` to `protected`. This allows
subclasses to call `walk()` independently from the `parse()` method, e.g. to walk multiple listeners over
the same parse tree:

```java
var ctx = parse(lexer, MyParser::new, MyParser::startRule);
walk(firstListener, ctx);
walk(secondListener, ctx);
```

## Bug Fixes

_No bug fixes in this release._
