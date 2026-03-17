# Design: Benefit Paragraphs

## Pattern
Each `##` section gets a benefit paragraph placed at the section's end (before the next `##`). If a `:::{note}` or `:::{tip}` exists at the end, the benefit paragraph goes before it... actually after it but before the next `##`.

## Style
- Contrast pattern: "Instead of X, you can Y" or "Without this, X; with this, Y"
- Direct benefit: "This saves time by..." / "This helps you..."
- 1-2 sentences max
- English only in `.md`, Japanese translation in `.po`

## No Changes
- Existing content is not modified
- `###` sections do not get benefit paragraphs
