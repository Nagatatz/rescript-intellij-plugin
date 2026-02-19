# Task List: Batch Improvements

## Phase 1: Setup
- [x] Create batch branch `feature/batch-improvements` from main
- [x] Create steering documents (requirements.md, design.md, tasklist.md)
- [x] Commit steering documents to batch branch

## Phase 2: Create Worktrees
- [ ] Create worktree `../rescript-wt-error-lens/` with branch `feature/error-lens`
- [ ] Create worktree `../rescript-wt-debugger/` with branch `feature/debugger`
- [ ] Create worktree `../rescript-wt-unused-open/` with branch `feature/unused-open-removal`
- [ ] Create worktree `../rescript-wt-quality/` with branch `feature/quality-improvements`

## Phase 3: Implementation (Parallel Tracks)

### Track 1: Error Lens
- [ ] Create steering docs in `.steering/20260220-error-lens/`
- [ ] Implement `RescriptErrorLensSeverity.kt`
- [ ] Implement `RescriptErrorLensHighlighterInfo.kt`
- [ ] Implement `RescriptErrorLensRenderer.kt`
- [ ] Implement `RescriptErrorLensManager.kt`
- [ ] Implement `RescriptErrorLensEditorListener.kt`
- [ ] Add settings (errorLensEnabled, errorLensMinSeverity) to `RescriptProjectSettings`
- [ ] Add Error Lens section to `RescriptConfigurable`
- [ ] Register `FileEditorManagerListener` in `plugin.xml`
- [ ] Create tests: `RescriptErrorLensSeverityTest.kt`
- [ ] Create tests: `RescriptErrorLensRendererTest.kt`
- [ ] Create tests: `RescriptErrorLensHighlighterInfoTest.kt`
- [ ] Build verification (`./gradlew buildPlugin`)
- [ ] Commit

### Track 2: Debugger Integration
- [ ] Create steering docs in `.steering/20260220-debugger/`
- [ ] Add optional dependencies to `build.gradle.kts`
- [ ] Implement `RescriptDebugCompiledJsAction.kt`
- [ ] Implement `RescriptDebugConfigurationType.kt`
- [ ] Implement `RescriptDebugConfigurationFactory.kt`
- [ ] Implement `RescriptDebugRunConfiguration.kt`
- [ ] Implement `RescriptDebugRunConfigurationOptions.kt`
- [ ] Implement `RescriptDebugSettingsEditor.kt`
- [ ] Create `META-INF/rescript-debug.xml`
- [ ] Create `META-INF/rescript-nodejs.xml`
- [ ] Update `plugin.xml` with optional `<depends>`
- [ ] Register action in `plugin.xml`
- [ ] Create tests: `RescriptDebugCompiledJsActionTest.kt`
- [ ] Create tests: `RescriptDebugRunConfigurationTest.kt`
- [ ] Build verification (`./gradlew buildPlugin`)
- [ ] Commit

### Track 3: Unused Open Auto-Removal
- [ ] Create steering docs in `.steering/20260220-unused-open-removal/`
- [ ] Implement `RescriptUnusedOpenDetector.kt`
- [ ] Modify `RescriptImportOptimizer.kt` to integrate unused open detection
- [ ] Add `removeUnusedOpensEnabled` setting to `RescriptProjectSettings`
- [ ] Add checkbox to `RescriptConfigurable`
- [ ] Create tests: `RescriptUnusedOpenDetectorTest.kt`
- [ ] Update tests: `RescriptImportOptimizerTest.kt`
- [ ] Build verification (`./gradlew buildPlugin`)
- [ ] Commit

### Track 4: Quality Improvements
- [ ] Create steering docs in `.steering/20260220-quality-improvements/`
- [ ] 4a: Add integration tests or extend unit test coverage
- [ ] 4b: Extract common logic from `RescriptReanalyzeAnnotator` (remove DuplicatedCode suppression)
- [ ] 4b: Extract common logic from Generate actions (remove DuplicatedCode suppression)
- [ ] 4b: Create/update tests for refactored code
- [ ] 4c: Update `docs/ideas/concept.md`
- [ ] 4c: Fix `sphinx-docs/dev/building.md`
- [ ] 4c: Update `docs/product-requirements.md` NFR-03 and CE support
- [ ] Build verification (`./gradlew buildPlugin`)
- [ ] Commit

## Phase 4: Merge to Batch Branch
- [ ] Merge `feature/error-lens` into `feature/batch-improvements`
- [ ] Merge `feature/debugger` into `feature/batch-improvements`
- [ ] Merge `feature/unused-open-removal` into `feature/batch-improvements`
- [ ] Merge `feature/quality-improvements` into `feature/batch-improvements`
- [ ] Resolve any merge conflicts (plugin.xml, etc.)
- [ ] Build verification (`./gradlew buildPlugin`)

## Phase 5: Documentation Update
- [ ] Update `CLAUDE.md` with new directories (errorlens/, debug/)
- [ ] Update `docs/product-requirements.md` (move features to implemented)
- [ ] Update `docs/functional-design.md` (Extension Point map, feature comparison)
- [ ] Update `README.md` (feature list)
- [ ] Update `sphinx-docs/` (English + Japanese translations)
- [ ] Commit documentation updates

## Phase 6: Merge to Main
- [ ] Update this tasklist (all tasks checked)
- [ ] Merge `feature/batch-improvements` into `main`
- [ ] Delete batch branch
- [ ] Final build verification (`./gradlew clean buildPlugin`)
