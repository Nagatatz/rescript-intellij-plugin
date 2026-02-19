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

### Class Responsibilities in Detail

#### RescriptLspServerSupportProvider

**File:** `src/main/kotlin/com/rescript/plugin/lsp/RescriptLspServerSupportProvider.kt`

This class implements the `LspServerSupportProvider` interface from the IntelliJ Platform LSP API. It is the entry point that the platform calls whenever a file is opened in the editor. Its sole responsibility is deciding whether the LSP server should be started for the given file.

The decision logic is straightforward: if the opened file has a `.res` or `.resi` extension, the provider calls `serverStarter.ensureServerStarted()` with a new `RescriptLspServerDescriptor` instance. The `ensureServerStarted` method is idempotent --- if the server is already running for the project, it does nothing. If the file extension does not match, the provider returns without action.

The list of recognized extensions is defined in the companion object as `RESCRIPT_EXTENSIONS = setOf("res", "resi")` and is reused by `RescriptLspServerDescriptor.isSupportedFile()` for consistency.

This class does not contain any server configuration, detection, or lifecycle logic. It serves purely as a trigger that bridges the platform's file-open event to the LSP server lifecycle.

#### RescriptLspServerDescriptor

**File:** `src/main/kotlin/com/rescript/plugin/lsp/RescriptLspServerDescriptor.kt`

This class extends `ProjectWideLspServerDescriptor` and is responsible for all aspects of finding, configuring, and launching the language server process.

**Server detection order:**

The `createCommandLine()` method resolves the language server binary through the following steps:

1. **Custom path check:** If the user has configured a custom LSP server path in Settings > Languages & Frameworks > ReScript, that path is used directly. This allows developers to point to a development build of the language server or a specific version.

2. **Project-local `node_modules`:** The plugin checks `<project-root>/node_modules/.bin/rescript-language-server` for an executable binary. This covers the standard case where `@rescript/language-server` is installed as a project dependency.

3. **JS fallback in project:** If the `.bin` executable is not found, it checks `<project-root>/node_modules/@rescript/language-server/out/cli.js`. This handles cases where the `.bin` symlink was not created (e.g., certain installation methods or Windows environments).

4. **Parent directory traversal (monorepo):** The plugin walks up from the project root to each parent directory, checking `node_modules/.bin/` and `node_modules/@rescript/language-server/out/cli.js` at each level. This supports monorepo setups where `@rescript/language-server` is hoisted to a workspace root.

5. **Global PATH lookup:** As a final fallback, the plugin runs `which rescript-language-server` (Unix) or `where rescript-language-server` (Windows) to find a globally installed binary.

6. **Error:** If none of the above steps succeed, an `ExecutionException` is thrown with a user-facing message explaining how to install the language server.

**Launch configuration:**

If the resolved path ends with `.js`, the server is launched as `node <path> --stdio` (using the custom Node.js path from settings or `node` from PATH). Otherwise, the binary is executed directly with `--stdio`. The working directory is set to the project root.

**Initialization options:**

The `createInitializationOptions()` method sends configuration to the language server during initialization. Currently it sends:
- `extensionConfiguration.codeLens: true` to enable Code Lens support.
- `extensionConfiguration.incrementalTypechecking.enabled` to control incremental type checking based on the user's setting.

**Customization hooks:**

The descriptor configures two important customizations:
- `lsp4jServerClass` is set to `RescriptLanguageServer::class.java` to enable custom request methods.
- `createLsp4jClient()` returns a `RescriptLsp4jClient` instance to handle custom notifications.
- `lspCustomization.semanticTokensCustomizer` is set to a `RescriptSemanticTokensSupport` instance for semantic highlighting.

#### RescriptLanguageServer

**File:** `src/main/kotlin/com/rescript/plugin/lsp/RescriptLanguageServer.kt`

This is an interface (not a class) that extends the standard `LanguageServer` interface from LSP4J. It declares additional JSON-RPC request methods that are specific to the ReScript language server and not part of the standard LSP protocol.

**Custom requests:**

- **`textDocument/createInterface`** --- Takes a `TextDocumentIdentifier` (pointing to a `.res` file) and asks the language server to generate a `.resi` interface file from it. The server returns a `TextDocumentIdentifier` pointing to the generated file. This is used by the `RescriptCreateInterfaceAction` to provide the "Create Interface" navigation action.

- **`textDocument/openCompiled`** --- Takes a `TextDocumentIdentifier` (pointing to a `.res` file) and asks the language server to resolve the path to the compiled JavaScript output. The server returns a `TextDocumentIdentifier` pointing to the `.js` file. This powers the `RescriptOpenCompiledJsAction` and the Compiled JS Preview panel.

Both methods are annotated with `@JsonRequest` so that LSP4J automatically handles the JSON-RPC serialization and dispatching. They return `CompletableFuture` for asynchronous execution.

The reason for defining a custom interface instead of relying on `LanguageServer` directly is that LSP4J uses reflection on the interface type specified in `lsp4jServerClass` to discover available methods. By declaring these methods on a sub-interface, the plugin makes them available for invocation through the standard LSP4J request mechanism.

#### RescriptLsp4jClient

**File:** `src/main/kotlin/com/rescript/plugin/lsp/RescriptLsp4jClient.kt`

This class extends `Lsp4jClient` and handles ReScript-specific notifications sent from the language server to the IDE. While the standard LSP client handles standard notifications (diagnostics, log messages, etc.), this subclass adds support for custom notifications.

**Handled notifications:**

- **`rescript/compilationStatus`** --- This notification is sent by the ReScript language server whenever the build status changes (e.g., compilation started, compilation succeeded, compilation failed with errors). The notification includes the following parameters:
  - `project` --- The project name.
  - `projectRootPath` --- The root path of the ReScript project.
  - `status` --- A string indicating the compilation state (e.g., `"success"`, `"failed"`, `"building"`).
  - `errorCount` --- The number of compilation errors.
  - `warningCount` --- The number of compilation warnings.

When this notification arrives, the client checks that the project is not disposed (to prevent errors during shutdown) and then delegates to `RescriptCompilationStatusService.updateStatus()` to store the new status and notify any registered listeners (such as the status bar widget).

The `@JsonNotification` annotation on the `compilationStatus` method tells LSP4J to route incoming `rescript/compilationStatus` messages to this method via reflection. The `@Suppress("unused")` annotation is present because the method is never called directly from Kotlin code --- it is invoked only by LSP4J's reflection-based dispatch.

#### RescriptSemanticTokensSupport

**File:** `src/main/kotlin/com/rescript/plugin/lsp/RescriptSemanticTokensSupport.kt`

This class extends `LspSemanticTokensSupport` and maps LSP semantic token types (strings like `"variable"`, `"type"`, `"namespace"`) to IntelliJ `TextAttributesKey` instances for rendering in the editor.

**Token type mapping:**

| LSP Token Type | IntelliJ TextAttributesKey | Visual Purpose |
|---------------|---------------------------|----------------|
| `variable` | `SEMANTIC_VARIABLE` | Local variables, function parameters |
| `type` | `SEMANTIC_TYPE` | Type names (e.g., `int`, `option`, user-defined types) |
| `namespace` | `SEMANTIC_NAMESPACE` | Module names (semantically resolved) |
| `enumMember` | `SEMANTIC_ENUM_MEMBER` | Variant constructors (e.g., `Some`, `None`, `Ok`) |
| `property` | `SEMANTIC_PROPERTY` | Record field names |
| `interface` | `SEMANTIC_INTERFACE` | JSX HTML element names (e.g., `div`, `span`) |
| `operator` | `SEMANTIC_OPERATOR` | Operators (semantically resolved) |
| `modifier` | `SEMANTIC_MODIFIER` | JSX tag brackets (semantically resolved) |

**Fallback behavior:** If the language server returns a token type not listed above (e.g., a future addition to the protocol), the `getTextAttributesKey()` method returns `null`. This tells the IntelliJ Platform to preserve the lexer-based highlighting for that token, ensuring that unrecognized semantic token types do not cause highlighting to disappear.

**Always-on behavior:** The `shouldAskServerForSemanticTokens()` method unconditionally returns `true`, meaning the plugin always requests semantic tokens when the LSP is connected. This ensures that semantic highlighting is active as soon as the language server is running, without requiring any user action.

The `modifiers` parameter in `getTextAttributesKey()` is currently not used by the ReScript language server but is available for future use (e.g., distinguishing `readonly` properties or `deprecated` identifiers).

#### RescriptCompilationStatusService

**File:** `src/main/kotlin/com/rescript/plugin/lsp/RescriptCompilationStatusService.kt`

This is a project-level service (`@Service(Service.Level.PROJECT)`) that maintains the current compilation status of the ReScript project. It acts as a centralized store and event bus for build state information.

**State management:**

The service holds a single `@Volatile` property `currentStatus` of type `CompilationStatus`, which is a data class containing three fields:
- `status: String` --- The build state, typically `"success"`, `"failed"`, `"building"`, or `"unknown"`.
- `errorCount: Int` --- The number of compilation errors in the last build.
- `warningCount: Int` --- The number of compilation warnings in the last build.

The initial state is `CompilationStatus.UNKNOWN` (status `"unknown"`, zero errors, zero warnings).

**Update flow:**

1. The ReScript language server sends a `rescript/compilationStatus` notification when the build state changes.
2. `RescriptLsp4jClient.compilationStatus()` receives the notification and calls `updateStatus()` on this service.
3. `updateStatus()` stores the new status in `currentStatus` and iterates over all registered listeners, calling `statusChanged()` on each one.

**Listener management:**

Components that need to react to build state changes (such as the status bar widget `RescriptCompilerStatusWidgetFactory`) register a `CompilationStatusListener` via `addListener(disposable, listener)`. The listener is automatically removed when the provided `Disposable` is disposed, preventing memory leaks when tool windows or widgets are closed.

The `CompilationStatusListener` is defined as a functional interface (`fun interface`), so listeners can be registered with a lambda:

```kotlin
service.addListener(disposable) { status ->
    // Update UI based on status.status, status.errorCount, etc.
}
```

**Thread safety:** The `currentStatus` property is marked `@Volatile` to ensure visibility across threads, since `updateStatus()` is called from the LSP message processing thread while `currentStatus` may be read from the EDT (UI thread).

### LSP Server Detection

The plugin searches for the Language Server in this order:

1. Custom path configured in Settings > Languages & Frameworks > ReScript
2. `node_modules/.bin/rescript-language-server` (project-local binary)
3. `node_modules/@rescript/language-server/out/cli.js` (JS fallback)
4. Parent directory `node_modules/.bin/` (monorepo support, walks up to root)
5. Global installation via `which` / `where`

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
