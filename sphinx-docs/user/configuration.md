# Configuration

The plugin provides configuration options through the JetBrains IDE settings.

## Plugin Settings

Go to **Settings** → **Languages & Frameworks** → **ReScript** to access plugin-specific settings.

### Language Server Path

Configure a custom path to the `rescript-language-server` executable or `cli.js` entry point. Leave this field empty to let the plugin auto-detect the server from `node_modules` or the system PATH.

When a custom path is specified, the plugin validates that the file exists before applying the setting. If the path points to a `.js` file, the plugin launches it with Node.js; otherwise it executes the path directly as a binary.

### Node.js Interpreter Path

Configure a custom path to the Node.js interpreter. Leave this field empty to use `node` from the system PATH. This setting is only relevant when the language server is launched via a `.js` entry point (e.g., `node_modules/@rescript/language-server/out/cli.js`).

### Incremental Type Checking

Enable or disable incremental type checking in the Language Server. When enabled, the Language Server only re-checks files that have changed, improving performance on large projects. This setting is sent to the language server as an initialization option and takes effect after an LSP server restart.

:::{note}
Changing any plugin setting triggers an automatic LSP server restart. The server shuts down and re-launches with the updated configuration.
:::

### reanalyze Path

Configure a custom path to the `reanalyze` binary for dead code analysis. By default, the plugin searches for `reanalyze` in `node_modules/.bin/`.

## Color Scheme

Go to **Settings** → **Editor** → **Color Scheme** → **ReScript** to customize syntax highlighting colors.

### Available Color Keys

| Key | Description | Default Base |
|-----|-------------|-------------|
| Keyword | `let`, `type`, `module`, `switch`, etc. | Language defaults > Keyword |
| String | String literals and template strings | Language defaults > String |
| Number | Integer and float literals | Language defaults > Number |
| Line comment | `// ...` comments | Language defaults > Line comment |
| Block comment | `/* ... */` comments | Language defaults > Block comment |
| Operator | `+`, `-`, `=>`, `\|>`, etc. | Language defaults > Operation sign |
| Braces | `{`, `}` | Language defaults > Braces |
| Brackets | `[`, `]` | Language defaults > Brackets |
| Parentheses | `(`, `)` | Language defaults > Parentheses |
| Dot | `.` | Language defaults > Dot |
| Comma | `,` | Language defaults > Comma |
| Semicolon | `;` | Language defaults > Semicolon |
| Type argument | `'a`, `'b` | Language defaults > Metadata |
| Polymorphic variant | `#Tag` | Language defaults > Constant |
| Module name | `Belt`, `Array` | Language defaults > Class name |
| Annotation | `@module`, `@send` | Language defaults > Metadata |
| Pattern pipe | `\|` in pattern matching | Language defaults > Keyword |
| Wildcard | `_` in patterns | Language defaults > Keyword |
| JSX tag | HTML element names in JSX | Language defaults > Markup tag |
| JSX tag bracket | `<`, `>`, `</`, `/>` in JSX | Language defaults > Markup tag |

### Customizing Lexer-Based Colors

Each color key listed above controls a specific category of tokens recognized by the JFlex lexer. Here is a detailed guide on what each key affects and how to customize it effectively.

#### Keywords

The **Keyword** color applies to all reserved words in ReScript: `let`, `type`, `module`, `switch`, `if`, `else`, `open`, `include`, `external`, `exception`, `try`, `catch`, `as`, `rec`, `and`, `true`, `false`, `async`, `await`, and others. Changing this color affects a large proportion of the visible code. Bold styling works well for keywords to make code structure scannable.

#### Strings

The **String** color applies to double-quoted string literals (`"hello"`), character literals (`'a'`), and template string delimiters (`` ` ``). Template string interpolation markers (`${` and `}`) inherit the string color as well. If you want interpolated expressions inside template strings to stand out, consider using a slightly muted string color so that the interpolated identifiers (colored by their own token type) create visible contrast.

#### Operators

The **Operator** color applies to arithmetic operators (`+`, `-`, `*`, `/`), comparison operators (`==`, `!=`, `<`, `>`), the arrow (`=>`), the pipe operator (`|>`), spread (`...`), and other symbolic tokens. In ReScript, pipe chains (`|>` or `->`) are a very common idiom, so the operator color has significant visual impact. Choosing a distinct operator color helps pipe chains stand out from surrounding identifiers and keywords.

The **Pattern pipe** key is separate from the general Operator key. It controls only the `|` character used in pattern matching arms (e.g., `| Some(x) => ...`). By default it uses the Keyword color to visually group it with `switch`, but you can assign a different color if you prefer pattern arms to stand out differently.

#### Module Names and Namespaces

The **Module name** color applies to uppercase identifiers recognized by the lexer, such as `Belt`, `Array`, `React`, `Option`, and user-defined module names. This is a lexer-level heuristic: any identifier starting with an uppercase letter is treated as a module name. For more precise coloring that distinguishes actual modules from variant constructors, use the semantic highlighting colors described below (the **Namespace** and **Enum member** semantic keys override this lexer-level color when the LSP is connected).

#### Type Arguments

The **Type argument** color applies to type variables like `'a`, `'b`, `'key`. These appear in polymorphic function signatures and type definitions. A distinct color helps readers quickly identify generic parameters in type annotations.

#### Polymorphic Variants

The **Polymorphic variant** color applies to `#`-prefixed tags like `#success`, `#error`, `#loading`. These are structurally typed in ReScript, and highlighting them distinctly from regular variant constructors helps distinguish nominal variants from structural ones.

#### Annotations (Decorators)

The **Annotation** color applies to `@`-prefixed decorators such as `@module`, `@send`, `@get`, `@react.component`, and `@genType`. Annotations often carry important FFI or framework metadata, so a visible but non-distracting color (such as a muted gold or gray) works well.

#### JSX Colors

The **JSX tag** color applies to lowercase HTML element names inside JSX (e.g., `div`, `span`, `input`), while uppercase JSX component names (e.g., `<MyComponent>`) are colored using the **Module name** key instead. The **JSX tag bracket** color applies to the angle brackets that delimit JSX elements: `<`, `>`, `</`, and `/>`. Assigning distinct colors to JSX tags and brackets helps visually separate markup from logic in component code.

### Semantic Highlighting Colors

When the Language Server is connected, additional semantic token colors are available. These colors are layered on top of the lexer-based highlighting and provide type-aware precision.

| Key | Description | Default Fallback |
|-----|-------------|-----------------|
| Variable | Variables and parameters | Language defaults > Local variable |
| Type | Type names | Language defaults > Class name |
| Namespace | Module names (semantically resolved) | Module name (lexer key) |
| Enum member | Variant constructors | Polymorphic variant (lexer key) |
| Property | Record fields | Language defaults > Instance field |
| Interface | JSX HTML elements | JSX tag (lexer key) |
| Operator | Operators (semantically resolved) | Operator (lexer key) |
| Modifier | JSX tag brackets (semantically resolved) | JSX tag bracket (lexer key) |

### Customizing Semantic Colors

Semantic colors override lexer colors for the tokens they cover, providing more accurate highlighting. Here is what each semantic key controls and how it interacts with the lexer layer.

#### Variable

The **Variable** key colors all variables and function parameters identified by the language server's type analysis. Without the LSP, local identifiers are not specifically colored (they appear as plain text). Enabling semantic highlighting and assigning a color to this key makes variable references visually distinct from keywords and types. A subtle color (slightly brighter than the default text) works well to avoid visual clutter.

#### Type

The **Type** key colors type names wherever they appear: in type annotations, type definitions, and type constructor applications. This provides more accurate coloring than the lexer, which cannot distinguish a type name from a regular identifier. For example, `int`, `string`, and `option` are colored as types only when semantic highlighting is active.

#### Namespace vs. Module Name

The **Namespace** semantic key and the **Module name** lexer key both color module names, but they operate at different precision levels. The lexer-based **Module name** key colors every uppercase identifier as a potential module name, including variant constructors like `Some` and `None`. The semantic **Namespace** key only colors identifiers that the compiler has actually resolved as modules. When the LSP is connected, the **Namespace** color takes precedence for true module references, while variant constructors receive the **Enum member** color instead. This eliminates the ambiguity that exists at the lexer level.

#### Enum Member

The **Enum member** key colors variant constructors such as `Some`, `None`, `Ok`, `Error`, `Red`, `Green`, and user-defined variants. At the lexer level, these are indistinguishable from module names (both start with uppercase letters). The semantic layer resolves this ambiguity by consulting the compiler's type information.

#### Property

The **Property** key colors record field names in both record definitions and record access expressions. For example, in `user.name` the field `name` receives the Property color, while `user` receives the Variable color. This distinction is only possible with semantic analysis.

#### Interface and Modifier

The **Interface** key colors HTML element names in JSX expressions (e.g., `div`, `span`) with semantic precision, and the **Modifier** key colors the corresponding JSX angle brackets. These override the lexer-based JSX tag and JSX tag bracket colors when the LSP is connected.

### Color Scheme Files

The plugin ships with two built-in color scheme files:

- **RescriptDarcula.xml** --- Colors optimized for the Darcula (dark) theme.
- **RescriptDefault.xml** --- Colors optimized for the Default (light) theme.

These files define the initial colors for both lexer-based and semantic token keys. Any customizations you make in the Color Scheme settings override these defaults and are saved in your IDE profile.

## Code Style

Go to **Settings** → **Editor** → **Code Style** → **ReScript** to configure indentation.

### Options

| Setting | Default | Description |
|---------|---------|-------------|
| Tab size | 2 | The width of a tab character in spaces. |
| Indent | 2 | The number of spaces (or tab width) used for each indentation level. |
| Continuation indent | 2 | The indentation applied to continuation lines (e.g., when a long expression wraps to the next line). |
| Use tab character | No (spaces) | Whether to insert actual tab characters or spaces for indentation. |

### What Each Setting Affects

#### Tab Size

The **Tab size** controls how wide a tab character appears in the editor. Since ReScript convention uses 2-space indentation, the default tab size is 2. This setting only matters if you have files that already contain tab characters; it determines how they are visually rendered.

#### Indent

The **Indent** setting controls the number of spaces inserted when you press Tab or when the editor auto-indents a new line inside a block. This is the primary setting that shapes the visual structure of your code. ReScript projects conventionally use 2-space indentation to match the output of `rescript format`.

:::{tip}
Since `rescript format` enforces 2-space indentation, changing this setting to a different value means your code will look different before and after formatting. For consistency, keep this at 2.
:::

#### Continuation Indent

The **Continuation indent** controls the extra indentation applied when a statement or expression spans multiple lines. For example, if a function call's arguments wrap to the next line, they are indented by this amount relative to the start of the call. The default of 2 spaces keeps wrapped lines aligned with ReScript conventions.

#### Use Tab Character

When **Use tab character** is enabled, the editor inserts actual tab characters (`\t`) instead of spaces. Most ReScript projects use spaces, and `rescript format` outputs spaces, so the default is disabled. Enable this only if you have a specific need for tab characters.

## Inlay Hints

Go to **Settings** → **Editor** → **Inlay Hints** → **ReScript** to toggle inlay hint visibility.

### What Inlay Hints Show

Inlay hints are small inline annotations displayed in the editor that show type information inferred by the compiler. They appear next to `let` bindings and function parameters where the type is not explicitly written.

For example, given:

```rescript
let add = (a, b) => a + b
```

Inlay hints display the inferred types directly in the editor:

```
let add = (a: int, b: int): int => a + b
```

### Configuration Options

The IntelliJ Platform provides several controls for inlay hints:

- **Enable/Disable:** Toggle all ReScript inlay hints on or off. When disabled, no type annotations are shown.
- **Show for parameters:** Controls whether parameter type hints appear in function signatures.
- **Show for types:** Controls whether return type hints appear on `let` bindings.

:::{note}
Inlay hints require the Language Server to be connected. When the LSP is disconnected, no hints are displayed regardless of the setting.
:::

### When to Disable Inlay Hints

Inlay hints are useful when exploring unfamiliar code or debugging type inference issues. However, in files where you already know the types well or where the hints create too much visual noise, you may want to disable them. You can toggle hints quickly using the right-click context menu on any inlay hint in the editor, which provides options to disable hints by category without opening the Settings dialog.

## Live Templates

Go to **Settings** → **Editor** → **Live Templates** → **ReScript** to view, edit, and add custom live templates.

The plugin ships with 21 built-in live templates covering common ReScript patterns (declarations, control flow, FFI bindings, and React components). You can customize the expansion text, add new templates, or change the abbreviation keys to match your workflow.
