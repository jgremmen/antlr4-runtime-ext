# Version [0.5.4](https://github.com/jgremmen/antlr4-runtime-ext/releases/tag/0.5.4) (2025-05-28)

## Breaking Changes

_No breaking changes in this release._

## New Features

_No new features in this release._

## Bug Fixes

- Fixed `SyntaxErrorBuilder.with(SyntaxTree)` incorrectly setting the stop token to `ParserRuleContext.getStart()`
  instead of `ParserRuleContext.getStop()`, causing the error marker to always point to the start of the context
  instead of spanning the full range.
