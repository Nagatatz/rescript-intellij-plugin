# Requirements: Documentation & Test Gap Fill

## Overview

Project audit revealed missing KDoc comments and unit tests across the codebase. This task fills those gaps to comply with the project's coding standards defined in CLAUDE.md.

## Scope

### KDoc Gaps (Class-Level)
- `imports/RescriptImportUtil.kt` — object missing KDoc
- `intention/RescriptWrapWithIntention.kt` — 3 subclasses missing KDoc
- `completion/RescriptPostfixTemplateProvider.kt` — 8 private template classes missing KDoc
- `surround/RescriptSurroundDescriptor.kt` — 4 concrete surrounder subclasses missing KDoc
- `statusbar/RescriptCompilerStatusWidgetFactory.kt` — inner widget class missing KDoc
- `hierarchy/RescriptDependencyAnalyzer.kt` — `enum class ReferenceKind` missing KDoc
- `generate/RescriptGenerateModuleTypeAction.kt` — inner `data class Declaration` missing KDoc
- `inspection/RescriptDuplicateOpenInspection.kt` — private QuickFix class missing KDoc
- `inspection/RescriptEmptyModuleInspection.kt` — private QuickFix class missing KDoc

### KDoc Gaps (Method-Level) — 11 files with methods missing @param/@return

### Test Gaps — 15 testable source files without corresponding tests

## Acceptance Criteria
- All class/object/enum declarations have KDoc
- Public/internal methods with 2+ parameters have KDoc with @param/@return
- Unit tests exist for all testable source files (High + Medium priority)
- `./gradlew buildPlugin` succeeds
- `./gradlew test` passes all tests
