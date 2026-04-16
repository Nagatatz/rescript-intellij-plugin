---
myst:
  html_meta:
    "keywords": "api reference, dokka, kotlin, kdoc, classes, packages"
---

# API Reference

The Kotlin API reference is generated from KDoc comments using [Dokka](https://kotlinlang.org/docs/dokka-introduction.html).

:::{note}
The API reference lives at [`/api/`](../../api/) alongside this documentation. The link above points to the deployed site; when running docs locally, generate the API docs first with `./gradlew dokkaGenerate` from the project root.
:::

## Generating the API reference locally

```bash
# From the project root (not sphinx-docs/)
./gradlew dokkaGenerate

# Output lives at build/dokka/html/
open build/dokka/html/index.html
```

## What's included

All `public` and `internal` Kotlin declarations under `src/main/kotlin/com/rescript/plugin/`:

- Classes, objects, interfaces, enums
- Top-level functions and properties
- KDoc comments, `@param`, `@return`, `@see` annotations
- Source links to GitHub for each declaration

## What's excluded

- The auto-generated JFlex lexer (`RescriptFlexLexer.java`) — not useful as API
- `private` members (Dokka shows only `public` and `internal` by default)

## Key packages

| Package | Purpose |
|---------|---------|
| `com.rescript.plugin.lang` | Lexer, parser, token types |
| `com.rescript.plugin.lsp` | Language Server integration |
| `com.rescript.plugin.refactor` | Refactoring support |
| `com.rescript.plugin.intention` | Intention Actions |
| `com.rescript.plugin.inspection` | Code inspections |
| `com.rescript.plugin.util` | Shared utilities |

## See also

- [Architecture](architecture.md) — High-level architecture overview
- [Project Structure](project-structure.md) — Source layout and file organization
- [Extending](extending.md) — How to add new plugin features
