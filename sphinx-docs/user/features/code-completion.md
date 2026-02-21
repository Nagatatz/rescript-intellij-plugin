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

## Lookup Character Filter

The plugin intelligently filters completion behavior based on typed characters:
- Typing `.` accepts the current completion and inserts the dot (for module access patterns)
- Typing `(` accepts the current completion and inserts parentheses (for function calls)
- Other special characters behave appropriately for ReScript syntax
