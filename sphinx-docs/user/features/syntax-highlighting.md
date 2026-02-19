# Syntax Highlighting

The plugin provides two layers of syntax highlighting that work together for accurate, rich coloring.

## Layer 1: Lexer-Based Highlighting

The built-in JFlex lexer tokenizes ReScript source code and applies colors based on token types. This works instantly, without any external dependencies.

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

### Nested Comments

ReScript supports nested block comments. The lexer correctly handles patterns like:

```rescript
/* outer /* inner */ still comment */
```

### Template String Interpolation

Template strings with `${}` interpolation are fully supported:

```rescript
let greeting = `Hello, ${name}! You are ${age->Int.toString} years old.`
```

## Layer 2: Semantic Highlighting

When the Language Server is connected, semantic tokens provide an additional layer of highlighting based on actual type information.

### Semantic Token Types

| Token Type | Meaning | Example |
|------------|---------|---------|
| `variable` | Variables and parameters | `let x = 1` → `x` colored as variable |
| `type` | Type names | `type t = int` → `t` colored as type |
| `namespace` | Module names | `Belt.Array` → `Belt` colored as namespace |
| `enumMember` | Variant constructors | `Some(x)` → `Some` colored as enum member |
| `property` | Record fields | `user.name` → `name` colored as property |
| `interface` | JSX HTML elements | `<div>` → `div` colored as interface |
| `operator` | Operators | `+`, `\|>` colored as operator |

Semantic highlighting overlays on top of lexer highlighting. If the Language Server is unavailable, the lexer highlighting still provides accurate coloring.

## Customizing Colors

Go to **Settings** → **Editor** → **Color Scheme** → **ReScript** to customize colors for each token type.

The plugin ships with optimized color schemes for both **Darcula** (dark) and **Default** (light) themes.
