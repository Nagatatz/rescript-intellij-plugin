# ReScript IntelliJ Plugin

[![CI](https://github.com/Nagatatz/rescript-intellij-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/Nagatatz/rescript-intellij-plugin/actions/workflows/ci.yml)

[ReScript](https://rescript-lang.org) language support for IntelliJ IDEA and other JetBrains IDEs.

## Features

- **Syntax highlighting** — Full lexer-based highlighting for ReScript (.res) and interface (.resi) files
- **Code completion** — Via Language Server Protocol (LSP)
- **Go to definition** — Via LSP
- **Hover documentation** — Via LSP
- **Find references** — Via LSP
- **Diagnostics** — Real-time error and warning display via LSP
- **Error Lens** — Inline diagnostic messages at the end of editor lines (configurable severity threshold)
- **Inlay hints** — Type annotations via LSP
- **Code folding** — Collapse blocks, comments, and multi-line JSX elements
- **Brace matching** — Automatic matching for `{}`, `[]`, `()`
- **Comments** — Toggle line (`//`) and block (`/* */`) comments
- **Structure view** — Navigate module, function, and type declarations
- **LSP auto-install** — One-click installation of `@rescript/language-server` with auto-detected package manager
- **Run configuration** — Build ReScript projects from the IDE
- **Debugger integration** — Debug compiled JavaScript via Node.js (Ultimate/WebStorm)
- **Unused open removal** — Auto-detect and remove unused `open` statements via LSP diagnostics
- **Semantic highlighting** — Enhanced token coloring via LSP semantic tokens
- **Code style** — Indentation settings for ReScript files
- **rescript.json icon** — Custom icon for ReScript configuration files

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
