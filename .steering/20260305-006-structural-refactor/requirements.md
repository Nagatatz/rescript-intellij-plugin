# Requirements: Structural Refactoring

## Background

The codebase has process execution patterns duplicated in 4 locations (LspServerDescriptor, ReplExecutor, DtsNodeDetector, DtsParserProcess). RescriptConfigurable.kt is 366 lines handling both LSP and editor settings.

## Goals

1. Extract a shared `RescriptProcessUtils` utility for process execution
2. Split `RescriptConfigurable.kt` into focused panel components

## Scope

### In Scope

- `RescriptProcessUtils` — shared ProcessBuilder utility with timeout, stdout/stderr capture
- `RescriptConfigurable` split — LSP panel, Editor panel, orchestrator
- Tests for `RescriptProcessUtils`

### Out of Scope

- Changing process execution logic (pure extraction, no behaviour changes)
- Tests for Configurable panels (Swing UI — exempt)

## Acceptance Criteria

- [ ] `RescriptProcessUtils` extracted with `runProcess()` method
- [ ] At least 2 of 4 call sites migrated to use `RescriptProcessUtils`
- [ ] `RescriptConfigurable` split into ≤200 line files
- [ ] `RescriptProcessUtilsTest` with 3+ test cases
- [ ] `./gradlew test` passes
- [ ] `./gradlew clean buildPlugin` passes
