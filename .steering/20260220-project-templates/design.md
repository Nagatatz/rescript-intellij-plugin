# Design: Project Wizard Templates

## Architecture
- `ProjectTemplate.kt` — enum with 12 entries + TemplateCategory enum
- `ProjectFileBuilders.kt` — shared utilities (rescriptJson, packageJson, honoBindings, etc.)
- `templates/*.kt` — 12 template objects, each with `generate(projectName): Map<String, String>`
- `RescriptProjectGenerator.kt` — refactored to delegate to ProjectTemplate
- `RescriptModuleBuilder.kt` — `selectedTemplate: ProjectTemplate` replaces `includeReact: Boolean`
- `RescriptProjectWizardStep.kt` — template JList with category headers + description panel
