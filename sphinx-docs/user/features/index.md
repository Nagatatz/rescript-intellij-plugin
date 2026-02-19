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

Code Lens, Compiled JS Preview, Module Hierarchy, and more.
:::
::::

## Native vs. LSP Features

| Feature | Native | LSP |
|---------|:------:|:---:|
| Syntax highlighting | ✓ | ✓ (semantic overlay) |
| Code folding | ✓ | |
| Brace matching | ✓ | |
| Comments toggle | ✓ | |
| Structure view | ✓ | |
| Code completion | | ✓ |
| Go to Definition | | ✓ |
| Hover documentation | | ✓ |
| Find references | | ✓ |
| Diagnostics | | ✓ |
| Inlay hints | | ✓ |
| Rename | | ✓ |
| Code formatting | | ✓ (via CLI) |

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
