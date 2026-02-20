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

### Navigation

- **Go to Symbol** — Quick symbol search (`Cmd+Option+O`)
- **Structure view** — Navigate module, function, and type declarations
- **Switch .res/.resi** — Toggle between implementation and interface files (`Alt+O`)
- **Go to Related** — Jump between `.res`, `.resi`, and compiled `.js` files
- **Go to Test** — Navigate between implementation and test files, create test boilerplate (`Ctrl+Shift+T`)
- **Open Compiled JavaScript** — View compiled JS output (`Alt+Shift+J`)
- **Create Interface File** — Generate `.resi` from current `.res` file
- **Module hierarchy** — Visualize module dependency tree
- **Copy qualified name** — Copy fully-qualified module path (`Cmd+Shift+Alt+C`)
- **Context Info** — Sticky declaration header when scrolling through long files

### Editing Assistance

- **Live Templates** — 15 code snippets (`let`, `mod`, `sw`, `pipe`, `log`, etc.)
- **Postfix Completion** — `.switch`, `.pipe`, `.log` and more
- **Intention Actions** — Wrap with `Some`/`Ok`/`Error`, add `@genType`, generate doc comment
- **Surround With** — Wrap selection in `if`/`switch`/`try`/block
- **Unwrap/Remove** — Remove wrapping constructs like `Some(...)`, `Ok(...)`, `if`, `switch`, `try`, `{ }` (`Ctrl+Shift+Delete`)
- **JSX auto-close** — Automatically insert closing tags when typing `>` in JSX
- **Generate actions** — Generate switch arms and module types (`Cmd+N`)
- **Statement mover** — Move declarations up/down (`Alt+Shift+Up/Down`)
- **Smart Enter** — Insert new line with correct indentation (`Shift+Enter`)
- **File templates** — Create Module, Interface, and React Component files
- **Paste as JSON.t** — Convert clipboard JSON to ReScript `JSON.t`
- **.d.ts binding generation** — Generate ReScript bindings from TypeScript definition files

### Code Analysis & Inspections

- **Error Lens** — Inline diagnostic messages at end of editor lines (configurable severity)
- **Import optimizer** — Auto-detect and remove unused/duplicate `open` statements
- **Dead code analysis** — Detect unused code via `reanalyze` integration
- **Duplicate open detection** — Warn on duplicate `open` statements
- **Empty module detection** — Warn on empty module declarations
- **Missing rescript.json** — Warn when configuration file is absent

### Build, Run & Test

- **Run configuration** — Build ReScript projects from the IDE with gutter run icons
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
- **Markdown code fence** — ReScript syntax highlighting in Markdown code blocks
- **Color settings** — Customizable highlighting colors (Settings > Editor > Color Scheme > ReScript)
- **Code style settings** — Indentation configuration for ReScript files
- **TODO indexing** — Track TODO/FIXME comments in ReScript files
- **Editor notification** — Guidance banner when LSP server is not detected

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
