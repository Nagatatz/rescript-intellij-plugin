# Tasklist: S-Priority LSP Features

## #110 Restart LSP Action
- [x] `RescriptRestartLspAction.kt` 作成
- [x] `plugin.xml` にアクション登録
- [x] `RescriptRestartLspActionTest.kt` 作成
- [ ] コミット: `✨ Add Restart LSP action`

## #111 LSP Initialization Options
- [x] `RescriptProjectSettings.kt` に6設定追加
- [x] `RescriptLspServerDescriptor.kt` の `createInitializationOptions()` 更新
- [x] `RescriptConfigurable.kt` に6 UI コントロール追加
- [x] `RescriptProjectSettingsTest.kt` 作成
- [ ] コミット: `✨ Add missing LSP initialization options`

## ドキュメント・仕上げ
- [ ] CLAUDE.md 更新
- [ ] README.md 更新
- [ ] sphinx-docs 更新
- [ ] product-requirements.md 更新（実装済みに移動）
- [ ] コミット: `📝 Update docs for S-priority features`
- [ ] ビルド検証: `./gradlew clean buildPlugin test`
- [ ] main マージ
