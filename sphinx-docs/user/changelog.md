# Changelog

## 0.1.10 (2026-03-19)

### Features

- Add benefit descriptions to all Sphinx feature documentation pages
- Complete Japanese translations for all Sphinx documentation

### Bug Fixes

- Add project scope validation and safe path construction
- Fix REPL tool window keyboard input not working
- Fix ktlint violations from ktlint 14.1.0 upgrade
- Fix CI build and Qodana failures

### Refactoring

- Extract RescriptReanalyzeHealthChecker from RescriptReanalyzeServerService
- Deduplicate weigh logic in RescriptCompletionWeigher
- Extract common ancestor traversal in RescriptLspDetector
- Consolidate hardcoded paths to RescriptPaths utility object
- Extract magic numbers to named constants in RescriptUnwrapDescriptor
- Remove junit-vintage-engine and upgrade to JUnit 6.0.3

### Infrastructure

- Add unit tests for 13 classes (parsers, handlers, view models, stub types)
- Update Gradle plugin versions and wrapper
- Add *.hprof to .gitignore

## 0.1.9 (2026-03-16)

### Features

- Add file type recovery startup activity for .res/.resi files

### Bug Fixes

- Fix plugin descriptor registration for liveTemplate context and Grazie
- Add missing getDisplayName override to WorksheetFileType
- Add null safety for LSP notification params
- Fix empty string crash in completion weigher

### Infrastructure

- Bump project template dependency versions

## 0.1.8 (2026-03-12)

### Bug Fixes

- Add explicit ModalityState to invokeLater calls
- Use CopyOnWriteArrayList for thread-safe listener lists
- Add getActionUpdateThread() to AnAction subclasses
- Add remediation guidance to user-facing error messages
- Replace broad catch(Exception) with specific exception types in analysis/
- Fix thread leak in RescriptFormatCheckAnnotator
- Suppress duplicate "Start a build" prompts from LSP server

### Improvements

- Add missing keyboard shortcuts, settings, and intention actions to documentation
- Add undocumented features to Sphinx advanced docs

### Infrastructure

- Add KDoc check, EP registration check, and Trivy vulnerability scan to CI
- Add quality check Gradle tasks and raise coverage threshold
- Add version consistency check and publish approval gate
- Remove Qodana Dependabot bypass and add fail threshold
- Add performance benchmark and headless IDE smoke tests
- Expand test fixtures from 4 to 22 files
- Add missing KDoc comments to 43 class/object declarations

## 0.1.7 (2026-03-11)

### Bug Fixes

- Fix read-action threading violation in getHoverType
- Add icon attribute to fileType registration for .res/.resi
- Remove platform version suffix from plugin version string

## 0.1.6 (2026-03-10)

### Bug Fixes

- Fix EDT threading violation in TypeInfoPanel when accessing caret offset from pooled thread
- Fix GenerateGroup to extend DefaultActionGroup for proper child action support
- Fix testCreator extension point attribute name (`implementation` → `implementationClass`)

## 0.1.5 (2026-03-10)

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

## 0.1.4 (2026-03-06)

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

## 0.1.3 (2026-03-05)

### Improvements

- Add plugin icon for JetBrains Marketplace display
- Fix plugin verifier deprecated API warnings
- Update docs to reflect current implementation state

## 0.1.2 (2026-03-03)

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
