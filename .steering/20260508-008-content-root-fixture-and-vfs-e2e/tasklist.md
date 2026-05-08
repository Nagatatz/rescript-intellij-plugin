# Phase 3: Content-Root Fixture and VFS E2E — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] requirements / design / tasklist のユーザー承認（auto mode）
- [ ] `EnterWorktree content-root-fixture-and-vfs-e2e` で worktree 作成

## Phase 2: 共通インフラ
- [ ] `IntelliJPlatformExtensionWithContentRoot.kt` を heavy fixture ベースで実装（`createFixtureBuilder(name, basePath)`）
- [ ] populated index が動くことを単純なテスト（`addFileToProject` → `FileTypeIndex.getFiles` で件数確認）でローカル検証

## Phase 3: 実装（populated integration tests）
- [ ] `interop/RescriptInteropScannerPopulatedIntegrationTest.kt` を実装（3 ケース）
- [ ] `migration/RescriptMigrationFinderPopulatedIntegrationTest.kt` を実装（3 ケース）

## Phase 3: 実装（VFS e2e）
- [ ] `migration/RescriptMigrationConverterE2eTest.kt` を実装（CLI gated、`Assumptions.assumeTrue`）
- [ ] 成功ケース（`.re → .res` リネーム + 書き換え）と失敗ケース（不正 `.re`）を分割

## Phase 3: コミット前検証
- [ ] `./gradlew ktlintCheck` パス
- [ ] `./gradlew clean buildPlugin` パス
- [ ] `./gradlew test` パス
- [ ] heavy fixture テストの実行時間が 30 秒以内
- [ ] CLI 不在時に e2e が skip されることを実機確認

## Phase 3: ドキュメント更新
- [ ] `docs/repository-structure.md` に heavy fixture の説明を追記
- [ ] 6 機能の関連 steering で「Phase 2/3 で対応」と記載した項目を完了化
- [ ] 20260508-007 (前ステアリング) の「撤回した作業」メモに追補（解決された旨）

## Phase 3: コミット
- [ ] heavy fixture コミット（`✨ Add heavy fixture extension for populated index tests`）
- [ ] populated integration test コミット（`✅ Extend interop / migration tests with populated content-root fixtures`）
- [ ] VFS e2e コミット（`✅ Add migration converter e2e test against rescript CLI`）
- [ ] ドキュメント更新コミット（`📝 Document content-root fixture and resolve phase 2 follow-ups`）
- [ ] tasklist 完了化コミット

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認
- [ ] requirements 受け入れ条件確認
- [ ] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [ ] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- なし — 新規プロダクションコード追加なし、テストとインフラのみ

## リスクと回避策

- **heavy fixture が予想以上に遅い**: 4 件以下に件数を抑える。1 件当たり 10 秒以上かかる場合はテスト粒度を統合（複数アサートを 1 メソッド内）
- **`rescript convert` が project context を要求する**: 必要なら `bsconfig.json` 相当を fixture に配置。最低限のスタブで足りるか実機確認
- **temp directory のクリーンアップ漏れ**: `afterEach` で `deleteRecursively()`、`Files.createTempDirectory` を使用してプロセス終了時の自動 cleanup も保険として効く
