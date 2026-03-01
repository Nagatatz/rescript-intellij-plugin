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

### PPX Annotation Hints

PPX annotation hints display inline descriptions of what each `@`-attribute generates or binds to, helping developers understand PPX behavior without consulting documentation.

```rescript
@react.component           // generates React.createElement
let make = (~name: string) => {
  <div> {React.string(name)} </div>
}

@module("fs")              // binds to JS module "fs"
external readFile: string => promise<string> = "readFile"

@genType                   // generates .gen.tsx
let format = (s: string) => s->String.trim
```

Supported annotations include `@react.component`, `@genType`, `@module`, `@val`, `@send`, `@get`, `@set`, `@new`, `@deriving(json)`, `@deriving(accessors)`, `@unboxed`, `@scope`, `@string`, `@int`, `@unwrap`, `@return`, `@obj`, `@variadic`, `@as`, `@live`, `@dead`, and `@inline`.

Configure via **Settings** > **Editor** > **Inlay Hints** > **ReScript** > **PPX annotations**.

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

## RegExp Injection in %re()

Regular expressions inside `%re()` blocks receive full RegExp language support, including syntax highlighting, validation, and bracket matching.

```rescript
let emailPattern = %re("/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/")

let phonePattern = %re("/^\+?[1-9]\d{1,14}$/")

let urlPattern = %re("/^https?:\/\/[\w\-]+(\.[\w\-]+)+[/#?]?.*$/i")
```

### Language Injection Mechanism

The plugin uses IntelliJ's `MultiHostInjector` API to inject the RegExp language into string literals inside `%re()` blocks:

1. **Pattern detection** --- The injector checks whether a string literal is preceded by the token pattern `% re (`
2. **Range calculation** --- The regex delimiters (`/` ... `/`) are detected and the injection range is set to the content between delimiters
3. **Flags handling** --- Regex flags after the closing delimiter (e.g., `i`, `g`, `m`) are passed to the RegExp language

### What You Get

Inside injected `%re()` blocks:

- RegExp syntax highlighting (character classes, quantifiers, groups, anchors)
- Error highlighting for invalid regex patterns
- Bracket matching for groups `(` ... `)`
- Hover documentation for regex constructs (when the RegExp plugin provides it)

### Requirements

This feature uses IntelliJ's built-in RegExp language support, which is available in all JetBrains IDEs.

## Project Wizard

Create new ReScript projects directly from the IDE with 12 pre-configured templates covering frontend, backend, serverless, mobile, and more.

### Steps

1. **File** > **New** > **Project**
2. Select **ReScript** from the generator list on the left
3. Configure project settings:
   - **Project name** and **location**
   - **Template** --- Choose from 12 project templates grouped by category
   - **Package manager** --- Choose between npm, pnpm, or yarn
4. Click **Create** to generate the project

### Available Templates

| Category | Template | Description |
|----------|----------|-------------|
| Basic | **Basic** | Minimal ReScript project with console output |
| Frontend | **Vite + React** | React single-page application with Vite bundler |
| Frontend | **Next.js** | Server-side rendered React application with Next.js |
| Desktop | **Electron** | Cross-platform desktop application with Electron |
| Backend | **Hono (Node.js)** | Lightweight web server with Hono framework on Node.js |
| Serverless | **Cloudflare Workers** | Serverless API on Cloudflare Workers with Hono |
| Serverless | **AWS Lambda** | Serverless function on AWS Lambda with Hono |
| Serverless | **Google Cloud Run** | Container-based service on Google Cloud Run with Hono |
| Mobile | **React Native (Expo)** | Mobile application with React Native and Expo |
| Library | **npm Library** | Publishable npm package with `@genType` for TypeScript consumers |
| Tool | **CLI Tool** | Command-line tool with argument parsing |
| Full Stack | **Monorepo (Hono + React)** | Full-stack monorepo with Hono backend and React frontend |

### Generated Project Structure

Each template generates a ready-to-use project with `rescript.json`, `package.json`, and template-specific source files.

**Basic template:**

```
my-project/
+-- rescript.json
+-- package.json
+-- src/
    +-- App.res
```

**Vite + React template:**

```
my-project/
+-- rescript.json
+-- package.json
+-- index.html
+-- vite.config.mjs
+-- src/
    +-- App.res
    +-- Main.res
```

**Monorepo template:**

```
my-project/
+-- package.json            # Root with workspaces
+-- packages/
    +-- shared/
    |   +-- rescript.json
    |   +-- package.json
    |   +-- src/Types.res
    +-- server/
    |   +-- rescript.json
    |   +-- package.json
    |   +-- src/Server.res
    +-- client/
        +-- rescript.json
        +-- package.json
        +-- src/App.res
```

### Template Details

**React-based templates** (Vite+React, Next.js, Electron, React Native) include JSX configuration in `rescript.json` and React dependencies.

**Hono-based templates** (Hono, Cloudflare Workers, AWS Lambda, Google Cloud Run) share common Hono bindings (`src/Hono.res`) and differ in their deployment configuration:
- **Hono (Node.js)** --- Uses `@hono/node-server` for local development
- **Cloudflare Workers** --- Includes `wrangler.jsonc` configuration
- **AWS Lambda** --- Includes esbuild bundling and Lambda adapter bindings
- **Google Cloud Run** --- Includes a `Dockerfile` for containerized deployment

**npm Library** includes `@genType` configuration for generating TypeScript type definitions.

**CLI Tool** includes a `bin` entry in `package.json` and argument parsing via `Process.argv`.

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

## .d.ts Binding Generation

Generate ReScript `external` binding code from TypeScript `.d.ts` definition files. This automates the tedious process of writing FFI declarations manually.

### How to Use

1. Right-click a `.d.ts` file in the Project panel
2. Select **Generate ReScript Binding**
3. The plugin parses the file using the TypeScript Compiler API and generates a `.res` file with binding declarations

The action is also available from the editor context menu when a `.d.ts` file is open.

### Supported Constructs

| TypeScript | ReScript Output |
|------------|----------------|
| `function` | `@module external fn: params => ret = "fn"` |
| `interface` | Record type `type t = { field1: string, field2?: int }` |
| `class` | Module with `@new`, `@get`, `@send` externals |
| `type` alias | `type t = typeStr` |
| `enum` (string) | Polymorphic variant `type t = [#"a" \| #"b"]` |
| `enum` (numeric) | Module with `@inline let` constants |
| `const` / `let` | `@module external name: type = "name"` |

### Type Mapping

| TypeScript | ReScript |
|------------|----------|
| `string` | `string` |
| `number` | `float` |
| `boolean` | `bool` |
| `void` | `unit` |
| `any` / `unknown` | `JSON.t` |
| `T \| null` | `Nullable.t<T>` |
| `T \| undefined` | `option<T>` |
| `Array<T>` / `T[]` | `array<T>` |
| `Promise<T>` | `promise<T>` |
| `Record<string, T>` | `Dict.t<T>` |
| `[A, B, C]` | `(A, B, C)` |
| `(a: A) => B` | `A => B` |
| String literal union | Polymorphic variant with `@string` |

### Requirements

- **Node.js** must be installed and available in PATH (or configured in Settings)
- **TypeScript** must be installed in the project's `node_modules` (the plugin searches parent directories for monorepo support)

### Limitations

The following TypeScript constructs are not yet supported and will generate `/* TODO */` comments:

- Conditional types and mapped types
- Template literal types
- Complex generics with constraints
- Intersection types (generates `JSON.t` fallback)
- Overloaded function signatures (uses first signature)
- Declaration merging

## Color Preview

Inline color swatches are displayed in the editor gutter for color values in your ReScript code:
- Hex colors: `"#ff0000"`, `"#f00"`
- RGB: `"rgb(255, 0, 0)"`
- HSL: `"hsl(0, 100%, 50%)"`

Click the swatch to open the color picker.

## VCS Code Vision

Author and last-change annotations appear on top-level declarations (let, type, module, external), providing Git blame information directly in the editor. Enable via **Settings** > **Editor** > **Inlay Hints** > **Code Vision**.

## Package Dependencies

A dedicated tool window shows the dependencies and devDependencies from your `rescript.json`:

**Open:** **View** > **Tool Windows** > **ReScript Dependencies**

The tree view organizes packages into "Dependencies" and "Dev Dependencies" groups with version numbers.

## Quick Documentation

Press `Ctrl+Q` (or hover) to see documentation for ReScript elements. When the LSP server is connected, documentation comes from the language server. When LSP is unavailable, a PSI-based fallback shows the declaration type, name, and source file.

## Safe Delete

Use **Refactor** > **Safe Delete** to delete ReScript declarations with usage checking. If the element is still referenced, a confirmation dialog shows all usage locations before proceeding.

## Name Suggestions

During rename refactoring, the plugin suggests names based on:
- The element's type (e.g., `user` for `User.t`)
- The containing file name
- camelCase conversion from snake_case

## Reader Mode

Files in `node_modules/` directories are automatically displayed in Reader Mode, providing a cleaner read-only view for library source files.

## TODO Indexing

The plugin integrates with IntelliJ's TODO tool window (`Alt+6` > **TODO** tab) to detect and list TODO, FIXME, and other task comments in ReScript files.

Recognized patterns include:
- `// TODO: ...`
- `// FIXME: ...`
- `/* TODO: ... */`

TODO items appear in the IDE's **TODO** panel alongside items from other file types in your project. You can customize TODO patterns and filters in **Settings** > **Editor** > **TODO**.

## Open Statement Index

The plugin indexes all `open` statements across your project for fast module resolution. This powers features like auto-import suggestions and module dependency analysis.

## Project View Enhancements

- **Interface indicator:** `.res` files with a corresponding `.resi` show a "(has .resi)" suffix
- **Version display:** `rescript.json` shows the ReScript version from its content
- **Compiled JS nesting:** Compiled `.res.js` / `.res.mjs` files are nested under their corresponding `.res` source file in the Project panel, reducing visual clutter
- **Compiled JS graying:** Nested compiled JS files are displayed in gray text to visually distinguish generated output from source files

## Auto Import Options

Configure auto-import behavior in **Settings** > **Editor** > **General** > **Auto Import**:
- Toggle automatic `open` statement insertion
- Exclude specific modules from auto-import

## Expression Type

Press `Ctrl+Shift+P` (`Cmd+Shift+P` on macOS) to display the inferred type of the expression at the cursor position.

```rescript
let add = (a, b) => a + b
// Place caret on "add", press Ctrl+Shift+P
// Shows: (int, int) => int
```

The type information is fetched from the Language Server via an LSP `textDocument/hover` request. If the LSP is not connected, a message indicates that no type information is available.

:::{tip}
This is useful when you want to quickly check the type of a sub-expression without adding an explicit type annotation. Unlike inlay hints (which show types persistently), Expression Type is on-demand and works on any expression, not just declarations.
:::

## Type Info Tool Window

A persistent tool window that continuously displays the inferred type of the expression at the current caret position. Unlike Expression Type (`Ctrl+Shift+P`) which shows types on demand, the Type Info Tool Window updates automatically as you navigate through code.

**Open:** **View** > **Tool Windows** > **ReScript Type**

### How It Works

1. As you move the caret in a ReScript file, the tool window sends an LSP `textDocument/hover` request for the current position
2. The response is debounced to avoid excessive requests during rapid navigation
3. The inferred type is displayed in the tool window panel, updating in real time

### Use Cases

- **Exploring unfamiliar code** --- See types continuously without pressing any shortcut
- **Debugging type errors** --- Move through expressions to understand where types diverge
- **Learning ReScript** --- Observe how the type system infers types for different expressions

### Requirements

The Type Info Tool Window requires the Language Server to be running. If LSP is not connected, the panel shows a "No type information available" message.

## LSP Auto-Install

When you open a ReScript project without `@rescript/language-server` installed, the plugin displays a notification with a one-click install button.

### Notification Actions

| Action | Description |
|--------|-------------|
| **Install with npm/yarn/pnpm** | Installs `@rescript/language-server` as a dev dependency using the detected package manager |
| **Configure...** | Opens the ReScript settings page to set a custom LSP path |
| **Don't show again** | Dismisses the notification for the current session |

The installation runs in the background with a progress indicator. On success, the Language Server starts automatically --- no IDE restart required.

The notification only appears when:
- The project contains `rescript.json` or `bsconfig.json`
- No custom LSP path is configured in settings
- The Language Server is not found in `node_modules`

## GitHub Error Reporter

The plugin includes an automatic error reporting system that sends unhandled exceptions to GitHub Issues, helping the maintainers quickly identify and fix bugs.

### How It Works

When an unexpected exception occurs within the plugin, the IDE's standard error dialog appears with a **Report to Plugin Author** button. Clicking this button opens a pre-filled GitHub issue in your browser with:

- The exception stack trace
- Plugin version and IDE version
- Operating system information

### Privacy

The error report is opened in your browser as a draft GitHub issue. You can review and edit the content before submitting. No data is sent automatically --- you have full control over what is shared.

### Requirements

A GitHub account is required to submit error reports. The report opens on the plugin's GitHub repository issue tracker.

## Inspection Suppressor

Suppress specific inspections using `// noinspection` comments:

```rescript
// noinspection RescriptDuplicateOpen
open Belt
open Belt  // This duplicate open won't be flagged
```

## Framework Detector

The plugin automatically detects ReScript projects by looking for `rescript.json` files. When a project containing `rescript.json` is opened, the IDE recognizes it as a ReScript project and suggests configuring the framework accordingly.

This enables framework-aware features like project-specific settings and tool integrations.

## Code Rearranger

Rearrange top-level declarations in your ReScript files into a canonical ordering via **Code** > **Rearrange Code**.

The default order is:

1. `open` / `include` statements
2. `type` declarations
3. `exception` declarations
4. `module` declarations
5. `external` declarations
6. `let` declarations

This helps maintain a consistent file structure across your project.
