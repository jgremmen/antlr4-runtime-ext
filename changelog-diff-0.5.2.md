# Version [0.5.2](https://github.com/jgremmen/antlr4-runtime-ext/releases/tag/0.5.2) (2025-03-18)

## Breaking Changes

### getLineNumberFormat() replaced by getLineNumberFormatter()

The protected method `getLineNumberFormat()` in `GenericSyntaxErrorFormatter` has been replaced by
`getLineNumberFormatter()`. Instead of returning a format string, the new method returns a `LineNumberFormatter`
instance.

```java
// before
protected String getLineNumberFormat(int lines, int stopLine) {
  return "%02d: ";
}

// after
protected LineNumberFormatter getLineNumberFormatter(int lines, int stopLine) {
  return new DefaultLineNumberFormatter(2, '0', null, ": ");
}
```

If you have overridden `getLineNumberFormat()` in a subclass, rename the method and update the return type.

## New Features

### LineNumberFormatter interface

A new `LineNumberFormatter` functional interface has been introduced in `GenericSyntaxErrorFormatter`. It provides
a `format(int lineNumber, boolean markedLine)` method that returns a fixed-length formatted line number string.

The `markedLine` parameter indicates whether the current line contains an error marker, allowing implementations
to visually distinguish error lines from context lines (e.g., by using different colors or prefixes).

### DefaultLineNumberFormatter

A default implementation `DefaultLineNumberFormatter` is provided as a public static class inside
`GenericSyntaxErrorFormatter`. It supports configurable line number width, padding character, prefix, and suffix:

```java
// 3-digit zero-padded line number followed by ": "
var formatter = new DefaultLineNumberFormatter(3, '0', null, ": ");
// produces: "001: ", "042: ", etc.
```

## Bug Fixes

_No bug fixes in this release._
