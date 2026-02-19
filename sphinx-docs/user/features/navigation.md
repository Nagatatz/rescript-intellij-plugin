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

## Breadcrumb Navigation

The editor shows a breadcrumb trail at the top, displaying your current scope path (file → module → function). Click any segment to navigate to that scope.
