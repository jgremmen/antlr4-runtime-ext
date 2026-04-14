# ANTLR4 Runtime Extensions

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/de.sayayi.lib/antlr4-runtime-ext)](https://central.sonatype.com/artifact/de.sayayi.lib/antlr4-runtime-ext)

A Java library that extends the [ANTLR4](https://www.antlr.org/) runtime with an abstract parser framework, human-readable syntax error formatting, custom vocabularies, and heap-based parse tree walkers.

## Features

- **Abstract parser framework** — Simplifies setting up ANTLR4 lexers, parsers, error listeners, and tree walkers with minimal boilerplate.
- **Rich syntax error messages** — Formats parsing errors with source code snippets, line numbers, and visual caret markers pointing to the exact error location.
- **Custom vocabularies** — Convenience base class for defining readable token names used in error messages.
- **Heap-based tree walkers** — Iterative (non-recursive) parse tree traversal strategies that prevent stack overflows on deeply nested inputs.
- **Java module support** — Ships with a `module-info.java` (`de.sayayi.lib.antlr`).

## Requirements

- **Java** 21+
- **ANTLR4 Runtime** 4.13.2

## Installation

### Gradle

```kotlin
dependencies {
  implementation("de.sayayi.lib:antlr4-runtime-ext:0.7.0")
}
```

### Maven

```xml
<dependency>
  <groupId>de.sayayi.lib</groupId>
  <artifactId>antlr4-runtime-ext</artifactId>
  <version>0.7.0</version>
</dependency>
```

## Quick Start

The example below shows a JSON parser built on top of `AbstractAntlr4Parser`. It demonstrates custom vocabulary, error formatting, walker selection, and semantic validation inside a listener.

### 1. Define a custom vocabulary

```java
private static final Vocabulary VOCABULARY = new AbstractVocabulary() {
  @Override
  protected void addTokens() 
  {
    add(STRING, "<string>", "STRING");
    add(NUMBER, "<number>", "NUMBER");
    add(WS, "<whitespace>", "WS");
    add(T__0, "'{'", "LBRACE");
    add(T__1, "','", "COMMA");
    add(T__2, "'}'", "RBRACE");
    add(T__3, "':'", "COLON");
    add(T__4, "'['", "LBRACKET");
    add(T__5, "']'", "RBRACKET");
    add(T__6, "'true'", "TRUE");
    add(T__7, "'false'", "FALSE");
    add(T__8, "'null'", "NULL");
  }
};
```

### 2. Create a parser subclass

```java
public final class JsonCompiler extends AbstractAntlr4Parser 
{
  private static final SyntaxErrorFormatter ERROR_FORMATTER =
      new GenericSyntaxErrorFormatter(2, 1, 1, 2);

  public JsonCompiler() {
    super(ERROR_FORMATTER);
  }

  public void parseJson(String text) 
  {
    parse(
        new JSONLexer(CharStreams.fromString(text)),
        lexer -> new JSONParser(new BufferedTokenStream(lexer)),
        JSONParser::json,
        new JsonListener(),
        ctx -> null
    );
  }
}
```

### 3. Implement a listener with walker selection

```java
private final class JsonListener extends JSONBaseListener
    implements AbstractAntlr4Parser.WalkerSupplier
{
  @Override
  public Walker getWalker() 
  {
    // Only exit callbacks needed — skip enter, terminal, and error visits
    return Walker.WALK_EXIT_RULES_HEAP;
  }

  @Override
  public void exitObj(JSONParser.ObjContext ctx) 
  {
    if (ctx.pair().size() > 3) 
    {
      syntaxError("object must have at most 3 pairs")
          .withStart(ctx.pair().get(3))
          .withStop(ctx.pair().getLast())
          .report();
    }
  }
}
```

### Error output

When a syntax error is encountered, the library throws a `SyntaxErrorException` with a message like:

```
mismatched input <EOF> expecting ':'

  {"test"
         ^
```

For multi-line input:

```
token recognition error at: 'u'

  2:   "data": 4.5e-3,
  3:   "test": ull,
               ^
  4:   "more": true
```

## Package Overview

| Package | Description |
|---|---|
| `de.sayayi.lib.antlr4` | Core framework — `AbstractAntlr4Parser`, `AbstractVocabulary`, `LocationToken` |
| `de.sayayi.lib.antlr4.syntax` | Syntax error reporting — `SyntaxErrorFormatter`, `GenericSyntaxErrorFormatter`, `SyntaxErrorBuilder`, `SyntaxErrorException` |
| `de.sayayi.lib.antlr4.walker` | Heap-based parse tree walkers — `Walker` enum with `WALK_FULL_HEAP`, `WALK_EXIT_RULES_HEAP`, `WALK_ENTER_AND_EXIT_RULES_HEAP` |

### Key Classes

#### `AbstractAntlr4Parser`

Abstract base class that manages the full parse lifecycle: lexer configuration, parser creation, error listener installation, error strategy, and tree walking. Subclasses implement a public parse method and optionally override hooks for customizing error messages (`createTokenRecognitionMessage`, `createInputMismatchMessage`, `createMissingTokenMessage`, `createUnwantedTokenMessage`, `createNoViableAlternativeMessage`, `createException`).

#### `AbstractVocabulary`

Convenience `Vocabulary` implementation. Override `addTokens()` to register token entries with literal and symbolic names. The vocabulary is sealed after construction and cannot be modified.

#### `GenericSyntaxErrorFormatter`

Configurable `SyntaxErrorFormatter` that produces multi-line error output with line numbers, source context lines, and caret markers. Configure tab size, context lines before/after the error, and output prefix/indentation.

#### `Walker`

Enum with three heap-based depth-first-search walk strategies:

| Variant | Enter Rule | Exit Rule | Terminal / Error Nodes |
|---|:---:|:---:|:---:|
| `WALK_FULL_HEAP` | ✔ | ✔ | ✔ |
| `WALK_EXIT_RULES_HEAP` | ✘ | ✔ | ✘ |
| `WALK_ENTER_AND_EXIT_RULES_HEAP` | ✔ | ✔ | ✘ |

A listener can implement `AbstractAntlr4Parser.WalkerSupplier` to declare which walker it needs; otherwise `WALK_FULL_HEAP` is used by default.

#### `SyntaxErrorBuilder`

Fluent builder API for constructing syntax errors with precise start/stop locations from tokens or syntax tree nodes.

#### `SyntaxErrorException`

`RuntimeException` carrying both a plain error message and a formatted visual representation of the error location.

## Building from Source

```bash
./gradlew build
```

## License

This project is licensed under the [Apache License 2.0](LICENSE).

## Author

Jeroen Gremmen — [jeroen.gremmen@sayayi.de](mailto:jeroen.gremmen@sayayi.de)

