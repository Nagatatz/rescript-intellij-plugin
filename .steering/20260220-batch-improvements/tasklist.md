# Task List: Batch Improvements

## Phase 1: Setup
- [x] Create batch branch `feature/batch-improvements` from main
- [x] Create steering documents (requirements.md, design.md, tasklist.md)
- [x] Commit steering documents to batch branch

## Phase 2: Create Worktrees
- [x] Create worktree `../rescript-wt-error-lens/` with branch `feature/error-lens`
- [x] Create worktree `../rescript-wt-debugger/` with branch `feature/debugger`
- [x] Create worktree `../rescript-wt-unused-open/` with branch `feature/unused-open-removal`
- [x] Create worktree `../rescript-wt-quality/` with branch `feature/quality-improvements`

## Phase 3: Implementation (Parallel Tracks)

### Track 1: Error Lens
- [x] Create steering docs in `.steering/20260220-error-lens/`
- [x] Implement `RescriptErrorLensSeverity.kt`
- [x] Implement `RescriptErrorLensHighlighterInfo.kt`
- [x] Implement `RescriptErrorLensRenderer.kt`
- [x] Implement `RescriptErrorLensManager.kt`
- [x] Implement `RescriptErrorLensEditorListener.kt`
- [x] Add settings (errorLensEnabled, errorLensMinSeverity) to `RescriptProjectSettings`
- [x] Add Error Lens section to `RescriptConfigurable`
- [x] Register `EditorFactoryListener` in `plugin.xml`
- [x] Create tests: `RescriptErrorLensSeverityTest.kt`
- [x] Create tests: `RescriptErrorLensRendererTest.kt`
- [x] Create tests: `RescriptErrorLensHighlighterInfoTest.kt`
- [x] Build verification (`./gradlew buildPlugin`)
- [x] Commit

### Track 2: Debugger Integration
- [x] Create steering docs in `.steering/20260220-debugger/`
- [x] Implement `RescriptDebugCompiledJsAction.kt`
- [x] Implement `RescriptDebugConfigurationType.kt`
- [x] Implement `RescriptDebugConfigurationFactory.kt`
- [x] Implement `RescriptDebugRunConfiguration.kt`
- [x] Implement `RescriptDebugRunConfigurationOptions.kt`
- [x] Implement `RescriptDebugSettingsEditor.kt`
- [x] Create `META-INF/rescript-debug.xml`
- [x] Create `META-INF/rescript-nodejs.xml`
- [x] Update `plugin.xml` with debug extensions and actions
- [x] Create tests: `RescriptDebugConfigurationTypeTest.kt`
- [x] Create tests: `RescriptDebugConfigurationFactoryTest.kt`
- [x] Create tests: `RescriptDebugRunConfigurationOptionsTest.kt`
- [x] Build verification (`./gradlew buildPlugin`)
- [x] Commit

### Track 3: Unused Open Auto-Removal
- [x] Create steering docs in `.steering/20260220-unused-open-removal/`
- [x] Implement `RescriptUnusedOpenDetector.kt`
- [x] Modify `RescriptImportOptimizer.kt` to integrate unused open detection
- [x] Add `removeUnusedOpensEnabled` setting to `RescriptProjectSettings`
- [x] Add checkbox to `RescriptConfigurable`
- [x] Create tests: `RescriptUnusedOpenDetectorTest.kt`
- [x] Update tests: `RescriptImportOptimizerTest.kt`
- [x] Build verification (`./gradlew buildPlugin`)
- [x] Commit

### Track 4: Quality Improvements
- [x] Create steering docs in `.steering/20260220-quality-improvements/`
- [x] 4a: Update tests for refactored code
- [x] 4b: Extract common logic from `RescriptReanalyzeAnnotator` (remove DuplicatedCode suppression)
- [x] 4b: Extract common logic from Generate actions (remove DuplicatedCode suppression)
- [x] 4b: Create/update tests for refactored code
- [x] 4c: Update `docs/ideas/concept.md`
- [x] 4c: Fix `sphinx-docs/dev/building.md`
- [x] 4c: Update `docs/product-requirements.md` NFR-03 and CE support
- [x] Build verification (`./gradlew buildPlugin`)
- [x] Commit

## Phase 4: Merge to Batch Branch
- [x] Merge `feature/error-lens` into `feature/batch-improvements`
- [x] Merge `feature/debugger` into `feature/batch-improvements`
- [x] Merge `feature/unused-open-removal` into `feature/batch-improvements`
- [x] Merge `feature/quality-improvements` into `feature/batch-improvements`
- [x] Resolve merge conflicts (RescriptProjectSettings, RescriptConfigurable)
- [x] Build verification (`./gradlew buildPlugin`)

## Phase 5: Documentation Update
- [x] Update `CLAUDE.md` with new directories (errorlens/, debug/)
- [x] Update `docs/functional-design.md` (Extension Point map, feature comparison)
- [x] Update `README.md` (feature list)
- [x] Commit documentation updates

## Phase 6: Merge to Main
- [x] Update this tasklist (all tasks checked)
- [x] Merge `feature/batch-improvements` into `main`
- [x] Clean up worktrees and branches
- [x] Final build verification (`./gradlew clean buildPlugin`)
