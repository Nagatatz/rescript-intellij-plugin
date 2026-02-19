# Advanced Features

These features provide additional productivity tools for ReScript development.

## Code Lens

Code Lens displays inferred type signatures as inline annotations above function definitions. This gives you immediate visibility into the types the compiler has inferred, without needing to hover over each identifier.

```rescript
// (int, int) => int        <-- Code Lens annotation
let add = (a, b) => a + b

// string => string         <-- Code Lens annotation
let greet = (name) => `Hello, ${name}!`

// (array<'a>, 'a => 'b) => array<'b>   <-- Code Lens annotation
let mapArray = (arr, fn) => arr->Array.map(fn)
```

### How It Works

Code Lens is powered by the LSP `textDocument/codeLens` protocol, bridged to IntelliJ's CodeVision API:

1. The plugin sends a `codeLens` request to the rescript-language-server for the current file
2. The language server returns an array of Code Lens entries, each containing a line position and a `command.title` with the inferred type string
3. The plugin maps each entry to a `TextCodeVisionEntry` and positions it above the corresponding line in the editor

Code Lens annotations are **display-only** --- they show the inferred type but do not trigger any action when clicked.

### Configuration

- **Toggle Code Lens:** Go to **Settings** > **Editor** > **Inlay Hints** > **Code Vision** and find "ReScript Type Annotations" in the list
- **Anchor position:** Annotations always appear above the function definition line (top anchor)
- **File types:** Code Lens is shown only in `.res` files, not in `.resi` interface files (since interface files already contain explicit type declarations)

### Requirements

Code Lens requires the Language Server to be running. If the LSP server is not connected, no annotations are displayed. The project must also be built at least once so the language server has type information available.

## Compiled JavaScript Preview

A dedicated tool window that shows the compiled JavaScript output for the currently active ReScript file, providing a side-by-side view of your ReScript source and its JavaScript compilation result.

**Open:** **View** > **Tool Windows** > **Compiled JS Preview**

### Auto-Update Behavior

The preview panel automatically refreshes in two situations:

1. **Active file change** --- When you switch between editor tabs, the preview updates to show the compiled JS for the newly focused `.res` or `.resi` file
2. **Compilation success** --- When the ReScript compiler finishes a successful build (detected via `rescript/compilationStatus` LSP notification), the preview reloads to display the latest compiled output

### Split View Setup

For a side-by-side workflow:

1. Open a `.res` file in the editor
2. Open **View** > **Tool Windows** > **Compiled JS Preview**
3. Drag the tool window tab to the right side of the editor to create a vertical split
4. As you edit and save your ReScript code, the JavaScript output updates automatically on the right

### Toolbar Actions

The preview panel includes two toolbar buttons:

- **Refresh** --- Manually reloads the compiled JS content for the current file
- **Open in Editor** --- Opens the compiled `.js` file as a full editor tab (useful for searching or copying larger sections)

### Use Cases

- **Understanding compilation output** --- See how ReScript constructs (pattern matching, pipe operators, variants) translate to JavaScript
- **Debugging** --- Compare the source and output when investigating runtime behavior
- **Performance review** --- Verify that the generated JavaScript is efficient and meets expectations
- **Learning** --- Understand how ReScript features map to JavaScript idioms

If the compiled JS file is not found (e.g., the project has not been built), the panel displays a message prompting you to build the project first.

## Module Hierarchy

View the module structure and dependency relationships for any ReScript module.

**Open:** Place the cursor on a module, then use **Navigate** > **Type Hierarchy** (`Ctrl+H`)

### Two View Modes

The hierarchy browser provides two distinct tree views:

**1. Module Nesting**

Shows the nested module structure within a file. This is the default view. It displays how modules are organized hierarchically:

```rescript
// File: Utils.res
module String = {          // <-- Root
  module Validate = {      //     child of String
    let isEmail = ...
    let isUrl = ...
  }
  module Format = {        //     child of String
    let capitalize = ...
    let truncate = ...
  }
}
module Number = {          // <-- Root
  module Parse = {         //     child of Number
    let toInt = ...
  }
}
```

In the hierarchy view, this displays as:

```
Utils.res
  +-- String
  |     +-- Validate
  |     +-- Format
  +-- Number
        +-- Parse
```

**2. Module Dependencies**

Shows modules referenced by `open` and `include` statements. This view reveals the external dependencies of the current file:

```rescript
open Belt
open Belt.Array
include SharedUtils

let process = (arr) => arr->Array.map(x => x + 1)
```

The dependency view lists:

```
MyModule.res
  +-- open Belt
  +-- open Belt.Array
  +-- include SharedUtils
```

### Navigation

The hierarchy view supports:

- **Module Nesting** and **Module Dependencies** view switching via toolbar buttons
- Alphabetical sorting of modules by name
- Navigation to the source module by double-clicking a node

## Inlay Hints

The Language Server displays inferred types as inline hints next to variables and parameters, making it easier to understand code without explicit type annotations.

```rescript
let x /* : int */ = 42
let name /* : string */ = "ReScript"
let items /* : array<int> */ = [1, 2, 3]

let greet = (name /* : string */, age /* : int */) => {
  `${name} is ${age->Int.toString} years old`
}

let result /* : option<string> */ = list->List.head
```

### Configuration

Navigate to **Settings** > **Editor** > **Inlay Hints** > **ReScript** to configure inlay hints:

- **Enable/disable** --- Toggle inlay hints globally for ReScript files
- Inlay hints are provided by the Language Server; their availability depends on the LSP server being connected and the project being built

### Difference from Code Lens

- **Code Lens** shows the full function signature **above** the function definition line
- **Inlay Hints** show individual type annotations **inline** next to each variable or parameter

Both features rely on the Language Server, but they display information in different locations and at different granularities.

## JSON Schema for rescript.json

The plugin provides JSON Schema validation and auto-completion for `rescript.json` and `bsconfig.json` configuration files. The schema covers the full ReScript build configuration specification.

### Auto-Completed Fields

When editing `rescript.json`, you get auto-completion and validation for all top-level and nested fields, including:

| Field | Description |
|-------|-------------|
| `name` | Package name (required) |
| `sources` | Source directory configuration (required) |
| `bs-dependencies` | ReScript package dependencies |
| `dev-dependencies` | Development-only ReScript dependencies |
| `jsx` | JSX transformation settings (`version`, `module`, `preserve`) |
| `suffix` | Output file suffix (e.g., `.res.mjs`, `.js`) |
| `package-specs` | Module format (`esmodule` or `commonjs`) |
| `namespace` | Package namespace configuration |
| `ppx-flags` | PPX preprocessor macros |
| `warnings` | Warning number configuration and error promotion |
| `reanalyze` | Dead code analysis settings (`analysis`, `suppress`, `transitive`) |
| `editor` | Editor-specific settings (autocomplete extensions) |
| `experimental-features` | Experimental compiler features (e.g., `LetUnwrap`) |

### Features

- **Auto-completion** --- Press `Ctrl+Space` to see available configuration keys at any nesting level
- **Validation** --- Invalid values are highlighted with error markers (e.g., using an unsupported module format)
- **Hover documentation** --- Hover over any key to see its description and expected type
- **Nested structure support** --- Full completion inside `sources`, `jsx`, `reanalyze`, and other nested objects

The schema file is bundled with the plugin and applied automatically when you open any file named `rescript.json` or `bsconfig.json`.

## Markdown Code Fence Highlighting

ReScript code blocks in Markdown files receive full syntax highlighting:

````markdown
```rescript
type user = {name: string, age: int}

let greet = (user) => `Hello, ${user.name}!`
```
````

### Integration Mechanism

The plugin registers a `CodeFenceLanguageProvider` that recognizes three info-string identifiers:

- `` ```rescript `` --- Primary identifier
- `` ```res `` --- Short form for ReScript implementation files
- `` ```resi `` --- Short form for ReScript interface files

When the Markdown plugin encounters a code fence with one of these identifiers, it delegates syntax highlighting to the ReScript lexer. This provides the same token-level highlighting (keywords, strings, comments, operators) that you see in `.res` files.

### Requirements

This feature requires the **Markdown** plugin to be installed, which is bundled with most JetBrains IDEs. If the Markdown plugin is not present, code fences render as plain text.

## JavaScript Injection in %raw()

JavaScript code inside `%raw()` and `%%raw()` blocks receives full JavaScript syntax highlighting, enabling comfortable FFI (Foreign Function Interface) editing within ReScript files.

```rescript
let add = %raw(`
  function(a, b) {
    return a + b;
  }
`)

let timestamp = %raw(`Date.now()`)

%%raw(`
  import * as fs from 'fs';
  const data = fs.readFileSync('config.json', 'utf8');
`)
```

### Language Injection Mechanism

The plugin uses IntelliJ's `MultiHostInjector` API to inject JavaScript into string literals inside `%raw()` blocks:

1. **Pattern detection** --- The injector checks whether a string literal is preceded by the token pattern `% raw (` (with optional whitespace between tokens)
2. **Range calculation** --- For regular strings (`"..."`), the enclosing quotes are trimmed from the injection range. For template strings (`` ` ... ` ``), the full string content is used
3. **Language resolution** --- The injector looks for either "JavaScript" or "ECMAScript 6" language support in the IDE

### What You Get

Inside injected `%raw()` blocks, you have access to:

- JavaScript syntax highlighting (keywords, strings, numbers, comments)
- Basic error highlighting for JavaScript syntax errors
- Bracket matching within the JavaScript block

### Requirements

This feature requires the **JavaScript** plugin (or JavaScript and TypeScript support) to be available in your JetBrains IDE. IntelliJ IDEA Ultimate and WebStorm include this by default. For IntelliJ IDEA Community, you may need to install the JavaScript plugin separately.

## Project Wizard

Create new ReScript projects directly from the IDE with pre-configured build files and starter code.

### Steps

1. **File** > **New** > **Project**
2. Select **ReScript** from the generator list on the left
3. Configure project settings:
   - **Project name** and **location**
   - **Package manager** --- Choose between npm, pnpm, or yarn
   - **Include React** --- Toggle to add React and `@rescript/react` dependencies
4. Click **Create** to generate the project

### Generated Project Structure

**Basic project** (without React):

```
my-project/
+-- rescript.json         # ReScript build configuration
+-- package.json          # Node.js package manifest
+-- src/
    +-- App.res           # Starter module
```

**React project** (with Include React enabled):

```
my-project/
+-- rescript.json         # Includes jsx configuration
+-- package.json          # Includes react and @rescript/react dependencies
+-- src/
    +-- App.res           # React component starter
```

### Generated File Contents

**rescript.json** (basic project):

```json
{
  "name": "my-project",
  "sources": [
    {
      "dir": "src",
      "subdirs": true
    }
  ],
  "package-type": "module",
  "suffix": ".res.mjs",
  "bs-dependencies": ["@rescript/core"],
  "bsc-flags": ["-open RescriptCore"]
}
```

When React is included, the `rescript.json` also contains a `jsx` section and `@rescript/react` in `bs-dependencies`.

**src/App.res** (basic project):

```rescript
let greeting = "Hello, ReScript!"

Console.log(greeting)
```

**src/App.res** (React project):

```rescript
@react.component
let make = () => {
  <div>
    {React.string("Hello, ReScript + React!")}
  </div>
}
```

### After Project Creation

After the wizard creates the project:

1. Run your package manager's install command (e.g., `npm install`) to install dependencies
2. Run `rescript build` (or use the ReScript run configuration) to compile the project
3. The Language Server will start automatically once `@rescript/language-server` is available in `node_modules`

## File Templates

Create new ReScript files with pre-filled boilerplate code via the context menu.

### How to Use

1. Right-click on a directory in the Project panel
2. Select **New** > **ReScript File**
3. Enter the file name (without extension)
4. Choose a template from the dropdown

### Available Templates

**Module** --- Creates a `.res` file with a module comment header:

```rescript
// MyModule module
```

**Interface** --- Creates a `.resi` interface file with a comment header:

```rescript
// MyModule interface
```

**Component** --- Creates a `.res` file with a React component boilerplate:

```rescript
@react.component
let make = () => {
  <div> {React.string("MyComponent")} </div>
}
```

In all templates, the file name you enter is automatically substituted into the template content. The Component template is particularly useful for React projects, providing a ready-to-use functional component with JSX.

### Customizing Templates

File templates can be customized in **Settings** > **Editor** > **File and Code Templates**. Look for the templates under the **Internal** tab with names starting with "ReScript". You can modify the template content using IntelliJ's template variable syntax (e.g., `${NAME}` for the file name).
