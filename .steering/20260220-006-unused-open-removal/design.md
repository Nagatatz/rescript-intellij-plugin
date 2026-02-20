# Design: Unused Open Auto-Removal

## Architecture

### New Class: `RescriptUnusedOpenDetector`

- Package: `com.rescript.plugin.imports`
- Queries `DaemonCodeAnalyzerEx.processHighlights()` for WARNING-severity highlights
- Filters by message pattern matching "unused open" (ReScript LSP warning format)
- Maps highlight ranges back to `OPEN_STATEMENT` PSI elements via `file.findElementAt()`

### Modified: `RescriptImportOptimizer`

- After collecting duplicates, calls `RescriptUnusedOpenDetector.findUnusedOpens()` if setting enabled
- Merges both lists (deduplicating by text range)
- Deletes all in reverse offset order
- Notification message reports both counts

### Modified: `RescriptProjectSettings`

- New `State` field: `removeUnusedOpensEnabled: Boolean = true`
- Corresponding property accessor

### Modified: `RescriptConfigurable`

- New `JCheckBox` for "Remove unused open statements (requires LSP)"
- Wired into `isModified()`, `apply()`, `reset()`, `disposeUIResources()`

## Data Flow

```
Optimize Imports (Ctrl+Alt+O)
  -> RescriptImportOptimizer.processFile()
    -> Duplicate detection (existing)
    -> RescriptUnusedOpenDetector.findUnusedOpens()
      -> DaemonCodeAnalyzerEx.processHighlights()
      -> Filter WARNING + "unused open" message
      -> Map to OPEN_STATEMENT PSI elements
    -> Merge lists, delete in reverse order
```
