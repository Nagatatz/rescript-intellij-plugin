# Changelog

## 0.1.6

### Bug Fixes

- Fix EDT threading violation in TypeInfoPanel when accessing caret offset from pooled thread
- Fix GenerateGroup to extend DefaultActionGroup for proper child action support
- Fix testCreator extension point attribute name (`implementation` → `implementationClass`)

## 0.1.5

### Refactoring

- Migrate test suite from JUnit 4 to JUnit 5 (Jupiter)
- Centralize regex patterns into RescriptRegexPatterns
- Extract brace balance utilities into RescriptBraceBalanceUtil
- Extract common patterns: quick fix in StyleLintInspection, Gson deserialization, region search in UnwrapDescriptor
- Reduce postfix template boilerplate with data-driven approach
- Remove redundant Elvis operator, duplicate type wrappers, unnecessary companion object shims
- Replace inline regex with string matching in SignatureSyncInspection and isModuleOpened

### Infrastructure

- Remove unused LOG fields in DtsNodeDetector and RescriptReplExecutor
- Restore Kover coverage verification above 54% threshold

## 0.1.4

### Bug Fixes

- Rewrite CodeVisionProvider in Java to avoid internal API reference
- Add exception logging and fix InterruptedException handling

### Refactoring

- Extract RescriptProcessUtils for shared process execution

### Infrastructure

- Optimize CI by merging verify job and reordering Kover tasks
- Add Kover exclusions and unit tests for coverage improvement
- Add medium-difficulty tests for StringLiteral, DtsParserProcess, and GenerateActionUtil
- Expand plugin description and fix changelog versioning

## 0.1.3

### Improvements

- Add plugin icon for JetBrains Marketplace display
- Fix plugin verifier deprecated API warnings
- Update docs to reflect current implementation state

## 0.1.2

### Features

Initial release submitted to JetBrains Marketplace with 109 features:

- Full syntax highlighting via JFlex lexer
- LSP integration with `@rescript/language-server`
- Semantic highlighting via LSP semantic tokens
- Code completion, Go to Definition, Hover, Find Usages
- Code folding (modules, declarations, comments, custom regions)
- Brace matching and smart quotes
- Structure view
- Code formatting via `rescript format` CLI
- Run configurations (Build, Build Watch, Clean)
- Test runner integration (Jest/Vitest)
- Code inspections (duplicate open, empty module, missing config, signature sync, style linting)
- Dead code analysis via reanalyze with server mode
- Import optimization
- 20+ Intention actions (Wrap with, unwrap, pipe conversion, case split, labeled args, etc.)
- Surround with (if/switch/try/block)
- Postfix completion (.switch, .pipe, .log, .promise, .await, etc.)
- Live templates (21 snippets including FFI bindings and React component)
- File templates (Module, Interface, Component)
- Go to Symbol, Go to Related, File switching (.res/.resi)
- Search Everywhere with stub-indexed symbols
- Create interface file, Open compiled JavaScript
- Code Lens (type annotations), Inlay hints
- Compiled JS Preview tool window
- Module Hierarchy and Call Hierarchy views
- Build status in status bar, Error Lens inline diagnostics
- Signature Help, Parameter Info
- Statement up/down mover, Smart Enter
- Extract Variable/Function, Inline, Introduce Constant, Change Signature
- React component extraction, Safe Delete
- Unresolved reference Quick Fix (add open/qualifier)
- Generate function from usage, Type hole Quick Fix
- Generate actions (switch arms, module type, make, record value, JSON encoder/decoder)
- .d.ts to ReScript binding generation
- Project Wizard with 12 templates
- LSP auto-install with package manager detection
- REPL, Worksheet mode (.resw), Scratch files
- Dependency diagram, PPX expansion view
- Type signature search, Type Info tool window
- Editor floating toolbar, framework detector
- GitHub error reporter
- And many more IDE integrations
