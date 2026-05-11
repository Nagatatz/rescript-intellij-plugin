# Phase 2: External Tools and Content-Root Fixtures — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] requirements / design / tasklist のユーザー承認（auto mode）
- [x] `EnterWorktree phase2-external-tools-and-content-root` で worktree 作成

## Phase 2: 共通インフラ
- [~] `RescriptContentRootProjectDescriptor.kt` を実装 → 撤回。`DefaultLightProjectDescriptor` が `IdeaTestUtil.getMockJdk17` 経由で Java module 限定の `LanguageLevelModuleExtension` を要求し、Kotlin-only sandbox では NoClassDefFoundError。`LightProjectDescriptor` 直接継承では index に乗らないことを実機で確認。content-root fixture は別ステアリングで Java module 依存を入れる前提で再挑戦する。
- [~] `IntelliJPlatformExtensionWithContentRoot.kt` を実装 → 同上、撤回。
- [x] `cli/ExternalCliAvailability.kt` を実装

## Phase 3: 実装（content-root fixture テスト）— Phase 2 では撤回
- [~] `interop/RescriptInteropScannerPopulatedIntegrationTest.kt` → 撤回（content-root が動かないため）
- [~] `migration/RescriptMigrationFinderPopulatedIntegrationTest.kt` → 撤回

## Phase 3: 実装（外部 CLI テスト）
- [x] `cli/RescriptVariantFlowMermaidExporterCliTest.kt` を実装
- [x] `cli/RescriptVariantFlowDotExporterCliTest.kt` を実装
- [x] `cli/RescriptMigrationConverterCliTest.kt` を実装（argv が CLI に受理されることを検証。VFS 経路の e2e は content-root fixture が必要なため将来課題）

## Phase 3: CI ワークフロー
- [x] `.github/workflows/ci.yml` の `build` ジョブに `setup-node` + `mmdc` / `graphviz` / `rescript` のインストールステップを追加

## Phase 3: コミット前検証
- [x] `./gradlew ktlintCheck` パス
- [x] `./gradlew clean buildPlugin` パス
- [x] `./gradlew test` パス（ローカル CLI 不在のため 3 件は skip、test 自体は緑）
- [x] CLI tests が `Assumptions.assumeTrue` で skip されることを実機で確認

## Phase 3: ドキュメント更新
- [x] `docs/repository-structure.md` のテスト構成セクションに `cli/` を追記
- [x] 20260508-001 (variant flow) の Phase 2 リファレンスを CLI test 名に置き換え
- [x] 20260508-005 (migration) の Phase 2 リファレンスを CLI test 名に置き換え（VFS e2e は content-root fixture 待ち）

## Phase 3: コミット
- [x] CLI 可用性ヘルパーコミット（`✨ Add availability probe for external CLI tests`）
- [x] CLI test コミット（`✅ Add external CLI verification tests for Mermaid / DOT / rescript convert`）
- [x] CI ワークフローコミット（`🔧 Install Mermaid CLI / graphviz / rescript on CI for external test gates`）
- [ ] ドキュメント更新コミット（`📝 Document phase 2 test infrastructure and re-scope content-root work`）
- [ ] tasklist 完了化コミット

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認（content-root 部分は `[~]` で撤回理由を明記）
- [ ] requirements 受け入れ条件確認（content-root 関連は将来課題に再分類）
- [x] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [x] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- なし — 新規プロダクションコード追加なし、テストとインフラのみ

## 撤回した作業（メモ）

content-root fixture の追加は当初 Phase 2 のスコープに入っていたが、`DefaultLightProjectDescriptor` が `IdeaTestUtil.getMockJdk17` 経由で `LanguageLevelModuleExtension` を要求し、Kotlin-only sandbox では `NoClassDefFoundError` が発生する制約に遭遇した。`LightProjectDescriptor` 単独では index に乗らない（実機で確認）。Java module サポートを追加するか、または別アプローチ（実プロジェクトを参照する heavy fixture）を採るかは、より大きな設計変更になるため別ステアリングで対応する。

**追補（20260508-008 で再挑戦した結果）:** heavy fixture（`IdeaTestFixtureFactory.createFixtureBuilder(name, path, false)`）による content root 付きプロジェクトを試したところ、ファイルは実 file system に書かれ file type も認識されるが、`addFileToProject` で追加したファイルが project module に bind されず `FileTypeIndex.getFiles(...).projectScope` が空のままだった（`inProjectScope=false` を実機ログで確認）。これは Java module による source root の自動構成が無いためで、populated index を駆動するには Java module 依存追加が引き続き必要。20260508-008 では populated index test は plugin 責務外（IntelliJ Platform の挙動）と再分類し、heavy fixture は VFS write action を要する e2e テスト（`RescriptMigrationConverterE2eTest`）に活用した。
