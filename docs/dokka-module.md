# Module ReScript IntelliJ Plugin

Kotlin API reference for the ReScript IntelliJ Plugin, automatically generated from KDoc comments.

The plugin uses a hybrid architecture:

- **Layer 1** — JFlex lexer + lightweight parser + PSI tree (`lang/`, `highlight/`, `folding/`)
- **Layer 2** — LSP integration over stdio (`lsp/`)
- **Layer 3** — IDE integration features (remaining packages)

See the [user-facing architecture guide](https://nagatatz.github.io/rescript-intellij-plugin/en/dev/architecture.html) for a higher-level overview.

# Package com.rescript.plugin

Root package — Language/FileType/Icons definitions.

# Package com.rescript.plugin.lang

Lexer, parser, and token types. Start here if you are adding new syntax support.

# Package com.rescript.plugin.lsp

Language Server integration: server detection, launch, custom requests (`textDocument/createInterface`, `textDocument/openCompiled`), and custom notifications (`rescript/compilationStatus`).

# Package com.rescript.plugin.refactor

Refactoring support: rename, extract variable/function, inline, change signature, React component extraction.

# Package com.rescript.plugin.intention

Intention Actions (Alt+Enter quick transforms): wrap with Some/Ok/Error, Pipe ⇔ function call, add/remove qualifier, and many more.

# Package com.rescript.plugin.inspection

Code inspections: duplicate open detection, empty module check, signature sync, style linting, mutability analysis.

# Package com.rescript.plugin.util

Shared utilities: `RescriptOffsetUtils` (LSP Position ⇔ offset), `RescriptRegexPatterns` (common regex), `RescriptProcessUtils` (external process execution), `RescriptEditorUtils` (write-action helpers).
