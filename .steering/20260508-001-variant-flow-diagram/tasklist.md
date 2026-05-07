# Variant Flow Diagram — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] requirements / design / tasklist のユーザー承認
- [x] `EnterWorktree variant-flow-diagram` で worktree 作成

## Phase 2: 既存資産の準備
- [x] `RescriptSwitchArm` に `bodyEndOffset` フィールドを追加し、collector 側で計算
- [x] `RescriptSwitchArmCollectorTest` に bodyEndOffset 検証ケース 2 件を追加（合計 14 ケース、全 green）
- [x] `MermaidLabelEscaping` を `diagram/` 配下に抽出（`RescriptMermaidExporter` から切り出し）
- [x] 既存 `RescriptMermaidExporterTest` がパスし続けることを確認

## Phase 3: 実装（コアロジック）
- [x] `flow/RescriptVariantFlowModel.kt` を実装（FlowNode/FlowDiagram + buildAtOffset）
- [x] `flow/RescriptVariantFlowModelTest.kt` を作成（option/result/custom + ネスト + offset 範囲外 + 深さキャップ + body truncate の 8 ケース）
- [x] `flow/RescriptVariantFlowMermaidExporter.kt` を実装
- [x] `flow/RescriptVariantFlowMermaidExporterTest.kt` を作成（スナップショット 4 件）
- [x] `flow/RescriptVariantFlowDotExporter.kt` を実装
- [x] `flow/RescriptVariantFlowDotExporterTest.kt` を作成（スナップショット 3 件）

## Phase 3: 実装（IDE 統合）
- [x] `flow/RescriptVariantFlowPanel.kt` を実装（CaretListener + 200ms debounce + Mermaid プレビュー + Copy Mermaid/DOT）
- [x] `flow/RescriptVariantFlowToolWindowFactory.kt` を実装
- [x] `flow/RescriptVariantFlowAction.kt` を実装（Tools メニュー）
- [x] `plugin.xml` に ToolWindow と action を登録

## Phase 3: コミット前検証
- [x] `./gradlew ktlintCheck` パス
- [x] `./gradlew clean buildPlugin` パス
- [x] `./gradlew test` パス
- [x] ビルド警告が増加していない（既存の RescriptLsp4jClient 警告のみ）
- [x] Deprecated API なし（新規 import 確認）

## Phase 3: ドキュメント更新
- [x] `CLAUDE.md` レイヤー 3 に `flow/` パッケージを追記
- [x] `docs/repository-structure.md` パッケージ表に `flow/` を追加
- [x] `docs/functional-design.md` Extension Point マップに ToolWindow + Action を追加
- [x] `README.md` Features セクションに「Variant flow diagram」追加
- [x] `sphinx-docs/user/features/advanced.md` に Variant Flow Diagram セクションを追加
- [x] 日本語 `.po` 同時更新（`make build-ja` 成功）
- [x] `docs/lsp-fallback-matrix.md` に「Variant Flow Diagram」行（LSP 不要）を追加

## Phase 3: コミット
- [x] Phase 2 リファクタリングコミット（`♻️ Extract Mermaid label escaping for reuse`）
- [x] Collector 拡張コミット（`✨ Track switch arm body offsets for downstream tools`）
- [x] モデル + Exporter コミット（`✨ Add variant flow decision-tree model and exporters`）
- [x] ToolWindow/Panel/Action コミット（`✨ Add variant flow diagram tool window`）
- [x] ドキュメント更新コミット（`📝 Document variant flow diagram`）
- [x] tasklist / requirements 完了化コミット（マージ前最終）

## Phase 4: マージ前
- [x] 全タスク `[x]` 確認（手動検証 2 件のみマージ後に持ち越し）
- [x] requirements 受け入れ条件確認（Phase 1 スコープ全て [x]）
- [x] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [ ] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- `RescriptVariantFlowPanel`: Swing UI のためテスト免除
- `RescriptVariantFlowToolWindowFactory`: IDE ライフサイクル依存のためテスト免除
- `RescriptVariantFlowAction`: AnAction 単発呼び出しのみ（テスト免除）
