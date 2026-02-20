# Requirements: VS Code Parity Batch (Tier 1)

## Overview

Implement 6 low-cost, high-impact features to achieve parity with the official rescript-vscode extension.

## Features

### 1. `%ffi()` JavaScript Injection
- Inject JavaScript language into `%ffi("...")` and `%%ffi(`...`)` blocks, same as existing `%raw()` support
- Accept criteria: JS syntax highlighting works inside `%ffi()` expressions

### 2. `dict{}` Keyword Highlighting
- Recognize ReScript v12 `dict` as a keyword for syntax highlighting
- Accept criteria: `dict` is colored as a keyword in the editor

### 3. Cross-file Incremental Type Checking Setting
- Add `incrementalTypecheckingAcrossFiles` boolean setting
- Send to LSP as `extensionConfiguration.incrementalTypechecking.acrossFiles`
- Accept criteria: Setting appears in UI and value is sent to LSP on initialization

### 4. LSP Additional Settings
- Add `rescriptBinaryPath`, `platformPath`, `runtimePath`, `logLevel` settings
- Send to LSP as `extensionConfiguration.*` matching rescript-vscode format
- Accept criteria: All 4 settings appear in UI and are sent to LSP

### 5. `compilationFinished` Notification
- Handle `rescript/compilationFinished` LSP notification
- Add listener infrastructure to `RescriptCompilationStatusService`
- Accept criteria: Notification is received and listeners are notified

### 6. Doc Comment Stub Generation
- Intention action to insert `/** */` template above declarations
- Extract parameters from function declarations for `@param` tags
- Accept criteria: Alt+Enter shows "Generate doc comment" on declarations

## Constraints

- No breaking changes to existing functionality
- All features must include unit tests
- Build must pass (`./gradlew clean buildPlugin`)
