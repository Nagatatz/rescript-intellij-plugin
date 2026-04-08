---
myst:
  html_meta:
    "keywords": "cheat sheet, quick reference, shortcuts, overview"
---

# Quick Reference Card

A condensed overview of the most important shortcuts and features. Print this page for a handy desk reference.

:::{note}
Shortcuts are shown as **macOS / Windows-Linux**. On Windows/Linux, replace `Cmd` with `Ctrl` and `Option` with `Alt`.
:::

::::{grid} 1 1 2 3
:gutter: 2

:::{grid-item-card} Navigation
:class-header: sd-bg-primary sd-bg-text-primary

| Shortcut | Action |
|----------|--------|
| `Cmd+B` / `Ctrl+B` | Go to Definition |
| `Alt+F7` | Find Usages |
| `Cmd+Opt+O` / `Ctrl+Alt+O` | Go to Symbol |
| `Shift` `Shift` | Search Everywhere |
| `Alt+O` | Switch .res / .resi |
| `Cmd+Alt+B` / `Ctrl+Alt+B` | Go to Implementation |
| `Cmd+U` / `Ctrl+U` | Goto Super (.res -> .resi) |
| `Cmd+Shift+T` / `Ctrl+Shift+T` | Go to Test |
| `Cmd+Alt+H` / `Ctrl+Alt+H` | Call Hierarchy |
| `Shift+F1` | External Documentation |
:::

:::{grid-item-card} Editing
:class-header: sd-bg-success sd-bg-text-success

| Shortcut | Action |
|----------|--------|
| `Cmd+Opt+L` / `Ctrl+Alt+L` | Format Code |
| `Alt+Enter` | Intentions / Quick Fix |
| `Ctrl+Alt+T` | Surround With |
| `Cmd+/` / `Ctrl+/` | Toggle Comment |
| `Ctrl+Shift+Delete` | Unwrap / Remove |
| `Alt+Shift+Up/Down` | Move Statement |
| `Shift+Enter` | Smart Enter |
| `Cmd+Alt+O` / `Ctrl+Alt+O` | Optimize Imports |
:::

:::{grid-item-card} Refactoring
:class-header: sd-bg-warning sd-bg-text-warning

| Shortcut | Action |
|----------|--------|
| `Shift+F6` | Rename |
| `Cmd+Alt+V` / `Ctrl+Alt+V` | Extract Variable |
| `Cmd+Alt+M` / `Ctrl+Alt+M` | Extract Function |
| `Cmd+Alt+N` / `Ctrl+Alt+N` | Inline |
| `Cmd+F6` / `Ctrl+F6` | Change Signature |
:::

:::{grid-item-card} Completion
:class-header: sd-bg-info sd-bg-text-info

| Input | Result |
|-------|--------|
| `Cmd+Space` / `Ctrl+Space` | Trigger Completion |
| `expr.switch` + `Tab` | switch expression |
| `expr.pipe` + `Tab` | pipe chain (`->`) |
| `expr.log` + `Tab` | `Console.log(expr)` |
| `expr.some` + `Tab` | `Some(expr)` |
| `let` + `Tab` | let binding |
| `sw` + `Tab` | switch block |
| `comp` + `Tab` | React component |
:::

:::{grid-item-card} Run & Build
:class-header: sd-bg-secondary sd-bg-text-secondary

| Shortcut | Action |
|----------|--------|
| `Ctrl` `Ctrl` | Run Anything |
| Gutter **&#9654;** icon | Run file |
| `Alt+6` | Problems panel |
| `Cmd+7` / `Alt+7` | Structure View |
:::

:::{grid-item-card} Code Generation (`Cmd+N`)
:class-header: sd-bg-dark sd-bg-text-dark

| Generator | Description |
|-----------|-------------|
| Switch Arms | Generate switch cases |
| Module Type | Generate module type |
| Make Function | Record constructor |
| Record Value | Record with defaults |
| JSON Encoder | `JSON.Encode` functions |
| JSON Decoder | `JSON.Decode` functions |
:::

::::
