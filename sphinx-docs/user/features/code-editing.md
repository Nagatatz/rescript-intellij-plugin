---
myst:
  html_meta:
    "keywords": "code editing, formatting, intentions, surround, refactoring, extract, inline"
---

# Code Editing

The plugin provides a rich set of editing features that make writing ReScript code more productive.

## Code Folding

{bdg-success}`Native`

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

Code folding lets you hide implementation details and focus on the declarations that matter, making large files navigable without splitting them into smaller ones.

## Code Formatting

{bdg-primary}`LSP Required`

Press `Ctrl+Alt+L` (`Cmd+Option+L` on macOS) to format the current file using the `rescript format` CLI.

### How It Works

The plugin pipes the current document content to `rescript format --stdin .<ext>` via stdin and replaces the document text with the formatted output. Both `.res` and `.resi` files are supported. Formatting runs asynchronously via IntelliJ's `AsyncDocumentFormattingService`, so the editor remains responsive during the operation.

### Requirements

The ReScript compiler (`rescript`) must be installed in your project's `node_modules`. The plugin searches for the CLI starting from the file's directory up to the project root. If the CLI is not found, a notification is displayed.

### Error Handling

- **CLI not found** --- A notification indicates that `rescript` was not found in `node_modules`
- **Timeout** --- If formatting takes longer than 10 seconds, the process is terminated and an error notification is shown
- **Formatter errors** --- If the formatter exits with a non-zero code (e.g., syntax errors in the file), the stderr output is displayed as a notification

### Format on Save

To automatically format ReScript files when saving, enable **Reformat code** in **Settings** > **Tools** > **Actions on Save**.

:::{seealso}
[Code Analysis --- Format Check](code-analysis.md#format-check) can highlight unformatted files and offer a quick-fix to format them.
:::

One shortcut formats your entire file to match the official ReScript style, eliminating manual whitespace adjustments and ensuring consistent code style across your team.

## Brace Matching

{bdg-success}`Native`

The editor highlights matching brace pairs:
- `{` and `}`
- `[` and `]`
- `(` and `)`

Brace matching helps you instantly see where a block opens and closes, preventing mismatched delimiters in deeply nested code.

## Smart Quotes

{bdg-success}`Native`

When you type `"`, `'`, or `` ` ``, the closing quote is automatically inserted.

Auto-paired quotes save keystrokes and prevent common syntax errors from unmatched string delimiters.

## Smart Enter

{bdg-success}`Native`

Press `Shift+Enter` to intelligently complete the current statement and start a new line:

- Closes unclosed parentheses and braces
- Adds missing `switch` body braces
- Moves the cursor to the right position

Smart Enter intelligently closes open constructs and positions your cursor, so you can keep typing the next statement without manually balancing delimiters.

## Comment Toggle

{bdg-success}`Native`

- `Ctrl+/` — Toggle line comment (`//`)
- `Ctrl+Shift+/` — Toggle block comment (`/* */`)

Quickly toggle comments on and off for debugging or temporarily disabling code, without manually typing or removing comment delimiters.

## Statement Mover

{bdg-success}`Native`

Move top-level declarations up or down:

- `Alt+Shift+Up` — Move declaration up
- `Alt+Shift+Down` — Move declaration down

This moves the entire declaration (including decorators) as a unit.

Reorder declarations without cut-and-paste — the mover handles the entire declaration block including decorators, so you never accidentally leave behind an annotation or break a multi-line definition.

## Intention Actions

{bdg-success}`Native`

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
| Convert filter+map to filterMap | Convert `->Array.filter(f)->Array.map(g)` to `->Array.filterMap(...)` |
| Add type annotation | Add explicit type annotation to a `let` binding using LSP hover info |
| Add `->ignore` | Append `->ignore` to discard the expression's return value |
| Add `_` prefix | Add underscore prefix to suppress unused variable warnings |
| Remove redundant braces | Remove unnecessary `{ }` around single expressions |
| Fix identifier case | Correct identifier casing (e.g., lowercase for values, uppercase for modules) |
| Expand destructuring | Expand `let {a, b} = x` into individual `let` bindings |
| Convert call to uncurried form | Rewrite `f(x)` to `f(. x)` when `f` is defined with the uncurried syntax `let f = (. x) => ...` |
| Extract module to file | Move a top-level `module M = { ... }` declaration into a sibling `M.res` file |
| Rename variant constructor (project-wide, no LSP) | Rename a variant constructor across every `.res` / `.resi` file in the project using a token-shape classifier; works without the language server |

Intention actions turn common code transformations into one-click operations — instead of manually restructuring code, press `Alt+Enter` and let the IDE handle the mechanical changes while you focus on the logic.

:::{seealso}
[Code Analysis](code-analysis.md) provides inspections that detect issues automatically, with quick fixes for one-click resolution.
:::

### Wrap with Some(...)

Wraps a selected expression in `Some()`, useful when you need to convert a plain value into an `option` type.

Select the expression, press `Alt+Enter`, and choose **Wrap with Some(...)**.

Select `name`, then:

::::{tab-set}
:::{tab-item} Before
```rescript
let name = "Alice"
let greeting = getGreeting(name)
```
:::
:::{tab-item} After
```rescript
let name = "Alice"
let greeting = getGreeting(Some(name))
```
:::
::::

### Wrap with Ok(...)

Wraps a selected expression in `Ok()`, useful when returning a success value from a function that uses the `result` type.

Select `value`, then:

::::{tab-set}
:::{tab-item} Before
```rescript
let value = computeResult()
let response = processResponse(value)
```
:::
:::{tab-item} After
```rescript
let value = computeResult()
let response = processResponse(Ok(value))
```
:::
::::

### Wrap with Error(...)

Wraps a selected expression in `Error()`, useful when returning an error value from a function that uses the `result` type.

Select `message` on the second line, then:

::::{tab-set}
:::{tab-item} Before
```rescript
let message = "Something went wrong"
let result = message
```
:::
:::{tab-item} After
```rescript
let message = "Something went wrong"
let result = Error(message)
```
:::
::::

### Add @genType

Adds the `@genType` annotation above a `let`, `type`, or `module` declaration. This annotation tells the ReScript compiler to generate TypeScript type definitions for the declaration, enabling seamless interop with TypeScript code.

The intention is only available on declarations that do not already have a `@genType` annotation.

::::{tab-set}
:::{tab-item} Before
```rescript
let greet = (name: string) => `Hello, ${name}!`
```
:::
:::{tab-item} After
```rescript
@genType
let greet = (name: string) => `Hello, ${name}!`
```
:::
::::

This also works on type and module declarations:

::::{tab-set}
:::{tab-item} Before
```rescript
type user = {
  name: string,
  age: int,
}
```
:::
:::{tab-item} After
```rescript
@genType
type user = {
  name: string,
  age: int,
}
```
:::
::::

### Generate doc comment

Inserts a `/** ... */` documentation comment stub above the current declaration. For function declarations, `@param` tags are automatically generated for each parameter.

The intention is available on `let`, `type`, `module`, `external`, and `exception` declarations that do not already have a doc comment.

::::{tab-set}
:::{tab-item} Before
```rescript
let add = (~a: int, ~b: int) => a + b
```
:::
:::{tab-item} After
```rescript
/**
 *
 * @param a
 * @param b
 */
let add = (~a: int, ~b: int) => a + b
```
:::
::::

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

::::{tab-set}
:::{tab-item} Before
```rescript
makeUser()
```
:::
:::{tab-item} After
```rescript
makeUser(~name, ~age, ~role)
```
:::
::::

### Merge Switch Cases

When multiple switch cases have identical bodies, place the caret on one of them and press `Alt+Enter`, then choose **Merge switch cases**. The cases are combined into a single arm with multiple patterns.

::::{tab-set}
:::{tab-item} Before
```rescript
switch status {
| Active => "valid"
| Pending => "valid"
| Inactive => "invalid"
}
```
:::
:::{tab-item} After
```rescript
switch status {
| Active | Pending => "valid"
| Inactive => "invalid"
}
```
:::
::::

### Case Split

Expand a pattern match variable into all possible constructor cases. Place the caret on a variable pattern in a switch arm and press `Alt+Enter`, then choose **Case split**.

::::{tab-set}
:::{tab-item} Before
```rescript
switch option {
| x => handle(x)
}
```
:::
:::{tab-item} After
```rescript
switch option {
| Some(value) => handle(Some(value))
| None => handle(None)
}
```
:::
::::

### Add Missing Switch Arms

Fill in the variant constructors that are not yet covered by an existing `switch`. Place the caret on the `switch` keyword (or anywhere inside the scrutinee) of a partial switch and press `Alt+Enter`, then choose **Add missing switch arms**. The intention queries LSP hover for the scrutinee's type, computes the set difference between the variant's constructors and the arms already present, and inserts a skeleton arm for each missing constructor just before the closing `}`. The intention does not appear when the switch already contains a wildcard `_` arm or a bare lowercase binding pattern, because such arms make the switch exhaustive. Or-patterns (`| Foo | Bar => ...`) count both constructors as covered. In nested switches the innermost one containing the caret is targeted.

When the LSP hover returns just a bare type name (`color`, `status`) instead of an inline `| Red | Blue` body — which is what the ReScript LSP normally does for user-defined variants — the intention falls back to a project-wide stub-index lookup. The `RescriptNameIndex` is queried for a `type <name> = ...` declaration matching the hovered identifier, the right-hand side is re-parsed with the standard type declaration parser, and the resulting constructors feed back into the missing-arms set difference. The fallback runs silently and only when needed, so the intention now succeeds for the common case of "the variant is defined elsewhere in the project".

::::{tab-set}
:::{tab-item} Before
```rescript
let describe = (x: option<int>) =>
  switch x {
  | Some(v) => Belt.Int.toString(v)
  }
```
:::
:::{tab-item} After
```rescript
let describe = (x: option<int>) =>
  switch x {
  | Some(v) => Belt.Int.toString(v)
  | None => todo
  }
```
:::
::::

### Convert to Labeled Arguments

Convert positional function arguments to labeled arguments. Place the caret on a function call and press `Alt+Enter`.

::::{tab-set}
:::{tab-item} Before
```rescript
makeUser("Alice", 30, "admin")
```
:::
:::{tab-item} After
```rescript
makeUser(~name="Alice", ~age=30, ~role="admin")
```
:::
::::

### Remove Unnecessary Parentheses

Remove redundant parentheses around expressions. Place the caret on a parenthesized expression and press `Alt+Enter`.

::::{tab-set}
:::{tab-item} Before
```rescript
let x = (a + b)
```
:::
:::{tab-item} After
```rescript
let x = a + b
```
:::
::::

### Remove Redundant Qualifier

Remove unnecessary module path qualifiers when the module is already opened. Place the caret on a qualified identifier and press `Alt+Enter`.

::::{tab-set}
:::{tab-item} Before
```rescript
open Belt.Array
Belt.Array.map(arr, fn)
```
:::
:::{tab-item} After
```rescript
open Belt.Array
map(arr, fn)
```
:::
::::

### Convert filter+map to filterMap

Convert a `->Array.filter(f)->Array.map(g)` chain into a single `->Array.filterMap(...)` call. Place the caret on the filter or map call and press `Alt+Enter`.

::::{tab-set}
:::{tab-item} Before
```rescript
items
->Array.filter(x => x > 0)
->Array.map(x => x * 2)
```
:::
:::{tab-item} After
```rescript
items->Array.filterMap(x => {
  if x > 0 {
    Some(x * 2)
  } else {
    None
  }
})
```
:::
::::

### Add Type Annotation

Add an explicit type annotation to a `let` binding using type information from the LSP hover. Place the caret on a `let` binding and press `Alt+Enter`.

::::{tab-set}
:::{tab-item} Before
```rescript
let name = "Alice"
```
:::
:::{tab-item} After
```rescript
let name: string = "Alice"
```
:::
::::

### Add ->ignore

Append `->ignore` to an expression whose return value is unused, suppressing the compiler warning about discarded values. Place the caret on the expression and press `Alt+Enter`.

::::{tab-set}
:::{tab-item} Before
```rescript
Js.log("debug message")
Array.push(items, newItem)
```
:::
:::{tab-item} After
```rescript
Js.log("debug message")->ignore
Array.push(items, newItem)->ignore
```
:::
::::

### Add _ Prefix

Add an underscore prefix to a variable name to indicate it is intentionally unused. This suppresses the compiler's unused variable warning. Place the caret on an unused variable and press `Alt+Enter`.

::::{tab-set}
:::{tab-item} Before
```rescript
let result = someComputation()
```
:::
:::{tab-item} After
```rescript
let _result = someComputation()
```
:::
::::

### Remove Redundant Braces

Remove unnecessary `{ }` around a single expression. Place the caret on the braces and press `Alt+Enter`.

::::{tab-set}
:::{tab-item} Before
```rescript
let greet = (name) => {
  "Hello, " ++ name
}
```
:::
:::{tab-item} After
```rescript
let greet = (name) => "Hello, " ++ name
```
:::
::::

### Fix Identifier Case

Correct identifier casing to follow ReScript conventions: values and functions should start with a lowercase letter, modules and variant constructors should start with an uppercase letter. Place the caret on a misnamed identifier and press `Alt+Enter`.

::::{tab-set}
:::{tab-item} Before
```rescript
let MyValue = 42
```
:::
:::{tab-item} After
```rescript
let myValue = 42
```
:::
::::

### Expand Destructuring

Expand a destructured `let` binding into individual `let` bindings for each field. Place the caret on a destructuring pattern and press `Alt+Enter`.

::::{tab-set}
:::{tab-item} Before
```rescript
let {name, age, email} = user
```
:::
:::{tab-item} After
```rescript
let name = user.name
let age = user.age
let email = user.email
```
:::
::::

### Convert Call to Uncurried Form

Rewrites a curried call site to the uncurried form `f(. x)` when the target function is defined with the legacy uncurried syntax `let f = (. x) => ...`. Provided as a native fallback for the LSP `applyUncurried` quick fix on ReScript v10 / v11 codebases.

Place the caret on the call's identifier and press `Alt+Enter`, then choose **Convert call to uncurried form**.

::::{tab-set}
:::{tab-item} Before
```rescript
let add = (. x, y) => x + y
let result = add(1, 2)
```
:::
:::{tab-item} After
```rescript
let add = (. x, y) => x + y
let result = add(. 1, 2)
```
:::
::::

The intention only inspects the first matching definition found in the stub index. The zero-argument form `f()` is rewritten to `f(.)`.

### Extract Module to File

Moves a top-level `module M = { ... }` declaration out of the current file and into a sibling `M.res` file. Provided as a native fallback for the LSP `extractLocalModuleToFile` code action when the language server's `WorkspaceEdit.CreateFile` operation is not honoured by the IDE.

Place the caret on the module declaration's name and press `Alt+Enter`, then choose **Extract module to file**.

::::{tab-set}
:::{tab-item} Before (`Foo.res`)
```rescript
module Inner = {
  let value = 42
  let double = x => x * 2
}

let total = Inner.double(Inner.value)
```
:::
:::{tab-item} After

`Foo.res`:

```rescript
let total = Inner.double(Inner.value)
```

`Inner.res` (newly created):

```rescript
let value = 42
let double = x => x * 2
```
:::
::::

The intention does not rewrite references to the extracted module elsewhere in the source file. When such references are detected, a warning balloon appears so you can adjust the call sites manually (e.g., add `open Inner` or update qualified paths). The intention is hidden when a sibling `Inner.res` file already exists.

## Surround With

{bdg-success}`Native`

Select code and press `Ctrl+Alt+T` to surround it with:

- `if` expression
- `switch` expression
- `try` / `catch` block
- Block scope `{ ... }`
- JSX element `<div>...</div>`
- JSX fragment `<>...</>`

### if expression

Wraps the selected code inside an `if` block. The cursor is placed on the `condition` placeholder so you can immediately type the condition.

Select `Js.log("hello")`, then:

::::{tab-set}
:::{tab-item} Before
```rescript
Js.log("hello")
```
:::
:::{tab-item} After
```rescript
if (condition) {
  Js.log("hello")
}
```
:::
::::

### switch expression

Wraps the selected code inside a `switch` expression with a default `_` arm. The cursor is placed on the `expr` placeholder so you can type the expression to match on.

Select `defaultHandler()`, then:

::::{tab-set}
:::{tab-item} Before
```rescript
defaultHandler()
```
:::
:::{tab-item} After
```rescript
switch expr {
| _ => defaultHandler()
}
```
:::
::::

### try / catch block

Wraps the selected code in a `try` / `catch` block. The cursor is placed on the `()` placeholder in the catch arm so you can define the error handling logic.

Select `parseJson(input)`, then:

::::{tab-set}
:::{tab-item} Before
```rescript
parseJson(input)
```
:::
:::{tab-item} After
```rescript
try {
  parseJson(input)
} catch {
| exn => ()
}
```
:::
::::

### Block scope { ... }

Wraps the selected code in a block scope. This is useful for limiting variable scope or grouping expressions.

Select the two lines, then:

::::{tab-set}
:::{tab-item} Before
```rescript
let temp = calculate()
process(temp)
```
:::
:::{tab-item} After
```rescript
{
  let temp = calculate()
  process(temp)
}
```
:::
::::

### JSX element <div>...</div>

Wraps the selected markup in a `<div>` element. The cursor selects the opening tag name `div` so you can immediately rename it to the desired element.

Select `<Greeting name />`, then:

::::{tab-set}
:::{tab-item} Before
```rescript
<Greeting name />
```
:::
:::{tab-item} After
```rescript
<div>
  <Greeting name />
</div>
```
:::
::::

### JSX fragment <>...</>

Wraps the selected markup in a JSX fragment `<>...</>`, which groups sibling elements without introducing an extra DOM node.

Select the two lines, then:

::::{tab-set}
:::{tab-item} Before
```rescript
<Header />
<Body />
```
:::
:::{tab-item} After
```rescript
<>
  <Header />
<Body />
</>
```
:::
::::

Surround With wraps selected code in a control structure with one shortcut, saving you from manually typing the surrounding syntax and re-indenting the enclosed code.

:::{seealso}
[Code Completion](code-completion.md) offers postfix templates (`.switch`, `.pipe`) and live templates for similar code generation patterns.
:::

## Import Optimization

{bdg-success}`Native`

Press `Ctrl+Alt+O` to optimize imports in the current file.

### How It Works

Import optimization runs in two phases:

1. **Duplicate detection** --- Scans top-level `open` statements and removes any that share the same module path as an earlier `open`
2. **Unused open detection** --- Uses LSP diagnostic warnings to identify `open` statements whose exports are never referenced in the file

Both phases run together, and a notification summarizes the result (e.g., "Removed 1 duplicate and 2 unused open statement(s)").

### Configuration

Unused open removal can be toggled in **Settings** > **Languages & Frameworks** > **ReScript** via the **Remove unused opens** checkbox. When disabled, only duplicate opens are removed.

### Example

::::{tab-set}
:::{tab-item} Before
```rescript
open Belt
open Belt.Array
open Belt          // duplicate
open Js.Promise    // unused
```
:::
:::{tab-item} After Ctrl+Alt+O
```rescript
open Belt
open Belt.Array
```
:::
::::

A single shortcut cleans up all redundant and unused `open` statements, keeping your imports tidy without manually scanning the file.

## Rename

{bdg-primary}`LSP Required`

Press `Shift+F6` to rename a symbol across the project. The Language Server handles finding all references and updating them.

Rename refactoring updates every reference across the entire project in one operation, eliminating the risk of missed or inconsistent renames that manual find-and-replace would leave behind.

## Extract Variable

{bdg-success}`Native`

Press `Ctrl+Alt+V` (`Cmd+Alt+V` on macOS) to extract the selected expression into a `let` binding.

The handler detects the selected expression within a `let` declaration body, creates a new `let` binding above the current statement, and replaces the original expression with a reference to the new variable.

Select `a + b`, then:

::::{tab-set}
:::{tab-item} Before
```rescript
let calculate = (a, b) => {
  let result = a + b * 2
  result
}
```
:::
:::{tab-item} After
```rescript
let calculate = (a, b) => {
  let sum = a + b
  let result = sum * 2
  result
}
```
:::
::::

Extract Variable lets you name intermediate expressions for clarity, making complex expressions self-documenting without manually restructuring the surrounding code.

## Extract Function

{bdg-success}`Native`

Press `Ctrl+Alt+M` (`Cmd+Alt+M` on macOS) to extract the selected code into a new function.

The handler detects the selected expression or statement range in a `let` declaration body, extracts it into a new `let` function above the current declaration, and replaces the original code with a call to the new function. Free variables in the selection become parameters of the extracted function.

Select `a + b`, then:

::::{tab-set}
:::{tab-item} Before
```rescript
let calculate = (a, b) => {
  let result = a + b
  result * 2
}
```
:::
:::{tab-item} After
```rescript
let extracted = (a, b) => a + b

let calculate = (a, b) => {
  let result = extracted(a, b)
  result * 2
}
```
:::
::::

Extract Function automatically detects free variables and turns them into parameters, so you can decompose large functions into smaller, reusable pieces without manually threading values through.

## Inline Variable/Function

{bdg-success}`Native`

Press `Ctrl+Alt+N` (`Cmd+Alt+N` on macOS) to inline a variable or function at the caret, replacing all references with the definition body.

The handler finds the `let` declaration at the caret, locates all references to it within the file, replaces each reference with the definition body, and removes the original declaration.

::::{tab-set}
:::{tab-item} Before
```rescript
let prefix = "Hello"
let greet = (name) => `${prefix}, ${name}!`
```
:::
:::{tab-item} After (inline `prefix`)
```rescript
let greet = (name) => `${"Hello"}, ${name}!`
```
:::
::::

Inline refactoring is the reverse of extraction — it replaces an unnecessary intermediate variable with its definition, simplifying code when a named binding adds no clarity.

## Introduce Constant

{bdg-success}`Native`

Extract a literal value into a module-level constant. Select a string, number, or other literal and use **Refactor** > **Introduce Constant**.

The handler extracts the selected literal into a new `let` binding at the top of the file (after any `open` statements) and replaces the original literal with a reference to the new constant.

Introduce constant for `"Hello, World!"`:

::::{tab-set}
:::{tab-item} Before
```rescript
let greeting = "Hello, World!"
let farewell = "Goodbye, World!"
```
:::
:::{tab-item} After
```rescript
let helloWorld = "Hello, World!"
let greeting = helloWorld
let farewell = "Goodbye, World!"
```
:::
::::

Introduce Constant extracts magic values into named bindings at the module level, making their intent explicit and enabling reuse across the file.

## Change Signature

{bdg-success}`Native`

Press `Ctrl+F6` to modify a function's parameters — reorder, rename, add, or remove — and update call sites within the file.

The handler parses the function declaration at the caret, supports both labeled (`~name: type`) and positional parameters, and applies changes to the declaration and all matching call sites.

Reorder parameters:

::::{tab-set}
:::{tab-item} Before
```rescript
let make = (~name: string, ~age: int) => { name, age }

let user = make(~name="Alice", ~age=30)
```
:::
:::{tab-item} After
```rescript
let make = (~age: int, ~name: string) => { name, age }

let user = make(~age=30, ~name="Alice")
```
:::
::::

Change Signature lets you restructure a function's parameter list and automatically updates all call sites, making API evolution safe and mechanical rather than error-prone manual editing.

## React Component Extraction

{bdg-success}`Native`

Extract selected JSX into a new React component. Select a JSX expression, then use **Refactor** > **Extract React Component**.

The handler creates a new `@react.component` module with the selected JSX as its body. Props used within the selection are detected and added as labeled parameters to the new component's `make` function.

Select `<div className="card">...</div>`, then:

::::{tab-set}
:::{tab-item} Before
```rescript
@react.component
let make = (~items) => {
  <div className="container">
    <div className="card">
      {items->Array.map(item => <span key={item}> {React.string(item)} </span>)->React.array}
    </div>
  </div>
}
```
:::
:::{tab-item} After
```rescript
module Card = {
  @react.component
  let make = (~items) => {
    <div className="card">
      {items->Array.map(item => <span key={item}> {React.string(item)} </span>)->React.array}
    </div>
  }
}

@react.component
let make = (~items) => {
  <div className="container">
    <Card items />
  </div>
}
```
:::
::::

Extracting a React component from JSX is a common refactoring in React development — this automation detects the required props and creates a properly structured module, saving you from manually copying code and threading props.

## Paste as JSON.t

{bdg-success}`Native`

Use **Edit** > **Paste as JSON.t** to convert JSON from your clipboard into a ReScript `JSON.t` value.

Instead of manually wrapping each JSON value in ReScript constructors, paste the raw JSON and get a correctly typed `JSON.t` expression instantly.

## Code Generation

{bdg-success}`Native`

Press `Cmd+N` (or `Alt+Insert`) to open the Generate menu:

- **Generate Switch Arms** — Auto-generate match arms for a variant type
- **Generate Module Type** — Generate a module type skeleton from a module implementation
- **Generate Make Function** — Generate a constructor function from a record type
- **Generate JSON Encoder/Decoder** — Generate JSON encoder and decoder functions from a type
- **Generate Record Value** — Generate a record value with default values for all fields

### Generate Switch Arms

When your caret is inside a variant type declaration, this action generates a `switch` expression with one arm per constructor. This helps you write exhaustive pattern matches without manually listing every variant.

Place your caret inside the type declaration and press `Cmd+N` (or `Alt+Insert`), then choose **Switch Arms**.

::::{tab-set}
:::{tab-item} Before
```rescript
type shape =
  | Circle(float)
  | Rectangle(float, float)
  | Triangle(float, float, float)
  | Point
```
:::
:::{tab-item} After (inserted below the type declaration)
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
:::
::::

Constructors that carry a payload (e.g., `Circle(float)`) get a `_` wildcard in the generated arm, while constructors without a payload (e.g., `Point`) match directly. Replace `todo` with your actual logic for each arm.

### Generate Module Type

When your caret is inside a module declaration, this action generates a `module type` signature skeleton and inserts it above the module. The generated signature includes all `let`, `type`, `external`, and nested `module` declarations found in the module body.

Place your caret inside the module declaration and press `Cmd+N` (or `Alt+Insert`), then choose **Module Type**.

::::{tab-set}
:::{tab-item} Before
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
:::
:::{tab-item} After (module type inserted above)
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
:::
::::

The generated type uses `'a` as a placeholder for value types -- replace these with the actual type signatures. Type declarations appear without their definition body so you can specify the exposed type shape. Nested modules are listed with an empty `{}` signature for you to fill in.

### Generate Make Function

When your caret is inside a record type declaration, this action generates a `make` constructor function with labeled arguments for each record field.

Place your caret inside the type declaration and press `Cmd+N` (or `Alt+Insert`), then choose **Make Function**.

::::{tab-set}
:::{tab-item} Before
```rescript
type user = {
  name: string,
  age: int,
  email: string,
}
```
:::
:::{tab-item} After (make function inserted below)
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
:::
::::

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

### Generate Record Value

When your caret is inside a record type declaration, this action generates a record value with default values for all fields. This is useful for quickly creating an initial value or test fixture.

Place your caret inside the type declaration and press `Cmd+N` (or `Alt+Insert`), then choose **Record Value**.

::::{tab-set}
:::{tab-item} Before
```rescript
type user = {
  name: string,
  age: int,
  active: bool,
  email: option<string>,
}
```
:::
:::{tab-item} After (record value inserted below)
```rescript
type user = {
  name: string,
  age: int,
  active: bool,
  email: option<string>,
}

let value: user = {
  name: "",
  age: 0,
  active: false,
  email: None,
}
```
:::
::::

Default values are inferred from field types: `""` for `string`, `0` for `int`, `0.0` for `float`, `false` for `bool`, `None` for `option<T>`, and `[]` for `array<T>`.

### Generate Module Type Implementation

When your caret is inside a `module type` declaration, this action generates a module that implements the module type with stub functions for each signature entry.

Place your caret inside the module type declaration and press `Cmd+N` (or `Alt+Insert`), then choose **Module Type Implementation**.

::::{tab-set}
:::{tab-item} Before
```rescript
module type Printable = {
  type t
  let toString: t => string
  let print: t => unit
}
```
:::
:::{tab-item} After (implementation generated below)
```rescript
module type Printable = {
  type t
  let toString: t => string
  let print: t => unit
}

module PrintableImpl: Printable = {
  type t
  let toString = (_) => todo
  let print = (_) => todo
}
```
:::
::::

Replace `todo` placeholders with actual implementations. The generated module is constrained to the module type, so the compiler will catch any missing or incorrect signatures.

Code generation turns type definitions into working code — switch arms, constructors, JSON codecs, and module skeletons are created automatically from your types, eliminating tedious and error-prone boilerplate writing.

## Strip Trailing Spaces

{bdg-success}`Native`

The plugin protects whitespace inside string literals when IntelliJ removes trailing spaces on save. Without this, intentional whitespace at the end of a string line would be stripped.

**Protected token types:**
- `STRING_VALUE` — regular string content
- `JS_STRING_OPEN` / `JS_STRING_CLOSE` — JavaScript template string boundaries

Configure trailing space behavior in **Settings** > **Editor** > **General** > **On Save**.

This protection ensures that intentional whitespace inside string literals is never silently removed by the editor's trailing space cleanup, preventing subtle bugs in string-heavy code.

## Editor Floating Toolbar

{bdg-success}`Native`

A floating toolbar appears when editing ReScript files, providing quick access to commonly used actions:

- **Format** — Format the current file (`Cmd+Option+L`)
- **Open Compiled JS** — Open the compiled JavaScript output
- **Create Interface** — Generate a `.resi` interface file

The toolbar auto-shows when you start editing a ReScript file and auto-hides when not in use.

The floating toolbar puts the most common ReScript-specific actions — format, view compiled JS, create interface — one click away, without memorizing keyboard shortcuts.

## Spellchecking

{bdg-success}`Native`

The plugin supports IntelliJ's built-in spellchecker for:

- Comments (line and block)
- String literals
- Identifiers (camelCase splitting)

Built-in spellchecking catches typos in comments, strings, and identifiers as you type, helping you maintain professional code quality without a separate review step.

## Backspace Handler

{bdg-success}`Native`

When you delete an opening JSX tag with backspace, the matching closing tag is automatically removed.

Paired tag deletion prevents orphaned closing tags — when you backspace through an opening JSX tag, the closing tag disappears automatically, keeping your markup balanced.

## Move Element Left/Right

{bdg-success}`Native`

Use `Alt+Shift+Cmd+Left` / `Alt+Shift+Cmd+Right` to swap comma-separated elements. Works with function arguments, array items, record fields, and tuple elements.

Reorder arguments, array items, or record fields with a keyboard shortcut instead of manually cutting and pasting, keeping commas and formatting correct automatically.

## Code Block Selection

{bdg-success}`Native`

Use `Ctrl+Shift+[` and `Ctrl+Shift+]` to navigate to the start and end of the enclosing code block (brace-delimited).

Quickly jump to block boundaries when working inside large function bodies or deeply nested structures, without scrolling or searching for the matching brace.

## Split/Join List

{bdg-success}`Native`

Toggle comma-separated lists between single-line and multi-line format. Available via **Edit** > **Split/Join List**.

Switching between compact and expanded list formats is a single action, so you can adjust code density for readability without manually adding or removing line breaks and trailing commas.

## Copy/Paste Escaping

{bdg-success}`Native`

When pasting text into a string literal, special characters (backslash, quotes, newlines, tabs) are automatically escaped.

Automatic escaping means you can paste raw text into a string literal and trust that special characters are handled correctly, avoiding hard-to-spot syntax errors from unescaped quotes or backslashes.

## Unwrap/Remove

{bdg-success}`Native`

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

::::{tab-set}
:::{tab-item} Before
```rescript
let greeting = Some("Hello, world!")
```
:::
:::{tab-item} After (Unwrap `Some(...)`)
```rescript
let greeting = "Hello, world!"
```
:::
::::

Unwrap/Remove is the inverse of Surround With — it strips away a wrapper like `Some()`, `if`, or `try` in one action, simplifying expressions without manually deleting delimiters and re-indenting.

## JSX Auto-Close Tag

{bdg-success}`Native`

When you type `>` to close a JSX opening tag, the corresponding closing tag is automatically inserted and the cursor is positioned between the tags.

Type `<div>`:

::::{tab-set}
:::{tab-item} Before
```rescript
<div>
```
:::
:::{tab-item} After (auto-inserted)
```rescript
<div></div>
```
:::
::::

This works for:
- HTML elements: `<div>`, `<span>`, `<input>`
- React components: `<MyComponent>`
- Module-qualified components: `<Module.Component>`

The auto-close does not trigger for self-closing tags (e.g., `<br />`), inside comments, or inside string literals.

Auto-closing tags saves keystrokes and prevents mismatched JSX tags — type the opening tag and the closing tag appears automatically, keeping your markup balanced as you write.

## Enter Handler (Comment Continuation)

{bdg-success}`Native`

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

Comment continuation keeps your multi-line comments properly formatted without manually typing prefixes on each new line, maintaining a clean comment style effortlessly.

## Smart Join Lines

{bdg-success}`Native`

Press `Ctrl+Shift+J` (`Cmd+Shift+J` on macOS) to join the current line with the next line using ReScript-aware logic.

### Pipe Chain Join

When a line ends with `->` or the next line starts with `->`, the lines are joined without adding a space:

::::{tab-set}
:::{tab-item} Before
```rescript
value->
  Array.map(x => x + 1)
```
:::
:::{tab-item} After
```rescript
value->Array.map(x => x + 1)
```
:::
::::

### Let Binding Join

When a line ends with `=`, the lines are joined with a single space after `=`:

::::{tab-set}
:::{tab-item} Before
```rescript
let result =
  computeValue()
```
:::
:::{tab-item} After
```rescript
let result = computeValue()
```
:::
::::

### Arrow Function Join

When a line ends with `=>`, the lines are joined with a single space:

::::{tab-set}
:::{tab-item} Before
```rescript
let fn = (x) =>
  x + 1
```
:::
:::{tab-item} After
```rescript
let fn = (x) => x + 1
```
:::
::::

For patterns not recognized above, the standard IDE join behavior is used.

Smart join understands ReScript syntax — pipe chains join without spaces, let bindings preserve their `=`, and arrow functions stay readable, producing cleaner results than generic line joining.

## Rename Variant Constructor (no LSP)

{bdg-success}`Native`

The standard `Shift+F6` rename routes through `textDocument/rename` on the language server, which gives the most accurate cross-file rewrite. When the language server isn't running — `node` not on `PATH`, `@rescript/language-server` not installed, network-restricted environments — that path is unavailable. This Alt+Enter intention provides a token-shape fallback that works everywhere.

**Trigger:** place the caret on a UIDENT used as a variant constructor (an arm pattern like `| Foo(_) =>`, a `type` declaration arm `| Foo`, or a constructor invocation `Foo(x)`), press `Alt+Enter`, and pick **Rename variant constructor (project-wide, no LSP)**.

### How It Works

`RescriptConstructorOccurrenceClassifier` decides what each UIDENT in the project represents — `CONSTRUCTOR`, `PATTERN`, `MODULE_QUALIFIED_TAIL`, or `OTHER` — by looking at the previous and next non-trivia tokens:

| Surrounding tokens | Verdict |
|--------------------|---------|
| `\| Foo` / `\| Foo(_)` | PATTERN (switch arm or `type` arm) |
| `Module.Foo` | MODULE_QUALIFIED_TAIL (only the `Foo` half is renamed) |
| `Foo(x)` | CONSTRUCTOR (constructor invocation) |
| `let x = Foo`, `[Foo, …]`, `f(Foo)` | CONSTRUCTOR (zero-arity in expression position) |
| `<Foo />`, `: Foo`, `module Foo = …` | OTHER (skipped) |
| Tokens inside string literals or comments | OTHER (lexer handles this) |

`RescriptConstructorOccurrenceFinder` then runs `PsiSearchHelper.processElementsWithWord` over `.res` and `.resi` files in the project, classifies each hit, and keeps the CONSTRUCTOR / PATTERN / MODULE_QUALIFIED_TAIL ones. Up to 500 occurrences are collected; beyond that the intention surfaces a "use Shift+F6 (LSP rename) or narrow the search" error so you don't accidentally produce an enormous diff.

The actual rewrite happens in a single `WriteCommandAction`, so the entire project-wide change Undoes as one step.

### Limitations

- The classifier is purely syntactic. Two different variants with the same constructor name (e.g. `type a = | Foo` and `type b = | Foo`) are renamed together — use `Shift+F6` (LSP rename) when you need disambiguation by type.
- Polymorphic variants (`#foo`) are not in scope for this intention.
- Module-qualified call sites are renamed by their tail only; the leading `Module.` segment is never touched.

## Highlight Related Keywords

{bdg-success}`Native`

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

Keyword highlighting reveals the full structure of a control-flow construct at a glance — place your caret on `switch` and see all its arms, or on `if` and see the entire `if`/`else` chain.

## Highlight Paired JSX Tags

{bdg-success}`Native`

When the caret is placed on a JSX tag name — either the opening or the closing one — the matching tag name on the other side of the element is highlighted, making the boundaries of the element obvious even in deeply nested markup.

**Example:**

```rescript
// Place caret on either "div": both the opening and closing tag names highlight
<div>
  <span> {React.string("hi")} </span>
</div>
```

For an unterminated element (no closing tag yet) only the opening name is highlighted. JSX fragments (`<>...</>`) have no tag names, so nothing is highlighted. When tags of the same name are nested, the highlight resolves to the pair of the element enclosing the caret.

Paired-tag highlighting reveals where a JSX element opens and closes at a glance — place your caret on a tag name and instantly see its counterpart, even when several same-named tags are nested.

## JSX Paired Tag Rename

{bdg-success}`Native`

When you edit a JSX tag name, the change is mirrored live onto its matching tag on the other side of the element, so the opening and closing names never drift apart. Typing or deleting characters in `<div>` keeps the trailing `</div>` in sync as you go, and the whole synchronized edit Undoes in a single step.

**Example:**

```rescript
// Caret right after "div" in the opening tag; type "x"
<div></div>
// Result — both names update together
<divx></divx>
```

The mirror only fires while the two names still match (the side you are editing matched the other side before the keystroke), so it never overwrites an intentionally different closing name. It does nothing for self-closing tags (`<br />`) or fragments (`<>...</>`), which have no second name to keep in sync.

Synchronized renaming removes the manual step of fixing the closing tag after changing the opening one — rename in place and the counterpart follows, with a single Undo to back it all out.

## Word Selection (Extend/Shrink)

{bdg-success}`Native`

Use `Ctrl+W` (`Alt+Up` on macOS) to extend the selection and `Ctrl+Shift+W` (`Alt+Down` on macOS) to shrink it, with ReScript-aware boundaries.

### Selection Boundaries

The word selector recognizes the following ReScript-specific boundaries for extend/shrink selection:

| Context | Selection Expands To |
|---------|---------------------|
| Inside a string literal | String content (without quotes) → Entire string (with quotes) |
| Inside parentheses `(...)` | Content inside parens → Including parentheses |
| Inside block comment `/* ... */` | Comment content → Entire comment including delimiters |
| Inside line comment `// ...` | Comment text → Entire comment line |

This provides more natural selection behavior than the default word selector when working with ReScript code structures.

ReScript-aware selection boundaries make it easy to select exactly the content you need — string contents without quotes, block comment text without delimiters — using the standard extend/shrink shortcuts.

## Paste as JSX

{bdg-success}`Native`

When pasting HTML content into a ReScript file, it is automatically converted to JSX syntax:
- HTML attributes (`class`, `for`, `onclick`) are renamed to JSX equivalents (`className`, `htmlFor`, `onClick`)
- Void elements (`<br>`, `<img>`, `<input>`) are self-closed (`<br />`)
- Inline `style` strings are converted to ReScript style objects
- Boolean attributes (e.g., `disabled`, `checked`) are preserved as JSX boolean props
- `data-*` and `aria-*` attributes are preserved as-is

Paste as JSX converts HTML to ReScript JSX automatically — instead of manually renaming `class` to `className` and self-closing void elements, the conversion happens on paste.

## Paste as ReScript

{bdg-success}`Native`

This conversion is triggered automatically when you paste JavaScript or TypeScript code into a `.res` file using the standard paste shortcut ({kbd}`Cmd+V` / {kbd}`Ctrl+V`). No separate menu action is needed — the plugin detects JavaScript/TypeScript patterns in the clipboard and converts them on the fly.

When pasting JavaScript or TypeScript code into a ReScript file, it is automatically converted to ReScript syntax:

### JavaScript conversions

- `const`/`let`/`var` declarations are converted to `let` bindings
- Arrow functions `(x) => { return x }` are simplified to `(x) => x`
- `===` / `!==` are converted to `==` / `!=`
- `console.log(x)` is converted to `Js.log(x)`
- `null` / `undefined` are converted to `None`
- Template literals remain as-is (ReScript uses the same syntax)

### TypeScript type stripping

- Variable type annotations (`const x: string = ...`) are removed
- Function parameter type annotations (`(a: number, b: string)`) are removed
- Return type annotations (`): boolean {`) are removed
- Type assertions (`value as string`) are removed
- `interface` and `enum` declarations are commented out
- `export type` / `export interface` / `export enum` are commented out

### JSX/TSX pattern conversion

- Conditional rendering `{condition && <expr>}` is converted to `{condition ? <expr> : React.null}`
- Array methods (`.map()`, `.filter()`, `.forEach()`) are converted to pipe-first style (`->Array.map()`)
- JSX spread `{...props}` gets a warning comment (not supported in ReScript JSX)

::::{tab-set}
:::{tab-item} Before (TypeScript in clipboard)
```typescript
const greeting = (name: string): string => {
  console.log("Hello, " + name);
  return name.toUpperCase();
}
```
:::
:::{tab-item} After (pasted into `.res` file)
```rescript
let greeting = (name) => {
  Js.log("Hello, " ++ name)
  name->String.toUpperCase
}
```
:::
::::

Paste as ReScript bridges the gap between JavaScript/TypeScript and ReScript — copy code from documentation, Stack Overflow, or existing JS/TS files and paste it directly into a `.res` file with automatic syntax conversion.

Note that React JSX/TSX code (containing `className=`, `{expression}` braces, or camelCase event handlers like `onClick=`) is handled by the JS/TS→ReScript converter rather than the HTML→JSX converter, ensuring correct treatment of JSX patterns.
