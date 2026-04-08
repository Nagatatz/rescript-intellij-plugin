---
myst:
  html_meta:
    "keywords": "navigation, go to definition, find usages, search everywhere, structure view"
---

# Navigation

The plugin provides several navigation features to help you move through your codebase efficiently.

## Go to Definition

{bdg-primary}`LSP Required`

Hold `Ctrl` (`Cmd` on macOS) and click on a symbol, or press `Ctrl+B` to jump to its definition.

Works for:
- Variables and functions
- Types
- Modules
- External bindings

Instead of manually searching files and scrolling through code, you can instantly jump to any symbol's definition, dramatically speeding up code reading and exploration.

## Find References

{bdg-primary}`LSP Required`

Right-click on a symbol and select **Find Usages** (or press `Alt+F7`) to see all locations where the symbol is used.

Before making changes to a function or type, you can instantly see every place it's referenced, making it easy to assess the impact of a refactoring.

## Go to Symbol

{bdg-success}`Native`

Press `Ctrl+Alt+O` (`Cmd+Option+O` on macOS) to search for any symbol across your project by name.

Symbol lookup is powered by PSI Stub Index, which pre-indexes declaration names at project load time for fast, O(log n) retrieval instead of scanning every file.

When you know a symbol's name but not which file it lives in, this saves you from manually browsing the project tree or using text search across the entire codebase.

## Structure View

{bdg-success}`Native`

Press `Alt+7` (`Cmd+7` on macOS) to open the Structure panel, which shows an outline of the current file:

- Module declarations
- Function bindings (`let`)
- Type definitions
- External declarations
- Exception declarations

This gives you a bird's-eye view of a file's structure at a glance, so you can quickly locate a specific declaration without scrolling through hundreds of lines.

:::{seealso}
[Code Editing](code-editing.md) provides code folding to collapse declarations shown in the Structure View.
:::

## File Switching (.res ↔ .resi)

{bdg-success}`Native`

Press `Alt+O` to switch between a ReScript source file (`.res`) and its interface file (`.resi`).

When working with module interfaces, you frequently need to switch between the implementation and its public API. This one-keystroke shortcut eliminates the need to find the counterpart file in the project tree.

## Go to Implementation

{bdg-success}`Native`

Press `Ctrl+Alt+B` (`Cmd+Alt+B` on macOS) to jump from an interface declaration in a `.resi` file to the corresponding implementation in the `.res` file.

This is the reverse of [Goto Super](#goto-super-res-resi): while Goto Super goes from implementation to interface, Go to Implementation goes from interface to implementation.

**Example:**

```rescript
// In Foo.resi, caret on:
let greet: string => string
// Press Ctrl+Alt+B → jumps to:
// let greet = (name) => `Hello, ${name}!`   (in Foo.res)
```

The plugin matches declarations by name and type (`let`, `type`, `module`, `external`, `exception`), including one level of nested module declarations.

When reviewing a `.resi` interface and you want to see how a function is actually implemented, this takes you directly to the implementation body instead of opening the file and searching for it manually.

## Search Everywhere

{bdg-success}`Native`

Press `Shift` twice (double-tap `Shift`) to open the **Search Everywhere** dialog, which provides unified search across:

- **Files** — Find `.res` and `.resi` files by name
- **Symbols** — Search for functions, types, modules, and other declarations (stub-indexed for fast lookup)
- **Actions** — Search for IDE actions and plugin commands

ReScript files and symbols appear alongside results from other languages in the search dialog. Use the tab bar at the top to filter by category.

This is the fastest way to find anything in your project — instead of remembering which menu or shortcut to use, a single double-tap of `Shift` lets you search files, symbols, and actions all at once.

## Go to Related

{bdg-success}`Native`

Use **Navigate** → **Related Symbol** to jump between related files:

- `.res` → `.resi` (interface)
- `.res` → `.js` (compiled output)
- `.resi` → `.res` (implementation)

This provides a unified view of all files associated with a module — its interface, implementation, and compiled output — letting you quickly switch between different representations of the same code.

## File Include Navigation

{bdg-success}`Native`

The plugin resolves `open` statements to their corresponding module files, enabling navigation from `open` declarations to the referenced file.

**Example:**

```rescript
open Belt.Array  // Ctrl+click to navigate to Belt_Array.res
open MyModule    // Ctrl+click to navigate to MyModule.res
```

Module names are converted to filenames by replacing `.` with `_` (e.g., `Belt.Array` → `Belt_Array.res`).

Instead of guessing which file an `open` statement refers to, you can `Ctrl+click` directly on it to navigate to the module source, making dependency tracing effortless.

## Create Interface File

{bdg-primary}`LSP Required`

With a `.res` file open, use **Navigate** → **Create Interface File** to auto-generate a `.resi` interface file from the Language Server.

Writing a `.resi` file by hand requires copying every public declaration and removing the bodies. This command generates an accurate interface automatically, saving significant time and avoiding copy-paste errors.

## Open Compiled JavaScript

{bdg-primary}`LSP Required`

Press `Alt+Shift+J` to open the compiled JavaScript output for the current `.res` file. The plugin asks the Language Server for the compiled file path and opens it in the editor.

When debugging runtime behavior or checking what code ReScript actually generates, this lets you instantly view the compiled output without manually navigating to the build directory.

## Qualified Name Copy

{bdg-success}`Native`

Press `Cmd+Shift+Alt+C` to copy the fully qualified name of the symbol at the cursor (e.g., `Module.SubModule.functionName`).

This is useful when referencing a symbol in documentation, issue trackers, or code reviews — you get the exact qualified path without manually constructing it from the module hierarchy.

## Goto Super (.res ↔ .resi)

{bdg-success}`Native`

Press `Ctrl+U` (`Cmd+U` on macOS) to jump from a declaration in a `.res` file to the corresponding declaration in the `.resi` interface file, or vice versa.

The plugin matches declarations by name and type (`let`, `type`, `module`, `external`, `exception`), including one level of nested module declarations. If no matching declaration is found, the counterpart file is opened at the beginning.

**Example:**

```rescript
// In Foo.res, caret on:
let greet = (name) => `Hello, ${name}!`
// Press Ctrl+U → jumps to:
// let greet: string => string   (in Foo.resi)
```

When editing a function, you can instantly check its public API signature in the interface file, helping you verify that the implementation matches the exposed contract.

## Go to Test

{bdg-success}`Native`

Press `Ctrl+Shift+T` (`Cmd+Shift+T` on macOS) to navigate between an implementation file and its test file.

### Naming Conventions

The plugin searches for test files using these naming conventions:

| Implementation | Test File |
|---------------|-----------|
| `Foo.res` | `Foo_test.res` |
| `Foo.res` | `Foo.test.res` |
| `Foo.res` | `__tests__/Foo_test.res` |

### Creating Test Files

If no test file exists, the plugin offers to create one with framework-specific boilerplate:

- **Vitest** --- `open Vitest` with `describe`/`test`/`expect`
- **Jest** --- `open Jest` with `describe`/`test`/`expect`
- **No framework** --- Simple `Js.log` boilerplate

The test framework is auto-detected from your `package.json` dependencies.

This eliminates the friction of switching between implementation and tests. You can jump to the test file with one shortcut, and if it doesn't exist yet, the plugin creates it with the right boilerplate for your test framework.

## Context Info

{bdg-success}`Native`

When you scroll inside a long declaration body, the IDE displays the declaration header as a sticky line at the top of the editor, so you always know which function or module you are inside.

**Trigger:** `Alt+Q` (`Ctrl+Shift+Q` on macOS) or **View** > **Context Info**. Also appears automatically when scrolling through long declarations.

```rescript
// When scrolled deep inside a function body:
// ┌──────────────────────────────────────┐
// │ let processData = (input) =>         │  ← sticky header
// │   ...                                │
// │   // you are scrolled here           │
// └──────────────────────────────────────┘
```

Works for top-level declarations: `let`, `type`, `module`, `external`, `open`, `include`, and `exception`.

In large files with long function bodies, it's easy to lose track of which declaration you're editing. The sticky header keeps you oriented without needing to scroll back up to check.

## External Documentation

{bdg-success}`Native`

Press `Shift+F1` to open the rescript-lang.org API documentation page for the standard library module at the cursor.

Supported modules include all `Belt.*` submodules (Belt.Array, Belt.Map, Belt.Option, etc.) and all `Js.*` submodules (Js.Promise, Js.String, Js.Array, etc.).

**Example:**

```rescript
// Caret on "Belt.Array", press Shift+F1
// Opens: https://rescript-lang.org/docs/manual/latest/api/belt/array
```

:::{note}
External documentation URLs are only available for `Belt.*` and `Js.*` standard library modules. For other modules, Shift+F1 has no effect.
:::

When you need detailed API documentation beyond what the hover tooltip provides, this takes you directly to the official docs page without having to search the website yourself.

## Breadcrumb Navigation

{bdg-success}`Native`

The editor shows a breadcrumb trail at the top, displaying your current scope path (file → module → function). Click any segment to navigate to that scope.

When working inside deeply nested modules, the breadcrumb trail shows exactly where you are in the hierarchy and lets you jump to any parent scope with a single click.

## Call Hierarchy

{bdg-success}`Native`

Press `Ctrl+Alt+H` (`Cmd+Alt+H` on macOS) to open the **Call Hierarchy** window for the function at the caret.

The Call Hierarchy view shows two perspectives:

- **Callers** — Functions that call the selected function (who calls me?)
- **Callees** — Functions called within the selected function body (who do I call?)

Use the toolbar buttons in the Call Hierarchy tool window to switch between Callers and Callees views. The tree can be expanded to explore deeper call chains.

**Example:**

```rescript
let helper = (x) => x + 1

let process = (data) => {
  let result = helper(data)
  result
}

// Place caret on `helper`, press Ctrl+Alt+H:
// Callers view shows: process → helper
// Place caret on `process`, press Ctrl+Alt+H:
// Callees view shows: process → helper
```

:::{note}
Call Hierarchy uses PSI-based text search to discover call relationships. It works within the project scope and matches identifiers by name. For best results, use it on `let` and `external` declarations.
:::

Understanding the call chain of a function — who calls it and what it calls — is essential when refactoring or debugging. This view reveals the full call graph without manually tracing through the code.

## Type Signature Search

{bdg-primary}`LSP Required`

Search for functions by their type signature in the Search Everywhere dialog. See [Advanced Features — Type Signature Search](advanced.md#type-signature-search) for details.

When you know what type of function you need (e.g., `string => int`) but not its name, this lets you discover the right function by searching its shape — a natural fit for a type-inferred language like ReScript.

## Usage Type Classification

{bdg-success}`Native`

The plugin classifies how ReScript symbols are used in Find Usages results, grouping them by usage context:

- **Read access** — Reading a variable or field value
- **Write access** — Assigning to a mutable reference
- **Import / Open** — Referenced in an `open` statement
- **Type reference** — Used as a type annotation

This helps you quickly filter and understand usage patterns when reviewing references to a symbol. Without this classification, all usages appear in a flat list, making it harder to distinguish between reads, writes, imports, and type references.

## Navigation Bar

{bdg-success}`Native`

The navigation bar at the top of the editor displays the structure-aware hierarchy of your current cursor position. It shows top-level declarations (`let`, `type`, `module`, `external`, `exception`) with their icons, leveraging the existing Structure View model for accurate navigation.

This provides a compact, always-visible alternative to the Structure View panel, letting you see and navigate the file's declaration hierarchy without opening a separate tool window.
