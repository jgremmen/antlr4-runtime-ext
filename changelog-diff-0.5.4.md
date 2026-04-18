# Version [0.5.4](https://github.com/jgremmen/antlr4-runtime-ext/tree/0.5.4) (2025-05-28)
## Breaking Changes
_No breaking changes in this release._
## New Features
_No new features in this release._
## Bug Fixes
- Fixed `AbstractAntlr4Parser` incorrectly using `getStart()` instead of `getStop()` when determining the stop token for a `ParserRuleContext`. This caused syntax error markers to point only to the beginning of a parser rule context instead of spanning its full range.
