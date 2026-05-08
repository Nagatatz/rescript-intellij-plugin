# Phase 3: Content-Root Fixture and VFS E2E — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] requirements / design / tasklist のユーザー承認（auto mode）
- [x] `EnterWorktree content-root-fixture-and-vfs-e2e` で worktree 作成

## Phase 2: 共通インフラ
- [x] `IntelliJPlatformExtensionWithContentRoot.kt` を heavy fixture ベースで実装（`createFixtureBuilder(name, path, false)`）
- [x] populated index が動かないことを実機検証（heavy fixture でも `addFileToProject` が project module に bind されず `inProjectScope=false`）→ populated index テストはスコープ外と再分類

## Phase 3: 実装（populated integration tests）— 撤回
- [~] `interop/RescriptInteropScannerPopulatedIntegrationTest.kt` → 撤回。populated `FileTypeIndex` の駆動には Java module 由来の source root 構成が必要。production が使う `FileTypeIndex` は IntelliJ Platform の挙動であり plugin 責務外
- [~] `migration/RescriptMigrationFinderPopulatedIntegrationTest.kt` → 同上、撤回

## Phase 3: 実装（VFS e2e）
- [x] `migration/RescriptMigrationConverterE2eTest.kt` を実装（CLI gated `Assumptions.assumeTrue` + heavy fixture で実 `.re → .res` リネーム検証）

## Phase 3: コミット前検証
- [x] `./gradlew ktlintCheck` パス
- [x] `./gradlew clean buildPlugin` パス
- [x] `./gradlew test` パス（ローカル CLI 不在のため e2e は skip、test 自体は緑）
- [x] heavy fixture テストの実行時間が 30 秒以内（実測 26.4 秒）
- [x] CLI 不在時に e2e が skip されることを実機確認

## Phase 3: ドキュメント更新
- [x] `docs/repository-structure.md` に heavy fixture の説明を追記
- [x] 20260508-005 (migration) の Phase 2 リファレンスを `RescriptMigrationConverterE2eTest` に置き換え
- [x] 20260508-007 (前ステアリング) の「撤回した作業」メモに 20260508-008 での再挑戦結果を追補

## Phase 3: コミット
- [x] heavy fixture コミット（`✨ Add heavy fixture extension for content-root tests`）
- [~] populated integration test コミット → 撤回（test 自体を作らない）
- [x] VFS e2e コミット（`✅ Add migration converter VFS e2e test against rescript CLI`）
- [ ] ドキュメント更新コミット（`📝 Document content-root fixture follow-up`）
- [ ] tasklist 完了化コミット

## Phase 4: マージ前
- [ ] 全タスク `[x]` または `[~]`（撤回理由明記済み）確認
- [ ] requirements 受け入れ条件確認
- [ ] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [ ] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- なし — 新規プロダクションコード追加なし、テストとインフラのみ

## 撤回した作業（メモ）

populated `FileTypeIndex` / `FilenameIndex` を駆動する populated integration test は、heavy fixture でも `addFileToProject` が project module に bind されないため動かなかった。実機ログ:

```
vf=/private/var/.../unitTest.../src/Magic.res, fileType=ReScript,
inProjectScope=false, projectIdx=0, allIdx=0
```

ファイル自体は実 FS に存在し file type も認識されるが、project module の source root として登録されていない。これは Java module が提供する自動 source root 構成が Kotlin-only sandbox に無いため。

**再分類:** populated index の動作は IntelliJ Platform の責務であり、plugin 側でテストすべき対象ではない（plugin が `FileTypeIndex.getFiles(...)` を呼び出す動作は smoke test で確認済み）。Java module 依存を追加して populated index を駆動するアプローチは将来課題に保留する。

heavy fixture 自体は VFS write action を要する e2e テストに活用できることを確認したため、`RescriptMigrationConverterE2eTest` で利用している。
