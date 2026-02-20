# Tasklist: Quality Improvements

## 4b. Code Quality - Remove Duplicated Code

- [x] Extract `parseDiagnosticEntry()` helper in `RescriptReanalyzeAnnotator.kt`
- [x] Remove `@Suppress("DuplicatedCode")` from `parseJsonOutput()`
- [x] Remove `@Suppress("DuplicatedCode")` from `apply()` method
- [x] Create `RescriptGenerateActionUtil.kt` utility object
- [x] Refactor `RescriptGenerateModuleTypeAction.kt` to use utility
- [x] Refactor `RescriptGenerateSwitchAction.kt` to use utility
- [x] Remove `@Suppress("DuplicatedCode")` from both generate actions

## 4a. Test Quality

- [x] Update `RescriptReanalyzeAnnotatorTest.kt` for refactored methods
- [x] Update `RescriptGenerateModuleTypeActionTest.kt` after refactoring
- [x] Verify `RescriptGenerateSwitchActionTest.kt` still passes

## 4c. Documentation Fixes

- [x] Add historical notice to `docs/ideas/concept.md`
- [x] Fix `pluginUntilBuild` reference in `sphinx-docs/dev/building.md`
- [x] Update NFR-03 in `docs/product-requirements.md`
- [x] Update platform support table in `docs/product-requirements.md`

## Build & Commit

- [x] Run `./gradlew buildPlugin` successfully
- [x] Run `./gradlew test` successfully
- [x] Commit with `♻️ Improve code quality and fix documentation`
