# Requirements: Batch Improvements

## Overview

This batch implements 3 new features + quality improvements across 4 parallel tracks.

## Track 1: Error Lens

**Goal:** Display LSP diagnostic messages (errors/warnings) inline at the end of editor lines, similar to VS Code Error Lens extension.

**User Story:** As a ReScript developer, I want to see diagnostic messages inline without hovering, so I can quickly identify and fix issues.

**Acceptance Criteria:**
- LSP diagnostic messages appear at the end of the corresponding line in the editor
- Error severity is visually distinguished by color (error=red, warning=yellow, info=gray)
- Multiple diagnostics on the same line show the highest severity + "(+N more)"
- Feature can be toggled on/off in Settings > Languages & Frameworks > ReScript
- Minimum severity level is configurable (ERROR, WARNING, INFO)
- Inlays update dynamically as diagnostics change
- No performance impact on large files (diagnostics processed incrementally via MarkupModelListener)

## Track 2: Debugger Integration

**Goal:** Provide Node.js debugger integration for compiled JavaScript from ReScript files.

**User Story:** As a ReScript developer, I want to debug my compiled JavaScript code from within the IDE.

**Acceptance Criteria:**
- "Debug Compiled JS" action available from Go To menu and context menu
- Action finds compiled JS via existing `findCompiledJsFile()` and launches with `--inspect-brk`
- Dedicated Run Configuration type for ReScript debugging (Run mode / Attach mode)
- Optional dependency on JavaScriptDebugger/NodeJS plugins (graceful degradation in CE)
- Settings editor for debug configuration (source file, Node.js args, working directory)

## Track 3: Unused Open Auto-Removal

**Goal:** Extend existing `RescriptImportOptimizer` to detect and remove unused `open` statements using LSP diagnostics.

**User Story:** As a ReScript developer, I want unused `open` statements to be automatically removed when I optimize imports.

**Acceptance Criteria:**
- Ctrl+Alt+O removes both duplicate AND unused `open` statements
- Unused detection uses LSP diagnostic warnings from `DaemonCodeAnalyzerEx`
- Feature toggleable via `removeUnusedOpensEnabled` setting (default: true)
- Graceful degradation: when LSP is not running, only duplicate removal works
- Compatible with "Optimize imports on save" action

## Track 4: Quality Improvements

### 4a. Test Quality
- Add integration tests using `BasePlatformTestCase` if feasible
- Priority: `RescriptImportOptimizer`, `RescriptStructureViewModel`, `RescriptSwitchFileAction`
- Fallback: extend edge case coverage in existing unit tests

### 4b. Code Quality
- Extract common methods from `RescriptReanalyzeAnnotator` (2x `@Suppress("DuplicatedCode")`)
- Extract common base/utility from `RescriptGenerateModuleTypeAction` and `RescriptGenerateSwitchAction`

### 4c. Documentation
- Update `docs/ideas/concept.md` (outdated info)
- Fix `sphinx-docs/dev/building.md` (removed `pluginUntilBuild` reference)
- Update `docs/product-requirements.md:375` (codebase size NFR)
- Update `docs/product-requirements.md:201` (Community Edition support)

## Constraints
- JavaScriptDebugger/NodeJS are Ultimate/WebStorm only -> optional dependency
- No .res source-level debugging (no source maps from ReScript)
- Error Lens must not degrade editor performance
