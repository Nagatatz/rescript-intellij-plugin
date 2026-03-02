# Design: S-Priority LSP Features

## #110 Restart LSP Action

`RescriptRestartLspAction` を `AnAction` として実装。`LspServerManager.stopAndRestartIfNeeded()` を呼び出す。既存パターン (`RescriptSwitchFileAction`) に準拠。plugin.xml の ToolsMenu に登録。

## #111 LSP Initialization Options

### 設定追加 (RescriptProjectSettings)
6つの新規プロパティを State クラスと委譲アクセサに追加。

### LSP 初期化 (RescriptLspServerDescriptor)
`createInitializationOptions()` に4つのネストマップを追加:
- `signatureHelp` → `enabled`, `forConstructorPayloads`
- `cache.projectConfig` → `enable`
- `inlayHints` → `enable`, `maxLength`
- `compileStatus` → `enable`

### 設定 UI (RescriptConfigurable)
5つの JCheckBox + 1つの JSpinner を FormBuilder で追加。
