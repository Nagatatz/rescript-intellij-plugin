# Tasklist: Project Wizard Templates

- [x] Create batch branch `feature/project-templates` from `main`
- [x] Create `ProjectTemplate.kt` — enum with 12 entries + TemplateCategory
- [x] Create `ProjectFileBuilders.kt` — shared utility methods
- [x] Create all 12 template files in `templates/` directory
- [x] Refactor `RescriptProjectGenerator.kt` — delegate to ProjectTemplate
- [x] Update `RescriptModuleBuilder.kt` — selectedTemplate: ProjectTemplate
- [x] Update `RescriptProjectWizardStep.kt` — template list UI
- [x] Create `ProjectTemplateTest.kt` — tests for all 12 templates
- [x] Create `ProjectFileBuildersTest.kt` — tests for shared utilities
- [x] Update `RescriptProjectGeneratorTest.kt` — new API tests
- [x] Update `RescriptModuleBuilderTest.kt` — selectedTemplate tests
- [x] Build check: `./gradlew buildPlugin` — PASSED
- [x] Test check: 62/63 passed (1 pre-existing env issue)
- [x] Commit: `✨ Add 12 project templates to wizard`
- [x] Update docs (CLAUDE.md, product-requirements.md, etc.)
- [x] Merge to `main` (committed directly to main)
