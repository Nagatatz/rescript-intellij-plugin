# タスクリスト: コード品質改善

## T1: エラーハンドリング改善

- [x] `RescriptCodeVisionProvider.java` — catch ブロックにデバッグログ追加
- [x] `DtsParserProcess.kt` — `extractScript()` に `synchronized` 追加
- [x] `RescriptDependenciesPanel.kt` — 汎用 Exception を具体的な例外型に分割
- [x] `RescriptLspUtils.kt` — URI パース失敗時にトレースログ追加
- [x] コミット

## T2: ブランチカバレッジ改善

- [ ] カバレッジレポートで未カバー分岐を特定
- [ ] `RescriptProcessUtilsTest.kt` — タイムアウト・例外パステスト追加
- [ ] `RescriptSecurityUtilsTest.kt` — `isWithinProject()` null ケーステスト追加
- [ ] `RescriptFileUtilTest.kt` — 未カバー分岐のテスト追加
- [ ] コミット

## T3: 検証

- [ ] `./gradlew ktlintCheck` 成功
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] `./gradlew test` 成功
- [ ] `./gradlew koverHtmlReport` でブランチカバレッジ確認（85%+ 目標）

## T4: ドキュメント・マージ

- [ ] tasklist.md 全タスク完了確認
- [ ] main にマージ
