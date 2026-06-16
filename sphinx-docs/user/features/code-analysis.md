---
myst:
  html_meta:
    "keywords": "code analysis, inspections, diagnostics, dead code, reanalyze, error lens"
---

# Code Analysis

The plugin provides several code analysis features to help you maintain clean, correct code.

## Real-Time Diagnostics

{bdg-primary}`LSP Required`

The Language Server provides real-time error and warning diagnostics as you type:

- **Errors** --- Shown with red underlines
- **Warnings** --- Shown with yellow underlines
- **Info** --- Shown with subtle underlines

View all diagnostics in the **Problems** panel (`Alt+6`).

Instead of waiting for a full build cycle to discover issues, you get immediate feedback on errors and warnings as you type, catching problems before they compound.

## Code Inspections

{bdg-success}`Native`

The plugin includes built-in inspections that run locally without requiring the Language Server.

### Duplicate Open Detection

Detects when the same module is opened multiple times in the same file. Duplicate `open` statements are redundant and add unnecessary noise to the code.

::::{tab-set}
:::{tab-item} Before (duplicate detected)
```rescript
open Belt
open Belt.Array
open Belt  // Warning: duplicate open statement

let arr = [1, 2, 3]
let doubled = arr->Array.map(x => x * 2)
```
:::
:::{tab-item} After (optimized)
```rescript
open Belt
open Belt.Array

let arr = [1, 2, 3]
let doubled = arr->Array.map(x => x * 2)
```
:::
::::

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

### Mismatched JSX Closing Tag

Warns when a JSX element's closing tag name does not match its opening tag name. Such mismatches usually result from renaming a tag and forgetting to update the matching side.

```rescript
let view = <div> {React.string("hello")} </span>
// Warning: Closing tag 'span' does not match opening tag 'div'
```

The warning is highlighted on the closing tag name, so you can spot and fix the mismatch immediately. Fragments (`<> ... </>`) and self-closing elements (`<input />`) are never flagged, and correctly nested elements sharing the same name are left untouched.

### Missing Configuration

When no `rescript.json` (or legacy `bsconfig.json`) is found in the project root, the plugin displays an editor notification bar at the top of ReScript files. This configuration file is essential for:

- **ReScript compiler** --- Without it, the project cannot be built. The compiler uses this file to locate source directories, dependencies, and build settings.
- **Language Server** --- The LSP server relies on `rescript.json` to understand the project structure. Without it, features like auto-completion, go-to-definition, and diagnostics will not function.
- **Plugin features** --- Several plugin features (Code Lens, semantic highlighting, inlay hints) depend on the Language Server, which in turn requires the configuration file.

If you see this warning, create a `rescript.json` file in your project root. The easiest way is to use the Project Wizard (**File** > **New** > **Project** > **ReScript**) or initialize a project with `npm init rescript-app@latest`.

## Dead Code Analysis (reanalyze)

{bdg-success}`Native` {bdg-warning}`Configuration Required`

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

### Server Mode (ReScript >= 12.1.0)

When ReScript 12.1.0 or later is installed, the plugin can start a **reanalyze server daemon** that transparently accelerates dead code analysis. Instead of running a fresh `reanalyze -json` process for every file, the server keeps analysis state in memory and communicates via a Unix socket (`.rescript-reanalyze.sock`), resulting in significantly faster results on large projects.

**Enabling server mode:**

Server mode is enabled by default in **Settings** > **Languages & Frameworks** > **ReScript** > **Enable reanalyze server mode**. The server starts automatically when a ReScript project is opened, provided:

1. The setting is enabled
2. The project contains `rescript.json` (or `bsconfig.json`)
3. ReScript >= 12.1.0 is installed in `node_modules`

**Features:**

- **Automatic startup** --- The server starts when the project opens and stops when the project closes
- **External server detection** --- If a reanalyze server is already running (e.g., started by another tool), the plugin detects it via the socket file and does not start a second instance
- **Health monitoring** --- The plugin checks the server every 5 seconds and automatically restarts it (up to 3 times) if it crashes
- **Transparent acceleration** --- No changes to your workflow; the existing annotator and inspection automatically benefit from the server

**Note:** Existing behavior is fully preserved when server mode is disabled or ReScript < 12.1.0 is installed. The plugin falls back to the standard per-invocation `reanalyze -json` mode.

Without dead code analysis, unused functions and types silently accumulate, increasing build times and cognitive load. Reanalyze integration surfaces this dead code automatically so you can keep your codebase lean and maintainable.

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

These local inspections run instantly without the Language Server, catching common code quality issues like redundant imports, empty scaffolding, and missing configuration before they become problems in code review.

:::{seealso}
[Code Editing](code-editing.md) offers Intention Actions (`Alt+Enter`) for manual code transformations that complement automated inspections.
:::

### Mutability Diagnostics

Detects `ref` bindings that are never reassigned with `:=`. If a mutable reference is created but never mutated, it can be simplified to a plain `let` binding.

::::{tab-set}
:::{tab-item} Before (warning detected)
```rescript
let counter = ref(0)
// counter is never reassigned with :=
let value = counter.contents
```
:::
:::{tab-item} After (quick fix applied)
```rescript
let counter = 0
let value = counter
```
:::
::::

Press `Alt+Enter` on the warning and select **Remove unnecessary ref** to apply the fix.

### Style Linting

Detects common style issues and suggests idiomatic alternatives:

**1. Redundant boolean expression:**

```rescript
// Before: redundant
let isValid = if condition { true } else { false }
// After: simplified
let isValid = condition
```

**2. Deprecated Belt.* usage:**

```rescript
// Before: legacy Belt API
Belt.Array.map(arr, fn)
// After: modern API
Array.map(arr, fn)
```

**3. Boolean switch:**

```rescript
// Before: verbose
switch flag { | true => a | false => b }
// After: idiomatic
if flag { a } else { b }
```

Each rule provides a quick fix via `Alt+Enter`.

### Suggested Refactoring

Automatically detects code patterns that can be improved and proposes refactoring suggestions. This inspection identifies opportunities for cleaner code without changing behavior.

**Detected patterns:**

**1. Extractable repeated expressions:**

```rescript
// Before: same expression used multiple times
let area = width * height
let perimeter = 2 * (width * height)
// Suggestion: Extract common expression into a variable
```

**2. Simplifiable conditional assignments:**

```rescript
// Before: verbose conditional
let result = if condition {
  value
} else {
  value
}
// Suggestion: Remove redundant conditional
```

**3. Inlineable single-use variables:**

```rescript
// Before: variable used only once
let temp = compute(x)
process(temp)
// Suggestion: Inline variable
```

Each suggestion appears as a weak warning with a quick fix via `Alt+Enter`.

## Error Lens

{bdg-primary}`LSP Required`

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

By showing diagnostics directly on the affected line, Error Lens eliminates the need to hover over underlines or check the Problems panel, letting you spot and fix issues without breaking your editing flow.

### Type Mismatch Inline Hints

When a type error involves a mismatch between expected and actual types, Error Lens displays a structured inline hint showing both types side by side:

```
let x: string = 42    ← Expected: string, Actual: int
```

This structured display makes it easier to understand type errors at a glance without needing to open the Problems panel or hover over the error.

### Type Mismatch Diff Highlighting

For type mismatch errors, Error Lens visually highlights the parts that differ between the expected and actual types. Segments that match are shown in the standard color, while differing segments are highlighted in a contrasting color, making it easy to pinpoint exactly where the types diverge.

```
let x: option<string> = Some(42)
← Expected: option<[string]> | Actual: option<[int]>
```

In this example, only `string` vs `int` would be highlighted, since `option<...>` matches in both types.

## Format Check

{bdg-success}`Native` {bdg-warning}`Configuration Required`

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

Format Check helps enforce a consistent code style across your team without manual review — unformatted files are flagged automatically and can be fixed with a single shortcut.

## Problem Highlight Filter

{bdg-success}`Native`

The plugin suppresses code analysis highlights in directories where they are not useful, such as `node_modules/` and other dependency directories. This prevents noise from third-party library files appearing in the Problems panel and editor gutter.

### Filtered Directories

- `node_modules/` --- npm/yarn/pnpm package directories
- Other generated or vendored directories that are not part of your source code

### How It Works

The plugin implements IntelliJ's `ProblemHighlightFilter` extension point, which checks whether a file should receive code analysis highlights. Files inside filtered directories are excluded from highlighting, reducing false positives and improving IDE performance.

This filter applies to all highlight types (errors, warnings, info) from both local inspections and the Language Server.

This keeps the Problems panel focused on issues in your own code, filtering out noise from third-party libraries that would otherwise clutter your diagnostics and slow down the editor.

## Import Optimization

{bdg-success}`Native`

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

Instead of manually scanning for and removing redundant `open` statements, a single shortcut cleans up all duplicates at once, keeping your imports tidy with zero effort.

## Quick Fixes (LSP)

{bdg-primary}`LSP Required`

The Language Server provides automatic code fixes via `Alt+Enter` (or the light bulb icon in the gutter). These LSP-powered quick fixes operate on semantic analysis from the ReScript compiler and can handle situations that local inspections cannot.

`@rescript/language-server` ships nine categories of quick fix that the plugin receives through the standard `textDocument/codeAction` flow:

- **Add missing pattern cases** --- Inserts unhandled constructors at the end of a `switch` block (`simpleAddMissingCases`)
- **Wrap in `Some` / unwrap option** --- Wraps a value in `Some(...)` (or unwraps an option) when the surrounding context expects an `option` type (`wrapInSome`, `unwrapOptional`)
- **Add missing record fields** --- Fills in fields that are required by the record type but absent from the literal (`addUndefinedRecordFields`)
- **Insert primitive conversion** --- Wraps an expression with `int_of_string`, `float_of_int`, etc. when an `int` / `float` / `string` mismatch is reported (`simpleConversion`)
- **Replace with suggested name** --- Replaces a misspelled identifier with the value the compiler suggested in *Did you mean ...?* (`didYouMean`)
- **Remove unused code** --- Deletes a declaration that reanalyze flagged as unused (`removeUnusedCode`; requires a `reanalyze` block in `rescript.json`)
- **Extract local module to file** --- Moves a `module M = { ... }` declaration into a new `M.res` file at the project root (`extractLocalModuleToFile`)
- **Expand catch-all pattern** --- Replaces an explicit `_ =>` case with one branch per variant constructor (`expandCatchAllPatterns`)
- **Apply uncurried** --- Converts a curried call site into the uncurried form `f(. x)` on legacy ReScript v10 / v11 codebases (`applyUncurried`; not emitted on uncurried-by-default builds)

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

::::{tab-set}
:::{tab-item} Before
```rescript
let result = processData(input, config)
// processData is not defined
```
:::
:::{tab-item} After (stub function generated)
```rescript
let processData = (input, config) => {
  todo
}

let result = processData(input, config)
```
:::
::::

### Type Hole Quick Fix

When the compiler reports a type hole (`_` used as a type placeholder), the plugin suggests candidate types to fill in. Press `Alt+Enter` on the type hole diagnostic to see matching type suggestions.

::::{tab-set}
:::{tab-item} Before
```rescript
let parse: string => _ = jsonStr => {
  // compiler error: type hole found
}
```
:::
:::{tab-item} After (applying suggested type)
```rescript
let parse: string => JSON.t = jsonStr => {
  // type hole filled with suggested type
}
```
:::
::::

The quick fix parses the compiler diagnostic to extract candidate types and offers them as replacement options.

LSP quick fixes turn compiler errors into one-click corrections, letting you resolve type mismatches, missing imports, and incomplete patterns directly from the error location instead of manually editing the code.

## Type Narrowing Visualizer

ReScript narrows the scrutinee of a `switch` expression to a more specific type inside each arm. The plugin visualizes this narrowing inline so you can see the refined type without hovering on every arm.

When the LSP server is running, an inlay hint is rendered immediately after each arm's `=>` showing the narrowed type. The hint reuses the same hover information that powers Quick Documentation, so it always agrees with what `textDocument/hover` returns at the scrutinee position.

Pattern bindings get an extra hint of their own. When an arm introduces a binding (`Some(x)`, `Loaded(payload, error)`, …), a second inlay is anchored right after each binding identifier showing that binding's own narrowed type. Bare lowercase catch-all arms like `| other => ...` are intentionally skipped because the binding's type is identical to the scrutinee — no information is gained.

::::{tab-set}
:::{tab-item} Without the hint
```rescript
let describe = result =>
  switch result {
  | Ok(value) => value
  | Error(_) => "fallback"
  }
```
:::
:::{tab-item} With the hint
```rescript
let describe = result =>
  switch result {
  | Ok(value: string) =>: result<string, error> value
  | Error(_) =>: result<string, error> "fallback"
  }
```
:::
::::

The visualizer also handles or-patterns (`| Some(_) | None =>` is treated as a single arm), `when` guards, and nested `switch` expressions.

### Configuration

- Toggle from **Settings > Editor > Inlay Hints > ReScript > Type narrowing in switch arms**.
- The project-level flag `narrowingHintsEnabled` (default on) is exposed on `RescriptProjectSettings` for projects that want to disable this hint without touching other inlay providers.
- A 50-arm-per-file soft cap suppresses the hint on switch-heavy files to keep editor scrolling responsive.

When the LSP server is not running the visualizer is silent — it never displays a partial or stale type. Trivial types (`unit`, free type variables) are also suppressed because they convey no narrowing information.
