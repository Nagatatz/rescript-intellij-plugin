# Tasklist: Structural Refactoring

## Implementation

- [x] 1. Create `RescriptProcessUtils.kt` with `runSimpleCommand()`
- [x] 2. Migrate `LspServerDescriptor.tryExec` to use `RescriptProcessUtils`
- [x] 3. Migrate `DtsNodeDetector.isNodeAvailable` to use `RescriptProcessUtils`
- [x] 4. Create `RescriptProcessUtilsTest.kt` (JUnit, 3 tests)

Note: RescriptConfigurable split deferred — pure Swing UI with low ROI.

## Verification

- [x] 5. `./gradlew test` passes
- [x] 6. `./gradlew clean buildPlugin` passes

## Commit & Merge

- [x] 7. Commit: `♻️ Extract RescriptProcessUtils for shared process execution`
- [x] 8. Merge to main
