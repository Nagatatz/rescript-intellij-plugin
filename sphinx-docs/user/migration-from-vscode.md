---
myst:
  html_meta:
    "keywords": "migration, vscode, visual studio code, shortcut mapping, feature comparison"
---

# Migration from VSCode

This guide helps ReScript developers transition from the VSCode ReScript extension to the JetBrains ReScript plugin. It covers keyboard shortcut mappings, feature comparisons, and setup differences.

## Keyboard Shortcut Mapping

The following table maps common actions between VSCode and JetBrains IDEs. Most shortcuts differ between the two platforms, but the underlying functionality is equivalent.

| Action | VSCode | JetBrains (macOS) | JetBrains (Win/Linux) |
|--------|--------|-------------------|----------------------|
| Go to Definition | F12 / Cmd+Click | Cmd+B / Cmd+Click | Ctrl+B / Ctrl+Click |
| Find References | Shift+F12 | Alt+F7 | Alt+F7 |
| Rename Symbol | F2 | Shift+F6 | Shift+F6 |
| Format Document | Shift+Alt+F | Cmd+Opt+L | Ctrl+Alt+L |
| Quick Fix / Code Action | Cmd+. | Alt+Enter | Alt+Enter |
| Command Palette | Cmd+Shift+P | Cmd+Shift+A | Ctrl+Shift+A |
| Go to Symbol (File) | Cmd+Shift+O | Cmd+F12 | Ctrl+F12 |
| Go to Symbol (Workspace) | Cmd+T | Cmd+Opt+O | Ctrl+Alt+O |
| Toggle Line Comment | Cmd+/ | Cmd+/ | Ctrl+/ |
| Toggle Block Comment | Shift+Alt+A | Cmd+Shift+/ | Ctrl+Shift+/ |
| Search Everywhere | Cmd+P (files) | Shift Shift | Shift Shift |
| Open Terminal | Ctrl+` | Alt+F12 | Alt+F12 |
| Trigger Completion | Ctrl+Space | Ctrl+Space | Ctrl+Space |
| Hover Documentation | Hover mouse | Hover / Ctrl+Q | Hover / Ctrl+Q |
| Surround With | -- | Cmd+Alt+T | Ctrl+Alt+T |
| Extract Variable | -- | Cmd+Alt+V | Ctrl+Alt+V |
| Extract Function | -- | Cmd+Alt+M | Ctrl+Alt+M |

```{tip}
JetBrains IDEs offer a **"VSCode" keymap plugin** that remaps most shortcuts to match VSCode defaults. Install it from **Settings > Plugins > Marketplace** and search for "VSCode Keymap". This can significantly ease the transition.
```

## Feature Comparison

The JetBrains ReScript plugin provides all the LSP-based features available in the VSCode extension, plus a wide range of native IDE integrations.

| Feature | VSCode Extension | JetBrains Plugin |
|---------|-----------------|-----------------|
| Syntax Highlighting | TextMate grammar | JFlex lexer + LSP semantic tokens (dual-layer) |
| Code Completion | LSP only | LSP + postfix templates + live templates |
| Diagnostics | LSP | LSP + Error Lens inline display |
| Code Formatting | LSP | LSP via `rescript format` CLI |
| Refactoring | LSP rename | Rename + Extract Variable/Function + Inline + Change Signature + React component extraction |
| Dead Code Analysis | Manual reanalyze CLI | Integrated inspection with reanalyze server mode |
| Test Runner | External terminal | Integrated test tree UI (Jest/Vitest auto-detect) |
| Code Generation | -- | Generate switch arms, module type, make function, JSON codecs |
| Surround With | -- | if/switch/try/block |
| Postfix Completion | -- | `.switch`, `.pipe`, `.log`, `.promise`, `.await`, etc. |
| Live Templates | -- | 21 snippets (`let`, `switch`, `@react.component`, FFI bindings) |
| Code Inspections | -- | Duplicate open, empty module, signature sync, style lint |
| Project Wizard | -- | 12 project templates |
| Module Hierarchy | -- | Visual module dependency tree |
| Call Hierarchy | -- | Caller/Callee tree |

## Setup Differences

### Language Server

- **VSCode**: The ReScript extension bundles the language server. No additional installation is required.
- **JetBrains**: Requires `@rescript/language-server` to be installed in your project's `node_modules`. When the language server is not detected, the plugin displays a notification bar with a **one-click install button** that automatically detects your package manager (npm/yarn/pnpm) and runs the installation in the background.

To install manually:

```bash
npm install -D @rescript/language-server
```

### Configuration

- **VSCode**: Settings are configured in `settings.json` (user or workspace level) under the `rescript.*` namespace.
- **JetBrains**: Settings are configured in **Settings > Languages & Frameworks > ReScript**. Options include incremental type checking, signature help, inlay hints, and compilation status display.

### Keymap

If you prefer to keep your VSCode muscle memory, install the **VSCode Keymap** plugin:

1. Go to **Settings > Plugins > Marketplace**
2. Search for **"VSCode Keymap"**
3. Click **Install** and restart the IDE

This remaps most common shortcuts to their VSCode equivalents.

## Features Only in JetBrains

The following features are available exclusively in the JetBrains ReScript plugin and have no equivalent in the VSCode extension:

**Code Editing**
- **Postfix completion** -- `.switch`, `.pipe`, `.log`, `.promise`, `.await`, and more
- **Live templates** -- 21 built-in snippets for common patterns (`let`, `switch`, `@react.component`, FFI bindings)
- **Surround with** -- Wrap selected code in `if`/`switch`/`try`/block (`Cmd+Alt+T`)
- **Smart Enter** -- Automatically completes brackets and braces on `Shift+Enter`
- **Statement mover** -- Move top-level declarations up/down with `Alt+Shift+Up/Down`

**Refactoring**
- **Extract Variable** (`Cmd+Alt+V`) -- Extract an expression into a `let` binding
- **Extract Function** (`Cmd+Alt+M`) -- Extract selected code into a new function
- **Inline Variable/Function** (`Cmd+Alt+N`) -- Inline a variable or function at its usage sites
- **Change Signature** (`Cmd+F6`) -- Modify function parameters with automatic call-site updates
- **React component extraction** -- Extract JSX into a new React component

**Code Generation** (`Cmd+N`)
- Generate switch arms from variant types
- Generate module type skeletons and implementations
- Generate `make` constructor functions from record types
- Generate JSON encoder/decoder functions
- Generate record values with default fields

**Code Inspections**
- Duplicate `open` detection
- Empty module detection
- `.resi` signature sync checking
- Style linting (redundant booleans, Belt API suggestions)
- **Reanalyze server mode** -- Persistent daemon for fast dead code analysis

**Test Runner**
- Integrated test tree UI with Jest and Vitest auto-detection
- Run/debug individual tests from gutter icons

**Project Wizard**
- Create new ReScript projects from 16 templates (Basic, Vite+React, Next.js, Electron, Hono, Cloudflare Workers, and more)

**Tool Windows**
- **Module hierarchy** -- Visual module dependency tree
- **Call hierarchy** -- Caller/Callee tree (`Ctrl+Alt+H`)
- **Dependency diagram** -- Graphical module dependency visualization
- **PPX expansion view** -- Inspect PPX macro output
- **Type info panel** -- Always-visible type display for the cursor position
- **REPL** -- Interactive ReScript evaluation environment
- **Package dependencies** -- View `rescript.json` dependency tree

**Additional Features**
- **.d.ts binding generation** -- Generate ReScript `external` bindings from TypeScript definitions
- **Worksheet mode** -- Evaluate `.resw` files interactively
- **Scratch files** -- Create and run temporary ReScript snippets
- **Type signature search** -- Find functions by their type signature (`Shift+Shift`)
- **Error Lens** -- Display diagnostics inline at the end of each line
- **JS/TS to ReScript paste conversion** -- Paste JavaScript/TypeScript code and convert it to ReScript automatically
