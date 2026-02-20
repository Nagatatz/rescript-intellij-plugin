# Requirements: Unused Open Auto-Removal

## Summary

Extend `RescriptImportOptimizer` to detect and remove unused `open` statements using LSP diagnostics, in addition to existing duplicate open removal.

## User Story

As a ReScript developer, when I run "Optimize Imports" (Ctrl+Alt+O), I want unused `open` statements (detected by the LSP server) to be automatically removed alongside duplicate opens.

## Acceptance Criteria

1. `RescriptUnusedOpenDetector` queries `DaemonCodeAnalyzerEx` for LSP diagnostic warnings matching "unused open" patterns
2. `RescriptImportOptimizer.processFile()` removes both duplicate and unused opens
3. A project setting `removeUnusedOpensEnabled` (default: true) controls the feature
4. Settings UI includes a checkbox for toggling unused open removal
5. User notification message reflects both duplicate and unused removal counts
6. Unit tests cover the detector's message matching logic and optimizer's merged removal logic

## Constraints

- Must not break existing duplicate open removal behavior
- Feature requires LSP to be running (gracefully returns empty list otherwise)
- Setting persisted in `rescriptSettings.xml`
