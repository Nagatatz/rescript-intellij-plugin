---
myst:
  html_meta:
    "keywords": "keyboard shortcuts, keybindings, hotkeys, shortcut reference"
---

# Keyboard Shortcuts

This page lists keyboard shortcuts for plugin-specific features. Standard JetBrains shortcuts (copy, paste, find, etc.) are not listed here.

:::{note}
Shortcuts shown are for macOS. On Windows/Linux, replace `Cmd` with `Ctrl` and `Option` with `Alt`.
:::

## Navigation

| Shortcut | Action |
|----------|--------|
| `Cmd+B` / `Ctrl+Click` | Go to Definition |
| `Cmd+Option+O` | Go to Symbol |
| `Alt+O` | Switch between `.res` and `.resi` |
| `Alt+Shift+J` | Open compiled JavaScript |
| `Cmd+Shift+Alt+C` | Copy qualified name |
| `Alt+F7` | Find Usages |
| `Cmd+7` | Structure View |
| `Ctrl+U` | Goto Super (.res ↔ .resi declaration) |
| `Ctrl+Alt+B` | Go to Implementation (.resi → .res) |
| `Ctrl+Shift+T` | Go to Test / Create Test |
| `Ctrl+Alt+H` | Call Hierarchy |
| `Alt+Q` | Context Info (sticky declaration header) |
| `Shift+F1` | External Documentation (Belt/Js API docs) |

### Tips

The most frequently used navigation shortcut is **Cmd+B** (Go to Definition). Press it on any identifier --- a function name, a module reference, a type --- and the IDE jumps to where it is defined. If the definition is in another file, that file opens automatically. You can also hold `Cmd` and click on an identifier with the mouse for the same effect.

**Go to Symbol** (`Cmd+Option+O`) is the fastest way to open a file when you know the name of the function or type you are looking for. Start typing a partial name, and the dialog filters all symbols across the project. This is especially useful in large codebases where you may not remember which module contains a particular function.

**Switch between .res and .resi** (`Alt+O`) is invaluable when working with interfaces. If you are editing `MyModule.res` and want to check or update its interface, press `Alt+O` to jump to `MyModule.resi` instantly. Press it again to jump back. If the `.resi` file does not exist, you can generate one using the **Create Interface** action available in the **Navigate > Go to Related** menu.

**Open compiled JavaScript** (`Alt+Shift+J`) opens the `.js` file that the ReScript compiler generated from the current `.res` file. This is useful for debugging interop issues, verifying that the compiled output matches your expectations, or understanding how ReScript translates specific patterns to JavaScript.

**Copy qualified name** (`Cmd+Shift+Alt+C`) copies the fully qualified module path of the symbol at the cursor (e.g., `MyModule.SubModule.myFunction`). This is convenient when writing `open` statements, referencing symbols in documentation, or constructing imports in JavaScript interop code.

**Find Usages** (`Alt+F7`) locates every reference to the symbol at the cursor across the entire project. The results appear in a tool window grouped by file, making it easy to understand the impact of changing a function or type.

**Structure View** (`Cmd+7`) opens a sidebar showing the outline of the current file: all `let` bindings, `type` definitions, `module` declarations, and `external` bindings. You can click any item to jump to it. This is particularly useful for navigating long files with many declarations.

**Goto Super** (`Ctrl+U`) jumps between a `.res` implementation and the corresponding declaration in the `.resi` interface file. This is the fastest way to verify that your implementation matches the interface signature.

**Go to Test** (`Ctrl+Shift+T`) navigates between an implementation file and its test file. If the test file does not exist, a dialog offers to create one with framework-specific boilerplate (Jest or Vitest).

**Go to Implementation** (`Ctrl+Alt+B`) jumps from a declaration in a `.resi` interface file to its corresponding implementation in the `.res` file. This is the inverse of **Goto Super** (`Ctrl+U`), which goes from `.res` to `.resi`.

**Call Hierarchy** (`Ctrl+Alt+H`) opens a tool window showing the call hierarchy for the function at the cursor. You can explore both callers (who calls this function) and callees (what this function calls) as a navigable tree. This is invaluable for understanding how a function fits into the larger codebase before refactoring it.

**Context Info** (`Alt+Q`) shows the enclosing declaration header as a sticky line at the top of the editor when you have scrolled past the beginning of a long function or module body.

**External Documentation** (`Shift+F1`) opens the rescript-lang.org API documentation for `Belt.*` and `Js.*` standard library modules in your browser.

## Editing

| Shortcut | Action |
|----------|--------|
| `Cmd+Option+L` | Format file (rescript format) |
| `Ctrl+/` | Toggle line comment |
| `Ctrl+Shift+/` | Toggle block comment |
| `Shift+Enter` | Smart Enter (complete statement + new line) |
| `Alt+Shift+Up` | Move declaration up |
| `Alt+Shift+Down` | Move declaration down |
| `Alt+Enter` | Show intentions (Wrap with Some/Ok/Error, etc.) |
| `Ctrl+Alt+T` | Surround with (if/switch/try/block) |
| `Ctrl+Alt+O` | Optimize imports (remove duplicate opens) |
| `Ctrl+Shift+Delete` | Unwrap/Remove (Some/Ok/Error/if/switch/try/block) |
| `Ctrl+Shift+J` | Smart Join Lines (pipe/let/arrow aware) |
| `Alt+Shift+Cmd+Left/Right` | Move element left/right |
| `Ctrl+Shift+[` / `]` | Code block boundary selection |

### Tips

**Alt+Enter** is the Swiss Army knife shortcut for quick fixes and context-aware actions. Place your cursor on an expression and press `Alt+Enter` to see available intentions. For example, on any expression you can choose **Wrap with Some(...)**, **Wrap with Ok(...)**, or **Wrap with Error(...)** to wrap the expression in an `option` or `result` constructor. On a declaration without a `@genType` annotation, you can choose **Add @genType** to add the decorator. The available intentions change depending on what is under the cursor.

**Format file** (`Cmd+Option+L`) runs `rescript format` through the language server on the current file. This reformats the entire file according to the standard ReScript style. Unlike some formatters that only adjust whitespace, `rescript format` can also normalize syntax (e.g., standardizing arrow function formatting). Since formatting requires the LSP, this shortcut has no effect when the language server is disconnected.

**Smart Enter** (`Shift+Enter`) is a time-saver when you want to start a new line below the current one without moving to the end of the line first. It completes the current statement contextually and positions the cursor on a new line with proper indentation.

**Move declaration up/down** (`Alt+Shift+Up/Down`) moves the entire top-level declaration at the cursor position up or down. Unlike the standard line-move shortcut, this operates on logical declarations --- it moves the whole `let` binding, `type` definition, or `module` block as a unit. This is useful for reordering declarations within a file without cut-and-paste.

**Surround with** (`Ctrl+Alt+T`) wraps the selected code in a control structure. Select an expression or block of code, press the shortcut, and choose from `if ... { }`, `switch ... { }`, `try { } catch { }`, or `{ }` (plain block). The selected code is placed inside the chosen structure with the cursor positioned for you to fill in the condition or pattern.

**Optimize imports** (`Ctrl+Alt+O`) removes duplicate `open` statements from the file. If you have accidentally opened the same module multiple times, this shortcut cleans them up in one action.

**Unwrap/Remove** (`Ctrl+Shift+Delete`) removes a surrounding wrapper and extracts the inner expression. Place your cursor inside `Some(expr)`, `Ok(expr)`, `Error(expr)`, `if`, `switch`, `try`, or bare braces, and choose which wrapper to remove. This is the inverse of the "Wrap with" intention actions.

**Smart Join Lines** (`Ctrl+Shift+J`) joins the current line with the next using ReScript-aware logic. When joining pipe chains (`->`), lines are joined without adding a space. When joining let bindings or arrow functions, a single space is inserted after `=` or `=>`.

## Refactoring

| Shortcut | Action |
|----------|--------|
| `Shift+F6` | Rename symbol |
| `Ctrl+Alt+V` | Extract Variable |
| `Ctrl+Alt+M` | Extract Function |
| `Ctrl+Alt+N` | Inline Variable/Function |
| `Ctrl+F6` | Change Signature |
| `Alt+Delete` | Safe Delete |

### Tips

**Rename symbol** (`Shift+F6`) renames the identifier at the cursor and updates all references across the project. This works for `let` bindings, type names, module names, and other named entities. Since it relies on semantic analysis, it requires the LSP to be connected.

**Extract Variable** (`Ctrl+Alt+V`) extracts the selected expression into a new `let` binding placed above the current statement. The original expression is replaced with a reference to the new binding. This is useful for breaking up complex expressions into named intermediate values that improve readability.

**Extract Function** (`Ctrl+Alt+M`) extracts the selected code into a new function. The plugin analyzes which variables from the surrounding scope are used in the selection and generates the appropriate parameters. The original code is replaced with a call to the new function.

**Inline Variable/Function** (`Ctrl+Alt+N`) is the inverse of extraction: it replaces a variable or function reference with its definition and removes the original binding. Use this to simplify code when an intermediate variable or wrapper function adds no clarity.

**Change Signature** (`Ctrl+F6`) opens a dialog to modify a function's parameters --- add, remove, reorder, or rename them. All call sites are updated automatically to match the new signature. This is especially useful for adding labeled arguments to an existing function.

**Safe Delete** (`Alt+Delete`) deletes a symbol only after verifying that it has no remaining usages in the project. If usages are found, a dialog shows them so you can review and decide whether to proceed. This prevents accidental breakage from removing a function or type that is still referenced elsewhere.

## Completion

| Shortcut | Action |
|----------|--------|
| `Cmd+Space` | Trigger code completion |
| `Tab` | Expand live template |
| `.switch` | Postfix: wrap in switch |
| `.pipe` | Postfix: add pipe operator |
| `.log` | Postfix: wrap in Console.log |
| `.some` | Postfix: wrap in Some(...) |
| `.ok` | Postfix: wrap in Ok(...) |
| `.error` | Postfix: wrap in Error(...) |
| `.ignore` | Postfix: pipe to ignore |
| `.promise` | Postfix: wrap in Promise.then chain |
| `.await` | Postfix: prepend await |

### Tips

**Code completion** (`Cmd+Space`) triggers the LSP completion popup, which provides context-aware suggestions based on the current scope and expected type. The completions include module members, record fields, variant constructors, and function names. Type information is shown alongside each suggestion, so you can distinguish between functions with similar names. Pressing `Tab` or `Enter` on a suggestion inserts it.

**Postfix templates** are one of the most productive features for writing idiomatic ReScript. They work by typing a dot (`.`) after an expression and then the template name. The expression is automatically rewritten into the expanded form.

The **`.switch`** postfix is especially useful when you want to pattern-match on a value. Type `myOption.switch` and the expression is rewritten to `switch myOption { | _ => }` with the cursor positioned after the arrow, ready for you to fill in the cases. This is faster than typing the `switch` keyword, parentheses, and braces manually.

The **`.pipe`** postfix converts `expr.pipe` into `expr->`, which is the standard ReScript pipe-first syntax. This is convenient when chaining operations: you can type the initial expression, add `.pipe`, and immediately start typing the function name.

The **`.log`** postfix wraps an expression in `Console.log(...)` for quick debugging. Type `myValue.log` and it becomes `Console.log(myValue)`. This is the fastest way to add a debug print statement.

The **`.some`**, **`.ok`**, and **`.error`** postfix templates wrap the preceding expression in the corresponding constructor: `Some(...)`, `Ok(...)`, or `Error(...)`. These are handy when you need to lift a plain value into an `option` or `result` type.

The **`.ignore`** postfix appends `->ignore` to the expression, which discards the return value. This is a common pattern in ReScript when calling a function for its side effects but not using the return value.

**Live templates** are expanded with `Tab` after typing the abbreviation. Unlike postfix templates (which transform existing expressions), live templates insert new code patterns. For example, typing `let` followed by `Tab` expands into a `let` binding skeleton with tab stops for the name and value.

## Code Generation

| Shortcut | Action |
|----------|--------|
| `Cmd+N` / `Alt+Insert` | Generate menu (Switch Arms, Module Type) |

### Tips

The **Generate** menu (`Cmd+N`) provides code generation actions that create boilerplate code from context. Place your cursor inside a `switch` expression and choose **Generate Switch Arms** to automatically generate pattern match arms for all constructors of the matched variant type. This is especially useful for large variant types where manually typing all cases is tedious and error-prone.

**Generate Module Type** creates a module type signature from the current module's contents. This can save time when you need to create a `.resi` interface file or define a module type for a functor argument.

## Running

| Shortcut | Action |
|----------|--------|
| `Ctrl+R` / `Shift+F10` | Run current configuration |
| `Ctrl+Ctrl` | Run Anything (ReScript CLI commands) |
| `Alt+Shift+D` | Debug compiled JavaScript |
| `Ctrl+Shift+P` | Expression Type (show inferred type) |
| `Alt+6` | Open Problems panel |

### Tips

**Run current configuration** (`Ctrl+R` or `Shift+F10`) executes the currently selected run configuration, which is typically a ReScript build command. The plugin automatically creates run configurations based on the `rescript.json` file in your project. You can also run tests directly from gutter icons next to test functions.

**Run Anything** (`Ctrl+Ctrl`) opens a universal run dialog where you can type `rescript build`, `rescript clean`, or `rescript format` to execute ReScript CLI commands directly without creating a run configuration.

**Debug Compiled JS** (`Alt+Shift+D`) resolves the current `.res` file to its compiled JavaScript output and launches a `node --inspect-brk` debug session. This requires the JavaScript Debugger plugin and a prior ReScript build.

**Expression Type** (`Ctrl+Shift+P`) shows the inferred type of the expression at the cursor position. This is an on-demand alternative to inlay hints --- useful when you need to check a specific type without persistent annotations.

The **Problems panel** (`Alt+6`) displays all diagnostics reported by the language server: type errors, syntax errors, and warnings. Each entry shows the file, line number, and error message. Double-clicking an entry jumps to the exact location in the source file. This panel is the central place for reviewing all issues in your project at a glance.

## View

| Shortcut | Action |
|----------|--------|
| `Cmd+Shift+Plus` | Expand all folds |
| `Cmd+Shift+Minus` | Collapse all folds |
| `Cmd+Plus` | Expand fold at cursor |
| `Cmd+Minus` | Collapse fold at cursor |

### Tips

**Code folding** helps manage large files by collapsing regions you are not currently working on. The plugin supports folding for all top-level declarations (`let`, `type`, `module`, `external`), block comments, and custom region markers (`//#region` ... `//#endregion`).

**Collapse all folds** (`Cmd+Shift+Minus`) is particularly useful when opening a large file for the first time: it gives you a high-level overview of all declarations, similar to the Structure View but inline. You can then selectively expand individual declarations with `Cmd+Plus` to drill into the ones you need.

Custom region markers let you define your own foldable sections. Add `//#region Section Name` and `//#endregion` comments around a group of related declarations, and the IDE treats them as a collapsible unit. This is handy for organizing utility modules or grouping related functions.
