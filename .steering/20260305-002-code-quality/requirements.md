# Requirements: Code Quality Fixes

## Background

The codebase has 8 locations with silent exception handling (`catch (_: Exception) {}`) that swallow errors without logging, making debugging difficult. Additionally, `RescriptReplExecutor` catches `InterruptedException` via a generic catch block without restoring the thread's interrupt status.

## Goals

1. Add diagnostic logging to all silent catch blocks
2. Fix InterruptedException handling in RescriptReplExecutor to restore interrupt status
3. Add test coverage for the InterruptedException fix

## Scope

### In Scope

- 6 files with silent catch blocks (8 locations total):
  - `DtsParserProcess.kt` — 1 location (IOException catch at line 62)
  - `DtsNodeDetector.kt` — 1 location (Exception catch at line 82)
  - `RescriptPasteAsJsonAction.kt` — 1 location (Exception catch at line 46)
  - `RescriptSignatureSyncInspection.kt` — 2 locations (Exception catches at lines 45, 89)
  - `RescriptErrorReporter.kt` — 2 locations (Exception catches at lines 182, 193)
- `RescriptReplExecutor.kt` — InterruptedException handling at lines 113, 135
- Test for InterruptedException fix

### Out of Scope

- `RescriptCompletionConfidence.kt` — no silent catch blocks found
- New test files for logging changes (logging is not unit-testable without mocking framework)

## Acceptance Criteria

- [ ] All 8 silent catch blocks have appropriate logging (LOG.debug or LOG.trace)
- [ ] RescriptReplExecutor handles InterruptedException explicitly with `Thread.currentThread().interrupt()` restoration
- [ ] `./gradlew test` passes
- [ ] `./gradlew clean buildPlugin` passes
