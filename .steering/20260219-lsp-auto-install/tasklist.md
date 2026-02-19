# Task List: LSP Auto-Install Promotion

## Implementation Tasks

- [x] Create `lsp/RescriptLspDetector.kt` — LSP and project detection utility
- [x] Create `lsp/RescriptLspDetectorTest.kt` — Unit tests for detector
- [x] Create `lsp/RescriptPackageManagerDetector.kt` — Package manager detection
- [x] Create `lsp/RescriptPackageManagerDetectorTest.kt` — Unit tests for PM detector
- [x] Create `lsp/RescriptLspInstaller.kt` — Background install execution
- [x] Create `lsp/RescriptLspInstallerTest.kt` — Unit tests for command construction
- [x] Modify `editor/RescriptEditorNotificationProvider.kt` — Add Install button, delegate to detector
- [x] Register `notificationGroup` in `plugin.xml`
- [x] Create `lsp/RescriptLspStartupActivity.kt` — Startup balloon notification
  - Test省略理由: `ProjectActivity` + `Notification` API はフル IDE 環境が必要で単体テスト困難
- [x] Register `postStartupActivity` in `plugin.xml`
- [x] Update `CLAUDE.md` project structure
- [x] Run `./gradlew buildPlugin` — Build verification
- [x] Run `./gradlew test` — All tests pass
- [x] Commit changes
- [x] Merge to `main`
