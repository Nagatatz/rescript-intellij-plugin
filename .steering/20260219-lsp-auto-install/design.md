# Design: LSP Auto-Install Promotion

## Architecture

### New Files

1. **`lsp/RescriptLspDetector.kt`** — `object` utility for LSP and project detection
   - Extracts logic from `RescriptEditorNotificationProvider`
   - `isLspAvailable(projectBasePath)` — checks `node_modules/@rescript/language-server`
   - `isRescriptProject(projectBasePath)` — checks `rescript.json` / `bsconfig.json`
   - `isLspConfigured(project)` — checks custom LSP path in settings
   - Searches project root + parent directories (monorepo)

2. **`lsp/RescriptPackageManagerDetector.kt`** — Package manager auto-detection
   - `PackageManager` enum: NPM, YARN, PNPM (with install commands and lock file names)
   - `PackageManagerDetectionResult` data class (packageManager + workingDirectory)
   - `detect(projectBasePath)` — searches lock files in project root + parents
   - Priority: pnpm-lock.yaml > yarn.lock > package-lock.json
   - Default: NPM + project root

3. **`lsp/RescriptLspInstaller.kt`** — Background install execution
   - `install(project, workingDirectory, packageManager, onSuccess?, onFailure?)`
   - Uses `ProgressManager` + `Task.Backgroundable`
   - Uses `GeneralCommandLine` + `OSProcessHandler`
   - On success: restart LSP + update editor notifications
   - On failure: error balloon with stderr

4. **`lsp/RescriptLspStartupActivity.kt`** — Project startup balloon
   - Implements `ProjectActivity` (suspend fun)
   - Shows balloon when: ReScript project + no custom LSP + no LSP available
   - Actions: "Install with {pm}" + "Configure..."
   - Session-scoped dismiss via `PropertiesComponent`

### Modified Files

5. **`editor/RescriptEditorNotificationProvider.kt`** — Delegate detection to `RescriptLspDetector`, add "Install with {pm}" button
6. **`resources/META-INF/plugin.xml`** — Register `notificationGroup` and `postStartupActivity`

## Data Flow

```
Project Open → RescriptLspStartupActivity
  ├─ isRescriptProject? → no → skip
  ├─ isLspConfigured? → yes → skip
  ├─ isLspAvailable? → yes → skip
  └─ Show balloon notification
       ├─ "Install with npm" → RescriptLspInstaller.install()
       └─ "Configure..." → open settings

File Open (.res) → RescriptEditorNotificationProvider
  ├─ Same checks as above
  └─ Show editor bar with "Install with npm" + "Configure..." + "Dismiss"
```
