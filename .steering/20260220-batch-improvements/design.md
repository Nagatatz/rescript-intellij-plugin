# Design: Batch Improvements

## Architecture Overview

```
feature/batch-improvements (batch branch)
├── feature/error-lens          (worktree: ../rescript-wt-error-lens/)
├── feature/debugger            (worktree: ../rescript-wt-debugger/)
├── feature/unused-open-removal (worktree: ../rescript-wt-unused-open/)
└── feature/quality-improvements(worktree: ../rescript-wt-quality/)
```

## Track 1: Error Lens Design

### Approach
`MarkupModelListener` + `InlayModel.addAfterLineEndElement()` pattern (proven by IntelliJ-Inspection-Lens).

### Components

```
errorlens/
├── RescriptErrorLensEditorListener.kt      # FileEditorManagerListener - installs manager per editor
├── RescriptErrorLensManager.kt             # Per-editor lifecycle: MarkupModelListener + Inlay Map
├── RescriptErrorLensRenderer.kt            # EditorCustomElementRenderer (HintRenderer subclass)
├── RescriptErrorLensSeverity.kt            # Severity -> color mapping utility
└── RescriptErrorLensHighlighterInfo.kt     # RangeHighlighter -> diagnostic info extraction
```

### Flow
1. `FileEditorManagerListener.fileOpened()` -> check if ReScript file -> create `ErrorLensManager`
2. `ErrorLensManager` registers `MarkupModelListener` on the editor's `MarkupModel`
3. On `afterAdded(highlighter)`: extract `HighlightInfo`, create afterLineEnd inlay
4. On `beforeRemoved(highlighter)`: dispose corresponding inlay
5. Same-line merge: keep highest severity, append "(+N more)" suffix

### Settings
- `RescriptProjectSettings.State.errorLensEnabled: Boolean = true`
- `RescriptProjectSettings.State.errorLensMinSeverity: String = "WARNING"`

### Registration
- `plugin.xml`: `<fileEditorManagerListener>` for `RescriptErrorLensEditorListener`

## Track 2: Debugger Design

### Tier 1: Debug Compiled JS Action
- Reuses `RescriptOpenCompiledJsAction.findCompiledJsFile()` to locate compiled JS
- Creates a `GeneralCommandLine` with `node --inspect-brk <jsFile>`
- Attaches via `JavaScriptDebuggerStarter` if available (optional dep)

### Tier 2: Run Configuration
- `RescriptDebugConfigurationType` + factory + configuration + options + editor
- Two modes: "Run" (launch with --inspect-brk) and "Attach" (connect to running process)

### Optional Dependencies
```xml
<depends optional="true" config-file="rescript-debug.xml">JavaScriptDebugger</depends>
<depends optional="true" config-file="rescript-nodejs.xml">NodeJS</depends>
```

### New Resource Files
- `META-INF/rescript-debug.xml` - extensions requiring JavaScriptDebugger
- `META-INF/rescript-nodejs.xml` - extensions requiring NodeJS

## Track 3: Unused Open Removal Design

### Approach
Extend `RescriptImportOptimizer.processFile()` to query LSP diagnostics.

### New Component
```
imports/
└── RescriptUnusedOpenDetector.kt  # Extracts unused open warnings from DaemonCodeAnalyzerEx
```

### Modified Flow
```
processFile():
  1. Collect duplicate opens (existing logic)
  2. If removeUnusedOpensEnabled:
     a. Query DaemonCodeAnalyzerEx.processHighlights() for unused open warnings
     b. Match warnings to OPEN_STATEMENT PSI elements
  3. Merge duplicate + unused sets
  4. Delete in reverse offset order
```

### Settings
- `RescriptProjectSettings.State.removeUnusedOpensEnabled: Boolean = true`

## Track 4: Quality Design

### 4a. Test Quality
- Investigate `BasePlatformTestCase` integration (test fixture setup for .res files)
- If feasible: create `RescriptImportOptimizerIntegrationTest` with real PSI
- Fallback: add edge case unit tests for existing test classes

### 4b. Code Duplication
**RescriptReanalyzeAnnotator:**
- `apply()` (line 86) and `parseJsonOutput()` (line 142) have `@Suppress("DuplicatedCode")`
- Extract shared diagnostic-to-TextRange mapping logic into a private helper

**Generate actions:**
- `actionPerformed()` and `update()` patterns in `ModuleTypeAction` and `SwitchAction` are similar
- Extract common `findEnclosingDeclaration()` and `isActionAvailable()` utilities

### 4c. Documentation
- Mark `docs/ideas/concept.md` as historical with a note about current state
- Fix `pluginUntilBuild` reference in `sphinx-docs/dev/building.md`
- Update NFR-03 codebase size description to reflect actual growth
- Update platform support table for Community Edition
