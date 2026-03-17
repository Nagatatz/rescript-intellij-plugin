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

## Injected Language Formatting

When you format a ReScript file (`Cmd+Option+L`), any injected language fragments (e.g., JavaScript inside `%raw()`) are also formatted according to their own language's formatting rules.

This feature provides a `FormattingModelBuilder` that delegates formatting to the injected language's formatter, so injected code stays properly formatted alongside your ReScript code.

## Grazie Integration

When the **Grazie** plugin is installed, the ReScript plugin extracts natural language text from comments and string literals for grammar and spell checking.

**Supported text domains:**
- **Comments** — Line comments (`//`), block comments (`/* */`), and documentation comments (`/** */`) are extracted as `COMMENTS` domain text
- **Strings** — String literals, template strings, and character literals are extracted as `LITERALS` domain text

This is an optional integration --- if Grazie is not installed, the feature is simply not available.

## Index Pattern Builder

Enhances the IDE's TODO/FIXME detection by providing a lexer-based index pattern builder for ReScript files. This enables more accurate pattern matching within comments compared to the basic text-based approach.

The index pattern builder uses the ReScript JFlex lexer to correctly classify comment tokens (line comments, block comments, and doc comments), ensuring that TODO/FIXME patterns are only matched inside actual comments and not in string literals or code.

## Element Signature Provider

Provides stable element signatures that persist editor fold states across IDE restarts. When you collapse code blocks in the editor, their folded state is remembered using a signature format (`TYPE#name#offset`) that survives file modifications.

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

```rescript
let primaryColor = "#3498db"
let errorColor = "rgb(231, 76, 60)"
let successColor = "hsl(120, 39%, 49%)"
```

Color values inside string literals are detected and a small color swatch appears in the editor gutter next to the corresponding line.

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

The open statement index enables instant module lookups across the entire project, powering auto-import and dependency analysis without scanning files on every request.

## Project View Enhancements

- **Interface indicator:** `.res` files with a corresponding `.resi` show a "(has .resi)" suffix
- **Version display:** `rescript.json` shows the ReScript version from its content
- **Compiled JS nesting:** Compiled `.res.js` / `.res.mjs` files are nested under their corresponding `.res` source file in the Project panel, reducing visual clutter
- **Compiled JS graying:** Nested compiled JS files are displayed in gray text to visually distinguish generated output from source files

Project View enhancements reduce visual clutter by nesting generated files under their sources and surfacing useful metadata like interface presence and ReScript version, so the file tree stays focused on your source code.

## Auto Import Options

Configure auto-import behavior in **Settings** > **Editor** > **General** > **Auto Import**:
- Toggle automatic `open` statement insertion
- Exclude specific modules from auto-import

Fine-grained auto-import settings let you control which modules are automatically opened, preventing unwanted imports from cluttering your files.

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

Expression Type gives you on-demand type inspection for any expression — unlike persistent inlay hints, you invoke it only when needed, keeping the editor clean while still having instant access to type information.

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

The Type Info Tool Window provides always-on type visibility as you navigate code — unlike Expression Type which requires a shortcut, this panel updates automatically, making it ideal for exploring unfamiliar codebases.

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

LSP Auto-Install removes the most common setup hurdle — instead of manually running npm commands and configuring paths, one click installs the Language Server and starts it automatically.

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

The error reporter makes it easy to help improve the plugin — when something goes wrong, a pre-filled GitHub issue lets you report the problem with minimal effort while maintaining full control over what is shared.

## Inspection Suppressor

Suppress specific inspections using `// noinspection` comments:

```rescript
// noinspection RescriptDuplicateOpen
open Belt
open Belt  // This duplicate open won't be flagged
```

When an inspection produces false positives in specific locations, you can suppress it with a comment rather than disabling it globally, keeping the inspection active elsewhere.

## Framework Detector

The plugin automatically detects ReScript projects by looking for `rescript.json` files. When a project containing `rescript.json` is opened, the IDE recognizes it as a ReScript project and suggests configuring the framework accordingly.

This enables framework-aware features like project-specific settings and tool integrations.

Automatic framework detection means the plugin activates its full feature set as soon as you open a ReScript project, with no manual configuration required.

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

Automatic rearrangement enforces a consistent declaration ordering across all files, so readers always know where to find types, modules, and functions without relying on individual developer habits.

## Dependency Diagram

Visualize module dependency relationships as an interactive graph diagram.

**Open:** **View** > **Tool Windows** > **Dependency Diagram**, or use **Analyze** > **Module Dependency Diagram**

### How It Works

The diagram provider scans all `.res` files in the project and builds a dependency graph based on `open` and `include` statements. Each module is represented as a node, and dependencies are shown as directed edges.

### Features

- **Interactive layout** — Drag nodes to rearrange the graph
- **Zoom and pan** — Navigate large dependency graphs
- **Click navigation** — Double-click a node to open the corresponding `.res` file
- **Cycle detection** — Circular dependencies are highlighted visually

### Use Cases

- **Understanding project structure** — See how modules relate to each other at a glance
- **Identifying tight coupling** — Spot modules with too many dependencies
- **Refactoring planning** — Understand the impact of moving or splitting modules

The dependency diagram reveals your project's module structure visually, making it easy to spot circular dependencies, tightly coupled modules, and refactoring opportunities that are hard to see in code alone.

## PPX Expansion View

A tool window that displays the expanded output of PPX macros applied to the current file.

**Open:** **View** > **Tool Windows** > **PPX Expansion**

### How It Works

The PPX Expansion View runs the ReScript compiler's PPX preprocessor on the current file and displays the transformed AST output. This helps you understand what code the PPX generates behind the scenes.

### Supported PPX Attributes

- `@react.component` — Shows the generated `React.createElement` calls and component wrapper
- `@deriving(json)` — Shows the generated `toJson` and `fromJson` functions
- `@deriving(accessors)` — Shows the generated field accessor functions
- `@genType` — Shows the generated TypeScript type definitions

### Use Cases

- **Debugging PPX behavior** — Understand why generated code doesn't work as expected
- **Learning** — See how PPX attributes transform your source code
- **Optimization** — Review the generated output for performance considerations

PPX macros generate code that you never see in your source files — this view makes the generated code visible, helping you debug PPX-related issues and understand what the compiler actually produces.

## Comment Code Evaluation

Evaluate ReScript code examples embedded in documentation comments directly from the editor.

When a `/** ... */` documentation comment contains a code block, the plugin can evaluate the code and display the result inline. This helps verify that code examples in documentation are correct and up to date.

### How to Use

1. Write a code example inside a documentation comment
2. Place the caret inside the code block
3. Use **Code** > **Evaluate Comment Code** (or the gutter action)

```rescript
/**
 * Adds two numbers.
 *
 * ```
 * add(1, 2)  // => 3
 * ```
 */
let add = (a, b) => a + b
```

The plugin extracts the code block, compiles and runs it, and displays the result as an inline annotation.

Comment code evaluation verifies that documentation examples are correct and up to date, catching stale or broken code samples before they mislead users.

## Type Signature Search

Search for functions by their type signature in the **Search Everywhere** dialog (`Shift+Shift`).

### How to Use

1. Open Search Everywhere with `Shift+Shift`
2. Switch to the **Types** tab
3. Enter a type signature query (e.g., `string => int`, `array<'a> => int`, `(int, int) => int`)
4. Matching functions from the project and dependencies are listed

### Query Syntax

Type signature queries use standard ReScript type syntax:

| Query | Matches |
|-------|---------|
| `string => int` | Functions taking a string and returning an int |
| `array<'a> => int` | Functions taking any array and returning an int |
| `(int, int) => int` | Functions taking two ints and returning an int |
| `option<'a> => 'a` | Functions unwrapping option values |

The search matches against function type signatures from the project's stub index, providing fast lookup without requiring the LSP server.

When you know what type of function you need but not its name, type signature search lets you discover the right function by its shape — a natural fit for a type-inferred language like ReScript.

## Restart LSP Action

If the Language Server becomes unresponsive or you need to pick up configuration changes, you can restart it via **Tools > Restart ReScript Language Server**.

### When to Use

- After manually updating `@rescript/language-server`
- When the LSP server stops responding
- After changing LSP-related settings that require a server restart

The action is only available when a project is open. It stops the current LSP server instance and starts a fresh one.

A quick manual restart is the simplest fix when the Language Server gets into a bad state, avoiding the need to restart the entire IDE.

## LSP Initialization Options

The plugin sends several initialization options to the ReScript Language Server, matching the settings available in the VSCode extension. Configure these in **Settings > Languages & Frameworks > ReScript**.

### Available Options

| Setting | Default | Description |
|---------|---------|-------------|
| **Enable signature help** | On | Show function parameter information on `(` input |
| **Signature help for constructor payloads** | On | Show signature help for variant constructor payloads |
| **Enable project config caching** | On | Cache project configuration for faster LSP startup |
| **Enable inlay hints** | Off | Show LSP-provided inlay hints in the editor (experimental) |
| **Inlay hints max length** | 25 | Maximum character length for inlay hint labels (0 = unlimited) |
| **Enable compile status** | On | Receive compile status notifications from the LSP server |

Changes to these settings take effect after the LSP server restarts (which happens automatically when you click **Apply** in the settings dialog).

These initialization options give you the same configuration flexibility as the VSCode extension, so you can fine-tune LSP behavior like signature help, caching, and inlay hints to match your preferences.

## Dump LSP State

The **Dump LSP State** action displays diagnostic information about the ReScript Language Server for troubleshooting.

**Access:** **Tools** > **Dump ReScript LSP State**

### What It Shows

The action collects and displays:

- LSP server status and count
- ReScript project detection information
- Relevant plugin settings

### Use Cases

- **Troubleshooting LSP issues** — Verify the LSP server is running and configured correctly
- **Bug reports** — Include LSP state when reporting issues to plugin maintainers
- **Debugging** — Check what configuration the LSP server has loaded

Dump LSP State provides the diagnostic information needed to troubleshoot Language Server issues or include in bug reports, without manually inspecting configuration files or log output.

## Predefined Code Style

The plugin registers a "ReScript Standard" predefined code style that can be applied via **Settings** > **Editor** > **Code Style** > **ReScript** > **Set from...** > **Predefined Style** > **ReScript Standard**.

This provides a one-click way to configure indentation and formatting settings to match the standard ReScript conventions (2-space indentation, no tabs).

For manual indentation and tab/space configuration, go to **Settings** > **Editor** > **Code Style** > **ReScript**.

The predefined code style gives you correct ReScript formatting conventions in one click, so you do not need to configure indentation settings manually.

## Element Descriptions

The plugin provides human-readable descriptions of ReScript elements for use in IDE dialogs such as **Find Usages**, **Safe Delete**, and refactoring confirmations.

For example, when using Safe Delete on a function, the confirmation dialog shows:

> Delete function 'greet'?

rather than a generic "Delete element" message. This applies to `let` bindings, `type` declarations, `module` declarations, `external` declarations, and `exception` declarations.

Descriptive element names in IDE dialogs make refactoring confirmations clearer — you see "Delete function 'greet'" instead of a generic message, reducing the risk of accidental deletions.

## Build Watch Auto-Start Prompt

When you open a ReScript project, the plugin shows a one-time balloon notification offering to start the ReScript watch build (`rescript build -w`).

### When It Appears

The prompt appears at project startup if:

- The project contains `rescript.json`
- The ReScript CLI (`rescript`) is found in `node_modules/.bin/`
- The prompt hasn't been dismissed for the current IDE session

### Notification Actions

| Action | Description |
|--------|-------------|
| **Start Build Watch** | Launches `rescript build -w` via a Run Configuration |
| **Don't ask again** | Dismisses the prompt for this IDE session |

Clicking **Start Build Watch** opens the Run tool window with a live-recompiling build process.

The build watch prompt ensures you start getting live compilation feedback from the moment you open your project, without needing to remember to run the build command manually.

## REPL

An interactive read-eval-print loop for executing ReScript code snippets directly within the IDE.

**Open:** **View** > **Tool Windows** > **ReScript REPL**

### How to Use

1. Open the REPL tool window
2. Type ReScript code in the input area at the bottom
3. Click **Run** to execute
4. Output appears in the output area above
5. Click **Clear** to reset the output

### Expression Handling

The REPL automatically wraps simple expressions for output:

- `let` bindings, `type`/`module` declarations, and `open` statements are used as-is
- Code already containing `Js.log` or `Console.log` is used as-is
- Simple expressions are automatically wrapped: `1 + 2` becomes `Js.log(1 + 2)`

```rescript
// Input: simple expression
1 + 2
// Output: 3

// Input: let binding
let greeting = "Hello"
Js.log(greeting)
// Output: Hello
```

### How It Works

Each execution is isolated:

1. Creates a temporary `.res` file with the user code
2. Compiles with `npx rescript build`
3. Executes the compiled JavaScript with `node`
4. Displays stdout/stderr in the output area

### Requirements

- ReScript CLI (`rescript`) must be installed in the project
- Node.js must be available in PATH

### Limitations

- No persistent state between executions (each run is isolated)
- 30-second timeout for compilation and execution

The REPL provides an interactive feedback loop for testing expressions and exploring APIs without creating files, compiling, and running — ideal for learning ReScript or verifying quick assumptions.

## Worksheet Mode

Worksheet files (`.resw`) allow you to write ReScript code and have each top-level expression evaluated with results displayed inline.

### How to Use

1. Create a new file with the `.resw` extension
2. Write ReScript code with top-level expressions
3. Each expression is evaluated and the result is displayed as an inline comment

```rescript
let x = 1 + 2
// => 3

let greeting = "Hello, " ++ "World!"
// => Hello, World!

type color = Red | Green | Blue
// (type declarations are skipped)
```

### Expression Grouping

The worksheet understands multi-line expressions by tracking brace and parenthesis depth. Empty lines, comments, type declarations, module declarations, and `open` statements are skipped during evaluation.

### Requirements

- ReScript CLI (`rescript`) must be installed in the project
- Node.js must be available in PATH

Worksheets provide a notebook-like experience where you see every expression's result inline, making them ideal for prototyping algorithms, testing transformations, and verifying documentation examples.

## Scratch Files

Create temporary ReScript files in the IDE's Scratches panel for quick experiments without adding files to your project.

### How to Use

1. Open **File** > **New** > **Scratch File** (or `Cmd+Shift+N` on macOS)
2. Select **ReScript** from the language list
3. A new scratch file opens with a default template:

```rescript
// ReScript Scratch File
let result = "Hello"
Js.log(result)
```

### Features

- Full ReScript syntax highlighting and language support
- Standalone files stored outside your project directory
- Can be compiled and run like normal `.res` files
- Useful for prototyping, testing library functions, or learning ReScript syntax

Scratch files give you a disposable workspace for quick experiments without adding files to your project or polluting your source tree.

## Call Hierarchy

View the call graph around a function, showing both what calls it (Callers) and what it calls (Callees).

**Open:** Place the cursor on a function name and press `Ctrl+Alt+H` (`Cmd+Alt+H` on macOS)

### View Modes

The hierarchy browser provides two tabs:

**Callers** (default) --- Shows all functions in the project that call the selected function. Discovery uses text-based search across the entire project.

**Callees** --- Shows all functions that the selected function calls. Discovery scans the function body within the same file.

### Navigation

- Double-click a node to navigate to the function source
- Use Previous/Next buttons to navigate between functions
- Nodes are sorted alphabetically

### How It Works

The call hierarchy uses PSI-based text search rather than LSP:

- **Callers:** Searches for text occurrences of the function name across all project files
- **Callees:** Scans the function body for identifier tokens and cross-references them against declarations in the same file

### Limitations

- Text-based matching --- aliased or module-qualified calls may not be found
- Callees are detected within the same file only (no cross-file callee detection)
- Works on `let` and `external` declarations in `.res` files

Understanding the call chain of a function is essential when refactoring or debugging — this view reveals who calls a function and what it calls without manually tracing through the code.
