# Tasklist: VS Code Parity Batch (Tier 1)

## Feature 1: %ffi() JavaScript Injection
- [x] Add `FFI` token to `Rescript.flex` and `RescriptTokenTypes.kt`
- [x] Extend `RescriptRawJsInjector.kt` to support FFI
- [x] Add lexer tests for `ffi` keyword
- [x] Add injector tests for FFI pattern
- [x] Update `RescriptTokenTypesTest.kt` counts
- [x] Commit: `✨ Add %ffi() JavaScript injection support`

## Feature 2: dict{} Keyword Highlighting
- [x] Add `DICT` token to `Rescript.flex` and `RescriptTokenTypes.kt`
- [x] Add lexer tests for `dict` keyword
- [x] Update `RescriptTokenTypesTest.kt` counts (combined with Feature 1)
- [x] Commit: `✨ Add dict{} keyword highlighting for ReScript v12`

## Feature 3: Cross-file Incremental Type Checking Setting
- [x] Add `incrementalTypecheckingAcrossFiles` to `RescriptProjectSettings.kt`
- [x] Add checkbox to `RescriptConfigurable.kt`
- [x] Update `RescriptLspServerDescriptor.kt` initialization options
- [x] Add settings tests
- [x] Commit: `✨ Add cross-file incremental type checking setting`

## Feature 4: LSP Additional Settings
- [x] Add 4 fields to `RescriptProjectSettings.kt`
- [x] Add UI elements to `RescriptConfigurable.kt`
- [x] Restructure `createInitializationOptions()` in `RescriptLspServerDescriptor.kt`
- [x] Add settings tests
- [x] Commit: `✨ Add LSP additional settings (binaryPath, platformPath, runtimePath, logLevel)`

## Feature 5: compilationFinished Notification
- [x] Add notification handler to `RescriptLsp4jClient.kt`
- [x] Add listener infrastructure to `RescriptCompilationStatusService.kt`
- [x] Add tests for CompilationFinishedParams
- [x] Commit: `✨ Handle rescript/compilationFinished LSP notification`

## Feature 6: Doc Comment Stub Generation
- [x] Create `RescriptGenerateDocCommentIntention.kt`
- [x] Register in `plugin.xml`
- [x] Create tests for `extractParams()` and `buildDocComment()`
- [x] Commit: `✨ Add doc comment stub generation intention`

## Finalization
- [x] Run `./gradlew clean buildPlugin` and verify
- [x] Run `./gradlew test` and verify all pass
- [x] Merge to main
