# Configuration

The plugin provides configuration options through the JetBrains IDE settings.

## Plugin Settings

Go to **Settings** → **Languages & Frameworks** → **ReScript** to access plugin-specific settings.

### Incremental Type Checking

Enable or disable incremental type checking in the Language Server. When enabled, the Language Server only re-checks files that have changed, improving performance on large projects.

### reanalyze Path

Configure a custom path to the `reanalyze` binary for dead code analysis. By default, the plugin searches for `reanalyze` in `node_modules/.bin/`.

## Color Scheme

Go to **Settings** → **Editor** → **Color Scheme** → **ReScript** to customize syntax highlighting colors.

### Available Color Keys

| Key | Description |
|-----|-------------|
| Keyword | `let`, `type`, `module`, `switch`, etc. |
| String | String literals and template strings |
| Number | Integer and float literals |
| Line comment | `// ...` comments |
| Block comment | `/* ... */` comments |
| Operator | `+`, `-`, `=>`, `\|>`, etc. |
| Braces | `{`, `}` |
| Brackets | `[`, `]` |
| Parentheses | `(`, `)` |
| Dot | `.` |
| Comma | `,` |
| Semicolon | `;` |
| Type argument | `'a`, `'b` |
| Polymorphic variant | `#Tag` |
| Module name | `Belt`, `Array` |
| Decorator | `@module`, `@send` |

### Semantic Highlighting Colors

When the Language Server is connected, additional semantic token colors are available:

| Key | Description |
|-----|-------------|
| Variable | Variables and parameters |
| Type | Type names |
| Namespace | Module names |
| Enum member | Variant constructors |
| Property | Record fields |
| Interface | JSX HTML elements |

## Code Style

Go to **Settings** → **Editor** → **Code Style** → **ReScript** to configure indentation.

### Options

| Setting | Default |
|---------|---------|
| Tab size | 2 |
| Indent | 2 |
| Continuation indent | 2 |
| Use tab character | No (spaces) |

## Inlay Hints

Go to **Settings** → **Editor** → **Inlay Hints** → **ReScript** to toggle inlay hint visibility.

## Live Templates

Go to **Settings** → **Editor** → **Live Templates** → **ReScript** to view, edit, and add custom live templates.
