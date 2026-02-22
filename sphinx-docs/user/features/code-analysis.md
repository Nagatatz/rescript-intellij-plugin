# Code Analysis

The plugin provides several code analysis features to help you maintain clean, correct code.

## Real-Time Diagnostics

The Language Server provides real-time error and warning diagnostics as you type:

- **Errors** --- Shown with red underlines
- **Warnings** --- Shown with yellow underlines
- **Info** --- Shown with subtle underlines

View all diagnostics in the **Problems** panel (`Alt+6`).

## Code Inspections

The plugin includes built-in inspections that run locally without requiring the Language Server.

### Duplicate Open Detection

Detects when the same module is opened multiple times in the same file. Duplicate `open` statements are redundant and add unnecessary noise to the code.

**Before** (duplicate detected):

```rescript
open Belt
open Belt.Array
open Belt  // Warning: duplicate open statement

let arr = [1, 2, 3]
let doubled = arr->Array.map(x => x * 2)
```

**After** (optimized):

```rescript
open Belt
open Belt.Array

let arr = [1, 2, 3]
let doubled = arr->Array.map(x => x * 2)
```

The inspection highlights the duplicate `open` statement with a warning. You can remove it manually or use **Optimize Imports** (`Ctrl+Alt+O`) to remove all duplicates automatically.

### Empty Module Detection

Warns about module declarations that have no content. Empty modules are typically leftover scaffolding or incomplete implementations.

```rescript
module Utils = {
  // Warning: empty module body
}

module Helpers = {
  let format = (s) => s->String.trim
}
```

The warning appears on the empty module declaration, prompting you to either add content or remove the empty module.

### Missing Configuration

When no `rescript.json` (or legacy `bsconfig.json`) is found in the project root, the plugin displays an editor notification bar at the top of ReScript files. This configuration file is essential for:

- **ReScript compiler** --- Without it, the project cannot be built. The compiler uses this file to locate source directories, dependencies, and build settings.
- **Language Server** --- The LSP server relies on `rescript.json` to understand the project structure. Without it, features like auto-completion, go-to-definition, and diagnostics will not function.
- **Plugin features** --- Several plugin features (Code Lens, semantic highlighting, inlay hints) depend on the Language Server, which in turn requires the configuration file.

If you see this warning, create a `rescript.json` file in your project root. The easiest way is to use the Project Wizard (**File** > **New** > **Project** > **ReScript**) or initialize a project with `npm init rescript-app@latest`.

## Dead Code Analysis (reanalyze)

The plugin integrates with [reanalyze](https://github.com/rescript-association/reanalyze), a static analysis tool built into `rescript-tools`, to detect unused and dead code in your project.

### What It Detects

- **Dead code** --- Unused functions, values, types, and modules that are never referenced
- **Dead exceptions** --- Exception declarations that are never raised or caught
- **Unhandled exceptions** --- Exception paths not covered by try/catch blocks

### Code Example

Consider a file with an unused helper function:

```rescript
let formatName = (first, last) => `${first} ${last}`

let greet = (name) => `Hello, ${name}!`

// Only greet is used elsewhere in the project.
// formatName is never called from any other module.
```

The reanalyze tool detects that `formatName` is dead code and displays a warning annotation on the function definition: **"formatName is dead code"**.

### Quick Fixes

When reanalyze identifies unused or dead code, the plugin offers quick fixes via `Alt+Enter`:

**1. Prefix with underscore** --- Marks the identifier as intentionally unused. This is useful when you want to keep the code for documentation or future use:

```rescript
// Before: warning on unused function
let formatName = (first, last) => `${first} ${last}`

// After: applying "Prefix with underscore" quick fix
let _formatName = (first, last) => `${first} ${last}`
```

**2. Remove unused code** --- Deletes the entire declaration line. Use this when the code is truly no longer needed:

```rescript
// Before
let formatName = (first, last) => `${first} ${last}`
let greet = (name) => `Hello, ${name}!`

// After: applying "Remove unused code" quick fix on formatName
let greet = (name) => `Hello, ${name}!`
```

The available quick fixes depend on the diagnostic type:
- **Unused variables/arguments/values/types/modules** --- Both "Prefix with underscore" and "Remove unused code" are offered
- **Dead code/modules/types/values/exceptions** --- Only "Remove unused code" is offered

### Setup Requirements

The reanalyze integration requires:

1. **ReScript compiler installed** --- The `rescript` package must be in your project's `node_modules`. The plugin looks for `rescript-tools` (or `rescript-tools.exe` on Windows) in `node_modules/rescript/`.

2. **Project must be built** --- reanalyze operates on the compiled output. Run `rescript build` at least once before expecting dead code analysis results.

3. **Optional: reanalyze configuration in rescript.json** --- You can fine-tune the analysis by adding a `reanalyze` section to your `rescript.json`:

```json
{
  "name": "my-project",
  "sources": [{"dir": "src", "subdirs": true}],
  "reanalyze": {
    "analysis": ["dce", "exception"],
    "suppress": ["src/bindings"],
    "transitive": false
  }
}
```

Configuration options:
- `analysis` --- Array of analysis types to enable: `"dce"` (dead code elimination), `"exception"` (exception analysis), `"termination"` (infinite loop detection)
- `suppress` --- Array of directory paths to exclude from analysis (useful for FFI bindings)
- `unsuppress` --- Specific files within suppressed directories to re-include
- `transitive` --- Whether to report transitively dead items (default: `false`)

### How It Works

The reanalyze annotator follows a three-phase lifecycle:

1. **Collect** --- On the EDT (Event Dispatch Thread), gathers the file path and project base path
2. **Annotate** --- In a background thread, runs `rescript-tools reanalyze -json` and parses the JSON output
3. **Apply** --- Maps diagnostics back to editor positions and creates warning annotations with quick-fix actions

### Global Inspection

In addition to per-file annotations in the editor, you can analyze the entire project at once:

1. Go to **Code** > **Inspect Code**
2. Select the inspection scope (whole project or specific directories)
3. Results appear in the **Inspection Results** panel, grouped by file

The global inspection runs a single invocation of `rescript-tools reanalyze -json` and distributes the results across all affected ReScript files in the project.

### Signature Sync Inspection

Detects mismatches between `.res` implementation files and their `.resi` interface files. When a declaration in `.res` is added, removed, or has a different signature than what's declared in `.resi`, the inspection highlights the discrepancy.

**Example** (mismatch detected):

```rescript
// In Foo.resi:
let greet: string => string

// In Foo.res:
let greet = (name: string, greeting: string) => `${greeting}, ${name}!`
// Warning: signature mismatch with .resi
```

The inspection helps catch situations where the implementation has diverged from the interface, which would cause compilation errors.

## Error Lens

Error Lens displays diagnostic messages (errors, warnings, info) as inline annotations at the end of the affected line, providing immediate visibility without needing to hover or check the Problems panel.

```
let x = "hello" + 1    ← This expression has type int but expected string
```

When multiple diagnostics appear on the same line, only the highest-severity message is shown with a "(+N more)" suffix.

### Severity Colors

Inline annotations are colored by severity:
- **Errors** --- Red
- **Warnings** --- Yellow/amber
- **Info/Hints** --- Gray

### Configuration

Error Lens can be configured in **Settings** > **Languages & Frameworks** > **ReScript**:

- **Enable/Disable** --- Toggle Error Lens on or off
- **Minimum severity** --- Set the minimum severity level to display (e.g., show only errors, or errors and warnings)

Error Lens updates automatically whenever the IDE's code analysis pass completes, so annotations stay in sync with the latest diagnostics.

### Type Mismatch Inline Hints

When a type error involves a mismatch between expected and actual types, Error Lens displays a structured inline hint showing both types side by side:

```
let x: string = 42    ← Expected: string, Actual: int
```

This structured display makes it easier to understand type errors at a glance without needing to open the Problems panel or hover over the error.

## Format Check

The plugin can check whether your ReScript files are formatted according to `rescript format` and highlight unformatted files with a warning.

### How It Works

When enabled, the plugin runs `rescript format --stdin` in the background and compares the output with the current editor content. If they differ, a file-level annotation appears indicating that the code is not formatted.

### Quick Fix

Press `Alt+Enter` on the annotation and select **Format this file** to reformat the code immediately. Alternatively, use the standard format shortcut (`Cmd+Option+L` on macOS or `Ctrl+Alt+L` on Windows/Linux).

### Configuration

The format check is **disabled by default**. To enable it:

1. Go to **Settings** > **Languages & Frameworks** > **ReScript**
2. Check **Enable format check (highlight unformatted code)**

### Requirements

- The `rescript` CLI must be installed in the project's `node_modules`. The plugin auto-detects it from `node_modules/.bin/rescript`.

## Import Optimization

Press `Ctrl+Alt+O` (or `Cmd+Alt+O` on macOS) to optimize imports in the current file.

The import optimizer performs the following actions:

- **Removes duplicate `open` statements** --- When the same module path appears in multiple `open` statements, only the first occurrence is kept
- **Preserves order** --- Unique `open` statements remain in their original position in the file
- **Preserves non-duplicate opens** --- The optimizer only removes exact duplicates; it does not remove unused opens (since that requires semantic analysis from the compiler)

**Example:**

```rescript
// Before optimization
open Belt
open Belt.Array
open Belt
open Belt.Option
open Belt.Array

let x = [1, 2, 3]->Array.map(v => Some(v))
```

```rescript
// After Ctrl+Alt+O
open Belt
open Belt.Array
open Belt.Option

let x = [1, 2, 3]->Array.map(v => Some(v))
```

After running the optimizer, a notification displays the result (e.g., "Removed 2 duplicate open statement(s)" or "No duplicate open statements found").

## Quick Fixes (LSP)

The Language Server provides automatic code fixes via `Alt+Enter` (or the light bulb icon in the gutter). These LSP-powered quick fixes operate on semantic analysis from the ReScript compiler and can handle situations that local inspections cannot.

Available quick fixes include:

- **Add missing type annotation** --- When the compiler suggests adding a type constraint to resolve ambiguity
- **Add missing open** --- Inserts an `open` statement for a module that contains the referenced value
- **Fix type mismatch** --- Suggests corrections when types do not match (e.g., wrapping in `Some()` for option types)
- **Insert missing pattern** --- Adds unhandled cases to incomplete pattern matches
- **Convert deprecated syntax** --- Updates old ReScript syntax to the current version

Quick fixes appear contextually based on the compiler diagnostic at the cursor position. Press `Alt+Enter` to see all available actions, or click the light bulb icon that appears in the editor gutter.

### Unresolved Reference Quick Fix

When an identifier cannot be resolved, the plugin offers quick fixes to resolve it:

- **Add `open` statement** — Insert an `open` statement for the module containing the referenced symbol
- **Add module qualifier** — Prefix the identifier with its full module path

**Example:**

```rescript
// "map" is unresolved
let result = map(arr, fn)

// Quick fix: Add open Belt.Array
open Belt.Array
let result = map(arr, fn)

// Or: Add qualifier
let result = Belt.Array.map(arr, fn)
```

### Generate Function from Usage

When you call a function that does not exist yet, the plugin can generate a stub function definition. Press `Alt+Enter` on the unresolved function call and choose **Generate function**.

**Before:**

```rescript
let result = processData(input, config)
// processData is not defined
```

**After** (stub function generated):

```rescript
let processData = (input, config) => {
  todo
}

let result = processData(input, config)
```
