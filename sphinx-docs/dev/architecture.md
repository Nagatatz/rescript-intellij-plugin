# Architecture

The ReScript IntelliJ Plugin uses a **hybrid architecture** that combines a built-in lexer with an external Language Server.

## Overview

```
┌─────────────────────────────────────────────────────┐
│                   JetBrains IDE                      │
│  ┌───────────────────────────────────────────────┐  │
│  │          ReScript IntelliJ Plugin              │  │
│  │                                                │  │
│  │  ┌─────────────────────────────────────────┐  │  │
│  │  │  Layer 1: Language Foundation (Built-in)  │  │  │
│  │  │                                          │  │  │
│  │  │  JFlex Lexer → Syntax Highlighting       │  │  │
│  │  │  Lightweight Parser → PSI Tree           │  │  │
│  │  │  Code Folding, Structure View, Brace     │  │  │
│  │  │  Matching, Comments, etc.                │  │  │
│  │  └─────────────────────────────────────────┘  │  │
│  │                                                │  │
│  │  ┌─────────────────────────────────────────┐  │  │
│  │  │  Layer 2: LSP Integration                │  │  │
│  │  │                                          │  │  │
│  │  │  Completion, Diagnostics, Navigation,    │  │  │
│  │  │  Hover, References, Rename, Inlay Hints, │  │  │
│  │  │  Semantic Tokens, Code Lens, etc.        │  │  │
│  │  └──────────────────┬──────────────────────┘  │  │
│  └──────────────────────┼────────────────────────┘  │
│                         │ stdio                      │
└─────────────────────────┼────────────────────────────┘
                          │
              ┌───────────┴───────────┐
              │  @rescript/            │
              │  language-server       │
              │  (Node.js process)     │
              └───────────────────────┘
```

## Layer 1: Language Foundation

This layer is entirely built into the plugin and works without any external dependencies.

### JFlex Lexer

- **Source:** `src/main/java/com/rescript/plugin/lang/Rescript.flex`
- **Generated:** `RescriptFlexLexer.java` (auto-generated, not committed)
- **Wrapper:** `RescriptLexer.kt` (FlexAdapter)

The lexer tokenizes ReScript source code into token types defined in `RescriptTokenTypes.kt`. It handles:

- Keywords, identifiers, literals, operators, punctuation
- Nested block comments (`/* /* */ */`)
- Template string interpolation (`` `${expr}` ``)
- Declaration context tracking (`let`/`type` → identifier classification)

### Lightweight Parser

- **Source:** `RescriptParser.kt`

The parser only recognizes **top-level declarations** — it does not parse expressions, types, or JSX in detail. This is intentional: complex parsing is delegated to the Language Server.

Recognized declarations:
- `let` / `let rec` bindings
- `type` / `type rec` definitions
- `module` / `module type` / `module rec` declarations
- `external` bindings (FFI)
- `open` / `include` directives
- `exception` declarations
- `@decorator` annotations

### PSI Tree

The parser produces a PSI (Program Structure Interface) tree used by:
- **Code folding** — Collapse multi-line declarations
- **Structure view** — Display file outline
- **Statement mover** — Move declarations up/down

## Layer 2: LSP Integration

This layer communicates with the ReScript Language Server over stdio.

### Key Classes

| Class | Role |
|-------|------|
| `RescriptLspServerSupportProvider` | Decides when to start the LSP server |
| `RescriptLspServerDescriptor` | Configures server detection and launch |
| `RescriptLanguageServer` | Custom LSP request interface |
| `RescriptLsp4jClient` | Custom LSP notification receiver |
| `RescriptSemanticTokensSupport` | Maps LSP semantic tokens to colors |
| `RescriptCompilationStatusService` | Tracks build status from LSP notifications |

### LSP Server Detection

The plugin searches for the Language Server in this order:

1. `node_modules/.bin/rescript-language-server` (project-local binary)
2. Parent directory `node_modules/.bin/` (monorepo support)
3. `node_modules/@rescript/language-server/out/cli.js` (JS fallback)
4. Global installation via `which` / `where`

### Custom LSP Extensions

The plugin uses ReScript-specific LSP requests beyond the standard protocol:

- `textDocument/createInterface` — Generate `.resi` from `.res`
- `textDocument/openCompiled` — Get compiled JS path
- `rescript/compilationStatus` — Build status notifications

## Layer 3: IDE Integration

Additional IDE features that build on both layers:

- **Run configurations** — Execute ReScript build commands
- **External formatter** — `rescript format` CLI integration
- **Code inspections** — Local analysis (duplicate opens, empty modules)
- **reanalyze integration** — Dead code analysis via external tool
- **Test runner** — Jest/Vitest integration with SMTestRunner

## Design Principles

1. **Delegate complex parsing to LSP** — The plugin never attempts to fully parse ReScript. Expression-level understanding comes from the Language Server.

2. **Graceful degradation** — If the Language Server is unavailable, all native features (highlighting, folding, structure view) still work.

3. **Use platform APIs** — The plugin uses IntelliJ Platform's official LSP API (`com.intellij.platform.lsp`) rather than implementing LSP from scratch.

4. **Keep the codebase small** — By leveraging the Language Server, the plugin avoids duplicating the complex ReScript type system and analysis logic.
