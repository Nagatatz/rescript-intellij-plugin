# Tasklist: VS Code Parity Batch (Tier 1)

## Feature 1: %ffi() JavaScript Injection
- [ ] Add `FFI` token to `Rescript.flex` and `RescriptTokenTypes.kt`
- [ ] Extend `RescriptRawJsInjector.kt` to support FFI
- [ ] Add lexer tests for `ffi` keyword
- [ ] Add injector tests for FFI pattern
- [ ] Update `RescriptTokenTypesTest.kt` counts
- [ ] Commit: `✨ Add %ffi() JavaScript injection support`

## Feature 2: dict{} Keyword Highlighting
- [ ] Add `DICT` token to `Rescript.flex` and `RescriptTokenTypes.kt`
- [ ] Add lexer tests for `dict` keyword
- [ ] Update `RescriptTokenTypesTest.kt` counts (combined with Feature 1)
- [ ] Commit: `✨ Add dict{} keyword highlighting for ReScript v12`

## Feature 3: Cross-file Incremental Type Checking Setting
- [ ] Add `incrementalTypecheckingAcrossFiles` to `RescriptProjectSettings.kt`
- [ ] Add checkbox to `RescriptConfigurable.kt`
- [ ] Update `RescriptLspServerDescriptor.kt` initialization options
- [ ] Add settings tests
- [ ] Commit: `✨ Add cross-file incremental type checking setting`

## Feature 4: LSP Additional Settings
- [ ] Add 4 fields to `RescriptProjectSettings.kt`
- [ ] Add UI elements to `RescriptConfigurable.kt`
- [ ] Restructure `createInitializationOptions()` in `RescriptLspServerDescriptor.kt`
- [ ] Add settings tests
- [ ] Commit: `✨ Add LSP additional settings (binaryPath, platformPath, runtimePath, logLevel)`

## Feature 5: compilationFinished Notification
- [ ] Add notification handler to `RescriptLsp4jClient.kt`
- [ ] Add listener infrastructure to `RescriptCompilationStatusService.kt`
- [ ] Add tests for CompilationFinishedParams
- [ ] Commit: `✨ Handle rescript/compilationFinished LSP notification`

## Feature 6: Doc Comment Stub Generation
- [ ] Create `RescriptGenerateDocCommentIntention.kt`
- [ ] Register in `plugin.xml`
- [ ] Create tests for `extractParams()` and `buildDocComment()`
- [ ] Commit: `✨ Add doc comment stub generation intention`

## Finalization
- [ ] Run `./gradlew clean buildPlugin` and verify
- [ ] Run `./gradlew test` and verify all pass
- [ ] Merge to main
