# タスクリスト: コード品質改善

## T1: エラーハンドリング改善

- [x] `RescriptCodeVisionProvider.java` — catch ブロックにデバッグログ追加
- [x] `DtsParserProcess.kt` — `extractScript()` に `synchronized` 追加
- [x] `RescriptDependenciesPanel.kt` — 汎用 Exception を具体的な例外型に分割
- [x] `RescriptLspUtils.kt` — URI パース失敗時にトレースログ追加
- [x] コミット

## T2: ブランチカバレッジ改善

- [x] カバレッジレポートで未カバー分岐を特定
- [x] `RescriptProcessUtilsTest.kt` — タイムアウト・空出力テスト追加（+3テスト）
- [x] `RescriptSecurityUtilsTest.kt` — `isWithinProject()` は IDE テストインフラ必要のため免除
- [x] `RescriptFileUtilTest.kt` — parent=null 分岐のテスト追加（+4テスト）
- [x] コミット
- 注: 残り未カバー分岐は PSI/IDE 依存（BraceBalanceUtil, StringLiteral, SecurityUtils）で免除対象

## T3: 検証

- [ ] `./gradlew ktlintCheck` 成功
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] `./gradlew test` 成功
- [ ] `./gradlew koverHtmlReport` でブランチカバレッジ確認（85%+ 目標）

## T4: ドキュメント・マージ

- [ ] tasklist.md 全タスク完了確認
- [ ] main にマージ
