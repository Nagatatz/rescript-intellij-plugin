# Design: .d.ts to ReScript Binding Generation

## Architecture

```
User right-clicks .d.ts file → DtsGenerateBindingAction
  ↓
DtsNodeDetector: find Node.js + typescript package
  ↓
DtsParserProcess: spawn `node dts-to-json.js <file.d.ts>`
  ↓
dts-to-json.js: TypeScript Compiler API → JSON stdout
  ↓
DtsJsonModel: Gson deserialization (sealed class hierarchy)
  ↓
DtsTypeMapper: TS type → ReScript type string
  ↓
DtsToRescriptConverter: JSON model → .res binding code
  ↓
Write .res file + open in editor
```

## New Files

### Kotlin (`src/main/kotlin/com/rescript/plugin/binding/`)

| File | Responsibility | Pattern Reference |
|------|---------------|-------------------|
| `DtsJsonModel.kt` | Data classes for JSON schema + Gson deserializers | `RescriptTypeDeclarationParser.kt` sealed class pattern |
| `DtsTypeMapper.kt` | TS→ReScript type mapping (pure functions) | `RescriptTypeDeclarationParser.kt` object pattern |
| `DtsToRescriptConverter.kt` | JSON model → ReScript code string | `RescriptPasteAsJsonAction.kt` companion object pattern |
| `DtsNodeDetector.kt` | Find Node.js + typescript package | `RescriptLspDetector.kt` object pattern |
| `DtsParserProcess.kt` | Spawn Node.js, collect JSON stdout | `RescriptFormattingService.kt` process pattern |
| `DtsGenerateBindingAction.kt` | AnAction for project tree + editor | `RescriptCreateInterfaceAction.kt` action pattern |

### Resources

| File | Responsibility |
|------|---------------|
| `src/main/resources/scripts/dts-to-json.js` | Bundled Node.js TypeScript parser |

### Tests (`src/test/kotlin/com/rescript/plugin/binding/`)

| File | Tests |
|------|-------|
| `DtsJsonModelTest.kt` | JSON deserialization correctness |
| `DtsTypeMapperTest.kt` | Type mapping rules |
| `DtsToRescriptConverterTest.kt` | Code generation output |
| `DtsNodeDetectorTest.kt` | Node/typescript detection logic |

## JSON Intermediate Schema

```json
{
  "fileName": "lodash.d.ts",
  "moduleName": "lodash",
  "declarations": [
    { "kind": "function", "name": "...", "exported": true, "parameters": [...], "returnType": {...} },
    { "kind": "interface", "name": "...", "members": [...] },
    { "kind": "typeAlias", "name": "...", "type": {...} },
    { "kind": "variable", "name": "...", "type": {...}, "isConst": true },
    { "kind": "enum", "name": "...", "members": [...], "isStringEnum": true },
    { "kind": "class", "name": "...", "constructors": [...], "methods": [...], "properties": [...] }
  ],
  "errors": []
}
```

Type nodes use a tagged `"kind"` field: `primitive`, `reference`, `array`, `tuple`, `function`, `union`, `intersection`, `objectLiteral`, `stringLiteral`, `numericLiteral`, `indexSignature`, `unknown`.

## Type Mapping Table

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
| `Date` | `Date.t` |
| `RegExp` | `RegExp.t` |
| `Map<K,V>` | `Map.t<K, V>` |
| `Set<T>` | `Set.t<T>` |
| string literal union | polymorphic variant with `@string` |
| numeric enum | module with `@int` |

## Code Generation Patterns

- **Function** → `@module("lib") external fn: (params) => ret = "fn"`
- **Interface** → `type t = { field1: string, field2?: int }`
- **Class** → `module ClassName = { type t; @new external make: ...; @send external method: ... }`
- **Variable** → `@module("lib") external name: type = "name"`
- **String enum** → `type t = [#value1 | #value2]`
- **Numeric enum** → module with `@int` external declarations

## Key Design Decisions

1. **Node.js script** (not pure Kotlin parser): TypeScript's type system is too complex for a regex-based parser. The TS compiler API handles all edge cases.
2. **JSON intermediate format**: Clean separation of parsing (JS) and code generation (Kotlin). Code generation is fully unit-testable without Node.js.
3. **Script bundling**: Bundled in `resources/scripts/`, extracted to temp file at runtime.
4. **TypeScript detection**: Project `node_modules/typescript` → parent dirs (monorepo) → prompt to install.

## Modified Files

- `plugin.xml` — Register action in `<actions>` block
- `CLAUDE.md` — Add `binding/` to project structure
- `docs/product-requirements.md` — Add to implemented features
- `docs/functional-design.md` — Add binding generation design
- `README.md` — Add feature to feature list
