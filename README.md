# ReScript IntelliJ Plugin

[ReScript](https://rescript-lang.org) language support for IntelliJ IDEA and other JetBrains IDEs.

## Features

- **Syntax highlighting** — Full lexer-based highlighting for ReScript (.res) and interface (.resi) files
- **Code completion** — Via Language Server Protocol (LSP)
- **Go to definition** — Via LSP
- **Hover documentation** — Via LSP
- **Find references** — Via LSP
- **Diagnostics** — Real-time error and warning display via LSP
- **Inlay hints** — Type annotations via LSP
- **Code folding** — Collapse blocks and comments
- **Brace matching** — Automatic matching for `{}`, `[]`, `()`
- **Comments** — Toggle line (`//`) and block (`/* */`) comments

## Requirements

- IntelliJ IDEA Ultimate 2024.2+ (or other JetBrains IDE with LSP support)
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

3. **Lightweight parser** — A minimal parser provides PSI structure for IDE features like code folding and structure view, without attempting to fully parse ReScript's complex syntax (including JSX).

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

The JFlex lexer must be generated from `Rescript.flex`. You can generate it using the [Grammar-Kit](https://plugins.jetbrains.com/plugin/6606-grammar-kit) plugin in IntelliJ IDEA:

1. Install Grammar-Kit plugin
2. Open `src/main/java/com/rescript/plugin/lang/Rescript.flex`
3. Run "Generate JFlex Lexer" (Ctrl+Shift+G)

## License

MIT
