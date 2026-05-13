# タスクリスト: Migration Converter の相対パス修正

## セクション A: 修正 + ユニットテスト

- [x] `RescriptMigrationConverter.kt` の `convert()` で `candidate.relativePath` を渡すよう変更
- [x] `buildCommand` の `@param sourcePath` KDoc を「project root からの相対パス」に書き直す
- [x] `RescriptMigrationConverterTest.kt` の 4 ケースを相対パス入力に書き換え
- [x] `./gradlew ktlintCheck` グリーン
- [x] `./gradlew clean buildPlugin test` グリーン
- [x] `🐛 Fix Migration Pilot passing absolute path to rescript convert` でコミット

## セクション B: マージ

- [x] tasklist の全項目を `[x]` に更新（このコミットに含める）
- [x] `main` にマージ、worktree クリーンアップ
