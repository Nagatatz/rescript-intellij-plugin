---
myst:
  html_meta:
    "keywords": "glossary, terms, definitions, vocabulary"
---

# Glossary

Key terms used throughout this documentation.

## ReScript Language

```{glossary}
Binding
   A `let` declaration that associates a name with a value or function. Similar to variable declaration in other languages, but bindings are immutable by default.

Module
   A namespace unit in ReScript that groups related types, values, and sub-modules. Every `.res` file is implicitly a module named after the file.

Interface
   A `.resi` file that declares the public API of a module. Only declarations listed in the interface file are visible to other modules.

Variant
   An algebraic data type that can hold one of several named constructors, each optionally carrying data. For example, `type color = Red | Green | Blue`.

Record
   A structured data type with named fields. Fields are accessed with dot notation and are immutable by default.

Pipe Operator
   The `->` operator that passes the result of the left expression as the first argument to the function on the right. Enables readable left-to-right data transformation chains.

Decorator
   An `@` annotation that modifies the behavior of the following declaration. Examples include `@module`, `@val`, `@send`, `@genType`, and `@react.component`.

External
   An `external` declaration that binds a ReScript name to a JavaScript value or function. Used for FFI (Foreign Function Interface) with JavaScript.

JSX
   XML-like syntax used in ReScript for building React components. ReScript JSX compiles to `React.createElement` calls.

Pattern Matching
   A `switch` expression that destructures values and matches them against patterns. More powerful than traditional switch/case — it checks exhaustiveness at compile time.

Type Inference
   The compiler's ability to automatically determine the type of an expression without explicit type annotations. ReScript has complete type inference powered by the Hindley-Milner type system.

PPX
   A preprocessor extension that transforms the AST (Abstract Syntax Tree) at compile time. Decorators like `@react.component` and `@deriving` are implemented as PPX transformations.

Open Statement
   An `open` declaration that brings all exports of a module into the current scope, allowing you to use them without the module prefix.
```

## IDE Features

```{glossary}
Intention Action
   A context-aware code transformation available via `Alt+Enter` (or `Option+Return` on macOS). Intentions suggest improvements or refactorings based on the code at the cursor position.

Live Template
   A code snippet that expands from an abbreviation when you press `Tab`. For example, typing `letfn` and pressing `Tab` expands to a full function declaration.

Postfix Completion
   A completion triggered by typing a dot suffix after an expression. For example, `expr.switch` transforms the expression into a `switch` statement. Unlike live templates, postfix completions wrap existing code.

Code Folding
   The ability to collapse code blocks (modules, functions, comments) to show only their first line, making it easier to navigate large files.

Structure View
   A tool window (`Alt+7`) that displays a tree outline of declarations in the current file — modules, functions, types, and externals.

Code Lens
   Inline annotations displayed above function declarations showing their inferred type signatures. Powered by the CodeVision API.

Inlay Hint
   A small inline label displayed in the editor showing type information for variables and parameters. Provided by the Language Server.

Code Inspection
   An automated code quality check that runs in the background and highlights potential issues with warnings or suggestions. Available inspections can be configured in Settings.

Quick Fix
   An automatic correction for a detected issue, offered in the context menu or via `Alt+Enter`. Quick fixes are typically paired with inspections or compiler diagnostics.

Surround With
   A feature (`Ctrl+Alt+T`) that wraps selected code with a control structure such as `if`, `switch`, `try`, or a block `{}`.

Run Configuration
   A saved set of parameters for executing a task — such as building a ReScript project, running tests, or debugging compiled JavaScript.

Error Lens
   Inline diagnostic messages displayed at the end of each line in the editor, showing errors and warnings without hovering. Color-coded by severity.
```

## Plugin Architecture

```{glossary}
LSP
   Language Server Protocol. A standardized JSON-RPC protocol for communication between an IDE (client) and a language-specific server that provides semantic features like completion, diagnostics, and navigation.

Language Server
   The `@rescript/language-server` process that runs in the background, analyzing your ReScript project and providing semantic features to the plugin via {term}`LSP`.

Native Feature
   A feature implemented directly in the plugin using JFlex lexer and IntelliJ Platform APIs. Native features work without the {term}`Language Server` — for example, syntax highlighting, code folding, and brace matching.

Semantic Token
   A token classification provided by the {term}`Language Server` that carries semantic meaning (e.g., "this identifier is a type" or "this is a namespace"). Enables more accurate highlighting than lexer-based tokenization alone.

Diagnostics
   Errors, warnings, and informational messages reported by the {term}`Language Server` and displayed inline in the editor. Diagnostics update as you edit the code.

PSI
   Program Structure Interface. IntelliJ Platform's in-memory representation of source code as a tree of typed elements. The plugin's lexer and parser build PSI trees that power navigation, structure view, and refactoring.

Stub Index
   A persistent, file-based index of declaration signatures (names, types, modules) extracted from PSI stubs. Enables fast symbol search across the entire project without parsing every file.
```
