# Syntax Highlighting

The plugin provides two layers of syntax highlighting that work together for accurate, rich coloring. Layer 1 (lexer-based) provides instant, dependency-free highlighting the moment you open a file. Layer 2 (semantic) overlays type-aware coloring when the Language Server is connected, giving you the best of both worlds.

## Layer 1: Lexer-Based Highlighting

The built-in JFlex lexer tokenizes ReScript source code and applies colors based on token types. This works instantly, without any external dependencies. Even when the Language Server is unavailable (e.g., during initial project setup or in files outside a ReScript project), lexer-based highlighting ensures your code remains fully colored.

### Highlighted Elements

| Element | Examples |
|---------|---------|
| Keywords | `let`, `type`, `module`, `switch`, `if`, `else`, `async`, `await` |
| Keyword operators | `mod`, `land`, `lor`, `lxor`, `lsl`, `lsr`, `asr` |
| Strings | `"hello"`, `` `template ${x}` ``, `'c'` |
| Numbers | `42`, `3.14`, `0xFF`, `0b1010` |
| Comments | `// line comment`, `/* block comment */` |
| Operators | `+`, `-`, `=>`, `->`, `\|>`, `==`, `===` |
| Decorators | `@module`, `@send`, `@genType` |
| Type parameters | `'a`, `'b` |
| Polymorphic variants | `#Red`, `#Blue` |
| Module names | `Belt`, `Array`, `React` |

The following sections describe each element category in detail with code examples.

### Keywords

ReScript keywords control program flow, declare bindings, and define structures. They are displayed in the **Keyword** color (typically bold or a distinct hue).

**Declaration keywords** introduce new names:

```rescript
let message = "hello"
type color = Red | Green | Blue
module Utils = {
  let add = (a, b) => a + b
}
external readFile: string => string = "readFileSync"
exception NotFound(string)
```

**Control flow keywords** determine execution paths:

```rescript
if condition {
  doSomething()
} else {
  doOther()
}

switch value {
| Some(x) => x
| None => default
}

for i in 0 to 10 {
  Console.log(i)
}

while running {
  process()
}

try {
  riskyOperation()
} catch {
| Exn.Error(e) => handleError(e)
}
```

**Other keywords** include:

- `open` and `include` for module access
- `async` and `await` for asynchronous programming
- `rec` and `nonrec` for recursive binding control
- `mutable` for mutable record fields
- `as`, `with`, `when` for pattern matching refinements
- `lazy` for deferred evaluation
- `assert` for runtime assertions

```rescript
open Belt

let rec fibonacci = (n) =>
  switch n {
  | 0 | 1 => n
  | n => fibonacci(n - 1) + fibonacci(n - 2)
  }

let result = await fetchData()
```

### Keyword Operators

The keyword operators `mod`, `land`, `lor`, `lxor`, `lsl`, `lsr`, and `asr` are integer arithmetic and bitwise operations that are displayed with **keyword-style coloring** rather than the typical operator color. This is because they are alphabetic identifiers reserved as operators, similar to how other ML-family languages handle them.

| Operator | Meaning |
|----------|---------|
| `mod` | Integer modulo (remainder) |
| `land` | Bitwise AND |
| `lor` | Bitwise OR |
| `lxor` | Bitwise XOR |
| `lsl` | Logical shift left |
| `lsr` | Logical shift right |
| `asr` | Arithmetic shift right (sign-preserving) |

```rescript
let remainder = 17 mod 5       // 2
let masked = flags land 0xFF   // bitwise AND with mask
let combined = a lor b         // bitwise OR
let flipped = a lxor b         // bitwise XOR
let shifted = 1 lsl 8          // 256 (left shift)
let halved = 256 lsr 1         // 128 (right shift)
let signAware = -128 asr 1     // -64 (arithmetic shift)
```

### Strings

The lexer recognizes three types of string literals, all displayed in the **String** color:

**Double-quoted strings** are the most common form:

```rescript
let name = "ReScript"
let withEscape = "line1\nline2\ttab"
let withUnicode = "Hello \u0041"  // "Hello A"
let withQuote = "She said \"hello\""
```

**Template literals** (backtick strings) support `${}` interpolation. The string content is highlighted as a string, while the interpolated expressions inside `${}` receive their own syntax highlighting:

```rescript
let greeting = `Hello, ${name}!`
let multiline = `
  Name: ${user.name}
  Age: ${user.age->Int.toString}
  Score: ${(score *. 100.0)->Float.toString}%
`
```

**Character literals** use single quotes and contain exactly one character:

```rescript
let letter = 'A'
let newline = '\n'
let tab = '\t'
```

### Numbers

All numeric literals are displayed in the **Number** color. ReScript supports several numeric formats:

**Integer literals** in various bases:

```rescript
let decimal = 42
let negative = -17
let hex = 0xFF          // 255 in hexadecimal
let octal = 0o77        // 63 in octal
let binary = 0b1010     // 10 in binary
let bigDecimal = 1_000_000  // underscores for readability
```

**Float literals** with decimal points or scientific notation:

```rescript
let pi = 3.14159
let negative = -2.5
let scientific = 1.0e10
let small = 6.022e-23
```

### Comments

ReScript supports two comment styles, each with its own color attribute:

**Line comments** start with `//` and extend to the end of the line. They use the **Line comment** color:

```rescript
// This is a line comment
let x = 42 // inline comment
```

**Block comments** are delimited by `/*` and `*/`. They use the **Block comment** color:

```rescript
/* This is a block comment */

/*
 * Multi-line block comment
 * often used for documentation
 */
let important = true
```

#### Nested Comments

Unlike many languages, ReScript supports nested block comments. The lexer correctly tracks nesting depth, so you can comment out code that already contains block comments:

```rescript
/* outer comment
  /* inner comment is fine */
  still inside the outer comment
*/
```

This is particularly useful when temporarily commenting out large sections of code during development.

### Operators

Operators are displayed in the **Operator** color. The lexer recognizes a wide range of operators:

**Arithmetic operators:**

```rescript
let sum = a + b
let diff = a - b
let product = a * b
let quotient = a / b
let fSum = a +. b      // float addition
let fDiff = a -. b     // float subtraction
let fProd = a *. b     // float multiplication
let fQuot = a /. b     // float division
```

**Comparison operators:**

```rescript
let equal = a == b         // structural equality
let strictEqual = a === b  // referential equality
let notEqual = a != b
let strictNotEqual = a !== b
let less = a < b
let lessEq = a <= b
let greater = a > b
```

**Logical operators:**

```rescript
let both = a && b
let either = a || b
```

**Pipe and arrow operators** are among the most distinctive ReScript operators:

```rescript
// Pipe forward — data-first composition
let result = data
  |> Array.map(transform)
  |> Array.filter(isValid)

// Fat arrow — function body
let add = (a, b) => a + b

// Thin arrow — type annotation for functions
type callback = int -> string

// String concatenation
let full = first ++ " " ++ last
```

**Other operators:**

```rescript
let spread = {...record, name: "new"}  // spread operator (...)
let ref = contents := newValue         // ref assignment (:=)
```

### Decorators

Decorators (annotations) start with `@` and are displayed in the **Annotation** color. They are heavily used in ReScript for FFI (Foreign Function Interface) bindings and compiler directives:

**FFI decorators** for JavaScript interop:

```rescript
@module("fs")
external readFileSync: string => string = "readFileSync"

@send
external push: (array<'a>, 'a) => unit = "push"

@val
external document: Dom.document = "document"

@get
external innerHTML: Dom.element => string = "innerHTML"

@set
external setInnerHTML: (Dom.element, string) => unit = "innerHTML"

@new
external createDate: unit => Js.Date.t = "Date"

@scope("Math") @val
external random: unit => float = "random"
```

**Compiler decorators:**

```rescript
@genType
let add = (a, b) => a + b

@dead
let unusedHelper = () => ()

@inline
let maxSize = 100

@unboxed
type stringOrNumber = String(string) | Number(float)
```

### Type Parameters

Type parameters (generics) such as `'a`, `'b`, `'key`, `'value` are displayed in the **Type argument** color, which typically uses a metadata/annotation style. They represent parametric polymorphism, allowing types and functions to work with any type:

```rescript
type option<'a> = Some('a) | None

type result<'ok, 'err> = Ok('ok) | Error('err)

let first: array<'a> => option<'a> = arr =>
  if Array.length(arr) > 0 {
    Some(arr[0])
  } else {
    None
  }

type map<'key, 'value> = {
  get: 'key => option<'value>,
  set: ('key, 'value) => unit,
}
```

### Polymorphic Variants

Polymorphic variants are prefixed with `#` and displayed in the **Polymorphic variant** color (typically a constant-like style). Unlike regular variants which must be defined in a `type` declaration, polymorphic variants can be used ad-hoc:

```rescript
let color = #Red
let status = #"needs-review"   // string polymorphic variant

let describe = (c) =>
  switch c {
  | #Red => "warm"
  | #Blue => "cool"
  | #Green => "natural"
  }

// Polymorphic variants work across module boundaries without type definitions
let handleResponse = (status) =>
  switch status {
  | #ok => Console.log("Success")
  | #error => Console.log("Failed")
  | #pending => Console.log("Waiting...")
  }
```

The key difference from regular variants is that polymorphic variants do not require a prior type declaration and can be shared across unrelated types and modules.

Lexer-based highlighting works instantly and offline — even before the Language Server starts or in files outside a ReScript project, your code is fully colored with accurate keyword, string, comment, and operator recognition.

### Module Names

Uppercase identifiers are colored as **Module name** (typically using the class name color). In ReScript, uppercase identifiers conventionally denote modules, variant constructors, and functors:

```rescript
open Belt.Array

module StringMap = Map.Make(String)

let data = Array.map([1, 2, 3], x => x * 2)
let encoded = Js.Json.stringify(value)
let element = React.string("hello")
```

:::{note}
At the lexer level, all uppercase identifiers receive the module name color. When the Language Server is connected, semantic highlighting can refine this by distinguishing actual modules (`namespace` token type) from variant constructors (`enumMember` token type).
:::

## JSX / React Component Highlighting

ReScript has first-class JSX support, and the lexer provides dedicated token types and coloring for JSX elements. The plugin recognizes JSX structure within ReScript files and applies specialized highlighting.

### JSX Tag Highlighting

JSX tags are colored differently from regular ReScript code. The plugin distinguishes between:

- **HTML elements** (lowercase tags like `<div>`, `<span>`) -- displayed in the **JSX tag** color
- **React components** (uppercase tags like `<App>`, `<Header>`) -- displayed in the **Module name** color, matching their role as component modules

```rescript
// HTML elements — use JSX tag color
let page = <div className="container">
  <h1> {React.string("Title")} </h1>
  <p> {React.string("Content")} </p>
</div>

// React components — use module name color
let app = <Layout>
  <Header title="My App" />
  <Sidebar items={menuItems} />
  <MainContent>
    {children}
  </MainContent>
</Layout>
```

### JSX Bracket Coloring

JSX brackets (`<`, `>`, `</`, `/>`) are displayed in the **JSX tag bracket** color, visually distinguishing them from comparison operators and other angle bracket usages:

```rescript
// Opening tag brackets: < and >
<div className="app">

// Closing tag brackets: </ and >
</div>

// Self-closing bracket: />
<Component prop="value" />
```

### Props Highlighting

JSX props are highlighted as regular ReScript identifiers and expressions. Named props, punned props, and optional props all receive appropriate coloring:

```rescript
let button = <Button
  onClick={handleClick}           // expression prop
  disabled={true}                 // boolean prop
  className="primary"             // string prop
  size={#large}                   // polymorphic variant prop
  ?tooltip                        // optional punned prop
/>
```

### Expression Interpolation

Expressions within JSX children are enclosed in `{}` and receive normal ReScript syntax highlighting inside the braces:

```rescript
let greeting = <div>
  {React.string("Hello, " ++ name ++ "!")}
  {React.int(count)}
  {items
    ->Array.map(item => <li key={item.id}> {React.string(item.name)} </li>)
    ->React.array}
</div>
```

### Full React Component Example

Here is a complete React component demonstrating the full range of JSX highlighting:

```rescript
@react.component
let make = (~title: string, ~items: array<item>, ~onSelect: item => unit) => {
  let (selected, setSelected) = React.useState(() => None)

  let handleClick = (item, _event) => {
    setSelected(_ => Some(item))
    onSelect(item)
  }

  <div className="item-list">
    <h2> {React.string(title)} </h2>
    <ul>
      {items
        ->Array.map(item => {
          let isActive = selected == Some(item)
          <li
            key={item.id}
            className={isActive ? "active" : ""}
            onClick={handleClick(item)}
          >
            <span className="name"> {React.string(item.name)} </span>
            <span className="price"> {React.float(item.price)} </span>
          </li>
        })
        ->React.array}
    </ul>
    {switch selected {
    | Some(item) => <p> {React.string(`Selected: ${item.name}`)} </p>
    | None => <p className="hint"> {React.string("Click an item")} </p>
    }}
  </div>
}
```

In this example:
- `<div>`, `<h2>`, `<ul>`, `<li>`, `<span>`, `<p>` are colored as **JSX tags** (HTML elements)
- `<` `>` `</` `/>` brackets are colored as **JSX tag brackets**
- `className`, `key`, `onClick` are highlighted as regular identifiers
- Expressions inside `{}` receive full ReScript syntax highlighting
- `React.string`, `React.float`, `React.array` use **module name** coloring for `React`

Dedicated JSX highlighting visually separates markup structure from logic, making React component code easier to scan by distinguishing HTML elements, component references, and expression interpolations at a glance.

## Layer 2: Semantic Highlighting

When the Language Server is connected, semantic tokens provide an additional layer of highlighting based on actual type information from the compiler. This gives you precise, context-aware coloring that understands the meaning of each identifier.

### Semantic Token Types

| Token Type | Meaning | Example |
|------------|---------|---------|
| `variable` | Variables and parameters | `let x = 1` -- `x` colored as variable |
| `type` | Type names | `type t = int` -- `t` colored as type |
| `namespace` | Module names | `Belt.Array` -- `Belt` colored as namespace |
| `enumMember` | Variant constructors | `Some(x)` -- `Some` colored as enum member |
| `property` | Record fields | `user.name` -- `name` colored as property |
| `interface` | JSX HTML elements | `<div>` -- `div` colored as interface |
| `operator` | Operators | `+`, `\|>` colored as operator |
| `modifier` | JSX brackets | `<`, `/>` colored as modifier |

### How Semantic Tokens Work

When you open a ReScript file and the Language Server is running, the plugin requests semantic tokens for the entire document. The server analyzes the compiled program and returns a list of tokens with their types. The plugin then overlays these semantic colors on top of the lexer-based highlighting.

This process happens automatically in the background and updates as you edit the file.

### Detailed Token Type Descriptions

#### Variables (`variable`)

The `variable` token type is applied to local bindings, function parameters, and other value-level identifiers. This helps distinguish value names from type names, module names, and keywords:

```rescript
let count = 42              // "count" → variable
let add = (a, b) => a + b  // "add", "a", "b" → variable
let name = user.name        // "name" (binding) → variable
```

Without semantic highlighting, these identifiers would receive no special color from the lexer (they are lowercase identifiers, which are unstyled by default). With semantic tokens, they gain a distinct **Variable** color.

#### Types (`type`)

The `type` token applies to type names wherever they appear -- in definitions, annotations, and type expressions:

```rescript
type color = Red | Green | Blue   // "color" → type
let x: int = 42                   // "int" → type
type user = {name: string}        // "user", "string" → type
```

This makes type-level code visually distinct from value-level code, improving readability in complex type definitions.

#### Namespaces (`namespace`)

The `namespace` token applies to module names when they are used as qualifiers. This refines the lexer's broad "uppercase identifier" coloring by specifically marking known modules:

```rescript
Belt.Array.map(data, fn)     // "Belt", "Array" → namespace
Js.Json.stringify(value)     // "Js", "Json" → namespace
React.useState(() => None)   // "React" → namespace
```

#### Enum Members (`enumMember`)

Variant constructors and exception names receive the `enumMember` token, distinguishing them from module names even though both use uppercase identifiers:

```rescript
let result = Some("value")      // "Some" → enumMember
let empty = None                // "None" → enumMember
let color = Red                 // "Red" → enumMember
raise(NotFound("missing"))     // "NotFound" → enumMember
```

This is one of the most valuable semantic refinements: without the Language Server, `Some`, `None`, `Red`, and module names like `Belt` all share the same module name color from the lexer. Semantic tokens allow them to be visually separated.

#### Properties (`property`)

Record field names receive the `property` token when accessed or defined:

```rescript
type user = {
  name: string,    // "name" → property
  age: int,        // "age" → property
}

let userName = user.name    // "name" (access) → property
let updated = {...user, age: 30}  // "age" → property
```

#### Interface (`interface`)

HTML element names in JSX receive the `interface` token. This gives them a color distinct from React component names (which receive the `namespace` token):

```rescript
<div className="app">       // "div" → interface
  <span> {text} </span>     // "span" → interface
  <input type_="text" />    // "input" → interface
</div>
```

#### Operators (`operator`)

The semantic `operator` token provides type-aware operator highlighting that can distinguish operators by their resolved meaning, complementing the lexer's syntactic operator coloring:

```rescript
let sum = a + b     // "+" → operator
let piped = data |> transform  // "|>" → operator
let neg = !flag     // "!" → operator
```

#### Modifiers (`modifier`)

JSX bracket tokens (`<`, `>`, `</`, `/>`) receive the `modifier` token from the Language Server, allowing them to have a distinct semantic color:

```rescript
<div>       // "<", ">" → modifier
</div>      // "</", ">" → modifier
<br />      // "<", "/>" → modifier
```

### Semantic Override Behavior

Semantic highlighting overlays on top of lexer highlighting. When both layers assign a color to the same token, the semantic color takes precedence. Here are the key refinements that semantic tokens provide:

| Identifier | Lexer Color | Semantic Color | Benefit |
|-----------|-------------|----------------|---------|
| `Belt` (module) | Module name | Namespace | Confirms module usage |
| `Some` (constructor) | Module name | Enum member | Distinguishes from modules |
| `name` (field) | No color | Property | Highlights record access |
| `count` (variable) | No color | Variable | Highlights value bindings |
| `int` (type) | No color | Type | Highlights type annotations |
| `div` (JSX) | JSX tag | Interface | Semantic confirmation |

### Fallback Behavior

When the Language Server is disconnected or unavailable, the plugin gracefully falls back to lexer-only highlighting:

- All keyword, string, number, comment, and operator highlighting continues to work normally
- Module names, type parameters, polymorphic variants, and decorators retain their lexer-based colors
- Lowercase identifiers (variables, function names, record fields) lose their semantic coloring and appear in the default text color
- JSX elements retain their lexer-based tag coloring

The transition between connected and disconnected states is seamless. As soon as the Language Server reconnects, semantic tokens are re-requested and coloring is restored.

Semantic highlighting resolves ambiguities that lexer-based highlighting cannot — for example, visually distinguishing variant constructors like `Some` from module names like `Belt`, even though both are uppercase identifiers, making code significantly easier to read.

## Customizing Colors

Go to **Settings** → **Editor** → **Color Scheme** → **ReScript** to customize colors for each token type.

The plugin ships with optimized color schemes for both **Darcula** (dark) and **Default** (light) themes.

### Available Color Attributes

The settings page is organized into the following groups:

**Basic elements:**

| Attribute | Description |
|-----------|-------------|
| Keyword | `let`, `type`, `switch`, and all other keywords |
| String | String literals, template strings, and char literals |
| Number | Integer and float literals |
| Operator | Arithmetic, comparison, pipe, and arrow operators |
| Annotation | Decorator attributes (`@module`, `@genType`, etc.) |
| Type argument | Type parameters (`'a`, `'b`) |
| Polymorphic variant | Polymorphic variant tags (`#Red`, `#ok`) |
| Module name | Uppercase identifiers (modules, functors) |

**Comments:**

| Attribute | Description |
|-----------|-------------|
| Line comment | Single-line comments (`//`) |
| Block comment | Multi-line comments (`/* */`) |

**Braces and Operators:**

| Attribute | Description |
|-----------|-------------|
| Braces | `{` and `}` |
| Brackets | `[` and `]` |
| Parentheses | `(` and `)` |
| Dot | `.` accessor |
| Comma | `,` separator |
| Semicolon | `;` terminator |

**Pattern matching:**

| Attribute | Description |
|-----------|-------------|
| Pipe (`\|`) | Pattern match arm separator |
| Wildcard (`_`) | Catch-all pattern |

**JSX:**

| Attribute | Description |
|-----------|-------------|
| JSX tag | HTML element names (`div`, `span`) |
| JSX tag bracket | Tag delimiters (`<`, `>`, `</`, `/>`) |

**Semantic (LSP):**

| Attribute | Description |
|-----------|-------------|
| Variable | Variables and function parameters |
| Type | Type names in definitions and annotations |
| Namespace (module) | Module names used as qualifiers |
| Enum member (variant) | Variant constructors (`Some`, `None`, `Red`) |
| Property (record field) | Record field names |
| Interface (JSX tag) | HTML element names in JSX (semantic) |
| Operator | Operators (semantic) |
| Modifier (JSX bracket) | JSX brackets (semantic) |

### Tips

- The **Preview** pane in the color settings page shows a live ReScript code sample with both lexer and semantic highlighting applied, so you can see the effect of your changes immediately.
- If you use a custom color scheme, the plugin's defaults for Darcula and Default serve as a good starting point.
- Semantic color attributes only take effect when the Language Server is connected. You can customize them independently from their lexer-based counterparts.

Full color customization lets you tailor the ReScript highlighting to match your personal preferences or team conventions, with a live preview so you can see changes immediately.
