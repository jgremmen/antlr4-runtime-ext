# Version [0.5.0](https://github.com/jgremmen/antlr4-runtime-ext/releases/tag/0.5.0) (2024-10-01)

## Breaking Changes

### Minimum Java version raised to 11

The library now requires Java 11 or higher. Projects using Java 8 must upgrade their JDK before adopting this version.

### Dependency changes

| Dependency | Type | Old version | New version |
|---|---|---|---|
| org.antlr:antlr4-runtime | compile | 4.9.3 | 4.13.2 |
| org.jetbrains:annotations-java5 | compile | 24.0.1 | - |
| org.jetbrains:annotations | compile | - | 25.0.+ |

The `antlr4-runtime` dependency has been upgraded from 4.9.3 to 4.13.2. Projects should verify compatibility with
this version if they depend on ANTLR4 runtime directly.

The JetBrains annotations artifact changed from `annotations-java5` to `annotations`. The `annotations-java5`
artifact is a Java 5 compatible variant that is no longer needed since the minimum Java version is now 11. The
dependency scope changed from optional (`optionalCompileOnlyApi`) to non-transitive compile-only (`compileOnly`).
Projects that relied on the annotations being transitively available must now declare the dependency explicitly.

## New Features

_No new features in this release._

## Bug Fixes

_No bug fixes in this release._
