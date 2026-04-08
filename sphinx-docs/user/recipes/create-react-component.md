---
myst:
  html_meta:
    "keywords": "react component, file template, live template, jsx"
---

# Create a React Component

{bdg-success}`Native`

## Goal

Quickly scaffold a new React component file with proper structure, including the `@react.component` decorator and a `make` function.

## Steps

### 1. Create a new component file

Right-click on the target directory in the **Project** panel and select **New > ReScript Component**.

Enter the component name (e.g., `UserCard`). The plugin creates a new `.res` file with the basic component skeleton already in place.

### 2. Use the `comp` live template

If you prefer to start from an empty file or add a component to an existing file, type `comp` and press {kbd}`Tab`:

```rescript
// Type "comp" + Tab to expand:
@react.component
let make = () => {
  <div> {React.string("Hello")} </div>
}
```

The cursor is placed inside the JSX so you can start editing immediately.

### 3. Add props

Define your component's props by adding labeled arguments to the `make` function:

```rescript
@react.component
let make = (~name: string, ~age: int, ~onClick: unit => unit) => {
  <div onClick>
    {React.string(name ++ " (" ++ Int.toString(age) ++ ")")}
  </div>
}
```

Code completion ({kbd}`Ctrl+Space`) provides suggestions for React APIs and module members as you type.

### 4. Use JSX auto-close

When you type `>` to close an opening tag, the plugin automatically inserts the corresponding closing tag:

```rescript
// Type <div> and the closing </div> is inserted automatically
<div>|</div>
```

## Expected Result

A working React component file with:

- The `@react.component` decorator
- A `make` function with typed props
- JSX body with proper structure

## Related Features

- [File Templates](../features/code-editing.md) --- **New > ReScript Component** for file creation
- [Live Templates](../features/code-completion.md) --- `comp` and other snippets
- [Code Completion](../features/code-completion.md) --- LSP-powered intelligent suggestions
- [JSX Auto-Close](../features/code-editing.md) --- Automatic closing tag insertion
