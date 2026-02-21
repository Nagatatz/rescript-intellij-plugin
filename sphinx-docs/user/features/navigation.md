# Navigation

The plugin provides several navigation features to help you move through your codebase efficiently.

## Go to Definition

Hold `Ctrl` (`Cmd` on macOS) and click on a symbol, or press `Ctrl+B` to jump to its definition.

Works for:
- Variables and functions
- Types
- Modules
- External bindings

## Find References

Right-click on a symbol and select **Find Usages** (or press `Alt+F7`) to see all locations where the symbol is used.

## Go to Symbol

Press `Ctrl+Alt+O` (`Cmd+Option+O` on macOS) to search for any symbol across your project by name.

## Structure View

Press `Alt+7` (`Cmd+7` on macOS) to open the Structure panel, which shows an outline of the current file:

- Module declarations
- Function bindings (`let`)
- Type definitions
- External declarations
- Exception declarations

## File Switching (.res ↔ .resi)

Press `Alt+O` to switch between a ReScript source file (`.res`) and its interface file (`.resi`).

## Go to Related

Use **Navigate** → **Related Symbol** to jump between related files:

- `.res` → `.resi` (interface)
- `.res` → `.js` (compiled output)
- `.resi` → `.res` (implementation)

## Create Interface File

With a `.res` file open, use **Navigate** → **Create Interface File** to auto-generate a `.resi` interface file from the Language Server.

## Open Compiled JavaScript

Press `Alt+Shift+J` to open the compiled JavaScript output for the current `.res` file. The plugin asks the Language Server for the compiled file path and opens it in the editor.

## Qualified Name Copy

Press `Cmd+Shift+Alt+C` to copy the fully qualified name of the symbol at the cursor (e.g., `Module.SubModule.functionName`).

## Goto Super (.res ↔ .resi)

Press `Ctrl+U` (`Cmd+U` on macOS) to jump from a declaration in a `.res` file to the corresponding declaration in the `.resi` interface file, or vice versa.

The plugin matches declarations by name and type (`let`, `type`, `module`, `external`, `exception`), including one level of nested module declarations. If no matching declaration is found, the counterpart file is opened at the beginning.

**Example:**

```rescript
// In Foo.res, caret on:
let greet = (name) => `Hello, ${name}!`
// Press Ctrl+U → jumps to:
// let greet: string => string   (in Foo.resi)
```

## Go to Test

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

## Context Info

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

## External Documentation

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

## Breadcrumb Navigation

The editor shows a breadcrumb trail at the top, displaying your current scope path (file → module → function). Click any segment to navigate to that scope.
