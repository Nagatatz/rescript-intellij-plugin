---
myst:
  html_meta:
    "keywords": "changelog, release notes, version history"
---

# Changelog

## 0.1.15 (2026-06-13)

### Features

- Editor-grade highlighting in tool windows — Type Info, Notebook cells, Hoogle-style signature search results, and PPX annotations render with the same ReScript syntax colours as the editor; Variant Flow and Module Dependency source modes colorize Mermaid syntax
- Colour-coded diagram semantics — Variant Flow arms (constructor / wildcard / binding / todo / nested), Module Dependency node roles (entry / intermediate / leaf / cycle), Interop Risk severity bands, and Type Impact reference kinds each get a Light/Dark-aware palette with legends

### Bug Fixes

- Move Module Dependency diagram building and Type Impact reference resolution off the UI thread, keeping the IDE responsive on large projects
- Stop Type Info and PPX panels from reacting to caret movement in other open projects
- Raise vulnerable dependency floors in project templates: Next.js to ^16.2.9 (GHSA-26hh-7cqf-hhc6) and concurrently to ^10.0.3 (shell-quote advisory)
- Add missing intention description resources flagged by plugin verification

### Refactoring

- Share a common scaffold (toolbar, status bar, debounced refresh) across tool window panels; the debounce now uses platform coroutines instead of the internal Alarm API
- Generate wizard templates through a shared frame, guarded by golden characterization tests covering all 22 templates across 74 configuration combos

### Infrastructure

- Bump IntelliJ Platform to 2026.1.2; move project templates to Expo SDK 56
- Audit template npm versions monthly against the npm advisory database

## 0.1.14 (2026-05-13)

### Features

- Type Narrowing Visualizer — inlay hints showing the narrowed scrutinee type at each `switch` arm, plus narrowed types for pattern bindings
- Switch Flow Diagram — visualize a `switch` as a decision tree in a tool window with Visual / Source toggle, Mermaid + DOT export, and nested-switch sub-tree rendering
- Module Dependency Diagram — Visual mode with Java2D graph view and Kahn-BFS layering
- Type Impact Preview — tool window listing project-wide references for the `type` declaration under the caret
- Notebook Worksheet (`.resnb`) — cell-based ReScript notebook editor with per-cell REPL evaluation and Markdown export
- JS Interop Risk Map — tool window listing `%raw` / `external` / `Obj.magic` / `@bs.*` call sites with kind & risk classification
- Type Coverage Heat Map — tool window showing per-file type-annotation coverage with sortable columns
- Add Missing Switch Arms intention — Alt+Enter intention powered by LSP hover + stub-index fallback
- Rename Variant Constructor intention — LSP-free Alt+Enter rename via word-index occurrence collection
- Hoogle-style Type Signature Search — new "ReScript Types" tab in Search Everywhere with structural AST + unifier matching
- Monorepo workspace detection for pnpm / npm / yarn workspaces, with manual override in Settings
- Tauri, TanStack Start, Remix / React Router v7, Astro, Waku, and Hono + Inertia (SSR) project templates
- Database axis in Project Wizard — libSQL / Postgres / MySQL wired into Hono, Hono GraphQL, Hono+Inertia, Full-Stack, and Monorepo templates

### Bug Fixes

- Reject LSP rename edits that escape the project scope
- Surface diagnostics when Add Missing Switch Arms cannot apply
- Render nested switch arms as a sub-tree in Visual Flow view
- Render tuple patterns with meaningful summaries in Switch Flow Diagram and the narrowing visualizer
- Realign Hono + Inertia template with the `@hono/inertia` v0.2 API
- Pass `--no-sandbox` to mmdc's bundled Chromium so Mermaid renders work on hosted CI

### Refactoring

- Migrate ReScript run configurations from `build -w` to `watch` (ReScript 12 CLI)
- Centralise single-content tool window installation
- Extract declaration signature parser to its own pure object
- Consolidate 1-based line counting in `RescriptOffsetUtils`

### Infrastructure

- Bump IntelliJ Platform to 2026.1.1 and pin verifier-cli 1.403
- Bump IntelliJ Platform Gradle plugin to 2.16.0, Kotlin 2.3.21, Gradle wrapper 9.5.0
- Add pitest mutation testing for `util/` and `lang/` (PR-only job)
- Pin third-party GitHub Actions to commit SHA; pin CI `npm install -g` versions
- Add Trivy secret-scan step uploading SARIF to the GitHub Security tab
- Add `Expires` field to `plugin-verifier-ignored-problems.txt` with monthly-verify warning

## 0.1.13 (2026-04-27)

### Features

- Module Dependency Diagram tool window with Mermaid rendering and DOT export
- Add res-x (HTMX on Bun) project wizard template
- Add Validation Library selection (zod / sury) across all wizard templates
- Add Bun package manager option to the project wizard
- Add Full-Stack GraphQL variant with graphql-yoga and rescript-relay
- Add native PSI fallback intentions for extractLocalModuleToFile and applyUncurried

### Bug Fixes

- Fix latent ReScript build failures across multiple templates
- Fix async event handler bindings in Vite, Next.js, and Electron templates
- Fix Monorepo and Full-Stack dev scripts so res file edits trigger rebuilds
- Declare sury in rescript.json dependencies

### Refactoring

- Migrate `RescriptConfigurable` settings panel to a schema-driven architecture
- Extract static template content from 10 wizard template files to bundled resources
- Replace deprecated FloatingToolbarProvider.priority, ReadAction.compute, and StubBasedPsiElementBase.elementType APIs

### Infrastructure

- Add CodeQL SAST workflow, monthly plugin verifier workflow, and weekly multi-OS verification workflow
- Auto-purge stale plugin jars from the prepareSandbox output to avoid version skew
- Migrate from Dokka V1 to V2
- Bump IntelliJ Platform 2.14.0, Kover 0.9.8, and template dependency floors

## 0.1.12 (2026-04-17)

### Features

- Add React Native (Community CLI) project template
- Add Full-Stack project template (single package)
- Add Hono GraphQL template with yoga, GraphiQL, and Drizzle
- Upgrade Hono template with SQLite/Drizzle and OpenAPI/Scalar UI
- Add vitest smoke tests across all 14 project templates
- Add PackageManager selection through to template generation
- Add TemplateResourceLoader for static template content extraction

### Bug Fixes

- Fix broken tests with unreachable assertions (null early-return pattern)
- Fix flaky integration test exec and preserve interrupt flag

### Refactoring

- Extract static template content from 5 template files to resources
- Extract common paste processor base class
- Extract process execution logic to RescriptProcessUtils
- Replace deprecated AnActionEvent.createFromDataContext API

### Infrastructure

- Add integration test harness for template generation
- Add pa11y-ci accessibility audit and pofilter translation check to CI
- Ratchet kover minBound to 86%
- Add 11 new test classes for improved test coverage

## 0.1.11 (2026-04-01)

### Features

- Add TypeScript/JSX paste conversion support
- Add dedicated REPL icon and improve input area
- Improve REPL UX: resizable split, auto-scroll, history navigation, executor fixes
- Enable syntax highlighting in REPL input area
- Add UI test framework with Remote-Robot for automated Marketplace screenshots
- Add JSX state-based highlighting to Sphinx documentation lexer

### Bug Fixes

- Add missing process timeout to RescriptUnusedCodeInspection
- Improve error handling across 4 files
- Fix UI test failures on JDK 24 and improve test lifecycle
- Fix screenshot quality: IDE-only capture, reliable file open, notification dismissal
- Fix REPL input area being read-only in tool window
- Fix compiled JS file nesting in Project View

### Refactoring

- Modernize Kotlin: convert to data object and serviceOrNull
- Clean up REPL code for Qodana compliance

### Infrastructure

- Introduce Gradle version catalog for dependency management
- Sync version catalog with latest dependency versions
- Update Kover, ktlint, Gradle wrapper, and fix Trivy action tag
- Update dependencies per Dependabot suggestions
- Introduce parameterized tests for lexer keyword and literal tests
- Add branch coverage tests for ProcessUtils and FileUtil
- Add missing KDoc to ProcessResult data class

### Documentation

- Add glossary and FAQ pages to Sphinx docs
- Add tab-sets and dropdowns to Sphinx feature docs
- Add cross-references and improve admonitions in Sphinx docs
- Add Sphinx extensions for SEO, UX, and developer experience

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
