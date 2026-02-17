# Tasklist: Qodana レポート指摘事項の修正

## タスク

- [x] 1. `RescriptLanguage.kt` — `readResolve` メソッド追加
- [x] 2. `RescriptTokenTypes.kt` — 未使用シンボル 5件に `@Suppress("unused")` 追加
- [x] 3. `RescriptConfigurable.kt` — deprecated `addBrowseFolderListener` を新 API に置換 + `@Suppress("DialogTitleCapitalization")` 追加
- [x] 4. `RescriptSettingsEditor.kt` — deprecated `addBrowseFolderListener` を新 API に置換
- [x] 5. `RescriptFormattingService.kt` — `@Suppress("DialogTitleCapitalization")` 追加
- [x] 6. `RescriptMissingConfigInspection.kt` — `@Suppress("DialogTitleCapitalization")` 追加
- [x] 7. `RescriptLineIndentProvider.kt` — 戻り値型を `String?` → `String` に変更
- [x] 8. `RescriptCodeStyleSettingsProvider.kt` — multi-dollar interpolation に変換
- [x] 9. `RescriptRunConfiguration.kt` — if-null を Elvis 演算子に変換
- [x] 10. ビルド確認 (`./gradlew clean buildPlugin`)
- [x] 11. コミット
