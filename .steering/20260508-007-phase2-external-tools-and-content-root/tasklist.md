# Phase 2: External Tools and Content-Root Fixtures — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] requirements / design / tasklist のユーザー承認（auto mode）
- [ ] `EnterWorktree phase2-external-tools-and-content-root` で worktree 作成

## Phase 2: 共通インフラ
- [ ] `RescriptContentRootProjectDescriptor.kt` を実装（`DefaultLightProjectDescriptor` をベース）
- [ ] `IntelliJPlatformExtensionWithContentRoot.kt` を実装（既存 extension のコピー + descriptor 差し替え）
- [ ] `cli/ExternalCliAvailability.kt` を実装（`mmdc` / `dot` / `npx rescript` の可用性判定）

## Phase 3: 実装（content-root fixture テスト）
- [ ] `interop/RescriptInteropScannerIntegrationTest.kt` に populated ケースを追加（`@ExtendWith(IntelliJPlatformExtensionWithContentRoot::class)` で別クラス分割 or 既存クラスに追加）
- [ ] `migration/RescriptMigrationFinderIntegrationTest.kt` に populated ケースを追加（同上）

## Phase 3: 実装（外部 CLI テスト）
- [ ] `cli/RescriptVariantFlowMermaidExporterCliTest.kt` を実装（`mmdc` 経由で SVG 生成）
- [ ] `cli/RescriptVariantFlowDotExporterCliTest.kt` を実装（`dot -Tsvg` で SVG 生成）
- [ ] `cli/RescriptMigrationConverterCliTest.kt` を実装（実 `.re` の `rescript convert` 動作）

## Phase 3: CI ワークフロー
- [ ] `.github/workflows/ci.yml` の `build` ジョブに 3 つの CLI セットアップステップを追加
- [ ] CI ジョブの実行時間が ~2 分以内に収まることを確認（手動）

## Phase 3: コミット前検証
- [ ] `./gradlew ktlintCheck` パス
- [ ] `./gradlew clean buildPlugin` パス
- [ ] `./gradlew test` パス（ローカルで CLI 不在の場合は skip され、test 自体は緑）
- [ ] CLI を 1 つでもインストールしたローカル環境で対応 CLI test がグリーン（少なくとも `dot` か `npx rescript` のどちらか）

## Phase 3: ドキュメント更新
- [ ] `docs/repository-structure.md` のテスト構成セクションに `cli/` を追記
- [ ] 6 機能の関連 steering で「Phase 2 で対応」と記載した項目を完了化または対応 CLI/test 名に置き換え

## Phase 3: コミット
- [ ] 共通インフラコミット（`✨ Add content-root project descriptor and CLI availability helper`）
- [ ] populated integration test コミット（`✅ Extend interop / migration tests with populated content-root fixtures`）
- [ ] 外部 CLI test コミット（`✅ Add external CLI verification tests for Mermaid / DOT / rescript convert`）
- [ ] CI workflow コミット（`🔧 Install Mermaid CLI / graphviz / rescript on CI for external test gates`）
- [ ] ドキュメント更新コミット（`📝 Document phase 2 test infrastructure`）
- [ ] tasklist 完了化コミット

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認
- [ ] requirements 受け入れ条件確認
- [ ] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [ ] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- なし — 新規プロダクションコード追加なし、テストとインフラのみ
