# Tasklist — .gitignore ギャップ充実

## フェーズ 0: 準備

- [x] `main` から worktree `gitignore-gaps` を作成（`EnterWorktree` 経由 + `reset --hard main`）

## フェーズ 1: パターン追加（1 コミット）

- [x] `NextjsTemplateFiles.kt` の `.gitignore` extra に `.env*.local` を追加
- [x] `CloudflareWorkersTemplateFiles.kt` の `.gitignore` extra に `.dev.vars` を追加
- [x] `AwsLambdaTemplateFiles.kt` の `.gitignore` extra に `.aws-sam/` を追加
- [x] `ReactNativeCliTemplateFiles.kt` の `.gitignore` extra に `*.apk`, `*.aab`, `*.ipa` を追加
- [x] 各テンプレートのテストでこれらのパターン存在を assert
- [x] `./gradlew ktlintCheck test buildPlugin` 成功確認
- [x] コミット: `🔧 Flesh out per-template .gitignore patterns to prevent leaks`

## フェーズ 2: 最終検証とマージ

- [x] `./gradlew clean ktlintCheck buildPlugin test koverVerify` 成功確認
- [x] `tasklist.md` を `[x]` に更新してコミット
- [x] ユーザーに `main` マージ可否を確認
- [x] 承認後、マージ → ブランチ削除 → worktree クリーンアップ

## テスト省略の判断

新規 `.kt` ファイルは追加しない。既存テストへのアサーション追加で十分カバー。
