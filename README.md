# ReScript IntelliJ Plugin

[![CI](https://github.com/Nagatatz/rescript-intellij-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/Nagatatz/rescript-intellij-plugin/actions/workflows/ci.yml)
[![JetBrains Marketplace](https://img.shields.io/jetbrains/plugin/v/com.rescript.plugin.svg?label=Marketplace)](https://plugins.jetbrains.com/plugin/com.rescript.plugin)
[![Sponsor](https://img.shields.io/badge/Sponsor-%E2%9D%A4-pink?logo=github)](https://github.com/sponsors/Nagatatz)

[ReScript](https://rescript-lang.org) language support for IntelliJ IDEA and other JetBrains IDEs.

**[Documentation](https://nagatatz.github.io/rescript-intellij-plugin/)** · **[Version & Compatibility](docs/versions.md)**

## Features

### Language Support

- **Syntax highlighting** — Full JFlex lexer-based highlighting for `.res` and `.resi` files
- **Semantic highlighting** — Enhanced token coloring via LSP semantic tokens
- **Code folding** — Collapse blocks, comments, multi-line JSX, and custom `//#region` regions
- **Brace matching** — Automatic matching for `{}`, `[]`, `()`
- **Smart quotes** — Automatic quote pair completion
- **Line & block comments** — Toggle `//` and `/* */` comments
- **Spell checking** — Integrated spell checker for comments and strings with bundled ReScript dictionary
- **Breadcrumb navigation** — Editor breadcrumbs showing current code context
- **Navigation bar** — Structure-aware navigation bar showing declaration hierarchy

### Code Intelligence (via LSP)

- **Code completion** — Context-aware suggestions
- **Go to definition** — Navigate to symbol definitions
- **Hover documentation** — Inline type information and docs on hover
- **Find references** — Locate all usages of a symbol
- **Find Usages** — Symbol indexing and usage search for ReScript identifiers
- **Rename refactoring** — Safe project-wide renaming (`Shift+F6`, LSP-driven)
- **Rename variant constructor (no LSP)** — Alt+Enter intention that renames a variant constructor across the project using a token-shape classifier (CONSTRUCTOR / PATTERN / MODULE_QUALIFIED_TAIL); works even when the language server is offline, with a 500-occurrence cap that defers to `Shift+F6` for very common names
- **Diagnostics** — Real-time error and warning display
- **Inlay hints** — Type annotations displayed inline
- **Signature help** — Parameter info for function calls
- **Code Vision** — Function type annotations via Code Lens
- **Quick Documentation** — PSI-based fallback documentation when LSP is unavailable (`Ctrl+Q`)
- **Usage type classification** — Categorized Find Usages results (open, type, pipe, JSX, etc.)
- **Extract Variable** — Extract selected expression into a `let` binding (`Ctrl+Alt+V`)
- **Extract Function** — Extract selected code into a new function (`Ctrl+Alt+M`)
- **Inline Variable/Function** — Inline expand variables and functions (`Ctrl+Alt+N`)
- **Introduce Constant** — Extract literal values into module-level constants
- **Change Signature** — Modify function parameters and update call sites (`Ctrl+F6`)
- **React component extraction** — Extract JSX into a new React component
- **Safe Delete** — Usage-checking safe delete refactoring (`Refactor > Safe Delete`)
- **Name suggestions** — Type-based and filename-based name suggestions during rename
- **Completion Weigher** — Context-based prioritization of completion candidates
- **Pipe chain type hints** — Inline intermediate type display for `->` pipe chains
- **Type narrowing visualizer** — Inline display of the narrowed type at each `switch` arm (so `Some(_)`, `None`, etc. show their refined type without hovering)
- **Variant flow diagram** — Tool window (`Tools > Show Switch Flow Diagram`) that draws the `switch` under the caret as a decision tree, with Copy Mermaid / Copy DOT actions for sharing. The Visual mode colour-codes each arm by kind (constructor / wildcard / pattern binding / todo placeholder / nested switch) with a legend strip at the bottom; the Source mode highlights Mermaid `flowchart TD` keywords, arrows, quoted labels and `%%` comments using the editor scheme so the source view reads like code instead of plain text
- **Type impact preview** — Tool window (`Tools > Show Type Impact`) that lists every project-wide reference to the `type` declaration under the caret, classified as type-ref / constructor / pattern / field-access. The `[kind]` prefix on each row is rendered in a kind-specific bold colour so the reference flavour is scannable while sizing the blast radius of a type change
- **Notebook-style worksheet** — Cell-based `.resnb` editor where each cell holds ReScript code and its last evaluation output; runs cells through the existing REPL backend, persists output to JSON, and exports the whole notebook as Markdown. Cell code areas use `EditorTextField` bound to the ReScript file type, giving each cell full syntax highlighting and editor affordances. Cell borders, output backgrounds, and error highlights use `JBColor` pairs so both Light and Dark themes look right
- **JS interop risk map** — Tool window (`Tools > Show JS Interop Risk Map`) that lists every `%raw` / `external` / `Obj.magic` / `@bs.*` site in the project with a coarse risk label. Each row carries a 4 px left-edge colour band (red / amber / grey) keyed to the risk level so reviewers can sort the type-system escape hatches by severity without reading the text
- **Type coverage heat map** — Tool window (`View > Tool Windows > ReScript Type Coverage`) that lists every `.res` file with its annotated / inferred / total `let` counts and a colour-coded coverage percentage; sorted ascending so files in need of explicit types float to the top, double-click to jump
- **Parameter Info** — Native parameter info popup for labeled arguments (`Ctrl+P`)
- **Unresolved reference Quick Fix** — Add `open` or qualify unresolved references
- **Generate function from usage** — Create stub functions from call sites
- **Type hole Quick Fix** — Suggest candidate types for `_` type holes
- **Suggested refactoring** — Automatic detection and proposal of code quality improvements

### Navigation

- **Go to Symbol** — Stub-indexed quick symbol search (`Cmd+Option+O`)
- **Structure view** — Navigate module, function, and type declarations
- **Switch .res/.resi** — Toggle between implementation and interface files (`Alt+O`)
- **Go to Related** — Jump between `.res`, `.resi`, and compiled `.js` files
- **Go to Implementation** — Jump from `.resi` interface to `.res` implementation (`Ctrl+Alt+B`)
- **Search Everywhere** — Stub-indexed unified search for files, symbols, and actions (`Shift+Shift`)
- **Go to Test** — Navigate between implementation and test files, create test boilerplate (`Ctrl+Shift+T`)
- **Open Compiled JavaScript** — View compiled JS output (`Alt+Shift+J`)
- **Create Interface File** — Generate `.resi` from current `.res` file
- **Module hierarchy** — Visualize module dependency tree
- **Call Hierarchy** — View caller/callee relationships for functions (`Ctrl+Alt+H`)
- **Copy qualified name** — Copy fully-qualified module path (`Cmd+Shift+Alt+C`)
- **Context Info** — Sticky declaration header when scrolling through long files
- **File include navigation** — Navigate from `open` statements to their module files
- **Goto Super** — Jump from `.res` declaration to matching `.resi` declaration (`Ctrl+U`)
- **External documentation** — Open ReScript documentation for Belt/Js modules (`Shift+F1`)
- **Type signature search (Hoogle-style)** — Search Everywhere "ReScript Types" tab parses both the user query and each candidate's `: T` annotation into a structural AST and matches them with a unifier (EXACT / TVAR_MATCH / PARTIAL); supports `=> T` return-type mode and `'a` wildcards, ranks by match strength, and renders `name: signature  (path:line)` so the right hit is obvious before navigating. The signature itself is tokenised through `RescriptLexer` and coloured with the editor scheme, so keywords, type constructors, operators and type variables look exactly like they do inside an open `.res` file
- **Module dependency diagram** — Visualize `open`/`include` relationships in a tool window with a **Visual / Source toggle**. Visual mode renders a top-down layered graph (Java2D) where entry-point modules sit on top and downstream dependencies are pushed lower; nodes are colour-coded by structural role (entry-point / intermediate / leaf / cycle member) with a legend strip at the bottom so circular dependencies and entry points pop out. Source mode displays the Mermaid `flowchart TD` text with the same `MermaidSourceColorizer` highlighting used by Variant Flow. Both modes share Copy Mermaid / Copy DOT toolbar actions for external rendering

### Editing Assistance

- **Live Templates** — 21 code snippets with ReScript-aware context (`let`, `mod`, `sw`, `pipe`, `log`, `@module`, `@val`, `comp`, etc.)
- **Postfix Completion** — 9 postfix templates: `.switch`, `.pipe`, `.log`, `.some`, `.ok`, `.error`, `.ignore`, `.promise`, `.await`
- **Intention Actions** — Wrap with `Some`/`Ok`/`Error`, add `@genType`, generate doc comment, add `->ignore`, add `_` prefix, remove redundant braces, fix identifier case, pipe ⇔ function call conversion, interface publish/unpublish, insert labeled args, merge switch cases, case split, add missing switch arms, convert to labeled args, remove unnecessary parentheses, remove redundant qualifier, expand destructuring, filter+map to filterMap, add type annotation, convert call to uncurried form, extract local module to file
- **Surround With** — Wrap selection in `if`/`switch`/`try`/block, or in a JSX `<div>` element / `<>` fragment
- **Unwrap/Remove** — Remove wrapping constructs like `Some(...)`, `Ok(...)`, `if`, `switch`, `try`, `{ }` (`Ctrl+Shift+Delete`)
- **JSX auto-close** — Automatically insert closing tags when typing `>` in JSX
- **Generate actions** — Generate switch arms, module types, make functions, record values, JSON encoder/decoder, and module type implementation (`Cmd+N`)
- **Statement mover** — Move declarations up/down (`Alt+Shift+Up/Down`)
- **Smart Enter** — Insert new line with correct indentation (`Shift+Enter`)
- **Comment continuation** — Auto-continue `//` and `/** */` comments on Enter
- **Smart join lines** — Intelligent line joining for let bindings, pipe chains, and arrow functions
- **Extend/shrink selection** — Context-aware selection for strings, brackets, and comments
- **File templates** — Create Module, Interface, and React Component files
- **Paste as JSON.t** — Convert clipboard JSON to ReScript `JSON.t`
- **.d.ts binding generation** — Generate ReScript bindings from TypeScript definition files
- **Backspace handler** — Delete matching JSX tag pairs with backspace
- **Move element** — Swap comma-separated elements left/right (`Alt+Shift+Cmd+Left/Right`)
- **Code block selection** — Navigate to code block boundaries (`Ctrl+Shift+[` / `]`)
- **Split/Join list** — Toggle between single-line and multi-line comma-separated lists
- **Copy/Paste escaping** — Auto-escape special characters when pasting into string literals
- **Paste as JSX** — Convert HTML clipboard content to ReScript JSX syntax (excludes React JSX)
- **Paste as ReScript** — Convert JavaScript/TypeScript clipboard content to ReScript syntax (type annotations stripped, JSX patterns converted)
- **Strip trailing spaces** — Protect whitespace inside string literals during trailing-space removal

### Code Analysis & Inspections

- **Error Lens** — Inline diagnostic messages at end of editor lines (configurable severity) with structured type mismatch hints and diff highlighting
- **Signature sync inspection** — Detect `.res`/`.resi` signature mismatches
- **Import optimizer** — Auto-detect and remove unused/duplicate `open` statements
- **Dead code analysis** — Detect unused code via `reanalyze` integration with server mode acceleration (ReScript >= 12.1.0)
- **Duplicate open detection** — Warn on duplicate `open` statements
- **Empty module detection** — Warn on empty module declarations
- **Missing rescript.json** — Warn when configuration file is absent
- **Highlight usages** — Highlight related keywords (switch/if/try and matching control flow keywords)
- **Problem filter** — Suppress highlighting in `node_modules/`, `lib/bs/`, `lib/ocaml/`
- **Inspection suppressor** — Suppress inspections with `// noinspection` comments
- **Format check** — Highlight unformatted files with quick-fix to format (opt-in via Settings)
- **Mutability diagnostics** — Detect unnecessary `ref` bindings that are never reassigned
- **Style linting** — Detect redundant booleans, deprecated Belt.* usage, and boolean switch patterns

### Build, Run & Test

- **Run configuration** — Build ReScript projects from the IDE with gutter run icons
- **Run Anything** — Execute ReScript CLI commands from Run Anything dialog (`Ctrl+Ctrl`)
- **Expression type** — Show inferred type at caret position (`Ctrl+Shift+P`)
- **Completion confidence** — Suppress auto-popup in comments and string literals
- **Test runner** — Run tests with auto-detected jest/vitest (SMTRunner test tree)
- **Debugger integration** — Debug compiled JavaScript via Node.js (`Alt+Shift+D`)
- **Compiler status** — Real-time build status in the status bar
- **Console links** — Clickable `file:line` links in compiler output
- **Code formatting** — Format via `rescript format` CLI (`Cmd+Option+L`)
- **Scratch file** — Run ReScript scratch files for quick experimentation
- **REPL** — Interactive ReScript execution environment in a tool window
- **Worksheet mode** — Interactively evaluate entire `.resw` files with inline results
- **Build watch prompt** — Suggestion balloon at project open to start `rescript watch`

### Project & IDE Integration

- **LSP auto-install** — One-click installation of `@rescript/language-server` with auto-detected package manager
- **Monorepo support** — Detects ReScript packages in pnpm/npm/yarn workspaces even when `rescript.json` only lives under `packages/<name>/`. Honours `pnpm-workspace.yaml` / `package.json#workspaces`, falls back to a depth-limited scan, and accepts a manual `Project package roots` override in `Settings > Languages & Frameworks > ReScript`
- **Project Wizard** — Create new projects from 22 production-shaped templates (Basic, npm Library, CLI Tool, Vite+ + React, Next.js, Electron, Tauri, two React Native flavors, Hono REST/GraphQL/Inertia, Cloudflare Workers, AWS Lambda, Google Cloud Run, Monorepo, Full-Stack, res-x (HTMX on Bun), TanStack Start, Remix / React Router v7, Astro, Waku) with selectable package manager (npm / yarn / pnpm / bun). The validation library combo (zod / sury) is shown for the 18 original templates and hidden for the four React-framework templates that ship their own data layer. Each template includes "one step deeper" sample code, a Vitest smoke test, MIT `LICENSE`, `.nvmrc`, GitHub Actions CI, and Dependabot config. The Hono + Inertia template pairs Hono with `@inertiajs/react` v3 and the Vite+ unified toolchain (`vp dev` / `vp build` / `vp test` / `vp check`) for a server-driven SPA without a separate REST/GraphQL layer. The Tauri template ships a Vite+ React renderer plus a Rust `src-tauri/` skeleton with `greet` / `get_info` commands and writes the package-manager-specific `beforeDevCommand` / `beforeBuildCommand` into `tauri.conf.json`. See [docs/templates.md](docs/templates.md) for the full matrix.
- **Compiled JS preview** — Real-time preview of compiled JavaScript in a tool window
- **Project View nesting** — `.resi` interface files nested under corresponding `.res` files
- **rescript.json support** — Custom icon and JSON Schema for configuration files
- **`%raw()` JS injection** — JavaScript syntax highlighting inside `%raw()` blocks
- **`%re()` RegExp injection** — Regular expression highlighting and validation inside `%re()` blocks
- **Type Info tool window** — Always-visible panel showing inferred type at caret position; the type string is rendered through an `EditorTextField` bound to the ReScript file type so it picks up the editor scheme's syntax colours instead of staying a plain monospace label
- **Markdown code fence** — ReScript syntax highlighting in Markdown code blocks
- **Color settings** — Customizable highlighting colors (Settings > Editor > Color Scheme > ReScript)
- **Code style settings** — Indentation configuration for ReScript files
- **Code rearranger** — Automatic declaration ordering (open → type → exception → module → external → let)
- **Framework detector** — Automatic ReScript project detection via `rescript.json`
- **PPX annotation hints** — Inline descriptions of PPX attribute effects
- **PPX expansion view** — View PPX macro expansion results in a tool window; `@annotation` tokens are rendered in the editor scheme's annotation colour so they stand out against the surrounding English description
- **Comment code evaluation** — Evaluate code examples in documentation comments
- **TODO indexing** — Track TODO/FIXME comments in ReScript files
- **Editor notification** — Guidance banner when LSP server is not detected
- **File nesting** — Compiled JS files (`.res.js`/`.mjs`/`.cjs`, `.bs.js`/`.mjs`/`.cjs`) nested under `.res` in Project view with subdued gray color
- **Color preview** — Inline color swatches in gutter for hex/rgb/hsl color values
- **VCS Code Vision** — Author and last-change annotations on declarations
- **Project View decoration** — "has interface" suffix and rescript.json version display
- **Reader mode** — Auto-enable reader mode for `.res`/`.resi` files in `node_modules`
- **Package dependencies** — Tool window showing rescript.json dependency tree
- **Auto import options** — Configurable auto-import settings in `Settings > Editor > Auto Import`
- **Open statement index** — Fast module lookup via file-based indexing of `open` statements
- **Predefined code style** — "ReScript Standard" preset code style
- **Element descriptions** — Enhanced element descriptions in refactoring dialogs
- **Lookup char filter** — Smart character filtering for completion item selection
- **GitHub error reporter** — Automatic error reporting to GitHub Issues for unhandled exceptions
- **Editor floating toolbar** — Quick access to Format, Open Compiled JS, and Create Interface actions
- **Element signature provider** — Persistent fold states across IDE restarts
- **Index pattern builder** — Enhanced TODO/FIXME pattern search in ReScript comments
- **Grazie integration** — Grammar and spell checking in comments and strings via Grazie plugin
- **Injected language formatting** — Code formatting support for injected language fragments
- **Restart LSP action** — Restart the ReScript Language Server from the Tools menu
- **Dump LSP State** — Export Language Server diagnostic state via Tools menu
- **LSP initialization options** — Send signatureHelp, cache, inlayHints, and compileStatus settings to the LSP server

## Requirements

- IntelliJ IDEA 2025.3+ (or other JetBrains IDE with LSP support)
- Node.js installed and available in PATH
- `@rescript/language-server` installed:

```bash
# Local installation (recommended)
npm install @rescript/language-server

# Or global installation
npm install -g @rescript/language-server
```

## Architecture

The plugin combines a JFlex lexer for fast, accurate syntax highlighting with the official [ReScript Language Server](https://github.com/rescript-lang/rescript-vscode/tree/master/server) for semantic features (completion, diagnostics, navigation, hover). A lightweight parser provides PSI structure for code folding and structure view.

For the full architecture overview — layers, Extension Points, and class-level mapping — see [CLAUDE.md](CLAUDE.md) and [docs/architecture.md](docs/architecture.md).

## Contributing

Start with [CONTRIBUTING.md](CONTRIBUTING.md) for the English-language workflow summary, then browse [docs/good-first-issues.md](docs/good-first-issues.md) for contributor-friendly tasks.

Deeper references:

- [CLAUDE.md](CLAUDE.md) — build commands, architecture layers, development conventions
- [docs/](docs/) — permanent design documents (architecture, functional design, repository structure)
- [sphinx-docs/dev/](sphinx-docs/dev/) — developer guide (setup, building, testing, contributing)
- [.claude/rules/](.claude/rules/) — project rules (testing, Git conventions, documentation, release)

Quick reference:

```bash
./gradlew buildPlugin           # build the plugin
./gradlew runIde                # launch a sandbox IDE for manual testing
./gradlew test                  # run unit tests
./gradlew test -Pscope=fast     # tight loop: skip perf / integration / cli suites
./gradlew ktlintCheck           # verify Kotlin formatting
```

## License

MIT

### Third-Party Licenses

This plugin depends on the following external components:

- **[@rescript/language-server](https://github.com/rescript-lang/rescript-vscode)** — Licensed under [LGPL-3.0-or-later](https://github.com/rescript-lang/rescript-compiler/blob/master/LICENSE). Not bundled; users install it separately via npm.
- **rescript.json Schema** — Derived from the [rescript-compiler](https://github.com/rescript-lang/rescript-compiler) `build-schema.json`, licensed under LGPL-3.0-or-later.
- **IntelliJ Platform SDK** — Licensed under [Apache License 2.0](https://github.com/JetBrains/intellij-community/blob/master/LICENSE.txt). Used at compile time only; not bundled.
- **JFlex** — Licensed under [BSD-3-Clause](https://github.com/jflex-de/jflex/blob/master/LICENSE). Used as a build tool only; generated code inherits this project's MIT license.
