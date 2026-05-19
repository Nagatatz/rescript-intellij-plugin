---
myst:
  html_meta:
    "keywords": "advanced features, code lens, inlay hints, project wizard, REPL, worksheet"
---

# Advanced Features

These features provide additional productivity tools for ReScript development.

## Monorepo Support

The plugin recognises ReScript projects even when `rescript.json` (or `bsconfig.json`) is not at the workspace root — typical of pnpm, npm, and yarn workspace setups where ReScript lives inside a sub-package.

### Auto-Detection Pipeline

The plugin walks the following layers in order, stopping at the first that yields at least one package root:

1. **Manual override** — entries from `Settings > Languages & Frameworks > ReScript > Project package roots`.
2. **Workspace files** — `packages:` from `pnpm-workspace.yaml`, then `workspaces` (array form) and `workspaces.packages` (yarn classic object form) from `package.json`.
3. **Depth-limited scan** — recursive walk up to four directories deep, skipping `node_modules`, `.git`, `build`, `dist`, `.pnpm`, and similar non-source folders.
4. **Parent walk** — fallback for opening a sub-directory whose ReScript config sits in an ancestor.

Detection results feed every monorepo-aware behaviour: the missing-config inspection, the compile-status widget, the LSP startup notification, and the LSP binary search inside `node_modules/.bin/rescript-language-server`.

### Manual Override

Open `Settings > Languages & Frameworks > ReScript` and list one path per line under **Project package roots**, relative to the project base directory:

```
packages/core
packages/server
```

Leaving the field empty restores auto-detection. Entries that do not exist or do not contain a ReScript configuration file are silently ignored, so you can iterate on the list without being blocked from saving.

### Limitations

- `!negation` glob entries and brace expansion are not interpreted in v1.
- The depth-limited fallback caps at four directory levels; deeper layouts must be declared via the manual override or workspace file.
- The LSP server itself receives the workspace root as its working directory; per-file project resolution is delegated to `@rescript/language-server`.

## Code Lens

{bdg-primary}`LSP Required`

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

Code Lens shows inferred type signatures directly above function definitions, so you can see the types without adding explicit annotations or hovering over each identifier.

:::{seealso}
[Syntax Highlighting](syntax-highlighting.md) covers the semantic token system that powers Code Lens type annotations.
:::

## Compiled JavaScript Preview

{bdg-success}`Native`

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

The Compiled JS Preview gives you a live side-by-side view of ReScript and its JavaScript output, helping you understand the compilation result, debug runtime issues, and verify performance characteristics without leaving the IDE.

## Module Hierarchy

{bdg-success}`Native`

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

The Module Hierarchy view reveals both the internal structure of a file and its external dependencies, making it easy to understand how modules are organized and interconnected without reading through `open` and `include` statements manually.

## Inlay Hints

{bdg-primary}`LSP Required`

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

Inlay hints make the type system visible without cluttering your source code — inferred types appear as subtle annotations next to each binding, giving you the benefit of explicit types with the conciseness of type inference.

## JSON Schema for rescript.json

{bdg-success}`Native`

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

JSON Schema support turns `rescript.json` editing from guesswork into a guided experience — auto-completion suggests valid configuration keys, and validation catches mistakes before you run the compiler.

## Markdown Code Fence Highlighting

{bdg-success}`Native`

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

ReScript code fences in Markdown files get the same syntax highlighting as `.res` files, making documentation, READMEs, and code examples visually consistent and easier to read.

## JavaScript Injection in %raw()

{bdg-success}`Native`

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

JavaScript injection inside `%raw()` means you get proper JS syntax highlighting and error detection when writing FFI code, rather than working with a plain uncolored string.

## RegExp Injection in %re()

{bdg-success}`Native`

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

RegExp injection gives you syntax highlighting and validation inside `%re()` patterns, helping you catch regex errors at edit time rather than discovering them at runtime.

## Injected Language Formatting

{bdg-success}`Native`

When you format a ReScript file (`Cmd+Option+L`), any injected language fragments (e.g., JavaScript inside `%raw()`) are also formatted according to their own language's formatting rules.

This feature provides a `FormattingModelBuilder` that delegates formatting to the injected language's formatter, so injected code stays properly formatted alongside your ReScript code.

When you format a ReScript file, injected JavaScript inside `%raw()` is also formatted according to its own rules, so a single format command keeps both languages clean.

## Grazie Integration

{bdg-success}`Native`

When the **Grazie** plugin is installed, the ReScript plugin extracts natural language text from comments and string literals for grammar and spell checking.

**Supported text domains:**
- **Comments** — Line comments (`//`), block comments (`/* */`), and documentation comments (`/** */`) are extracted as `COMMENTS` domain text
- **Strings** — String literals, template strings, and character literals are extracted as `LITERALS` domain text

This is an optional integration --- if Grazie is not installed, the feature is simply not available.

With Grazie integration, your ReScript comments and strings get the same grammar and spell checking as natural language text, improving documentation quality without switching tools.

## Index Pattern Builder

{bdg-success}`Native`

Enhances the IDE's TODO/FIXME detection by providing a lexer-based index pattern builder for ReScript files. This enables more accurate pattern matching within comments compared to the basic text-based approach.

The index pattern builder uses the ReScript JFlex lexer to correctly classify comment tokens (line comments, block comments, and doc comments), ensuring that TODO/FIXME patterns are only matched inside actual comments and not in string literals or code.

Lexer-aware TODO detection prevents false positives — only TODOs inside actual comments are indexed, not string literals containing the word "TODO".

## Element Signature Provider

{bdg-success}`Native`

Provides stable element signatures that persist editor fold states across IDE restarts. When you collapse code blocks in the editor, their folded state is remembered using a signature format (`TYPE#name#offset`) that survives file modifications.

This ensures your code folding preferences persist across IDE sessions — blocks you collapsed stay collapsed, even after editing and restarting.

## Project Wizard

{bdg-success}`Native`

Create new ReScript projects directly from the IDE with 16 pre-configured templates covering frontend, backend, serverless, mobile, and more.

### Steps

1. **File** > **New** > **Project**
2. Select **ReScript** from the generator list on the left
3. Configure project settings:
   - **Project name** and **location**
   - **Template** --- Choose from 16 project templates grouped by category
   - **Package manager** --- Choose between npm, pnpm, yarn, or bun
   - **Validation library** --- Choose between `zod` and `sury`. Every template wires the choice into a `Validation.res` whose input differs by template (HTTP body, CLI options, form input, IPC payload, config file, or public API arguments)
4. Click **Create** to generate the project

### Available Templates

| Category | Template | Description |
|----------|----------|-------------|
| Basic | **Basic** | Minimal ReScript project with console output |
| Frontend | **Vite + React** | React single-page application with Vite bundler |
| Frontend | **Next.js** | Server-side rendered React application with Next.js |
| Desktop | **Electron** | Cross-platform desktop application with Electron |
| Backend | **Hono (Node.js)** | Lightweight web server with Hono framework on Node.js |
| Backend | **Hono GraphQL** | Hono server hosting `graphql-yoga` at `/graphql` with GraphiQL UI and Drizzle persistence |
| Serverless | **Cloudflare Workers** | Serverless API on Cloudflare Workers with Hono |
| Serverless | **AWS Lambda** | Serverless function on AWS Lambda with Hono |
| Serverless | **Google Cloud Run** | Container-based service on Google Cloud Run with Hono |
| Mobile | **React Native (Expo)** | Mobile application with React Native and Expo |
| Mobile | **React Native (Community CLI)** | Mobile app with React Native Community CLI (bare workflow) for native Android/iOS access |
| Library | **npm Library** | Publishable npm package with `@genType` for TypeScript consumers |
| Tool | **CLI Tool** | Command-line tool with argument parsing |
| Full Stack | **Monorepo (Hono + React)** | Full-stack monorepo with Hono backend and React frontend |
| Full Stack | **Full-Stack (single package)** | Single-package alternative to Monorepo: one `package.json`, Hono backend + Vite+React client |
| Full Stack | **res-x (HTMX on Bun)** | Server-driven web app with `rescript-x` JSX + HTMX, running on Bun + Vite |

For per-template detail pages (generated layout, dependencies, key files, scripts, day-two recipes), see {doc}`../templates/index`.

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

**React-based templates** (Vite+React, Next.js, Electron, React Native (Expo), React Native (Community CLI)) include JSX configuration in `rescript.json` and React dependencies.

**React Native (Community CLI)** targets Android Studio / Xcode users who need direct access to the native `android/` and `ios/` projects. The template ships only the JavaScript/TypeScript + ReScript surface and a `metro.config.js` that resolves `.res.mjs`; the native projects themselves are produced by running `@react-native-community/cli` after project creation. A `src/NativeGreeting.res` file demonstrates how to bind a custom Kotlin/Swift `NativeModule` through `@module("react-native") @scope("NativeModules")`, though the Kotlin/Swift implementation itself is outside the scope of the template and should be written following the [official React Native docs](https://reactnative.dev/docs/legacy/native-modules-android).

**Hono-based templates** (Hono, Hono GraphQL, Cloudflare Workers, AWS Lambda, Google Cloud Run) share common Hono bindings (`src/Hono.res`) and differ in their deployment configuration:
- **Hono (Node.js)** --- Uses `@hono/node-server` for local development
- **Cloudflare Workers** --- Includes `wrangler.jsonc` configuration
- **AWS Lambda** --- Includes esbuild bundling and Lambda adapter bindings
- **Google Cloud Run** --- Includes a `Dockerfile` for containerized deployment

**npm Library** includes `@genType` configuration for generating TypeScript type definitions.

**CLI Tool** includes a `bin` entry in `package.json` and argument parsing via `Process.argv`.

### Validation Library

All 16 templates ship a `Validation.res` module whose backing library is selected via the **Validation library** wizard option --- either [`zod`](https://zod.dev) (default) or [`sury`](https://github.com/DZakh/sury) (ReScript-native). The function signature is the same across backends --- `parseXxx: <input> => result<T, string>` --- so callers don't need to branch on the library choice. What each template validates depends on the shape of its input boundary:

| Template | Validates |
|----------|-----------|
| Hono / Hono GraphQL / AWS Lambda / Cloudflare Workers / Google Cloud Run / Next.js / Full-Stack / Monorepo / res-x | Incoming HTTP JSON bodies (failures short-circuit to HTTP 400) |
| CLI Tool | `init --name / --dir` subcommand options |
| npm Library | Public API arguments from JS/TS consumers |
| Basic | `config.json` shape when `--config` is supplied |
| Electron | IPC responses returned from the main process |
| React Native (Expo) / React Native (Community CLI) | Draft todo input on the form |
| Vite + React | Greet-form input before the fetch call |

### After Project Creation

After the wizard creates the project:

1. Run your package manager's install command (e.g., `pnpm install`) to install dependencies
2. Run `rescript build` (or use the ReScript run configuration) to compile the project
3. The Language Server will start automatically once `@rescript/language-server` is available in `node_modules`

### Quality of Life

Every generated project ships with the same baseline so you can start coding right away:

- **README.md** — Documents prerequisites, install/dev commands tuned to the selected package manager, and template-specific deployment notes (e.g. Cloudflare Workers `wrangler deploy`, AWS Lambda upload, Cloud Run `gcloud run deploy`)
- **`.gitignore`** — Excludes `node_modules/`, ReScript build artifacts, OS files, and template-specific output (`.next/`, `dist/`, `.wrangler/`, …)
- **`.editorconfig`** — Pins indentation (2 spaces) and line endings (LF)
- **`.github/workflows/ci.yml`** — Minimal CI pipeline that installs dependencies and runs `rescript`, plus the `build` / `test` script when the template defines one
- **`packageManager` field in `package.json`** — Pins the toolchain version for [Corepack](https://nodejs.org/api/corepack.html) so collaborators get the same package manager
- **Vitest smoke test + coverage** — Every template ships `src/__tests__/*.test.mjs` (or workspace equivalents) wired to `test` and `test:coverage` scripts (backed by `@vitest/coverage-v8`). Monorepo fans out with `pnpm -r run test` / `yarn workspaces foreach` / `npm --workspaces run test --if-present`; React Native uses a filesystem smoke test since `react-native` won't load under Node; Hono-based templates use Hono's built-in `app.request()` harness to hit DB-free baseline routes
- **`.nvmrc` / `LICENSE` / `.github/dependabot.yml`** — Every template pins the Node major version, ships an MIT license using the project name as the copyright holder, and wires Dependabot to poll npm + GitHub Actions dependencies weekly
- **`.env.example`** — Templates that read environment variables (Hono REST, Hono GraphQL, Full-Stack, Monorepo server, Google Cloud Run) ship a `.env.example` documenting the expected keys (`DATABASE_URL`, `PORT`, etc.); `.env` is added to `.gitignore` so populated copies never get committed
- **Hono `app.onError` global handler** — Hono-based templates (REST, GraphQL, Full-Stack, Monorepo server) wire `app.onError` to log the exception and return a JSON 500, so uncaught errors never leak a raw stack trace to clients
- **Centralized dependency versions** — All template versions live in `wizard/templates/TemplateVersions.kt`; a nightly GitHub Actions job (`integration-tests.yml`) verifies that every template still installs and compiles

### Vite+ Toolchain (Vite + React, Electron, Monorepo)

The Vite + React, Electron, and Monorepo templates use [Vite+](https://vite.plus) (`vite-plus`) — a unified wrapper that bundles Vite, Vitest, Oxlint, Oxfmt, and Rolldown. Scripts are exposed as `vp dev`, `vp build`, `vp test`, etc.

> **Known issue:** Vite+ is **pre-1.0** and currently does not link cleanly with `@vitejs/plugin-react` via pnpm's nested store, so `vp build` may fail with `ERR_MODULE_NOT_FOUND` on `vite/internal`. As a fallback, replace `vite-plus` with `vite` in `vite.config.mjs` and switch the npm scripts to `vite` / `vite build`. The migration back to Vite+ is a one-line change once Vite+ stabilizes.

The Project Wizard lets you create a fully configured ReScript project in seconds — select a template, choose your package manager, and get a ready-to-build project without manually writing configuration files.

## File Templates

{bdg-success}`Native`

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

File templates give you a consistent starting point for new modules, interfaces, and React components, so every new file follows the same pattern without copying boilerplate from an existing file.

## .d.ts Binding Generation

{bdg-success}`Native`

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

Writing ReScript FFI bindings for TypeScript libraries is one of the most tedious tasks in ReScript development — this generator automates the conversion, producing correct `external` declarations that you can refine rather than write from scratch.

## Color Preview

{bdg-success}`Native`

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

Inline color swatches let you visually verify color values without running the application — see the actual color next to the hex, RGB, or HSL code and click to open the color picker for adjustments.

## VCS Code Vision

{bdg-success}`Native`

Author and last-change annotations appear on top-level declarations (let, type, module, external), providing Git blame information directly in the editor. Enable via **Settings** > **Editor** > **Inlay Hints** > **Code Vision**.

VCS annotations on declarations show who last changed each function or type, so you know who to ask about unfamiliar code without running `git blame` separately.

## Package Dependencies

{bdg-success}`Native`

A dedicated tool window shows the dependencies and devDependencies from your `rescript.json`:

**Open:** **View** > **Tool Windows** > **ReScript Dependencies**

The tree view organizes packages into "Dependencies" and "Dev Dependencies" groups with version numbers.

The Package Dependencies view gives you a quick overview of your project's ReScript dependencies and their versions without opening `rescript.json` or running `npm list`.

## Quick Documentation

{bdg-primary}`LSP Required`

Press `Ctrl+Q` (or hover) to see documentation for ReScript elements. When the LSP server is connected, documentation comes from the language server. When LSP is unavailable, a PSI-based fallback shows the declaration type, name, and source file.

Quick Documentation surfaces type information and doc comments without navigating away from your current position, letting you understand APIs inline while coding.

## Safe Delete

{bdg-success}`Native`

Use **Refactor** > **Safe Delete** to delete ReScript declarations with usage checking. If the element is still referenced, a confirmation dialog shows all usage locations before proceeding.

Safe Delete prevents accidental breakage by checking for references before removing a declaration, so you can confidently clean up code without worrying about hidden dependencies.

## Name Suggestions

{bdg-success}`Native`

During rename refactoring, the plugin suggests names based on:
- The element's type (e.g., `user` for `User.t`)
- The containing file name
- camelCase conversion from snake_case

Intelligent name suggestions speed up rename refactoring by proposing contextually appropriate names, so you can pick a good name from a list rather than inventing one from scratch.

## Reader Mode

{bdg-success}`Native`

Files in `node_modules/` directories are automatically displayed in Reader Mode, providing a cleaner read-only view for library source files.

Reader Mode gives library source files a clean, distraction-free presentation, making it easier to read third-party code when exploring how a dependency works.

## TODO Indexing

{bdg-success}`Native`

The plugin integrates with IntelliJ's TODO tool window (`Alt+6` > **TODO** tab) to detect and list TODO, FIXME, and other task comments in ReScript files.

Recognized patterns include:
- `// TODO: ...`
- `// FIXME: ...`
- `/* TODO: ... */`

TODO items appear in the IDE's **TODO** panel alongside items from other file types in your project. You can customize TODO patterns and filters in **Settings** > **Editor** > **TODO**.

TODO indexing brings your ReScript task comments into the IDE's unified TODO panel, so you can track outstanding work across all languages in one place.

## Open Statement Index

{bdg-success}`Native`

The plugin indexes all `open` statements across your project for fast module resolution. This powers features like auto-import suggestions and module dependency analysis.

The open statement index enables instant module lookups across the entire project, powering auto-import and dependency analysis without scanning files on every request.

## Project View Enhancements

{bdg-success}`Native`

- **Interface indicator:** `.res` files with a corresponding `.resi` show a "(has .resi)" suffix
- **Version display:** `rescript.json` shows the ReScript version from its content
- **Compiled JS nesting:** Compiled JS files (`.res.js`/`.mjs`/`.cjs`, `.bs.js`/`.mjs`/`.cjs`) are nested under their corresponding `.res` source file in the Project panel, reducing visual clutter
- **Compiled JS graying:** Nested compiled JS files are displayed in gray text to visually distinguish generated output from source files

Project View enhancements reduce visual clutter by nesting generated files under their sources and surfacing useful metadata like interface presence and ReScript version, so the file tree stays focused on your source code.

## Auto Import Options

{bdg-success}`Native`

Configure auto-import behavior in **Settings** > **Editor** > **General** > **Auto Import**:
- Toggle automatic `open` statement insertion
- Exclude specific modules from auto-import

Fine-grained auto-import settings let you control which modules are automatically opened, preventing unwanted imports from cluttering your files.

## Expression Type

{bdg-primary}`LSP Required`

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

{bdg-primary}`LSP Required`

A persistent tool window that continuously displays the inferred type of the expression at the current caret position. Unlike Expression Type (`Ctrl+Shift+P`) which shows types on demand, the Type Info Tool Window updates automatically as you navigate through code.

**Open:** **View** > **Tool Windows** > **ReScript Type**

### How It Works

1. As you move the caret in a ReScript file, the tool window sends an LSP `textDocument/hover` request for the current position
2. The response is debounced to avoid excessive requests during rapid navigation
3. The inferred type is displayed in the tool window panel, updating in real time

### Syntax Highlighting

The type string is rendered through an `EditorTextField` bound to the ReScript file type, so keywords, type constructors, operators and type variables receive the same colours the editor scheme assigns inside an open `.res` file. Switching to a different IDE colour scheme refreshes the panel automatically — no separate theme configuration is needed.

### Use Cases

- **Exploring unfamiliar code** --- See types continuously without pressing any shortcut
- **Debugging type errors** --- Move through expressions to understand where types diverge
- **Learning ReScript** --- Observe how the type system infers types for different expressions

### Requirements

The Type Info Tool Window requires the Language Server to be running. If LSP is not connected, the panel shows a "No type information available" message.

The Type Info Tool Window provides always-on type visibility as you navigate code — unlike Expression Type which requires a shortcut, this panel updates automatically, making it ideal for exploring unfamiliar codebases.

## LSP Auto-Install

{bdg-success}`Native`

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

{bdg-success}`Native`

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

{bdg-success}`Native`

Suppress specific inspections using `// noinspection` comments:

```rescript
// noinspection RescriptDuplicateOpen
open Belt
open Belt  // This duplicate open won't be flagged
```

When an inspection produces false positives in specific locations, you can suppress it with a comment rather than disabling it globally, keeping the inspection active elsewhere.

## Framework Detector

{bdg-success}`Native`

The plugin automatically detects ReScript projects by looking for `rescript.json` files. When a project containing `rescript.json` is opened, the IDE recognizes it as a ReScript project and suggests configuring the framework accordingly.

This enables framework-aware features like project-specific settings and tool integrations.

Automatic framework detection means the plugin activates its full feature set as soon as you open a ReScript project, with no manual configuration required.

## Code Rearranger

{bdg-success}`Native`

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

## Module Dependency Diagram

{bdg-success}`Native`

Visualize module dependency relationships as a top-down layered graph drawn inside the IDE, or as Mermaid / Graphviz DOT source text you can paste into an external renderer.

**Open:** **View** > **Tool Windows** > **ReScript Module Diagram**, or use **Tools** > **Show ReScript Module Diagram**

### How It Works

The diagram provider scans every `.res` file in the project and builds a directed graph from `open` and `include` statements. Each module becomes a node; every `open ModuleName` or `include ModuleName` becomes an edge from the current module to the referenced one. The result is fed into both rendering modes simultaneously, so toggling between them does not rebuild the graph.

### Visual vs Source Mode

A **Visual / Source** toggle on the toolbar swaps between two rendering modes of the same graph:

- **Visual mode** (default) — A Java2D-rendered top-down layered diagram. Layer assignment uses Kahn's BFS: modules with no incoming edges (typically entry points such as `Main`) sit on the top layer; each downstream dependency is pushed one layer lower. Modules that participate in a cycle land on a single extra layer beneath the rest, so they stay visible even though Kahn's algorithm cannot order them. Edges are drawn as orthogonal arrows from the source module's bottom edge to the target module's top edge. Self-loops are suppressed.
- **Source mode** — A read-only `JEditorPane` displaying the Mermaid `flowchart TD` source. Tokens are colour-coded by `MermaidSourceColorizer` (`flowchart` / `graph` / `TD` / `LR` / `subgraph` / `end` in keyword colour; `-->` / `---` / `-.->` / `==>` in operator colour; `"…"` node labels in string colour; `%%` comments in comment colour), so the source view reads like code rather than monochrome plain text. Copy-as-Mermaid still exports the raw text.

### Node Colour Coding

Visual mode classifies every module into one of four **structural roles** based on its position in the dependency graph, and paints the box with a role-specific colour. A legend strip at the bottom of the canvas spells out the mapping.

| Role | Definition | What it tells you |
|------|------------|-------------------|
| Entry point | in-degree 0, not part of a cycle | A module nothing else depends on — typically `Main`, app entry, or a top-level binary |
| Intermediate | both in-degree and out-degree are positive | A transit module: depends on others and is depended on by others |
| Leaf | out-degree 0, not part of a cycle | A pure sink: opens / includes no other modules in the project (often a primitive utility) |
| Cycle | Kahn's BFS cannot drain it | A member of a strongly connected component — a circular dependency that needs untangling |

Cycle members are coloured distinctly so they jump out even when they sit on the fallback layer. The palette is built from `JBColor(light, dark)` pairs so both Light and Dark IDE themes get readable contrast.

### Tool Window Layout

- **Toolbar:** Visual / Source toggle, Refresh (rebuild the graph from current PSI), Copy as DOT, Copy as Mermaid
- **Main area:** Visual graph or Mermaid source, swapped in place via `CardLayout`
- **Status bar:** Module count and edge count

### Exporting

Both toolbar copy actions remain available in either mode:

- **Copy as Mermaid** — Puts the `flowchart TD` text on the clipboard. Paste into [Mermaid Live Editor](https://mermaid.live) or any Markdown file with Mermaid support to render the graph
- **Copy as DOT** — Puts a Graphviz `digraph` on the clipboard. Pipe into `dot -Tpng` or paste into a `.dot` file for rendering with Graphviz

Module names containing spaces, dots, or quotes are automatically escaped so the exported text is safe to feed into either renderer.

### Use Cases

- **Understanding project structure** — See how modules relate to each other at a glance without leaving the IDE
- **Identifying tight coupling** — Spot modules with too many dependencies in Visual mode
- **Spotting cycles** — Cycle members appear together on the fallback layer, so a circular dependency is visually distinct from a clean DAG
- **Refactoring planning** — Understand the impact of moving or splitting modules; switch to Source mode and copy to share findings in a PR or design doc

The dependency diagram reveals your project's module structure visually, making it easy to spot circular dependencies, tightly coupled modules, and refactoring opportunities that are hard to see in code alone.

## Variant Flow Diagram

{bdg-success}`Native`

Visualize the `switch` expression under the caret as a decision tree, so the structure of large or nested matches is obvious at a glance instead of having to scroll the source.

**Open:** **View** > **Tool Windows** > **ReScript Switch Flow**, or use **Tools** > **Show Switch Flow Diagram**

### How It Works

The provider scans the file with the same syntactic switch arm collector that powers the type narrowing visualizer, picks the innermost `switch` containing the caret (the `switch` keyword itself counts), and emits a Mermaid `flowchart TD` node tree. The root node is the scrutinee; each arm becomes a child node whose edge label is the pattern summary (`Some(_)`, `Ok(_)`, …) and whose node label is the arm body's first non-blank line, capped at 40 characters. A nested `switch` inside an arm body expands recursively; depth past three levels is collapsed to a single "(deeper switch hidden)" leaf to keep the picture readable.

The view re-renders 200 ms after each caret movement, so moving the cursor across a file scans through the project's `switch` expressions naturally.

### Visual vs Source Modes

The tool window has two display modes, selected from the toolbar:

- **Visual** (default) — A self-contained Swing graph view paints the scrutinee and each arm as rounded boxes connected by orthogonal arrows. The geometry is computed by a pure `computeLayout` helper, so the renderer needs no JCEF browser, no `mermaid.js`, and no external Graphviz binary.
- **Source** — A read-only `JEditorPane` displaying the Mermaid `flowchart TD` source with token colouring from `MermaidSourceColorizer` (keywords / arrows / quoted node labels / `%%` comments) so the source view reads like code. Toggle to this mode when you want to inspect the diagram text before sharing it; Copy-as-Mermaid still exports the raw text.

Visual mode expands nested `switch` arms as sub-trees beneath their parent arm box, mirroring what the Mermaid and DOT exporters already emit and capped at the same `MAX_NESTING_DEPTH` (deeper branches collapse into a single "(deeper switch hidden)" leaf). For flat single-level matches it falls back to a row of arm boxes that wraps onto additional rows when the tool window is too narrow, so the diagram stays usable in any layout. Source mode renders the Mermaid text with line-aware highlighting and keeps the underlying Mermaid string as the source of truth for both copy actions described below.

### Arm Colour Coding

Visual mode classifies every arm into one of five **arm kinds** and paints the box with a kind-specific colour. A legend strip at the bottom of the canvas spells out the mapping so the picture is self-describing.

| Arm kind | Trigger | When you see it |
|----------|---------|-----------------|
| Constructor | Pattern starts with an uppercase identifier (e.g. `Some(_)`, `Ok(value)`, `Red`) | Variant or polymorphic-variant constructor arms |
| Wildcard | Pattern is exactly `_` | Catch-all arms that match anything without binding |
| Binding | Pattern is a single lowercase identifier (e.g. `| other =>`) | Catch-all arms that bind the scrutinee |
| Todo | Body text starts with the `todo` literal | Unimplemented arms inserted by the Add Missing Switch Arms intention or written by hand |
| Nested | The arm contains another `switch` and renders a sub-tree | Multi-level pattern matches |

The palette is built from `JBColor(light, dark)` pairs so both Light and Dark IDE themes get readable contrast. Use the colours to spot incomplete arms (the yellow `Todo` row jumps out in a long match) or to follow the structural shape of nested switches without reading the labels.

### Tool Window Layout

- **Toolbar:** Visual / Source toggles, Refresh (rebuild the diagram), Jump to Switch, Copy Mermaid, Copy DOT
- **Main area:** The Visual graph view or the read-only Mermaid source, depending on the active toggle
- **Status bar:** Number of arms in the current diagram, or a placeholder when the caret is not inside a `switch`

### Exporting

- **Copy Mermaid** — Puts the `flowchart TD` text on the clipboard. Paste into [Mermaid Live Editor](https://mermaid.live) or any Markdown file with Mermaid support
- **Copy DOT** — Puts a Graphviz `digraph` on the clipboard. Pipe into `dot -Tpng` or save to a `.dot` file for rendering

Quotes, backslashes, and newlines inside arm bodies are escaped so the exported text is always safe to feed into either renderer.

::::{tab-set}
:::{tab-item} Source
```rescript
let describe = result =>
  switch result {
  | Ok(value) => value
  | Error(_) => "fallback"
  }
```
:::
:::{tab-item} Mermaid Output
```
flowchart TD
  root["switch result"]
  n0["value"]
  root -->|"Ok(_)"| n0
  n1["fallback"]
  root -->|"Error(_)"| n1
```
:::
::::

### Use Cases

- **Reviewing exhaustive matches** — See every arm and its body preview side by side, even when the source spans many lines
- **Sharing a code path** — Copy a Mermaid or DOT picture into a PR or design doc instead of pasting the whole `switch`
- **Onboarding new contributors** — Help readers follow the variant logic without forcing them to keep large pattern trees in their head

The flow diagram is purely syntactic, so it works even when the LSP server is not running.

## Type Impact Preview

{bdg-success}`Native`

Estimate the blast radius of a type change before you make it. Place the caret on a `type` declaration and the tool window lists every project-wide reference, each tagged with the role it plays (type annotation, variant constructor, pattern, field access).

**Open:** **View** > **Tool Windows** > **ReScript Type Impact**, or use **Tools** > **Show Type Impact**

### How It Works

The provider walks PSI up from the caret to the enclosing `type` declaration and reconstructs a dotted module path (`Outer.Inner.t`). It then drives `PsiSearchHelper.processElementsWithWord` over the project scope to collect every occurrence of the type's local name. Each hit is classified by `RescriptReferenceClassifier`, a small token-based heuristic that inspects the immediately preceding non-whitespace character:

- `:` → **type-ref** (e.g. `let x: t = ...`)
- `.` → **field-access** (e.g. `user.t`)
- `|` → **pattern** or **constructor** depending on whether the next identifier is uppercase and followed by `(`
- otherwise → **constructor** for uppercase identifiers, **unknown** otherwise

References that classify as `unknown` are still listed so the panel doesn't silently drop ambiguous hits.

### Tool Window Layout

- **Toolbar:** Refresh (re-scan references for the current type)
- **Main area:** A list of `[kind] file.res:line  preview` entries; double-click navigates to the reference site. The `[kind]` prefix is rendered in a kind-specific bold colour — type-ref (blue), constructor (purple), pattern (green), field-access (amber), unknown (grey) — so you can spot which flavour of reference dominates the list at a glance
- **Status bar:** `Type.name: N reference(s)` summary, with `(showing first 200)` appended when the result was truncated

### Soft cap

Up to 200 references are displayed per type. Beyond that, the truncation note appears in the status bar so authors know to either narrow their target type or rely on a more focused refactor.

### Use Cases

- **Sizing a type change** — Look at the kind distribution before renaming a field or removing a variant case
- **Audit before refactor** — Confirm that a `type t = int` alias really only appears in a few well-defined places before swapping in a richer type
- **Onboarding a codebase** — Use the panel as a navigable index of where each domain type is consumed

The impact preview is purely syntactic, so it works without LSP. The token-based classifier cannot follow aliases or `open` statements, so treat the kind labels as a quick guide rather than a precise semantic answer.

## Notebook-Style Worksheet

{bdg-success}`Native`

A cell-based editor for `.resnb` files that sits between the existing whole-file `.resw` Worksheet and the line-by-line REPL. Each cell holds a snippet of ReScript code and the captured output of its last run, so explorations can be saved and re-opened with their results intact, and shared as Markdown for PR descriptions or design docs.

**Open:** Create or open any file with the `.resnb` extension. The IDE substitutes a cell-based editor for the default JSON view.

### How It Works

A `.resnb` file is plain JSON of the form:

```json
{
  "version": 1,
  "cells": [
    {"code": "let x = 41", "lastOutput": ""},
    {"code": "Js.log(x + 1)", "lastOutput": "42"}
  ]
}
```

The custom `FileEditorProvider` parses this JSON into a `NotebookDocument`, hands it to `RescriptNotebookPanel`, and writes any change back through the in-memory document so the standard save lifecycle takes care of persistence. Cells run through the same `RescriptReplExecutor` used by the REPL tool window — each Run press wraps the cell's code in a temporary `.res` file, compiles it via `npx rescript build`, and executes the resulting JavaScript with `node`.

Invalid JSON is reported with a small warning header; the rest of the editor falls back to an empty notebook so users do not lose access to the file.

### Cell UI

- **Top:** A `Cell` label plus three icon buttons — Move Up, Move Down, Delete.
- **Code area:** An `EditorTextField` bound to the ReScript file type, so each cell receives the same syntax highlighting, code completion, and folding affordances as the REPL input field — keywords, type constructors, operators and string literals all pick up the editor scheme's colours.
- **Run button + Output area:** Pressing Run disables the button, evaluates the cell on a pooled thread, and renders the captured stdout/stderr in the output area. Errors render in red. All cell colours (border, output background, error / running-state foreground) use `JBColor(light, dark)` pairs, so both Light and Dark IDE themes give the cell adequate contrast.

### Toolbar

- **Add Cell** — Append a new empty cell.
- **Run All** — Trigger every cell's Run action in order.
- **Copy as Markdown** — Render the notebook as Markdown (`## Cell N` headings + `rescript` code fences + output fences) and place it on the clipboard for easy sharing.

### Use Cases

- **Investigations & POCs** — Keep code and observed output side by side instead of scrolling REPL history.
- **Bug reports & PR descriptions** — Copy a multi-cell exploration as Markdown straight into a GitHub issue.
- **Teaching & onboarding** — Author tutorials where the expected output is preserved alongside each example.

### Limitations (Phase 1)

- Cells are evaluated independently — the `let` from one cell is not visible in the next. State sharing is on the Phase 2 list.
- The cell editor offers ReScript syntax highlighting, but live LSP diagnostics inside a cell still require the standard editor. Authors who want red squigglies on every keystroke can keep a regular `.res` file open beside the notebook.
- No rich output (HTML, images). The output area is plain monospaced text.

## JS Interop Risk Map

{bdg-success}`Native`

Survey every place where the project leaves the type system: `%raw` / `%%raw` blocks, `external` declarations, `Obj.magic` casts, and `@bs.*` decorators. Each entry is tagged with a coarse risk level so reviewers can prioritise where to look.

**Open:** **View** > **Tool Windows** > **ReScript Interop Risk**, or use **Tools** > **Show JS Interop Risk Map**

### How It Works

`RescriptInteropScanner` walks every `.res` and `.resi` file in `GlobalSearchScope.projectScope` through `FileTypeIndex`, reads each one inside a read action, and feeds the lines to `RescriptInteropClassifier`. The classifier inspects each line in isolation:

- `Obj.magic` anywhere → **HIGH** risk, kind `obj-magic`
- `%raw` / `%%raw` → **HIGH** risk, kind `raw`
- `external …` plus `@bs.*` / `@send` / `@module` / `@scope` / `@val` decorators → **MEDIUM** risk, kind `external`
- Plain `external …` → **LOW** risk, kind `external`
- Standalone `@bs.*` / `@send` etc. → **LOW** risk, kind `bs-attr`

Soft caps keep the panel responsive: 50 entries per file, 500 entries total. When the cap fires, the status bar shows `(showing first 500)` so the result is never silently truncated.

### Tool Window Layout

- **Toolbar:** Refresh — re-runs the project-wide scan
- **Main area:** A list of `[risk/kind] file.res:line  preview` entries; double-click navigates to the line. Each row carries a 4 px-wide left-edge colour band that mirrors the risk level — **HIGH** red, **MEDIUM** amber, **LOW** grey — so severity is readable at a glance without parsing the `[risk/kind]` prefix
- **Status bar:** Total count, optional truncation note, and per-kind breakdown (`raw=N  obj-magic=N  external=N  bs-attr=N`)

The list is sorted by descending risk, then by file path and line number for stability.

### Use Cases

- **PR reviews** — Look at the high-risk rows first to scrutinise unsafe casts.
- **Library migration** — Track every `@bs.*` site that needs to move to the new `@send` / `@module` syntax.
- **Refactoring planning** — Find the small set of `%raw` blocks that gate a runtime upgrade.

The risk map is purely syntactic, so it works without LSP. The line-based heuristic cannot tell whether a particular `external` is genuinely safe — it surfaces every match and lets reviewers decide.

## Type Coverage Heat Map

{bdg-success}`Native`

A project-wide view of how many of your top-level `let` bindings carry an explicit type annotation versus how many fall through to inference. Use it to find the modules where adding a few `: int` / `: User.t` annotations would dramatically improve readability and review safety.

**Open:** **View** > **Tool Windows** > **ReScript Type Coverage**

### How It Works

`RescriptTypeCoverageScanner` walks every `.res` file in `GlobalSearchScope.projectScope` (capped at 2,000 files), splits each source into top-level `let` declarations using a lexer-based depth tracker, and feeds each declaration to `RescriptTypeCoverageClassifier`.

The classifier reports `ANNOTATED` when a `:` token appears at depth 0 between the binding name and the first depth-0 `=`. Parameter lists, record literals, and array literals push the depth counter, so `:` tokens inside them do not falsely register as annotations:

| Source | Result |
|--------|--------|
| `let x: int = 5` | ANNOTATED |
| `let f: (int, int) => int = (a, b) => a + b` | ANNOTATED |
| `let user: {name: string} = {name: "x"}` | ANNOTATED |
| `let x = 5` | INFERRED |
| `let f = (x: int) => x + 1` | INFERRED (annotation is param-only) |
| `let f = (x): int => x + 1` | INFERRED (annotation is return-only, after `=`) |

A binding annotated only on its parameters or return type is intentionally classified as INFERRED in v1 — readers of the file still have to fall back to inference for the binding itself. A future revision may surface a separate "param-annotated" tier.

### Tool Window Layout

- **Toolbar:** Refresh.
- **Table:** one row per scanned file with columns *File*, *Total*, *Annotated*, *Inferred*, *Coverage %*. The *Coverage %* cell is colour-coded — red < 30%, yellow 30–69%, green ≥ 70%.
- **Default sort:** ascending by Coverage %, so the files most in need of annotation float to the top. Click any column header to re-sort.
- **Status bar:** `<files> files, <bindings> bindings, <pct>% project coverage`. When the scanner hits the 2,000-file hard cap a `(truncated …)` note is appended.

Double-click a row to jump to that file in the editor.

### Use Cases

- **Public-API tightening** — Find library entry points or workspace boundaries that lean entirely on inference and add explicit signatures before publishing.
- **Refactor preparation** — Before reshaping a domain module, raise its coverage so the next change has clear documentation in source.
- **Onboarding** — Hand a low-coverage file to a new contributor as a graspable, type-by-type annotation task.

### Limitations

- Module-internal `let`s (declarations inside `module M = { … }`) are not counted; only depth-0 file-top-level bindings contribute to the totals, mirroring what the lightweight PSI parser models.
- The classifier is purely syntactic — it does not consult the language server, so very unusual formatting (e.g. annotations split across many lines with embedded comments) may occasionally misclassify. Double-check borderline rows by opening the file.
- Files with zero `let` declarations report 100% (vacuously covered).

## PPX Expansion View

{bdg-primary}`LSP Required`

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

### Annotation Highlighting

The panel renders its summary as HTML through a `JEditorPane`. Every `@annotation` token (e.g. `@react.component`, `@deriving`, `@module`) is wrapped in a bold `<span>` whose colour is sourced from the editor scheme's annotation attribute, so the annotation names jump out against the surrounding English description text. Switching IDE theme refreshes the colour automatically — re-open the tool window to re-render.

PPX macros generate code that you never see in your source files — this view makes the generated code visible, helping you debug PPX-related issues and understand what the compiler actually produces.

## Comment Code Evaluation

{bdg-success}`Native`

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

{bdg-success}`Native`

Search for functions by their type signature in the **Search Everywhere** dialog (`Shift+Shift`). Both the user query and each candidate signature are parsed into a structural AST and compared by `RescriptTypeUnifier`, so only declarations whose annotated type actually fits the query — not just contains the same words — appear in the result list.

### How to Use

1. Open Search Everywhere with `Shift+Shift`
2. Switch to the **ReScript Types** tab
3. Enter a type signature query (e.g., `string => int`, `array<'a> => int`, `(int, int) => int`)
4. Matching declarations from the project's `.res` and `.resi` files are listed as `name: signature  (relative/path:line)` and ordered by match strength

### Query Syntax

Type signature queries use standard ReScript type syntax:

| Query | Matches |
|-------|---------|
| `string => int` | Functions whose annotated type is exactly `string => int` |
| `array<'a> => int` | Functions whose first arg is some `array` and that return `int` |
| `(int, int) => int` | Two-int functions returning int |
| `option<'a> => 'a` | Functions whose first arg is some `option` and whose return type matches that arg |
| `=> result<int, string>` | "Returns T" mode — anything ending in that result, regardless of inputs |

### Match Tiers

The unifier emits one of four scores per candidate; higher-score hits float to the top of the list.

| Tier | When | Weight |
|------|------|--------|
| `EXACT` | Query and candidate have the same structure with identical type names | 100 |
| `TVAR_MATCH` | Query type variable (`'a`) absorbed a concrete type in the candidate | 60 |
| `PARTIAL` | "Returns T" query (`=> T`) matched the candidate's right-hand side | 30 |
| `MISMATCH` | Structures cannot be reconciled (different ctors / arity / arrow vs. tuple / …) | 0 (filtered out) |

### Limitations

- **Annotation required.** The contributor only sees declarations carrying an explicit `: T = …` annotation. Inferred types (e.g. `let x = 5`) don't participate; rely on the language server (`Go to Symbol`, `Find Usages`) or use the Type Coverage Heat Map to find files in need of annotation.
- **Subset of ReScript types.** Records (`{name: string}`), polymorphic variants (`[#Foo | #Bar]`), and labeled-argument signatures (`(~name: string) => unit`) are out of scope for the parser; those candidates simply don't appear in the result list.
- **Concrete query against polymorphic candidate is a mismatch.** Searching `int` will not match `let id: 'a => 'a` — the query says "I want exactly `int`" and the candidate is polymorphic. Use type variables in the query side when you want the unifier to absorb concrete candidates.

### Result Highlighting

Result rows are rendered as `name: signature  (path:line)`. The `signature` portion is tokenised through `RescriptLexer` and each token is coloured via the editor scheme — keywords, type constructors, operators, type variables and punctuation pick up the same attributes they would inside an open `.res` file, so the list reads like editor source rather than one grey-italic blob.

When you know what type of function you need but not its name, type signature search lets you discover the right function by its shape — a natural fit for a type-inferred language like ReScript.

## Restart LSP Action

{bdg-primary}`LSP Required`

If the Language Server becomes unresponsive or you need to pick up configuration changes, you can restart it via **Tools > Restart ReScript Language Server**.

### When to Use

- After manually updating `@rescript/language-server`
- When the LSP server stops responding
- After changing LSP-related settings that require a server restart

The action is only available when a project is open. It stops the current LSP server instance and starts a fresh one.

A quick manual restart is the simplest fix when the Language Server gets into a bad state, avoiding the need to restart the entire IDE.

## LSP Initialization Options

{bdg-primary}`LSP Required`

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

{bdg-primary}`LSP Required`

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

{bdg-success}`Native`

The plugin registers a "ReScript Standard" predefined code style that can be applied via **Settings** > **Editor** > **Code Style** > **ReScript** > **Set from...** > **Predefined Style** > **ReScript Standard**.

This provides a one-click way to configure indentation and formatting settings to match the standard ReScript conventions (2-space indentation, no tabs).

For manual indentation and tab/space configuration, go to **Settings** > **Editor** > **Code Style** > **ReScript**.

The predefined code style gives you correct ReScript formatting conventions in one click, so you do not need to configure indentation settings manually.

## Element Descriptions

{bdg-success}`Native`

The plugin provides human-readable descriptions of ReScript elements for use in IDE dialogs such as **Find Usages**, **Safe Delete**, and refactoring confirmations.

For example, when using Safe Delete on a function, the confirmation dialog shows:

> Delete function 'greet'?

rather than a generic "Delete element" message. This applies to `let` bindings, `type` declarations, `module` declarations, `external` declarations, and `exception` declarations.

Descriptive element names in IDE dialogs make refactoring confirmations clearer — you see "Delete function 'greet'" instead of a generic message, reducing the risk of accidental deletions.

## Build Watch Auto-Start Prompt

{bdg-success}`Native`

When you open a ReScript project, the plugin shows a one-time balloon notification offering to start the ReScript watch build (`rescript watch`).

### When It Appears

The prompt appears at project startup if:

- The project contains `rescript.json`
- The ReScript CLI (`rescript`) is found in `node_modules/.bin/`
- The prompt hasn't been dismissed for the current IDE session

### Notification Actions

| Action | Description |
|--------|-------------|
| **Start Build Watch** | Launches `rescript watch` via a Run Configuration |
| **Don't ask again** | Dismisses the prompt for this IDE session |

Clicking **Start Build Watch** opens the Run tool window with a live-recompiling build process.

The build watch prompt ensures you start getting live compilation feedback from the moment you open your project, without needing to remember to run the build command manually.

## REPL

{bdg-success}`Native`

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

{bdg-success}`Native`

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

{bdg-success}`Native`

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

{bdg-success}`Native`

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
