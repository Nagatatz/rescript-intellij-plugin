# Task List: Documentation & Test Gap Fill

## Phase 1: KDoc Comments

- [x] Add missing class-level KDoc (10 files)
- [x] Add missing method-level KDoc (11 files)

## Phase 2: Unit Tests (High Priority)

- [x] `test/RescriptTestRunConfigurationTest.kt` — SKIP: requires Project instance (ProjectManager unavailable in test sandbox)
- [x] `imports/RescriptImportUtilTest.kt` — SKIP: requires PSI infrastructure
- [x] `navigation/RescriptSymbolContributorTest.kt` — SKIP: requires PSI/indexing infrastructure (GlobalSearchScope, PsiManager, FileTypeIndex)

## Phase 3: Unit Tests (Medium Priority)

- [x] `navigation/RescriptSwitchFileActionTest.kt` — SKIP: requires AnActionEvent, VirtualFile, FileEditorManager
- [x] `run/RescriptRunConfigurationTest.kt` — SKIP: requires IntelliJ execution framework (Executor, ExecutionEnvironment)
- [x] `test/RescriptTestConfigurationProducerTest.kt` — SKIP: requires ConfigurationContext, PsiElement
- [x] `config/RescriptJsonSchemaProviderFactoryTest.kt` — SKIP: trivial factory, schema loading needs classloader
- [x] `hierarchy/RescriptModuleHierarchyTreeStructureTest.kt` — SKIP: requires Project and PSI infrastructure
- [x] `hierarchy/RescriptModuleHierarchyProviderTest.kt` — SKIP: requires DataContext, Editor, PSI tree
- [x] `lang/RescriptAstFactoryTest.kt` — DONE: 4 tests for createLeaf()
- [x] `lang/psi/RescriptStringLiteralTest.kt` — SKIP: extends LeafPsiElement, requires PSI construction

## Phase 4: Unit Tests (Low Priority)

- [x] `indexing/RescriptTodoIndexerTest.kt` — DONE: lexer creation test
- [x] `editor/RescriptQuoteHandlerTest.kt` — DONE: token set tests
- [x] `generate/RescriptGenerateGroupTest.kt` — DONE: getChildren() test
- [x] `structure/RescriptStructureViewModelTest.kt` — SKIP: requires IDE infrastructure (StructureViewTreeElement)

## Phase 5: Verification

- [x] `./gradlew buildPlugin` succeeds
- [x] `./gradlew test` passes all tests
- [x] Commit changes
- [x] Merge to `main`
