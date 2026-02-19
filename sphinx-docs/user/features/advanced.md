# Advanced Features

These features provide additional productivity tools for ReScript development.

## Code Lens

Function definitions display their inferred type signatures as inline annotations above the function:

```rescript
// (int, int) => int        ← Code Lens annotation
let add = (a, b) => a + b
```

Code Lens annotations are provided by the Language Server via the CodeVision API.

## Compiled JavaScript Preview

A tool window that shows the compiled JavaScript output for the current ReScript file.

**Open:** **View** → **Tool Windows** → **Compiled JS Preview**

The preview updates automatically as you edit and save your ReScript file.

## Module Hierarchy

View the module dependency hierarchy for any ReScript module.

**Open:** Right-click on a module → **Module Hierarchy** (or use **Navigate** → **Type Hierarchy**)

The hierarchy shows:
- Module nesting (parent/child relationships)
- Dependencies via `open` and `include` statements

## Inlay Hints

The Language Server displays inferred types as inline hints next to variables and parameters:

```rescript
let x /* : int */ = 42
let greet = (name /* : string */) => `Hello, ${name}`
```

Toggle inlay hints in **Settings** → **Editor** → **Inlay Hints** → **ReScript**.

## JSON Schema for rescript.json

The plugin provides JSON Schema validation and completion for `rescript.json` and `bsconfig.json` files:

- Auto-completion for configuration keys
- Validation of values
- Hover documentation for each field

## Markdown Code Fence Highlighting

ReScript code blocks in Markdown files are syntax-highlighted:

````markdown
```rescript
let x = 42
```
````

This requires the Markdown plugin to be installed (bundled with most JetBrains IDEs).

## JavaScript Injection in %raw()

JavaScript code inside `%raw()` blocks is highlighted with JavaScript syntax:

```rescript
let add = %raw(`
  function(a, b) {
    return a + b;
  }
`)
```

This requires the JavaScript plugin to be available.

## Project Wizard

Create new ReScript projects from the IDE:

1. **File** → **New** → **Project**
2. Select **ReScript** from the generator list
3. Configure project name and location
4. The wizard generates a basic ReScript project structure with `rescript.json`

## File Templates

Create new ReScript files with pre-filled boilerplate:

1. Right-click on a directory → **New** → **ReScript File**
2. Choose a template:
   - **Module** — Basic module file
   - **Interface** — Interface (`.resi`) file
   - **Component** — React component boilerplate
