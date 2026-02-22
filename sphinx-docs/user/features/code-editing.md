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
| Generate doc comment | Insert a `/** */` documentation comment stub above a declaration |
| Convert pipe to function call | Convert `arr->Array.map(f)` to `Array.map(arr, f)` |
| Convert function call to pipe | Convert `Array.map(arr, f)` to `arr->Array.map(f)` |
| Publish to interface | Add a declaration from `.res` to its `.resi` interface file |
| Unpublish from interface | Remove a declaration from the `.resi` interface file |
| Insert labeled arguments | Insert all labeled arguments for a function call |
| Merge switch cases | Merge switch cases with identical bodies into `\| A \| B => body` |
| Case split | Expand a pattern variable into all constructor cases |
| Convert to labeled arguments | Convert positional arguments to labeled arguments |
| Remove unnecessary parentheses | Remove redundant parentheses around expressions |
| Remove redundant qualifier | Remove unnecessary module path qualifiers |

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

### Generate doc comment

Inserts a `/** ... */` documentation comment stub above the current declaration. For function declarations, `@param` tags are automatically generated for each parameter.

The intention is available on `let`, `type`, `module`, `external`, and `exception` declarations that do not already have a doc comment.

**Before:**

```rescript
let add = (~a: int, ~b: int) => a + b
```

**After** (invoke "Generate doc comment"):

```rescript
/**
 *
 * @param a
 * @param b
 */
let add = (~a: int, ~b: int) => a + b
```

### Pipe ⇔ Function Call Conversion

Convert between pipe-first syntax and regular function call syntax. Place the caret on a pipe expression or function call and press `Alt+Enter`.

**Pipe to function call:**

```rescript
// Before
arr->Array.map(x => x + 1)

// After
Array.map(arr, x => x + 1)
```

**Function call to pipe:**

```rescript
// Before
Array.map(arr, x => x + 1)

// After
arr->Array.map(x => x + 1)
```

### Publish/Unpublish Interface

Control which declarations are exposed in the `.resi` interface file directly from the `.res` implementation.

**Publish to interface:** Place the caret on a `let`, `type`, `module`, or `external` declaration in a `.res` file and press `Alt+Enter`, then choose **Publish to interface**. The declaration signature is appended to the corresponding `.resi` file.

**Unpublish from interface:** Place the caret on a declaration that exists in the `.resi` file and press `Alt+Enter`, then choose **Unpublish from interface**. The matching declaration is removed from the `.resi` file.

:::{note}
These intentions require a corresponding `.resi` file to exist. Use **Create Interface File** to generate one first if needed.
:::

### Insert Labeled Arguments

When calling a function with labeled arguments, place the caret inside the function call parentheses and press `Alt+Enter`, then choose **Insert labeled arguments**. All labeled arguments are inserted as named parameters.

**Before:**

```rescript
makeUser()
```

**After:**

```rescript
makeUser(~name, ~age, ~role)
```

### Merge Switch Cases

When multiple switch cases have identical bodies, place the caret on one of them and press `Alt+Enter`, then choose **Merge switch cases**. The cases are combined into a single arm with multiple patterns.

**Before:**

```rescript
switch status {
| Active => "valid"
| Pending => "valid"
| Inactive => "invalid"
}
```

**After:**

```rescript
switch status {
| Active | Pending => "valid"
| Inactive => "invalid"
}
```

### Case Split

Expand a pattern match variable into all possible constructor cases. Place the caret on a variable pattern in a switch arm and press `Alt+Enter`, then choose **Case split**.

**Before:**

```rescript
switch option {
| x => handle(x)
}
```

**After:**

```rescript
switch option {
| Some(value) => handle(Some(value))
| None => handle(None)
}
```

### Convert to Labeled Arguments

Convert positional function arguments to labeled arguments. Place the caret on a function call and press `Alt+Enter`.

**Before:**

```rescript
makeUser("Alice", 30, "admin")
```

**After:**

```rescript
makeUser(~name="Alice", ~age=30, ~role="admin")
```

### Remove Unnecessary Parentheses

Remove redundant parentheses around expressions. Place the caret on a parenthesized expression and press `Alt+Enter`.

**Before:**

```rescript
let x = (a + b)
```

**After:**

```rescript
let x = a + b
```

### Remove Redundant Qualifier

Remove unnecessary module path qualifiers when the module is already opened. Place the caret on a qualified identifier and press `Alt+Enter`.

**Before:**

```rescript
open Belt.Array
Belt.Array.map(arr, fn)
```

**After:**

```rescript
open Belt.Array
map(arr, fn)
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
- **Generate Make Function** — Generate a constructor function from a record type
- **Generate JSON Encoder/Decoder** — Generate JSON encoder and decoder functions from a type

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

### Generate Make Function

When your caret is inside a record type declaration, this action generates a `make` constructor function with labeled arguments for each record field.

Place your caret inside the type declaration and press `Cmd+N` (or `Alt+Insert`), then choose **Make Function**.

**Before:**

```rescript
type user = {
  name: string,
  age: int,
  email: string,
}
```

**After** (make function inserted below the type declaration):

```rescript
type user = {
  name: string,
  age: int,
  email: string,
}

let make = (~name, ~age, ~email) => {
  name,
  age,
  email,
}
```

Optional fields (e.g., `email?: string`) are generated as optional labeled arguments (`~email=?`).

### Generate JSON Encoder/Decoder

When your caret is inside a record or variant type declaration, this action generates JSON encoder and decoder functions using `@rescript/core`'s `JSON` module (no external dependencies required).

Place your caret inside the type declaration and press `Cmd+N` (or `Alt+Insert`), then choose **JSON Encoder/Decoder**.

**Record type example:**

```rescript
type user = {name: string, age: int, email: option<string>}
```

Generates an encoder that converts each field to the appropriate `JSON.t` constructor (`String`, `Number`, `Boolean`, `Null`, `Object`, `Array`) and a decoder that pattern-matches the JSON structure back into the record type.

**Variant type example (simple enum):**

```rescript
type color = Red | Green | Blue
```

Generates a `String`-based encoder/decoder where each constructor maps to its name as a JSON string.

**Variant type example (tagged union):**

```rescript
type status = Success(string) | Error(int)
```

Generates a tagged union encoder/decoder using `Object` with a `"tag"` field and positional payload fields (`"_0"`, `"_1"`, ...).

**Supported types:** `string`, `int`, `float`, `bool`, `option<T>`, `array<T>`, and arbitrary nesting (e.g., `option<array<string>>`). Unrecognized types generate a `/* TODO */` placeholder for you to fill in.

**Naming convention:** Functions are named `encode` + capitalized type name and `decode` + capitalized type name (e.g., `encodeUser` / `decodeUser`). For a type named `t`, the functions are simply `encode` / `decode`.

## Spellchecking

The plugin supports IntelliJ's built-in spellchecker for:

- Comments (line and block)
- String literals
- Identifiers (camelCase splitting)

## Backspace Handler

When you delete an opening JSX tag with backspace, the matching closing tag is automatically removed.

## Move Element Left/Right

Use `Alt+Shift+Cmd+Left` / `Alt+Shift+Cmd+Right` to swap comma-separated elements. Works with function arguments, array items, record fields, and tuple elements.

## Code Block Selection

Use `Ctrl+Shift+[` and `Ctrl+Shift+]` to navigate to the start and end of the enclosing code block (brace-delimited).

## Split/Join List

Toggle comma-separated lists between single-line and multi-line format. Available via **Edit** > **Split/Join List**.

## Copy/Paste Escaping

When pasting text into a string literal, special characters (backslash, quotes, newlines, tabs) are automatically escaped.

## Unwrap/Remove

Press `Ctrl+Shift+Delete` (`Cmd+Shift+Delete` on macOS) to remove a surrounding wrapper and extract the inner expression.

A popup lists all applicable unwrap options at the cursor position. Select one to unwrap.

### Supported Wrappers

| Wrapper | Before | After |
|---------|--------|-------|
| `Some(...)` | `Some(myValue)` | `myValue` |
| `Ok(...)` | `Ok(result)` | `result` |
| `Error(...)` | `Error(msg)` | `msg` |
| `if (...) { ... }` | `if (cond) { body }` | `body` |
| `switch ... { ... }` | `switch x { \| _ => body }` | `body` |
| `try { ... } catch { ... }` | `try { body } catch { \| e => () }` | `body` |
| `{ ... }` (block) | `{ expr }` | `expr` |

**Example:**

**Before:**

```rescript
let greeting = Some("Hello, world!")
```

**After** (Unwrap `Some(...)`):

```rescript
let greeting = "Hello, world!"
```

## JSX Auto-Close Tag

When you type `>` to close a JSX opening tag, the corresponding closing tag is automatically inserted and the cursor is positioned between the tags.

**Before** (you type `<div>`):

```rescript
<div>
```

**After** (auto-inserted):

```rescript
<div></div>
```

This works for:
- HTML elements: `<div>`, `<span>`, `<input>`
- React components: `<MyComponent>`
- Module-qualified components: `<Module.Component>`

The auto-close does not trigger for self-closing tags (e.g., `<br />`), inside comments, or inside string literals.

## Enter Handler (Comment Continuation)

When you press `Enter` inside a comment, the next line is automatically prefixed with the appropriate comment continuation characters.

### Documentation Comments

Inside a `/** ... */` block, pressing Enter inserts ` * ` at the start of the new line:

```rescript
/** Some documentation|
```

After pressing Enter:

```rescript
/** Some documentation
 * |
```

### Line Comments

Inside a `//` comment, pressing Enter inserts `// ` at the start of the new line:

```rescript
// This is a comment|
```

After pressing Enter:

```rescript
// This is a comment
// |
```

Doc comments (`///`) are also continued:

```rescript
/// A doc comment|
```

After pressing Enter:

```rescript
/// A doc comment
/// |
```

## Smart Join Lines

Press `Ctrl+Shift+J` (`Cmd+Shift+J` on macOS) to join the current line with the next line using ReScript-aware logic.

### Pipe Chain Join

When a line ends with `->` or the next line starts with `->`, the lines are joined without adding a space:

**Before:**

```rescript
value->
  Array.map(x => x + 1)
```

**After:**

```rescript
value->Array.map(x => x + 1)
```

### Let Binding Join

When a line ends with `=`, the lines are joined with a single space after `=`:

**Before:**

```rescript
let result =
  computeValue()
```

**After:**

```rescript
let result = computeValue()
```

### Arrow Function Join

When a line ends with `=>`, the lines are joined with a single space:

**Before:**

```rescript
let fn = (x) =>
  x + 1
```

**After:**

```rescript
let fn = (x) => x + 1
```

For patterns not recognized above, the standard IDE join behavior is used.

## Highlight Related Keywords

When the caret is placed on a control-flow keyword, all related keywords in the same construct are highlighted, showing the structure at a glance.

### Supported Keywords

| Caret On | Highlights |
|----------|-----------|
| `switch` | `switch` and all `\|` arms at the same depth |
| `if` | `if`, `else`, and `else if` in the chain |
| `try` | `try` and `catch` |
| `\|` (pipe arm) | The enclosing `switch` and all sibling `\|` arms |
| `catch` | The enclosing `try` and `catch` |
| `else` | The enclosing `if` chain |

**Example:**

```rescript
// Place caret on "switch": switch and all | are highlighted
switch value {
| Some(x) => handleSome(x)
| None => handleNone()
}
```

## Paste as JSX

When pasting HTML content into a ReScript file, it is automatically converted to JSX syntax:
- HTML attributes (`class`, `for`, `onclick`) are renamed to JSX equivalents (`className`, `htmlFor`, `onClick`)
- Void elements (`<br>`, `<img>`, `<input>`) are self-closed (`<br />`)
- Inline `style` strings are converted to ReScript style objects
- Boolean attributes (e.g., `disabled`, `checked`) are preserved as JSX boolean props
- `data-*` and `aria-*` attributes are preserved as-is
