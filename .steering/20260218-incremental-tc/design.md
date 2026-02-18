# Design: Incremental Type Checking 設定

## 変更ファイル
1. `RescriptProjectSettings.kt` - `incrementalTypecheckingEnabled` プロパティ追加
2. `RescriptConfigurable.kt` - JCheckBox による UI 追加 + LSP 再起動
3. `RescriptLspServerDescriptor.kt` - `createInitializationOptions()` に設定値反映

## LSP 初期化オプション
```json
{
  "extensionConfiguration": {
    "codeLens": true,
    "incrementalTypechecking": {
      "enabled": true
    }
  }
}
```
