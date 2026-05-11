# Tasklist — TemplateContext への year / Node メタデータ集約

## フェーズ 0: 準備

- [x] `main` から worktree `template-context-metadata` を作成（`EnterWorktree` 経由）
- [x] `java.time.Year` を使えるか確認（JDK 21+ の前提、問題なし）

## フェーズ 1: refactor + バグ修正（1 コミット）

- [x] `TemplateContext.kt` に `year: Int`, `nodeMajor: String`, `nodeEngine: String` フィールドを追加
- [x] `CommonFiles.mitLicense(ctx, holder)` / `CommonFiles.nvmrc(ctx)` に signature 変更
- [x] `CommonFiles.ciWorkflow` 内の `node-version: 20` を `${ctx.nodeMajor}` に置換
- [x] 16 個の `*TemplateFiles.kt` すべてで `engines`, `mitLicense`, `nvmrc` の呼び出しを更新
- [x] 既存テスト (`CommonFilesTest` の LICENSE 検証) を `year = 2099` の明示注入に書き換え
- [x] `./gradlew ktlintCheck test` 成功確認
- [x] `./gradlew buildPlugin` 成功確認
- [x] コミット: `♻️ Centralize year and Node version metadata on TemplateContext`

## フェーズ 2: 新規テスト追加（1 コミット）

- [x] `CommonFilesTest` に `ciWorkflow uses nodeMajor from context` + regression test 追加
- [x] `TemplateContextTest` に `year defaults to current year` テスト追加
- [x] `TemplateContextTest` に `nodeMajor / nodeEngine default to TemplateVersions` テスト追加
- [x] `TemplateContextTest` に override round-trip テスト追加
- [x] `./gradlew test` 成功確認
- [x] コミット: `✅ Add regression tests for dynamic year and CI Node version`

## フェーズ 3: ドキュメント確認

- [x] `CLAUDE.md` / `docs/repository-structure.md` に `TemplateContext` の説明を確認 — 3 フィールド追加は既存の general な表現内でカバーされるため、追加コミットは不要

## フェーズ 4: 最終検証とマージ

- [x] `./gradlew clean buildPlugin ktlintCheck test koverVerify integrationTest` 成功確認（integration test は `--rerun-tasks` で再実行してバイトコードキャッシュ staleness を回避）
- [x] `tasklist.md` を `[x]` に更新してコミット
- [x] ユーザーに `main` マージ可否を `AskUserQuestion` で確認
- [x] 承認後、`main` へマージ → 作業ブランチ削除 → worktree クリーンアップ

## テスト省略の判断

今回の変更は全て既存クラスの修正であり、新規 `.kt` ファイルは作らない。従って、テスト免除対象は存在しない。
