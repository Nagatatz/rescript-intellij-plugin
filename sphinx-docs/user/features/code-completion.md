# Code Completion

The plugin provides multiple completion mechanisms to help you write code faster.

## LSP Completion

The Language Server provides intelligent, type-aware completions. Trigger completion with `Ctrl+Space` (or `Cmd+Space` on macOS), or just start typing.

### What Gets Completed

- Variable and function names
- Module names and paths
- Record field names
- Variant constructors
- Type names
- Pipe (`->`) chain suggestions

Type-aware completions mean you spend less time memorizing API surfaces — the IDE suggests the right functions, fields, and constructors based on the types in your code.

## Postfix Completion

Type an expression followed by `.` and a postfix template name to transform the expression.

| Template | Input | Output |
|----------|-------|--------|
| `.switch` | `expr.switch` | `switch expr { \| _ => }` |
| `.pipe` | `expr.pipe` | `expr->` |
| `.log` | `expr.log` | `Console.log(expr)` |
| `.some` | `expr.some` | `Some(expr)` |
| `.ok` | `expr.ok` | `Ok(expr)` |
| `.error` | `expr.error` | `Error(expr)` |
| `.ignore` | `expr.ignore` | `expr->ignore` |
| `.promise` | `expr.promise` | `expr->Promise.then(result => { ... })` |
| `.await` | `expr.await` | `await expr` |

```{note}
Postfix templates are not available inside comments or string literals.
```

### Detailed Usage

#### `.switch` -- Pattern Matching

Use `.switch` when you need to pattern match on a value. This is one of the most frequently used postfix templates, as `switch` expressions are central to ReScript programming.

Before:

```rescript
status.switch
```

After:

```rescript
switch status {
| _ =>
}
```

The cursor is placed after `=>` so you can immediately start typing the branch body. Add additional arms manually.

#### `.pipe` -- Functional Composition

Use `.pipe` to start or extend a pipe chain. ReScript uses `->` (pipe first) for chaining function calls, and this template saves keystrokes when building data transformation pipelines.

Before:

```rescript
array.pipe
```

After:

```rescript
array->
```

The cursor is placed right after `->` so you can type the next function name. This is especially useful for building chains:

```rescript
users
->Array.filter(u => u.active)
->Array.map(u => u.name)
->Array.sort(String.compare)
```

#### `.log` -- Debug Logging

Use `.log` to quickly wrap an expression in `Console.log()` for debugging.

Before:

```rescript
result.log
```

After:

```rescript
Console.log(result)
```

This is convenient for quick debugging without manually typing the wrapping function.

#### `.some` -- Optional Value Wrapping

Use `.some` to wrap a value in the `Some` constructor of the `option` type.

Before:

```rescript
user.name.some
```

After:

```rescript
Some(user.name)
```

Useful when you need to return an optional value from a function:

```rescript
let findUser = (id) => {
  // ...lookup logic...
  matchedUser.some // becomes Some(matchedUser)
}
```

#### `.ok` / `.error` -- Result Type Wrapping

Use `.ok` and `.error` to wrap values in the `result` type constructors.

Before:

```rescript
parsedData.ok
```

After:

```rescript
Ok(parsedData)
```

Before:

```rescript
"Invalid input".error
```

After:

```rescript
Error("Invalid input")
```

These are helpful when working with functions that return `result<'a, 'b>`:

```rescript
let validate = (input) => {
  if isValid(input) {
    process(input).ok     // becomes Ok(process(input))
  } else {
    "Bad input".error     // becomes Error("Bad input")
  }
}
```

#### `.ignore` -- Discarding Return Values

Use `.ignore` when you want to call a function for its side effects and discard the return value. This avoids compiler warnings about unused values.

Before:

```rescript
Js.Promise.then(promise, handler).ignore
```

After:

```rescript
Js.Promise.then(promise, handler)->ignore
```

#### `.promise` -- Async Promise Chain

Use `.promise` to wrap an expression in a `Promise.then` callback, useful when working with asynchronous code.

Before:

```rescript
fetchData().promise
```

After:

```rescript
fetchData()->Promise.then(result => {
  // cursor here
})
```

The cursor is placed inside the callback body so you can immediately write the continuation logic.

#### `.await` -- Await Expression

Use `.await` to prepend `await` to an expression, converting it from a promise to a resolved value inside an `async` function.

Before:

```rescript
fetchData().await
```

After:

```rescript
await fetchData()
```

This is the quickest way to await a promise expression without manually repositioning the cursor.

Postfix templates let you transform expressions without moving the cursor back to the beginning — type the expression first, then apply the transformation, keeping your natural left-to-right typing flow.

:::{seealso}
[Code Editing](code-editing.md) offers Intention Actions and Surround With for more code transformation patterns.
:::

## Live Templates

Type a snippet abbreviation and press `Tab` to expand. The cursor stops at each placeholder (shown in the expansions below); press `Tab` to move to the next placeholder.

| Abbreviation | Description | Expansion |
|-------------|-------------|-----------|
| `let` | let binding | `let name = value` |
| `letfn` | let function | `let name = (params) => { ... }` |
| `mod` | module definition | `module Name = { ... }` |
| `modt` | module type definition | `module type Name = { ... }` |
| `typ` | type definition | `type name = ...` |
| `typr` | record type | `type name = { field: type, ... }` |
| `typv` | variant type | `type name = \| Variant ...` |
| `sw` | switch expression | `switch expr { \| pattern => ... }` |
| `if` | if expression | `if condition { ... }` |
| `ife` | if-else expression | `if condition { ... } else { ... }` |
| `try` | try-catch expression | `try { ... } catch { \| exn => ... }` |
| `for` | for loop | `for i in 0 to 10 { ... }` |
| `ext` | external (FFI) | `external name: type = "jsName"` |
| `pipe` | pipe operator | `->func(...)` |
| `log` | Console.log | `Console.log(...)` |
| `@module` | @module external binding | `@module("name") external fn: 'a = "default"` |
| `@val` | @val external binding | `@val external name: 'a = "name"` |
| `@send` | @send external binding | `@send external name: (obj, 'a) => unit = "name"` |
| `@get` | @get external binding | `@get external name: obj => 'a = "name"` |
| `@set` | @set external binding | `@set external name: (obj, 'a) => unit = "name"` |
| `comp` | React component | `@react.component let make = (~children) => { ... }` |

### Template Details

#### Declarations

**`let`** -- Let binding

```rescript
let name = value
```

The cursor stops first at `name`, then at `value`.

**`letfn`** -- Function definition

```rescript
let name = (params) => {
  // cursor here
}
```

The cursor stops at `name`, then `params`, then inside the function body.

**`mod`** -- Module definition

```rescript
module Name = {
  // cursor here
}
```

**`modt`** -- Module type definition

```rescript
module type Name = {
  // cursor here
}
```

#### Type Definitions

**`typ`** -- Simple type alias

```rescript
type name = // cursor here
```

**`typr`** -- Record type definition

```rescript
type name = {
  field: string,
  // cursor here
}
```

The cursor stops at `name`, then `field`, then the field type, then inside the record for additional fields.

**`typv`** -- Variant type definition

```rescript
type name =
  | Variant // cursor here
```

The cursor stops at `name`, then `Variant`, allowing you to define the first constructor and continue adding more.

#### Control Flow

**`sw`** -- Switch expression

```rescript
switch expr {
| pattern => // cursor here
}
```

**`if`** -- If expression

```rescript
if condition {
  // cursor here
}
```

**`ife`** -- If-else expression

```rescript
if condition {
  // then branch
} else {
  // cursor here
}
```

**`try`** -- Try-catch expression

```rescript
try {
  // cursor here
} catch {
| exn => ()
}
```

**`for`** -- For loop

```rescript
for i in 0 to 10 {
  // cursor here
}
```

The cursor stops at the loop variable `i`, then the start value `0`, then the end value `10`, then the loop body.

#### FFI (Foreign Function Interface)

**`ext`** -- External declaration

```rescript
external name: 'a = "jsName"
```

The cursor stops at `name`, then the type annotation, then the JavaScript name string. Useful for binding to JavaScript functions and values.

#### Utility

**`pipe`** -- Pipe operator with function call

```rescript
->func()
```

Type this after an expression to pipe it into a function. The cursor stops at `func`, then inside the parentheses for arguments.

**`log`** -- Console.log

```rescript
Console.log()
```

The cursor is placed inside the parentheses.

#### FFI Binding Templates

**`@module`** -- Module binding

```rescript
@module("module-name")
external name: 'a = "default"
```

The cursor stops at the module name, then the binding name, type, and JavaScript export name.

**`@val`** -- Global value binding

```rescript
@val external name: 'a = "name"
```

**`@send`** -- Method binding

```rescript
@send external name: (obj, 'a) => unit = "name"
```

The cursor stops at the binding name, the receiver object type, the parameter type, the return type, and the JavaScript method name.

**`@get`** -- Property getter binding

```rescript
@get external name: obj => 'a = "name"
```

**`@set`** -- Property setter binding

```rescript
@set external name: (obj, 'a) => unit = "name"
```

#### React

**`comp`** -- React component

```rescript
@react.component
let make = (~children) => {
  // cursor here
}
```

The cursor stops at the props parameter, then the component body.

### Customization

You can customize all ReScript live templates at **Settings** | **Editor** | **Live Templates** | **ReScript**. From there you can:

- Edit existing template expansions and placeholder variables
- Add new templates
- Change the abbreviation trigger text
- Disable templates you do not use

Live templates eliminate repetitive boilerplate — instead of typing out common patterns like `switch`, `if-else`, `@module` bindings, or React components from scratch, a short abbreviation expands into the full structure with placeholder navigation.

## Completion Weigher

The plugin provides context-based prioritization of completion candidates, ensuring that the most relevant suggestions appear first in the completion popup.

### How It Works

When the Language Server returns completion candidates, the Completion Weigher re-orders them based on contextual signals:

- **Locality** --- Symbols defined in the current file or module are prioritized over external imports
- **Type compatibility** --- Candidates whose types match the expected type at the insertion point are boosted
- **Recency** --- Recently used completions are ranked higher
- **Pipe context** --- In a `->` pipe chain, functions whose first parameter matches the pipe input type are prioritized

This means that when you trigger completion, you typically see the most useful candidates at the top of the list without needing to scroll or type additional filter characters.

### Example

```rescript
let names: array<string> = ["Alice", "Bob", "Charlie"]

names->  // Completion popup prioritizes Array functions:
         // Array.map, Array.filter, Array.forEach, ...
         // over unrelated functions like String.length
```

Smart ranking means the completion you want is usually at the top of the list, reducing the number of keystrokes needed to select the right candidate.

## Pipe Chain Type Hints

When writing `->` pipe chains, the plugin displays intermediate type hints between each pipe step, making it easy to understand the data flow through a chain of transformations.

### Example

```rescript
users                          // : array<user>
->Array.filter(u => u.active)  // : array<user>
->Array.map(u => u.name)       // : array<string>
->Array.sort(String.compare)   // : array<string>
->Array.length                 // : int
```

Each inlay hint shows the type of the expression at that point in the chain, so you can trace how the data is transformed at every step.

### Configuration

Pipe chain type hints are displayed as inlay hints. Configure their visibility in **Settings** > **Editor** > **Inlay Hints** > **ReScript**.

### Requirements

This feature requires the Language Server to be running, as the type information is fetched via LSP hover requests for each intermediate expression in the pipe chain.

When building data transformation pipelines, intermediate type hints let you verify that each step produces the expected type, catching transformation errors mid-chain rather than at the end.

## Parameter Info

Press `Ctrl+P` (`Cmd+P` on macOS) inside a function call to display labeled argument information in a native IDE popup.

### How It Works

When you invoke Parameter Info inside the parentheses of a function call, the plugin requests hover information from the Language Server and parses the function signature to extract labeled arguments. The result is displayed in a native popup that highlights the current parameter position.

### Example

```rescript
let makeConfig = (~host: string, ~port: int, ~debug: bool=?) => {
  // ...
}

makeConfig(~host="localhost", | )
//                             ^ Ctrl+P here shows:
//                               ~host: string, ~port: int, ~debug: bool=?
//                               with ~port highlighted as the next expected argument
```

### Difference from Signature Help

| Feature | Trigger | Source |
|---------|---------|--------|
| **Signature Help** | Automatic on `(` | LSP `textDocument/signatureHelp` |
| **Parameter Info** | Manual via `Ctrl+P` | LSP `textDocument/hover` + argument parsing |

Parameter Info is useful when you want to check the expected arguments after the initial signature help popup has dismissed, or when you navigate back to an existing function call.

Parameter Info gives you on-demand access to a function's labeled arguments without leaving your current editing position, so you can fill in parameters correctly without checking the function definition.

## Signature Help

When you type `(` after a function name, a popup shows the function's parameter information including types and names. This helps you fill in arguments correctly without checking the documentation.

For example, given a function:

```rescript
let makeUser = (name: string, age: int, ~role: string=?) => {
  // ...
}
```

Typing `makeUser(` triggers the signature help popup, which displays:

```
(name: string, age: int, ~role: string=?) => user
```

As you type each argument and enter a comma, the popup highlights the current parameter position so you always know which argument you are filling in. This works for all functions whose signatures are known to the language server, including standard library functions and your own definitions.

Signature Help removes the guesswork from function calls — you see the expected parameter types and names inline as you type, reducing errors from mismatched argument order or types.

## Completion Confidence

The plugin suppresses the automatic completion popup in contexts where it would be unhelpful or disruptive:

- **Inside comments** — Line comments (`//`), block comments (`/* */`), and doc comments (`/** */`)
- **Inside string literals** — Regular strings, template strings, and character literals

In these contexts, you can still trigger completion manually with `Ctrl+Space`, but the popup will not appear automatically as you type. This prevents irrelevant suggestions from interrupting documentation or string authoring.

This ensures the completion popup only appears when it is genuinely useful, so writing comments and strings is never interrupted by irrelevant code suggestions.

## Lookup Character Filter

The plugin intelligently filters completion behavior based on typed characters:
- Typing `.` accepts the current completion and inserts the dot (for module access patterns)
- Typing `(` accepts the current completion and inserts parentheses (for function calls)
- Other special characters behave appropriately for ReScript syntax

Smart character handling lets you flow naturally from completion into the next editing action — accepting a module with `.` or a function with `(` — without extra keystrokes to dismiss the popup first.
