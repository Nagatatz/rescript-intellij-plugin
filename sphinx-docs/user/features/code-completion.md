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
- Pipe (`|>`) chain suggestions

## Postfix Completion

Type an expression followed by `.` and a postfix template name to transform the expression.

| Template | Input | Output |
|----------|-------|--------|
| `.switch` | `expr.switch` | `switch expr { \| _ => () }` |
| `.pipe` | `expr.pipe` | `expr->` |
| `.log` | `expr.log` | `Js.log(expr)` |
| `.some` | `expr.some` | `Some(expr)` |
| `.ok` | `expr.ok` | `Ok(expr)` |
| `.error` | `expr.error` | `Error(expr)` |
| `.ignore` | `expr.ignore` | `expr->ignore` |

## Live Templates

Type a snippet abbreviation and press `Tab` to expand. Available templates:

| Abbreviation | Expansion |
|-------------|-----------|
| `let` | `let name = value` |
| `letfn` | `let name = (params) => { ... }` |
| `mod` | `module Name = { ... }` |
| `switch` | `switch expr { \| pattern => ... }` |
| `if` | `if condition { ... }` |
| `ifelse` | `if condition { ... } else { ... }` |
| `type` | `type name = ...` |
| `typerec` | `type name = { ... }` |
| `typevar` | `type name = Variant1 \| Variant2` |
| `ext` | `@module("...") external name: type = "name"` |
| `comp` | React component boilerplate |
| `pipe` | `->` pipe expression |
| `test` | Test case boilerplate |
| `desc` | Test describe block |
| `expect` | Expect assertion |

Go to **Settings** → **Editor** → **Live Templates** → **ReScript** to view and customize templates.

## Signature Help

When you type `(` after a function name, a popup shows the function's parameter information (types and names). This helps you fill in arguments correctly without checking the documentation.
