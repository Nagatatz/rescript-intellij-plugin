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

### Wrap with Some(...)

Wraps a selected expression in `Some()`, useful when you need to convert a plain value into an `option` type.

Select the expression, press `Alt+Enter`, and choose **Wrap with Some(...)**.

**Before:**

```rescript
let name = "Alice"
let greeting = getGreeting(name)
```

**After** (with `name` selected):

```rescript
let name = "Alice"
let greeting = getGreeting(Some(name))
```

### Wrap with Ok(...)

Wraps a selected expression in `Ok()`, useful when returning a success value from a function that uses the `result` type.

**Before:**

```rescript
let value = computeResult()
let response = processResponse(value)
```

**After** (with `value` selected):

```rescript
let value = computeResult()
let response = processResponse(Ok(value))
```

### Wrap with Error(...)

Wraps a selected expression in `Error()`, useful when returning an error value from a function that uses the `result` type.

**Before:**

```rescript
let message = "Something went wrong"
let result = message
```

**After** (with `message` on the second line selected):

```rescript
let message = "Something went wrong"
let result = Error(message)
```

### Add @genType

Adds the `@genType` annotation above a `let`, `type`, or `module` declaration. This annotation tells the ReScript compiler to generate TypeScript type definitions for the declaration, enabling seamless interop with TypeScript code.

The intention is only available on declarations that do not already have a `@genType` annotation.

**Before:**

```rescript
let greet = (name: string) => `Hello, ${name}!`
```

**After:**

```rescript
@genType
let greet = (name: string) => `Hello, ${name}!`
```

This also works on type and module declarations:

**Before:**

```rescript
type user = {
  name: string,
  age: int,
}
```

**After:**

```rescript
@genType
type user = {
  name: string,
  age: int,
}
```

## Surround With

Select code and press `Ctrl+Alt+T` to surround it with:

- `if` expression
- `switch` expression
- `try` / `catch` block
- Block scope `{ ... }`

### if expression

Wraps the selected code inside an `if` block. The cursor is placed on the `condition` placeholder so you can immediately type the condition.

**Before** (with `Js.log("hello")` selected):

```rescript
Js.log("hello")
```

**After:**

```rescript
if (condition) {
  Js.log("hello")
}
```

### switch expression

Wraps the selected code inside a `switch` expression with a default `_` arm. The cursor is placed on the `expr` placeholder so you can type the expression to match on.

**Before** (with `defaultHandler()` selected):

```rescript
defaultHandler()
```

**After:**

```rescript
switch expr {
| _ => defaultHandler()
}
```

### try / catch block

Wraps the selected code in a `try` / `catch` block. The cursor is placed on the `()` placeholder in the catch arm so you can define the error handling logic.

**Before** (with `parseJson(input)` selected):

```rescript
parseJson(input)
```

**After:**

```rescript
try {
  parseJson(input)
} catch {
| exn => ()
}
```

### Block scope { ... }

Wraps the selected code in a block scope. This is useful for limiting variable scope or grouping expressions.

**Before** (with the two lines selected):

```rescript
let temp = calculate()
process(temp)
```

**After:**

```rescript
{
  let temp = calculate()
  process(temp)
}
```

## Import Optimization

Press `Ctrl+Alt+O` to remove duplicate `open` statements from the current file.

## Rename

Press `Shift+F6` to rename a symbol across the project. The Language Server handles finding all references and updating them.

## Paste as JSON.t

Use **Edit** > **Paste as JSON.t** to convert JSON from your clipboard into a ReScript `JSON.t` value.

## Code Generation

Press `Cmd+N` (or `Alt+Insert`) to open the Generate menu:

- **Generate Switch Arms** — Auto-generate match arms for a variant type
- **Generate Module Type** — Generate a module type skeleton from a module implementation

### Generate Switch Arms

When your caret is inside a variant type declaration, this action generates a `switch` expression with one arm per constructor. This helps you write exhaustive pattern matches without manually listing every variant.

Place your caret inside the type declaration and press `Cmd+N` (or `Alt+Insert`), then choose **Switch Arms**.

**Before:**

```rescript
type shape =
  | Circle(float)
  | Rectangle(float, float)
  | Triangle(float, float, float)
  | Point
```

**After** (inserted below the type declaration):

```rescript
type shape =
  | Circle(float)
  | Rectangle(float, float)
  | Triangle(float, float, float)
  | Point

switch value {
| Circle(_) => todo
| Rectangle(_) => todo
| Triangle(_) => todo
| Point => todo
}
```

Constructors that carry a payload (e.g., `Circle(float)`) get a `_` wildcard in the generated arm, while constructors without a payload (e.g., `Point`) match directly. Replace `todo` with your actual logic for each arm.

### Generate Module Type

When your caret is inside a module declaration, this action generates a `module type` signature skeleton and inserts it above the module. The generated signature includes all `let`, `type`, `external`, and nested `module` declarations found in the module body.

Place your caret inside the module declaration and press `Cmd+N` (or `Alt+Insert`), then choose **Module Type**.

**Before:**

```rescript
module StringUtils = {
  type config = {verbose: bool}

  let capitalize = (s: string) => {
    // ...
  }

  let truncate = (s: string, maxLen: int) => {
    // ...
  }

  module Internal = {
    let helper = () => ()
  }
}
```

**After** (module type inserted above the module):

```rescript
module type StringUtilsType = {
  type config
  let capitalize: 'a
  let truncate: 'a
  module Internal: {}
}

module StringUtils = {
  type config = {verbose: bool}

  let capitalize = (s: string) => {
    // ...
  }

  let truncate = (s: string, maxLen: int) => {
    // ...
  }

  module Internal = {
    let helper = () => ()
  }
}
```

The generated type uses `'a` as a placeholder for value types -- replace these with the actual type signatures. Type declarations appear without their definition body so you can specify the exposed type shape. Nested modules are listed with an empty `{}` signature for you to fill in.

## Spellchecking

The plugin supports IntelliJ's built-in spellchecker for:

- Comments (line and block)
- String literals
- Identifiers (camelCase splitting)
