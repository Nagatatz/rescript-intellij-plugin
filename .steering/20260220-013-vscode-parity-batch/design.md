# Design: VS Code Parity Batch (Tier 1)

## Feature 1: `%ffi()` JavaScript Injection

### Lexer Changes (`Rescript.flex`)
- Add `"ffi" { return RescriptTokenTypes.FFI; }` after `"raw"` rule

### Token Types (`RescriptTokenTypes.kt`)
- Add `FFI = token("FFI")` after `RAW`
- Add `FFI` to `KEYWORDS` TokenSet

### Injector Changes (`RescriptRawJsInjector.kt`)
- Extend `isInsideRawBlock()` to check for both `RAW` and `FFI` tokens

## Feature 2: `dict{}` Keyword Highlighting

### Lexer Changes (`Rescript.flex`)
- Add `"dict" { return RescriptTokenTypes.DICT; }` in keyword section

### Token Types (`RescriptTokenTypes.kt`)
- Add `DICT = token("DICT")` after `LIST`
- Add `DICT` to `KEYWORDS` TokenSet
- Highlighting is automatic (all KEYWORDS map to KEYWORD attribute)

## Feature 3: Cross-file Incremental Type Checking

### Settings (`RescriptProjectSettings.kt`)
- Add `var incrementalTypecheckingAcrossFiles: Boolean = false` to State
- Add property accessor

### UI (`RescriptConfigurable.kt`)
- Add checkbox "Cross-file incremental type checking (experimental)"

### LSP (`RescriptLspServerDescriptor.kt`)
- Add `"acrossFiles"` to `incrementalTypechecking` map

## Feature 4: LSP Additional Settings

### Settings (`RescriptProjectSettings.kt`)
- Add 4 fields: `rescriptBinaryPath`, `platformPath`, `runtimePath`, `logLevel`

### UI (`RescriptConfigurable.kt`)
- 3 `TextFieldWithBrowseButton` for paths, 1 `ComboBox` for logLevel

### LSP (`RescriptLspServerDescriptor.kt`)
- Restructure `extensionConfiguration` to match rescript-vscode format

## Feature 5: `compilationFinished` Notification

### Client (`RescriptLsp4jClient.kt`)
- Add `@JsonNotification("rescript/compilationFinished")` handler
- Add `CompilationFinishedParams` data class

### Service (`RescriptCompilationStatusService.kt`)
- Add `CompilationFinishedListener` fun interface
- Add `notifyCompilationFinished()` and `addFinishedListener()`

## Feature 6: Doc Comment Stub Generation

### New File: `RescriptGenerateDocCommentIntention.kt`
- `PsiElementBaseIntentionAction` subclass
- Detect declarations via `findParentDeclaration()` (reuse pattern from `RescriptAddGenTypeIntention`)
- Extract params from function text (`extractParams()`)
- Build doc comment template (`buildDocComment()`)
- Static companion methods for testability

### Registration (`plugin.xml`)
- Add `<intentionAction>` after existing intention actions
