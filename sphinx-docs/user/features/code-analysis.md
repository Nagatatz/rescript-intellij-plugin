# Code Analysis

The plugin provides several code analysis features to help you maintain clean, correct code.

## Real-Time Diagnostics

The Language Server provides real-time error and warning diagnostics as you type:

- **Errors** — Shown with red underlines
- **Warnings** — Shown with yellow underlines
- **Info** — Shown with subtle underlines

View all diagnostics in the **Problems** panel (`Alt+6`).

## Code Inspections

The plugin includes built-in inspections that run locally:

### Duplicate Open Detection

Detects when the same module is opened multiple times:

```rescript
open Belt
open Belt  // ← Warning: duplicate open
```

### Empty Module Detection

Warns about empty module declarations:

```rescript
module Empty = {}  // ← Warning: empty module
```

### Missing Configuration

Warns when `rescript.json` (or `bsconfig.json`) is not found in the project root.

## Dead Code Analysis (reanalyze)

The plugin integrates with [reanalyze](https://github.com/rescript-association/reanalyze) to detect:

- **Dead code** — Unused functions, values, and types
- **Dead exceptions** — Unused exception declarations
- **Unhandled exceptions** — Exception paths not covered by try/catch

### How It Works

1. reanalyze runs as an external annotator on file save
2. Results appear as editor annotations (warnings/info)
3. Quick fixes are available:
   - Add `_` prefix to mark as intentionally unused
   - Remove the `_` prefix when code is actually used

### Global Inspection

Run **Code** → **Inspect Code** to analyze the entire project for dead code. Results are grouped by category in the Inspection Results panel.

## Import Optimization

Press `Ctrl+Alt+O` to optimize imports:

- Removes duplicate `open` statements
- Keeps unique `open` statements in their original order

## Quick Fixes (LSP)

The Language Server provides automatic code fixes via `Alt+Enter`:

- Add missing imports
- Add type annotations
- Fix common type errors
