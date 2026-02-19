# Feature Overview

The ReScript IntelliJ Plugin provides 60+ features organized into two layers:

**Native features** are built into the plugin and work without any external dependencies. They provide instant, offline functionality.

**LSP features** require the [ReScript Language Server](https://github.com/rescript-lang/rescript-vscode) and provide semantic understanding of your code.

## Feature Categories

::::{grid} 1 1 2 2
:gutter: 3

:::{grid-item-card} Syntax Highlighting
:link: syntax-highlighting
:link-type: doc

JFlex lexer + LSP semantic tokens for accurate coloring.
:::

:::{grid-item-card} Code Completion
:link: code-completion
:link-type: doc

Intelligent completions, postfix templates, and live templates.
:::

:::{grid-item-card} Navigation
:link: navigation
:link-type: doc

Go to Definition, Symbol, Related files, and more.
:::

:::{grid-item-card} Code Editing
:link: code-editing
:link-type: doc

Folding, formatting, intentions, surround, and smart editing.
:::

:::{grid-item-card} Run & Build
:link: run-build
:link-type: doc

Run configurations, gutter icons, and build status.
:::

:::{grid-item-card} Testing
:link: testing
:link-type: doc

Jest/Vitest integration with test tree UI.
:::

:::{grid-item-card} Code Analysis
:link: code-analysis
:link-type: doc

Inspections, dead code analysis, and import optimization.
:::

:::{grid-item-card} Advanced Features
:link: advanced
:link-type: doc

Code Lens, Compiled JS Preview, Module Hierarchy, .d.ts Binding Generation, and more.
:::
::::

## Native vs. LSP Features

| Feature | Native | LSP | Details |
|---------|:------:|:---:|---------|
| Syntax highlighting | Yes | Yes (semantic overlay) | Native lexer handles keyword, string, operator, and punctuation coloring. LSP adds semantic precision for variables, types, namespaces, and variant constructors. |
| Code folding | Yes | | Collapses multi-line declarations, block comments, and region markers using the PSI tree. No semantic analysis needed. |
| Brace matching | Yes | | Matches `{}`, `[]`, `()`, and JSX tag brackets using token-level information from the lexer. |
| Comments toggle | Yes | | Inserts or removes `//` and `/* */` based on cursor position. Purely textual operation. |
| Structure view | Yes | | Displays top-level declarations (`let`, `type`, `module`, `external`, `open`, `include`, `exception`) from the lightweight parser. |
| Live templates | Yes | | Expands code snippets (e.g., `let`, `switch`, `@react.component`). Templates are defined within the plugin and need no external data. |
| Postfix templates | Yes | | Transforms expressions via suffixes (`.switch`, `.pipe`, `.log`, `.some`, `.ok`, `.error`, `.ignore`). Works by rewriting text around the cursor. |
| File switching (Alt+O) | Yes | | Toggles between `.res` and `.resi` by filename. No semantic understanding required. |
| Code completion | | Yes | Provides context-aware completions for identifiers, record fields, module members, and variant constructors using the type system. |
| Go to Definition | | Yes | Resolves the definition site of any symbol. Requires type analysis to follow module paths and aliases. |
| Hover documentation | | Yes | Displays inferred types and doc comments on mouse hover. Relies on the compiler's type information. |
| Find references | | Yes | Locates all usages of a symbol across the project. Requires cross-file semantic analysis. |
| Diagnostics | | Yes | Reports type errors, syntax errors, and warnings inline. Powered by the ReScript compiler via the language server. |
| Inlay hints | | Yes | Shows inferred types next to `let` bindings and function parameters. Requires type inference from the compiler. |
| Rename | | Yes | Renames a symbol and updates all references project-wide. Requires complete semantic understanding. |
| Code formatting | | Yes (via CLI) | Invokes `rescript format` through the language server to format the current file. |
| Signature help | | Yes | Displays parameter information when typing function arguments. Requires type analysis. |
| Code Lens | | Yes | Shows type annotations above functions via the CodeVision API. Requires type inference. |

### Understanding Native Features

Native features are powered entirely by the JFlex lexer and lightweight parser bundled within the plugin. They operate on the **syntactic structure** of ReScript code --- tokenizing keywords, matching brackets, recognizing top-level declarations --- without understanding types or semantics.

This means native features:

- Work instantly with zero startup delay, because there is no external process to launch.
- Function offline and in projects without `node_modules`, since they depend only on the plugin itself.
- Do not require `@rescript/language-server` or even Node.js to be installed.

Examples of purely syntactic operations include folding a `module` block (the parser sees the `module` keyword and matching braces), toggling a line comment (inserting `//` at the line start), and expanding a live template (replacing a text abbreviation with a predefined snippet).

### Understanding LSP Features

LSP features communicate with `@rescript/language-server` over a stdio connection. The language server runs the ReScript compiler internally, giving it access to:

- **Type inference** --- knowing that `x` is an `int` or that `user.name` is a `string`.
- **Cross-file resolution** --- following `open Belt` to find `Belt.Array.map`.
- **Error detection** --- reporting type mismatches, missing fields, and exhaustiveness warnings.

Because the language server performs full compilation, LSP features can provide rich, accurate information that would be impossible with syntax analysis alone. For example, Go to Definition can resolve a function imported through multiple module aliases, and code completion can suggest record fields that match the inferred type context.

### Graceful Degradation When LSP Is Disconnected

The plugin is designed so that the absence of the language server never prevents basic editing. When the LSP connection is unavailable --- for example, because `@rescript/language-server` is not installed, Node.js is missing, or the server process has crashed --- the following behavior applies:

**Features that continue to work normally:**

- Syntax highlighting (lexer-based coloring for keywords, strings, numbers, operators, punctuation, module names, JSX tags, and decorators)
- Code folding for all recognized declarations
- Brace matching and auto-closing brackets
- Line and block comment toggling
- Structure view with top-level declaration outline
- Live templates and postfix templates
- File switching between `.res` and `.resi`
- Smart Enter and declaration up/down movement
- Surround With templates

**Features that become unavailable:**

- Code completion shows no suggestions (the completion popup does not appear for ReScript files).
- Go to Definition, Find Usages, and Hover do nothing when invoked.
- No diagnostic errors or warnings are displayed in the editor gutter or Problems panel.
- Inlay hints and Code Lens annotations are not shown.
- Rename refactoring is not available.
- Code formatting via `Cmd+Option+L` does not modify the file.
- Signature help does not appear when typing function arguments.

**Visual indicator:** When the language server cannot be found, the plugin displays an editor notification bar at the top of ReScript files with a message explaining that LSP features are unavailable and providing instructions for installing `@rescript/language-server`.

**Recovery:** Once the language server is installed or its path is configured in **Settings > Languages & Frameworks > ReScript**, the LSP connection is established automatically when a `.res` or `.resi` file is opened. All LSP features become available without restarting the IDE.

```{toctree}
:hidden:

syntax-highlighting
code-completion
navigation
code-editing
run-build
testing
code-analysis
advanced
```
