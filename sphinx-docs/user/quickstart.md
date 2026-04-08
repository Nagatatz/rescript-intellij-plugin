---
myst:
  html_meta:
    "keywords": "quick start, tutorial, getting started, first steps"
---

# Quick Start

This hands-on guide walks you through your first experience with the ReScript IntelliJ Plugin. Follow each step and verify the expected result before moving on.

## Prerequisites

- Plugin installed ([Installation Guide](installation.md))
- A ReScript project with `rescript.json` (or `bsconfig.json`)
- `@rescript/language-server` installed in the project (`npm install -D @rescript/language-server`)

---

## Step 1: Open Your Project

1. **File** → **Open** → Select your ReScript project folder (the directory containing `rescript.json`)
2. Wait a few seconds for the IDE to index the project

:::{dropdown} Expected Result
:icon: check-circle

- The Project tree shows your `src/` folder and `rescript.json`
- A ReScript icon appears next to `.res` and `.resi` files in the project tree
- The status bar at the bottom may show "ReScript" with a build status indicator
:::

:::{dropdown} Troubleshooting
:icon: alert

- **"Cannot find rescript.json"**: Make sure you opened the project root directory, not a subdirectory
- **No ReScript icons on files**: The plugin may not be installed correctly. Check **Settings** → **Plugins** → search for "ReScript"
- **Monorepo setup**: If `rescript.json` is in a subdirectory, open that subdirectory as the project root, or ensure the language server is hoisted to the workspace root
:::

---

## Step 2: Verify Syntax Highlighting

Open any `.res` file from the project tree.

:::{dropdown} Expected Result
:icon: check-circle

- Keywords (`let`, `type`, `module`, `switch`) appear in a distinct color
- Strings are colored differently from code
- Comments (`//` and `/* */`) appear in a muted color
- If the Language Server is running, you may see additional semantic coloring (e.g., module names, variant constructors)
:::

:::{dropdown} Troubleshooting
:icon: alert

- **All text is plain black/white**: The file may not be recognized as ReScript. Check the file extension is `.res` or `.resi`
- **Partial highlighting only**: This is normal during the first few seconds. Layer 1 (lexer) highlighting is instant; Layer 2 (semantic) highlighting appears once the Language Server starts
:::

---

## Step 3: Try Code Completion

Place your cursor inside a function body and start typing. For example, type `Js.log` or `Console.`.

Press `Ctrl+Space` (`Cmd+Space` on macOS) to trigger completion manually.

:::{dropdown} Expected Result
:icon: check-circle

- A popup appears with context-aware suggestions (functions, modules, variables)
- Each suggestion shows its type signature
- You can also try postfix completion: type a value followed by `.switch` or `.log` and press `Tab`
:::

:::{dropdown} Troubleshooting
:icon: alert

- **No completions appear**: The Language Server may not be running. Check the status bar for "ReScript" indicator
- **"Language server not found" notification**: Install the language server with `npm install -D @rescript/language-server`, then restart the IDE
- **Only keywords appear**: LSP-based completion requires a running Language Server. Native postfix/live templates work without it
:::

---

## Step 4: Navigate with Go to Definition

Hold `Ctrl` (`Cmd` on macOS) and click on any symbol — a function name, module name, or type. You can also press `Ctrl+B` (`Cmd+B`).

:::{dropdown} Expected Result
:icon: check-circle

- The editor jumps to the symbol's definition, even if it's in another file
- You can also try `Alt+F7` to find all usages of a symbol
- Press `Alt+O` to switch between `.res` and `.resi` files
:::

:::{dropdown} Troubleshooting
:icon: alert

- **Nothing happens on Ctrl+Click**: The Language Server needs to be running for Go to Definition. Check the status bar
- **"Cannot find declaration"**: The symbol may be from an external library. Ensure dependencies are installed (`npm install`)
:::

---

## Step 5: See Diagnostics in Action

Intentionally introduce a type error — for example, change a `string` value to an `int` where a `string` is expected. Save the file or wait a moment.

:::{dropdown} Expected Result
:icon: check-circle

- Red wavy underlines appear on the erroneous code
- Hovering over the underline shows the error message with type details
- The **Problems** panel (`Alt+6`) lists all errors and warnings
- If Error Lens is enabled, the error message appears inline at the end of the line
:::

:::{dropdown} Troubleshooting
:icon: alert

- **No underlines appear**: The ReScript build watcher may not be running. Start it with **Run** → **Edit Configurations** → **ReScript** → **Build Watch**
- **Errors disappear immediately**: This is expected after fixing the code. The Language Server updates diagnostics in real time
:::

---

## Step 6: Format Your Code

Press `Ctrl+Alt+L` (`Cmd+Option+L` on macOS) to format the current file.

:::{dropdown} Expected Result
:icon: check-circle

- The file is reformatted according to ReScript's standard style using `rescript format`
- Indentation, spacing, and line breaks are adjusted automatically
:::

:::{dropdown} Troubleshooting
:icon: alert

- **"Cannot find rescript binary"**: The `rescript` CLI must be available in the project's `node_modules/.bin/`. Run `npm install` to install dependencies
- **No visible changes**: Your code may already be correctly formatted
:::

---

## Step 7: Run a Build

There are several ways to run a ReScript build:

1. **Gutter icon**: Click the green ▶ icon in the gutter next to your file
2. **Run Configuration**: **Run** → **Edit Configurations** → **+** → **ReScript** → Select "Build" or "Build Watch"
3. **Run Anything**: Press `Ctrl` twice (double-tap `Ctrl`) and type `rescript build`

:::{dropdown} Expected Result
:icon: check-circle

- The Run tool window opens at the bottom showing the build output
- The status bar shows the compilation result (success/error/warning count)
- Build errors appear as clickable links in the console output
:::

:::{dropdown} Troubleshooting
:icon: alert

- **No ReScript option in Run Configurations**: Make sure `rescript.json` exists in the project root
- **Build fails with "rescript not found"**: Run `npm install` to install the ReScript compiler
:::

---

## Step 8: Explore Further

You've covered the core workflow. Here are more features to explore:

::::{grid} 1 1 2 2
:gutter: 3

:::{grid-item-card} Structure View
Press `Cmd+7` / `Alt+7` to see an outline of your file's modules, functions, and types.
:::

:::{grid-item-card} Live Templates
Type `let` + `Tab`, `sw` + `Tab`, or `comp` + `Tab` to expand code snippets.
:::

:::{grid-item-card} Intentions
Press `Alt+Enter` on any expression for quick actions: wrap with `Some`, add `@genType`, and more.
:::

:::{grid-item-card} Find Usages
Press `Alt+F7` on a symbol to see all places it's used across the project.
:::
::::

## What's Next?

- [Feature Overview](features/index.md) — Explore all 90+ features organized by category
- [Quick Reference Card](cheatsheet.md) — A condensed cheat sheet of shortcuts and features
- [Keyboard Shortcuts](keyboard-shortcuts.md) — Complete shortcut reference
- [Configuration](configuration.md) — Customize the plugin settings
- [Recipes](recipes/index.md) — Task-oriented guides for common workflows
