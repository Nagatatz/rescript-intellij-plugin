# タスクリスト: Add Missing Switch Arms 通知

## セクション A: 実装 + ユニットテスト

- [x] `RescriptAddMissingSwitchArmsIntention.kt` に `ArmsOutcome` sealed class と `RescriptAddMissingArmsDiagnoser` object を追加
- [x] `invoke()` を diagnoser 呼び出し + Notification に書き換え
- [x] `RescriptAddMissingArmsDiagnoserTest.kt` を新規作成し全分岐を検証
- [x] `./gradlew ktlintCheck` グリーン
- [x] `./gradlew clean buildPlugin && ./gradlew test --rerun-tasks` グリーン
- [x] `🐛 Surface diagnostics when Add Missing Switch Arms cannot apply` でコミット

## セクション B: マージ

- [x] tasklist の全項目を `[x]` に更新（このコミットに含める）
- [x] `main` にマージ、worktree クリーンアップ
