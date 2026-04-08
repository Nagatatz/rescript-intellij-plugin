---
myst:
  html_meta:
    "keywords": "imports, optimize, open statements, cleanup"
---

# Optimize Imports

{bdg-success}`Native`

## Goal

Clean up redundant and duplicate `open` statements in your ReScript files to keep imports tidy and reduce noise.

## Steps

### 1. Run Optimize Imports

With a `.res` file open, press {kbd}`Ctrl+Alt+O` ({kbd}`Cmd+Alt+O` on macOS), or use **Code > Optimize Imports** from the menu.

The plugin scans the file and automatically removes:

- **Duplicate opens** --- When the same module is opened more than once

::::{tab-set}
:::{tab-item} Before
```rescript
open Belt
open Belt.Array
open Belt  // duplicate

let arr = [1, 2, 3]
let doubled = arr->Array.map(x => x * 2)
```
:::
:::{tab-item} After
```rescript
open Belt
open Belt.Array

let arr = [1, 2, 3]
let doubled = arr->Array.map(x => x * 2)
```
:::
::::

### 2. Review inspection warnings

The **Duplicate Open Detection** inspection highlights redundant opens in real time as you edit. Look for yellow warning underlines on `open` statements.

Hover over the warning to see the details, or press {kbd}`Alt+Enter` for a quick fix to remove the duplicate.

### 3. Configure auto-optimization (optional)

To run import optimization automatically on save or reformat:

1. Open **Settings > Editor > Auto Import**
2. Under the **ReScript** section, configure the auto-import behavior
3. Click **Apply**

You can also run import optimization as part of **Code > Reformat Code** ({kbd}`Ctrl+Alt+L` / {kbd}`Cmd+Alt+L`) by checking the **Optimize imports** option in the reformat dialog.

### 4. Project-wide optimization

To optimize imports across the entire project:

1. Select the project root (or a directory) in the **Project** panel
2. Use **Code > Optimize Imports** ({kbd}`Ctrl+Alt+O` / {kbd}`Cmd+Alt+O`)
3. Review the preview of changes and click **Run**

## Expected Result

All `.res` files in the selected scope have clean, non-redundant `open` statements.

## Tips

- Combine import optimization with **Reformat Code** for a one-step cleanup workflow
- The inspection can be configured in **Settings > Editor > Inspections > ReScript > Duplicate open statement**
- Use `// noinspection RescriptDuplicateOpen` to suppress the warning for intentional duplicates

## Related Features

- [Code Analysis](../features/code-analysis.md) --- Duplicate open detection and other inspections
- [Find Dead Code](find-dead-code.md) --- Remove unused code alongside unused imports
- [Code Editing](../features/code-editing.md) --- Formatting and code style
