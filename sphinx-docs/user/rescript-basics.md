# ReScript Basics

This page helps you get a ReScript project up and running if you're new to ReScript.

:::{tip}
For comprehensive ReScript documentation, visit the official [ReScript docs](https://rescript-lang.org/docs/manual/latest/introduction).
:::

## Setting Up a New Project

### Option 1: Using the Project Wizard

1. **File** → **New** → **Project**
2. Select **ReScript** from the generator list
3. Fill in the project name and location
4. Click **Create**

The wizard generates a basic project structure with `rescript.json` and source files.

### Option 2: Manual Setup

1. Create a new directory for your project
2. Initialize with npm:

```bash
mkdir my-rescript-project
cd my-rescript-project
npm init -y
```

3. Install ReScript:

```bash
npm install rescript @rescript/core
```

4. Install the Language Server:

```bash
npm install @rescript/language-server
```

5. Create `rescript.json`:

```json
{
  "name": "my-rescript-project",
  "sources": [
    { "dir": "src", "subdirs": true }
  ],
  "package-specs": [
    { "module": "esmodule", "in-source": true }
  ],
  "bs-dependencies": ["@rescript/core"]
}
```

6. Create `src/` directory and your first `.res` file:

```bash
mkdir src
```

```rescript
// src/Main.res
let greeting = "Hello from ReScript!"
Console.log(greeting)
```

7. Build:

```bash
npx rescript build
```

## Project Structure

A typical ReScript project looks like:

```
my-project/
├── rescript.json          # ReScript configuration
├── package.json           # npm dependencies
├── node_modules/
│   ├── rescript/          # ReScript compiler
│   └── @rescript/
│       ├── core/          # Standard library
│       └── language-server/  # LSP server
├── src/
│   ├── Main.res           # Source files
│   └── Utils.res
└── lib/                   # Compiled output (auto-generated)
```

## Key Files

### rescript.json

The main configuration file for your ReScript project. The plugin provides JSON Schema validation and auto-completion for this file.

Key fields:
- `name` — Package name
- `sources` — Source directories to compile
- `bs-dependencies` — ReScript package dependencies
- `package-specs` — Output format (commonjs or esmodule)

### .res Files

ReScript source files. Each `.res` file defines a module with the same name as the file.

### .resi Files

ReScript interface files. They define the public API of the corresponding `.res` module. Use `Alt+O` to switch between `.res` and `.resi` files.

## Basic Syntax Quick Reference

```rescript
// Variables
let name = "ReScript"
let age = 25
let isReady = true

// Functions
let add = (a, b) => a + b
let greet = (name) => `Hello, ${name}!`

// Types
type color = Red | Green | Blue
type user = {name: string, age: int}

// Pattern matching
let describe = (color) =>
  switch color {
  | Red => "red"
  | Green => "green"
  | Blue => "blue"
  }

// Modules
module Math = {
  let pi = 3.14159
  let square = (x) => x *. x
}

// Pipe operator
let result = [1, 2, 3]->Array.map(x => x * 2)->Array.filter(x => x > 2)
```
