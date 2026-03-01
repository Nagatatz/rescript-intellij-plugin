# ReScript IntelliJ Plugin

[![CI](https://github.com/Nagatatz/rescript-intellij-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/Nagatatz/rescript-intellij-plugin/actions/workflows/ci.yml)

[ReScript](https://rescript-lang.org) language support for IntelliJ IDEA and other JetBrains IDEs.

**[Documentation](https://nagatatz.github.io/rescript-intellij-plugin/)**

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
- **Rename refactoring** — Safe project-wide renaming
- **Diagnostics** — Real-time error and warning display
- **Inlay hints** — Type annotations displayed inline
- **Signature help** — Parameter info for function calls
- **Code Vision** — Function type annotations via Code Lens
- **Quick Documentation** — PSI-based fallback documentation when LSP is unavailable (`Ctrl+Q`)
- **Usage type classification** — Categorized Find Usages results (open, type, pipe, JSX, etc.)
- **Extract Variable** — Extract selected expression into a `let` binding (`Ctrl+Alt+V`)
- **Safe Delete** — Usage-checking safe delete refactoring (`Refactor > Safe Delete`)
- **Name suggestions** — Type-based and filename-based name suggestions during rename
- **Completion Weigher** — Context-based prioritization of completion candidates
- **Pipe chain type hints** — Inline intermediate type display for `->` pipe chains
- **Parameter Info** — Native parameter info popup for labeled arguments (`Ctrl+P`)
- **Unresolved reference Quick Fix** — Add `open` or qualify unresolved references
- **Generate function from usage** — Create stub functions from call sites

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
- **Goto Super** — Jump from `.res` declaration to matching `.resi` declaration (`Ctrl+U`)
- **External documentation** — Open ReScript documentation for Belt/Js modules (`Shift+F1`)

### Editing Assistance

- **Live Templates** — 21 code snippets with ReScript-aware context (`let`, `mod`, `sw`, `pipe`, `log`, `@module`, `@val`, `comp`, etc.)
- **Postfix Completion** — `.switch`, `.pipe`, `.log`, `.promise`, `.await` and more
- **Intention Actions** — Wrap with `Some`/`Ok`/`Error`, add `@genType`, generate doc comment, pipe ⇔ function call conversion, interface publish/unpublish, insert labeled args, merge switch cases, case split, convert to labeled args, remove unnecessary parentheses, remove redundant qualifier, expand destructuring, filter+map to filterMap, add type annotation
- **Surround With** — Wrap selection in `if`/`switch`/`try`/block
- **Unwrap/Remove** — Remove wrapping constructs like `Some(...)`, `Ok(...)`, `if`, `switch`, `try`, `{ }` (`Ctrl+Shift+Delete`)
- **JSX auto-close** — Automatically insert closing tags when typing `>` in JSX
- **Generate actions** — Generate switch arms, module types, make functions, record values, and JSON encoder/decoder (`Cmd+N`)
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
- **Paste as JSX** — Convert HTML clipboard content to ReScript JSX syntax

### Code Analysis & Inspections

- **Error Lens** — Inline diagnostic messages at end of editor lines (configurable severity) with structured type mismatch hints and diff highlighting
- **Signature sync inspection** — Detect `.res`/`.resi` signature mismatches
- **Import optimizer** — Auto-detect and remove unused/duplicate `open` statements
- **Dead code analysis** — Detect unused code via `reanalyze` integration
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

### Project & IDE Integration

- **LSP auto-install** — One-click installation of `@rescript/language-server` with auto-detected package manager
- **Project Wizard** — Create new projects from 12 templates (Basic, Vite+React, Next.js, Electron, Hono, Cloudflare Workers, AWS Lambda, Google Cloud Run, React Native, npm Library, CLI Tool, Monorepo)
- **Compiled JS preview** — Real-time preview of compiled JavaScript in a tool window
- **Project View nesting** — `.resi` interface files nested under corresponding `.res` files
- **rescript.json support** — Custom icon and JSON Schema for configuration files
- **`%raw()` JS injection** — JavaScript syntax highlighting inside `%raw()` blocks
- **`%re()` RegExp injection** — Regular expression highlighting and validation inside `%re()` blocks
- **Type Info tool window** — Always-visible panel showing inferred type at caret position
- **Markdown code fence** — ReScript syntax highlighting in Markdown code blocks
- **Color settings** — Customizable highlighting colors (Settings > Editor > Color Scheme > ReScript)
- **Code style settings** — Indentation configuration for ReScript files
- **Code rearranger** — Automatic declaration ordering (open → type → exception → module → external → let)
- **Framework detector** — Automatic ReScript project detection via `rescript.json`
- **PPX annotation hints** — Inline descriptions of PPX attribute effects
- **TODO indexing** — Track TODO/FIXME comments in ReScript files
- **Editor notification** — Guidance banner when LSP server is not detected
- **File nesting** — Compiled `.res.js` files nested under `.res` in Project view with subdued gray color
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

This plugin uses a **hybrid approach**:

1. **Lexer-based syntax highlighting** — A JFlex lexer tokenizes ReScript source code for fast, accurate syntax coloring without depending on external tools.

2. **LSP integration** — All semantic features (completion, diagnostics, navigation, hover, etc.) are provided by the [ReScript Language Server](https://github.com/rescript-lang/rescript-vscode/tree/master/server) via the IntelliJ Platform's built-in LSP API. This ensures feature parity with the official VSCode extension.

3. **Lightweight parser** — A minimal parser provides PSI structure for IDE features like code folding and structure view. It recognizes top-level declarations and JSX elements without attempting to fully parse ReScript's complex expression syntax.

## Development

### Prerequisites

- JDK 21+
- IntelliJ IDEA (for development)

### Build

```bash
./gradlew buildPlugin
```

### Run (development instance)

```bash
./gradlew runIde
```

### Generate Lexer

The JFlex lexer is automatically generated from `Rescript.flex` during the build process via the `generateRescriptLexer` Gradle task (dependency of `compileJava` / `compileKotlin`). Manual generation is not required.

## License

MIT
