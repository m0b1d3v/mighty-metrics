# ADR: Source formatting

## Status

Accepted.

## Context

Source files should have a standard consistent format for readability.
Some, like YAML or Makefile, require specific formatting or else parsers will fail.

## Decision

[EditorConfig](https://editorconfig.org/) will be used as it works with most code editors.

The following configurations will be set:
- Character encoding
- End of line character
- Indent style and sizes
- Trailing lines and spaces

## Consequences

Some developers are really particular about their indent styles.
This should not cause much friction though as most editors can be configured to display indents conditionally.
