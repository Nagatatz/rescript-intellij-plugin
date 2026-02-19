# Quick Start

This guide walks you through your first experience with the ReScript IntelliJ Plugin after installation.

## Prerequisites

- Plugin installed ([Installation Guide](installation.md))
- A ReScript project with `rescript.json` (or `bsconfig.json`)
- `@rescript/language-server` installed in the project

## Open Your Project

1. **File** → **Open** → Select your ReScript project folder
2. The IDE will detect `rescript.json` and configure the project automatically
3. Open any `.res` file — you should see syntax highlighting immediately

## Try the Core Features

### Code Completion

Start typing in a `.res` file. The Language Server provides intelligent completions:

- Variable and function names
- Module names
- Type-aware suggestions

Press `Ctrl+Space` (or `Cmd+Space` on macOS) to trigger completion manually.

### Go to Definition

Hold `Ctrl` (or `Cmd` on macOS) and click on any symbol to jump to its definition. You can also press `Ctrl+B`.

### Hover Documentation

Hover over any symbol to see its type signature and documentation (if available).

### Diagnostics

Save your file or wait a moment — the Language Server will show errors and warnings inline with red/yellow underlines. View all issues in the **Problems** panel (`Alt+6`).

### Code Formatting

Press `Ctrl+Alt+L` (or `Cmd+Option+L` on macOS) to format the current file using `rescript format`.

### Structure View

Press `Cmd+7` (or `Alt+7`) to open the Structure panel and see an outline of your file's modules, functions, and types.

### Run ReScript Build

1. Click the green ▶ icon in the gutter next to your file
2. Or create a Run Configuration: **Run** → **Edit Configurations** → **+** → **ReScript**
3. Select a command (Build, Build Watch, Clean) and run

## What's Next?

- [Feature Overview](features/index.md) — Explore all available features
- [Keyboard Shortcuts](keyboard-shortcuts.md) — Learn the shortcuts
- [Configuration](configuration.md) — Customize the plugin settings
