# Tasklist: Code Quality Fixes

## Implementation

- [x] 1. Add LOG.debug to `DtsParserProcess.kt` silent catch
- [x] 2. Add LOG.debug to `DtsNodeDetector.kt` silent catch (+ add LOG instance)
- [x] 3. Add LOG.debug to `RescriptPasteAsJsonAction.kt` silent catch (+ add LOG)
- [x] 4. Add LOG.debug to `RescriptSignatureSyncInspection.kt` 2 silent catches (+ add LOG)
- [x] 5. Add LOG.trace to `RescriptErrorReporter.kt` 2 silent catches (+ add LOG)
- [x] 6. Fix InterruptedException handling in `RescriptReplExecutor.kt` (+ add LOG)

Note: No new test files needed — logging changes are not unit-testable; InterruptedException fix is in private method covered by existing public API tests.

## Verification

- [x] 7. `./gradlew clean buildPlugin` passes
- [x] 8. `./gradlew test` passes

## Commit & Merge

- [x] 9. Commit: `🐛 Add exception logging and fix InterruptedException handling`
- [x] 10. Merge to main
