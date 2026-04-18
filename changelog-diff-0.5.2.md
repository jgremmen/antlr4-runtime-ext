# Version [0.5.2](https://github.com/jgremmen/antlr4-runtime-ext/tree/0.5.2) (2025-03-18)

## Breaking Changes

### `getLineNumberFormat` replaced by `getLineNumberFormatter`

The protected method `GenericSyntaxErrorFormatter.getLineNumberFormat(int, int)` has been removed and replaced by `getLineNumberFormatter(int, int)`. The old method returned a `String` format pattern (e.g. `"%02d: "`), while the new method returns a `LineNumberFormatter` instance.

If you have subclassed `GenericSyntaxErrorFormatter` and overridden `getLineNumberFormat`, you must migrate to the new method signature:

**Before:**

```java
@Override
protected @NotNull String getLineNumberFormat(int lines, int stopLine)
{
  // custom format pattern
  return "%0" + digits + "d| ";
}
```

**After:**

```java
@Override
protected @NotNull LineNumberFormatter getLineNumberFormatter(int lines, int stopLine)
{
  int digits = 1;
  for (int upperLimit = 10; stopLine >= upperLimit; digits++)
    upperLimit *= 10;

  return new DefaultLineNumberFormatter(digits, '0', null, "| ");
}
```

Alternatively, you can implement the `LineNumberFormatter` functional interface directly:

```java
return (lineNumber, markedLine) -> String.format("%0" + digits + "d| ", lineNumber);
```

## New Features

### `LineNumberFormatter` interface

A new functional interface `GenericSyntaxErrorFormatter.LineNumberFormatter` has been introduced. It formats a line number into a fixed-length string for use in syntax error output. The method signature is:

```java
@NotNull String format(int lineNumber, boolean markedLine);
```

The `markedLine` parameter indicates whether the line contains an error marker, allowing implementations to visually distinguish error lines from context lines.

### `DefaultLineNumberFormatter` class

A new public class `GenericSyntaxErrorFormatter.DefaultLineNumberFormatter` implements `LineNumberFormatter`. It supports configurable line number width, padding character, prefix, and suffix.

```java
// produces "06: " for line 6 with 2-digit width
var formatter = new DefaultLineNumberFormatter(2, '0', null, ": ");
formatter.format(6, false); // "06: "
```

Constructor parameters:

- `lineNumberWidth` - number of digits to display (0 to 10)
- `paddingChar` - character used for left-padding (e.g. `'0'` or `' '`)
- `prefix` - optional string prepended before the line number
- `suffix` - optional string appended after the line number

### Error line awareness in line number formatting

The line number formatting now receives a `markedLine` boolean, indicating whether the current line is part of the error range. This allows custom `LineNumberFormatter` implementations to highlight error lines differently from surrounding context lines.

## Bug Fixes

_No bug fixes in this release._

