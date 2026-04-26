---
myst:
  html_meta:
    "keywords": "version matrix, compatibility, features by version, IDE requirements"
---

# Version & Feature Matrix

This page provides IDE compatibility information and a per-version breakdown of features added to the ReScript IntelliJ Plugin. For the versioning policy, release workflow, and authoritative source of each piece of version information, see the [developer-facing version reference](https://github.com/Nagatatz/rescript-intellij-plugin/blob/main/docs/versions.md).

## IDE Compatibility

| Plugin Version | IntelliJ Platform | JDK | ReScript | Language Server |
|----------------|-------------------|-----|----------|-----------------|
| 0.1.2 -- 0.1.12 | 2025.3+ | 21+ | v11+ | `@rescript/language-server` 1.0.0+ (latest stable recommended) |

:::{note}
Native features (syntax highlighting, code folding, brace matching, etc.) work without the language server.
LSP-powered features (completion, diagnostics, definition jump, etc.) require `@rescript/language-server` 1.0.0 or later to be installed in your project. Older 0.x releases are not supported because the plugin uses LSP extensions (`rescript/compilationStatus`, `textDocument/createInterface`, `textDocument/openCompiled`, semantic tokens) introduced in 1.x.
:::

## Features by Version

::::{tab-set}

:::{tab-item} 0.1.11
**Unreleased**

| Feature | Category |
|---------|----------|
| TypeScript/JSX paste conversion | Paste |
| REPL icon and improved input area | REPL |
| REPL resizable split, auto-scroll, history navigation | REPL |
| REPL syntax highlighting in input area | REPL |
| UI test framework with Remote-Robot | Infrastructure |
:::

:::{tab-item} 0.1.10
**Released 2026-03-19**

| Feature | Category |
|---------|----------|
| Benefit descriptions in Sphinx feature docs | Documentation |
| Japanese translations for all Sphinx docs | Documentation |
| Health checker extraction | Refactoring |
| Completion weigher deduplication | Improvement |
| Path utilities consolidation | Improvement |
:::

:::{tab-item} 0.1.9
**Released 2026-03-16**

| Feature | Category |
|---------|----------|
| File type recovery startup activity | Infrastructure |
:::

:::{tab-item} 0.1.8
**Released 2026-03-12**

| Feature | Category |
|---------|----------|
| Missing keyboard shortcuts in docs | Documentation |
| Missing settings documentation in Sphinx docs | Documentation |
| Undocumented features added to Sphinx docs | Documentation |
:::

:::{tab-item} 0.1.7
**Released 2026-03-11**

Bug fixes only (threading fixes, icon attribute fix, version string fix).
:::

:::{tab-item} 0.1.6
**Released 2026-03-10**

Bug fixes only (EDT threading, GenerateGroup, testCreator extension point).
:::

:::{tab-item} 0.1.5
**Released 2026-03-10**

| Feature | Category |
|---------|----------|
| JUnit 5 migration | Refactoring |
| Regex centralization | Refactoring |
| Brace balance extraction | Refactoring |
| Postfix template deduplication | Refactoring |
:::

:::{tab-item} 0.1.4
**Released 2026-03-06**

| Feature | Category |
|---------|----------|
| Process utility extraction | Refactoring |
:::

:::{tab-item} 0.1.3
**Released 2026-03-05**

| Feature | Category |
|---------|----------|
| Plugin icon | Improvement |
| Verifier warnings fix | Improvement |
:::

:::{tab-item} 0.1.2
**Released 2026-03-03 -- Initial release with 109 features**

#### Syntax

| Feature | Description |
|---------|-------------|
| Syntax highlighting | JFlex lexer-based token coloring for all ReScript constructs |
| Semantic highlighting | LSP semantic tokens for high-precision coloring |
| Color scheme settings | Per-token color customization (Darcula / Default themes) |
| Brace matching | `{}`, `[]`, `()` pair highlighting |
| Code folding | Module, declaration, and block comment folding |
| `//#region` folding | Custom folding markers |
| Commenter | Line (`//`) and block (`/* */`) comment toggling |
| Breadcrumbs | Scope path display at editor top |
| Context Info | Sticky top-level declaration headers on scroll |

#### Completion

| Feature | Description |
|---------|-------------|
| LSP completion | Type-aware intelligent completion via language server |
| Postfix completion | `.switch`, `.pipe`, `.log`, `.some`, `.ok`, `.error`, `.ignore`, `.promise`, `.await` |
| Live templates | 15+ snippets (`let`, `mod`, `switch`, `if`, `@module`, `@val`, `comp`, etc.) |
| Live template context | ReScript-specific context + `moduleName`/`componentName` macros |
| Completion confidence | Suppress completion popup in comments and strings |
| Completion weigher | Context-based candidate ranking |
| Signature Help | Parameter info popup on `(` input |
| Parameter Info Handler | Labeled argument display via {kbd}`Ctrl+P` |
| Lookup char filter | Character filtering during candidate selection |

#### Navigation

| Feature | Description |
|---------|-------------|
| Definition jump | {kbd}`Ctrl+Click` / {kbd}`Ctrl+B` to definition |
| Find Usages | Symbol usage search with WordsScanner indexing |
| Go to Symbol | {kbd}`Cmd+Option+O` symbol search |
| Search Everywhere | {kbd}`Shift+Shift` unified file/symbol search |
| Go to Related | Navigate between `.res`/`.resi`/`.js` related files |
| Go to Implementation | `.resi` to `.res` implementation jump ({kbd}`Ctrl+Alt+B`) |
| Go to Super | `.res` to `.resi` declaration jump ({kbd}`Ctrl+U`) |
| Go to Test | Implementation/test file navigation ({kbd}`Ctrl+Shift+T`) |
| `.res`/`.resi` switch | {kbd}`Alt+O` to toggle implementation/interface |
| Navigation bar | Structure-based navigation bar |
| External documentation | Belt/Js module docs via {kbd}`Shift+F1` |
| Quick documentation | PSI-based fallback docs via {kbd}`Ctrl+Q` / hover |
| File Include Provider | `open` statement file navigation |
| Type signature search | Reverse function lookup by type signature |
| Run Anything | {kbd}`Ctrl+Ctrl` for ReScript CLI commands |

#### Editing

| Feature | Description |
|---------|-------------|
| 20+ Intention Actions | Wrap with Some/Ok/Error, @genType, labeled args, pipe conversion, etc. |
| Surround With | Wrap selection in if/switch/try/block ({kbd}`Ctrl+Alt+T`) |
| Unwrap/Remove | Remove Some/Ok/Error/if/switch/try/braces ({kbd}`Ctrl+Shift+Delete`) |
| Smart Enter | Complete statement and newline ({kbd}`Shift+Enter`) |
| Enter Handler | Auto-continue doc/line comments |
| Join Lines | Smart join for let/pipe/arrow |
| Word Selection | Expand/shrink selection for strings, brackets, comments |
| Statement Mover | Move top-level declarations up/down ({kbd}`Alt+Shift+Up/Down`) |
| Move Element Left/Right | Comma-separated element reordering |
| JSX close tag | Auto-insert closing tag on `>` |
| Backspace Handler | JSX tag pair deletion |
| Copy/Paste Pre-Processor | String literal escape on paste |
| Paste as JSON.t | Clipboard JSON to ReScript `JSON.t` |
| Paste as JSX | HTML to ReScript JSX conversion |
| Split/Join List | Toggle single-line/multi-line lists |
| Code Block Handler | Block boundary detection ({kbd}`Ctrl+Shift+[`/`]`) |
| Qualified Name Copy | Full qualified name copy ({kbd}`Cmd+Shift+Alt+C`) |
| File templates | Module, Interface, Component templates |
| Code Rearranger | Auto-sort top-level declarations |
| Strip Trailing Spaces | Preserve whitespace in string literals |
| Editor Floating Toolbar | Format/Open JS/Create Interface toolbar |

#### Run & Build

| Feature | Description |
|---------|-------------|
| Run configuration | ReScript build execution via rescript.json |
| Gutter run icons | Run icons in `.res` file gutter |
| Build status widget | Compiler status in status bar |
| Build auto-start prompt | `rescript build -w` suggestion on project open |
| Console filter | Clickable file:line links in compiler output |
| Compiled JS preview | Real-time preview of compiled JavaScript |
| Open compiled JS | Open corresponding `.js` file ({kbd}`Alt+Shift+J`) |
| Format on save | External formatter via `rescript format` |
| Format check | Detect unformatted code with Quick Fix |
| Interface file generation | `.res` to `.resi` auto-generation |
| Project wizard | 12 project templates (Basic, Vite+React, Next.js, etc.) |
| Framework detector | Auto-detect ReScript via rescript.json |
| LSP auto-install | One-click language server installation |
| Restart LSP action | Restart LSP server from Tools menu |

#### Testing

| Feature | Description |
|---------|-------------|
| Test runner | jest/vitest auto-detection with SMTRunner |
| Test source filter | `*_test.res`, `*.test.res`, `__tests__/` recognition |

#### Analysis

| Feature | Description |
|---------|-------------|
| Real-time diagnostics | Inline compile errors/warnings via LSP |
| Inlay hints | Inferred type annotations |
| Code Lens | Type signature display above functions |
| Error Lens | Inline diagnostics at line end |
| Type mismatch hints | Structured Expected/Actual type display |
| Type mismatch diff | Color-coded type error diff |
| Inspections | Duplicate open, empty module, missing config, signature sync, mutability, style lint |
| Reanalyze integration | Dead code / unhandled exception analysis |
| Reanalyze server mode | Daemon mode for fast incremental analysis |
| Import optimizer | Auto-remove duplicate/unused opens ({kbd}`Ctrl+Alt+O`) |
| Quick Fixes | Add open, qualify reference, generate function, type hole suggestions |
| Problem highlight filter | Suppress highlights in node_modules |
| Expression type | Show type at cursor ({kbd}`Ctrl+Shift+P`) |
| Pipe chain type hints | Intermediate types in `->` pipe chains |
| PPX visualization | PPX annotation effects as inlay hints |
| Color provider | Color preview swatches for #hex, rgb, hsl |
| Inspection suppressor | `// noinspection` comment support |

#### Advanced

| Feature | Description |
|---------|-------------|
| Refactoring (Extract Variable) | Extract expression to `let` binding ({kbd}`Ctrl+Alt+V`) |
| Refactoring (Extract Function) | Extract to new function ({kbd}`Ctrl+Alt+M`) |
| Refactoring (Inline) | Inline variable/function ({kbd}`Ctrl+Alt+N`) |
| Refactoring (Change Signature) | Modify function signature ({kbd}`Ctrl+F6`) |
| Refactoring (Introduce Constant) | Extract literal to constant |
| React component extraction | Extract JSX to new component |
| Rename | LSP-powered symbol rename |
| Safe Delete | Usage-checked safe deletion |
| Name suggestion | Type/filename-based name suggestions |
| Generate actions | Switch arms, module type, make function, record value, JSON encoder/decoder, module impl |
| .d.ts binding generation | TypeScript `.d.ts` to ReScript `external` conversion |
| Structure view | File symbol tree |
| Module hierarchy | Module dependency tree |
| Call hierarchy | Callers/Callees tree ({kbd}`Ctrl+Alt+H`) |
| Dependency diagram | Module dependency graph visualization |
| Package dependencies view | rescript.json dependency tree tool window |
| Type Info toolwindow | Always-visible type display |
| REPL | Interactive execution environment |
| Worksheet mode | `.resw` interactive evaluation |
| Scratch file | ReScript scratch file creation |
| PPX expansion view | PPX macro expansion tool window |
| Compiled JS nesting | Nest compiled JS under `.res` in Project View |
| `.resi` nesting | Nest `.resi` under `.res` in Project View |
| Project View decorator | "has interface" suffix, rescript.json version |
| VCS Code Vision | Author/last-change annotations |
| Stub index | Fast symbol search via PSI stub index |
| Open statement index | File-based index for fast module lookup |
| Spellcheck dictionary | ReScript-specific terms |
| `%raw()` JS injection | JavaScript highlighting in `%raw()` |
| `%re()` RegExp injection | RegExp language injection |
| Markdown ReScript highlight | ReScript in ` ```rescript ` code fences |
| Grazie text extractor | Natural language extraction for Grazie |
| JSON Schema | Completion/validation for rescript.json |
| Reader mode | Read-only view for node_modules `.res`/`.resi` |
| Auto Import options | Settings > Editor > Auto Import UI |
| Predefined code style | "ReScript Standard" preset |
| GitHub error reporter | Automatic error reporting to GitHub Issues |
| Highlight usages | Matching keyword highlighting (switch/if/try) |
| Element signature | Folding state persistence |
| TODO indexing | TODO/FIXME pattern recognition |
| LSP initialization options | 6 settings sent to LSP server |
| Dump LSP State | Debug LSP internal state |
:::

::::
