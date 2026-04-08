---
myst:
  html_meta:
    "keywords": "dead code, reanalyze, unused, cleanup"
---

# Find Dead Code

{bdg-success}`Native` {bdg-warning}`Configuration Required`

## Goal

Detect unused functions, values, types, and modules in your ReScript project using the integrated reanalyze tool, and remove or suppress them.

## Steps

### 1. Enable reanalyze in Settings

Open **Settings > Languages & Frameworks > ReScript** and enable the **Dead Code Analysis** option.

The plugin uses `reanalyze` (bundled with `rescript-tools`) to perform the analysis. No additional installation is required if you have ReScript in your project.

:::{note}
For ReScript >= 12.1.0, the plugin automatically uses **server mode**, which keeps the reanalyze process running as a daemon for significantly faster incremental analysis. The server starts automatically when you open a project and restarts if it becomes unresponsive.
:::

### 2. Open a `.res` file

Once reanalyze is enabled, open any `.res` file in your project. The analysis runs as an external annotator --- results appear after a short delay.

### 3. Look for dead code annotations

Unused items are marked with grey text and a warning annotation:

- **Unused values** --- `let` bindings that are never referenced
- **Unused types** --- Type definitions with no usage
- **Unused modules** --- Modules that are never opened or accessed
- **Redundant optionals** --- Optional parameters that are always (or never) provided

### 4. Apply quick fixes

Place the cursor on a dead code warning and press {kbd}`Alt+Enter` to see available quick fixes:

- **Remove unused binding** --- Deletes the unused `let` declaration
- **Add underscore prefix** --- Renames the binding with a `_` prefix to mark it as intentionally unused (e.g., `let _unusedHelper = ...`)

## Expected Result

Your project is free of unused code, or intentionally unused items are explicitly marked with the `_` prefix convention.

## Tips

- Run dead code analysis periodically, especially after refactoring sessions
- Use the **Problems** panel ({kbd}`Alt+6`) to see all dead code warnings across the project at once
- The `// noinspection` comment can suppress specific warnings if needed

## Related Features

- [Dead Code Analysis](../features/code-analysis.md) --- Detailed reanalyze integration documentation
- [Code Inspections](../features/code-analysis.md) --- Other built-in inspections
- [Optimize Imports](optimize-imports.md) --- Clean up unused `open` statements
