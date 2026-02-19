# Requirements: .d.ts to ReScript Binding Generation

## Overview

ReScript projects that use JavaScript/TypeScript libraries must write `external` binding declarations manually. This feature automatically parses TypeScript `.d.ts` definition files and generates ReScript binding code.

## User Stories

1. As a ReScript developer, I want to right-click a `.d.ts` file and generate ReScript bindings so that I don't have to write them manually.
2. As a ReScript developer, I want the generated bindings to correctly map TypeScript types to ReScript types so that the bindings are type-safe.

## Functional Requirements

### FR-1: .d.ts File Parsing
- Parse `.d.ts` files using the TypeScript Compiler API via a bundled Node.js script
- Support: interface, type alias, function, variable (const), enum (string/numeric), class declarations
- Output a JSON intermediate representation to stdout

### FR-2: Type Mapping
- Map TypeScript primitives to ReScript equivalents (string, number→float, boolean→bool, void→unit)
- Map `any`/`unknown` to `JSON.t`
- Map `T | null` to `Nullable.t<T>`, `T | undefined` to `option<T>`
- Map `Array<T>` to `array<T>`, `Promise<T>` to `promise<T>`, `Record<string,T>` to `Dict.t<T>`
- Map tuples, function types, Date, RegExp, Map, Set
- Map string literal unions to polymorphic variants with `@string`
- Map numeric enums to modules with `@int`

### FR-3: Code Generation
- Generate `@module` external declarations for functions and variables
- Generate record types for interfaces
- Generate modules with `@new`/`@send` for classes
- Generate polymorphic variant types for string enums
- Generate `/* TODO: unsupported ... */` comments for unsupported patterns

### FR-4: Node.js Detection
- Use project `node_modules/typescript` first
- Walk up parent directories for monorepo support
- Use custom Node.js path from project settings if configured
- Show error notification if Node.js or TypeScript is not available

### FR-5: IDE Integration
- Register as an AnAction available from project tree context menu and editor context menu
- Only visible on `.d.ts` files
- Show overwrite confirmation if target `.res` file already exists
- Open generated file in editor after creation

## Non-Functional Requirements

- Node.js script bundled in plugin resources, extracted to temp file at runtime
- JSON intermediate format decouples parsing from code generation for testability
- Process timeout of 30 seconds for large files

## Scope Limitations (Initial)

**Not supported** (generates TODO comment):
- Conditional types, mapped types, template literal types
- Complex generics with constraints
- Intersection types
- Overloaded functions (first signature only + comment)
- Declaration merging

## Acceptance Criteria

- `./gradlew buildPlugin` succeeds
- `./gradlew test` passes all new tests
- Right-click on a `.d.ts` file shows "Generate ReScript Binding" action
- Generated `.res` files contain valid ReScript binding syntax
