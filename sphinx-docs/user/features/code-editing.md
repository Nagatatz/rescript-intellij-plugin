# Code Editing

The plugin provides a rich set of editing features that make writing ReScript code more productive.

## Code Folding

Collapse and expand blocks to focus on the code that matters.

### Foldable Elements

- **Module declarations** — `module Name = { ... }`
- **Let bindings** — Multi-line `let` with `{ ... }`
- **Type definitions** — Multi-line `type` declarations
- **Block comments** — `/* ... */`
- **Custom regions** — `//#region` ... `//#endregion`

### Custom Regions

Use `//#region` and `//#endregion` markers to create custom foldable sections:

```rescript
//#region Helper functions
let add = (a, b) => a + b
let multiply = (a, b) => a * b
//#endregion
```

## Code Formatting

Press `Ctrl+Alt+L` (`Cmd+Option+L` on macOS) to format the current file using the `rescript format` CLI.

:::{note}
Formatting requires the ReScript compiler (`rescript`) to be installed in your project.
:::

## Brace Matching

The editor highlights matching brace pairs:
- `{` and `}`
- `[` and `]`
- `(` and `)`

## Smart Quotes

When you type `"`, `'`, or `` ` ``, the closing quote is automatically inserted.

## Smart Enter

Press `Shift+Enter` to intelligently complete the current statement and start a new line:

- Closes unclosed parentheses and braces
- Adds missing `switch` body braces
- Moves the cursor to the right position

## Comment Toggle

- `Ctrl+/` — Toggle line comment (`//`)
- `Ctrl+Shift+/` — Toggle block comment (`/* */`)

## Statement Mover

Move top-level declarations up or down:

- `Alt+Shift+Up` — Move declaration up
- `Alt+Shift+Down` — Move declaration down

This moves the entire declaration (including decorators) as a unit.

## Intention Actions

Press `Alt+Enter` on an expression to see available intentions:

| Intention | Description |
|-----------|-------------|
| Wrap with `Some(...)` | Wrap expression in `Some()` |
| Wrap with `Ok(...)` | Wrap expression in `Ok()` |
| Wrap with `Error(...)` | Wrap expression in `Error()` |
| Add `@genType` | Add `@genType` annotation to a declaration |

## Surround With

Select code and press `Ctrl+Alt+T` to surround it with:

- `if` expression
- `switch` expression
- `try` / `catch` block
- Block scope `{ ... }`

## Import Optimization

Press `Ctrl+Alt+O` to remove duplicate `open` statements from the current file.

## Rename

Press `Shift+F6` to rename a symbol across the project. The Language Server handles finding all references and updating them.

## Paste as JSON.t

Use **Edit** → **Paste as JSON.t** to convert JSON from your clipboard into a ReScript `JSON.t` value.

## Code Generation

Press `Cmd+N` (or `Alt+Insert`) to open the Generate menu:

- **Generate Switch Arms** — Auto-generate match arms for a variant type
- **Generate Module Type** — Generate a module type skeleton from a module implementation

## Spellchecking

The plugin supports IntelliJ's built-in spellchecker for:

- Comments (line and block)
- String literals
- Identifiers (camelCase splitting)
