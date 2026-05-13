# タスクリスト: Switch Flow 空状態ヒント

## セクション A: 実装 + ユニットテスト

- [x] `RescriptVariantFlowHints.kt` を新規作成 (`flow/` 配下)
- [x] `RescriptVariantFlowPanel.refresh()` を Reason enum 駆動に書き換え
- [x] `RescriptVariantFlowHintsTest.kt` を新規作成し全 Reason をテスト
- [x] `VariantUsage.res` の先頭コメントに fixture 註記を 2 行追加
- [x] `./gradlew ktlintCheck` グリーン
- [x] `./gradlew clean buildPlugin && ./gradlew test --rerun-tasks` グリーン
- [x] `✨ Add how-to hints to the Switch Flow tool window empty state` でコミット

## セクション B: マージ

- [x] tasklist の全項目を `[x]` に更新（このコミットに含める）
- [x] `main` にマージ、worktree クリーンアップ
