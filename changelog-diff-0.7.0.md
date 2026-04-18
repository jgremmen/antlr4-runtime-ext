# Version [0.7.0](https://github.com/jgremmen/antlr4-runtime-ext/tree/0.7.0) (2026-04-18)

## Breaking Changes

### Minimum Java version raised to 21

The library now requires Java 21 or higher. Projects using Java 11 must upgrade their JDK before adopting this version.


### Dependency changes

| Dependency | Type | Old version | New version |
|---|---|---|---|
| org.jetbrains:annotations | compile | [24.0,26.1) | [24.0,26.2) |

The JetBrains annotations version range upper bound has been widened from `26.1` to `26.2`. Projects that pin a
specific version within this range are not affected. Projects that use a version `>= 26.1` and `< 26.2` are now
also supported.


## New Features

### `getTerminalToken` method in `AbstractAntlr4Parser`

A new protected method `getTerminalToken(ParserRuleContext, int)` has been added to `AbstractAntlr4Parser`. It
retrieves the terminal token at a given child index of a parser rule context, returning `null` if the child at that
index is not a terminal node.

This method simplifies extracting tokens from specific positions in a parse tree rule, which is a common operation
when implementing parse tree listeners or visitors.

```java
@Override
public void exitAssignment(MyParser.AssignmentContext ctx)
{
  Token equalsToken = getTerminalToken(ctx, 1);
  if (equalsToken != null)
  {
    // process the '=' token
  }
}
```

## Bug Fixes

`createNoViableAlternativeMessage` used a hardcoded `"<EOF>"` string instead of delegating to `getEOFTokenDisplayText()`, inconsistent with other methods in `AbstractAntlr4Parser`.
