# Variant Flow Diagram — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [ ] requirements / design / tasklist のユーザー承認
- [ ] `EnterWorktree variant-flow-diagram` で worktree 作成

## Phase 2: 既存資産の準備
- [ ] `RescriptSwitchArm` に `bodyEndOffset` フィールドを追加し、collector 側で計算
- [ ] `RescriptSwitchArmCollectorTest` に bodyEndOffset 検証ケースを追加
- [ ] `MermaidLabelEscaping` を `diagram/` 配下に抽出（`RescriptMermaidExporter` から切り出し）
- [ ] 既存 `RescriptMermaidExporterTest` がパスし続けることを確認

## Phase 3: 実装（コアロジック）
- [ ] `flow/RescriptVariantFlowModel.kt` を実装（FlowNode/FlowDiagram + buildAtOffset）
- [ ] `flow/RescriptVariantFlowModelTest.kt` を作成（5 variant + ネスト + 不完全 + offset 範囲外）
- [ ] `flow/RescriptVariantFlowMermaidExporter.kt` を実装
- [ ] `flow/RescriptVariantFlowMermaidExporterTest.kt` を作成（スナップショット 4-5 件）
- [ ] `flow/RescriptVariantFlowDotExporter.kt` を実装
- [ ] `flow/RescriptVariantFlowDotExporterTest.kt` を作成

## Phase 3: 実装（IDE 統合）
- [ ] `flow/RescriptVariantFlowPanel.kt` を実装（CaretListener + debounce + Mermaid プレビュー）
- [ ] `flow/RescriptVariantFlowToolWindowFactory.kt` を実装
- [ ] `flow/RescriptVariantFlowAction.kt` を実装（Tools メニュー）
- [ ] `plugin.xml` に ToolWindow と action を登録

## Phase 3: コミット前検証
- [ ] `./gradlew ktlintCheck` パス
- [ ] `./gradlew clean buildPlugin` パス
- [ ] `./gradlew test` パス
- [ ] ビルド警告が増加していない
- [ ] Deprecated API なし（新規 import 確認）

## Phase 3: ドキュメント更新
- [ ] `CLAUDE.md` レイヤー 3 に `flow/` パッケージを追記
- [ ] `docs/repository-structure.md` パッケージ表に `flow/` を追加
- [ ] `docs/functional-design.md` Extension Point マップに ToolWindow + Action を追加
- [ ] `README.md` Features セクションに「Variant flow diagram」追加
- [ ] `sphinx-docs/user/features/advanced.md` に新セクション
- [ ] 日本語 `.po` 同時更新（`make build-ja` パス確認）
- [ ] `docs/lsp-fallback-matrix.md` に LSP 不要行を追加

## Phase 3: コミット
- [ ] Phase 2 リファクタリングコミット（`♻️ Extract Mermaid label escaping for reuse`）
- [ ] Collector 拡張コミット（`✨ Track switch arm body offsets for downstream tools`）
- [ ] モデル + Exporter コミット（`✨ Add variant flow decision-tree model and exporters`）
- [ ] ToolWindow/Panel/Action コミット（`✨ Add variant flow diagram tool window`）
- [ ] ドキュメント更新コミット（`📝 Document variant flow diagram`）
- [ ] tasklist 完了化コミット

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認
- [ ] requirements 受け入れ条件確認
- [ ] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [ ] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- `RescriptVariantFlowPanel`: Swing UI のためテスト免除
- `RescriptVariantFlowToolWindowFactory`: IDE ライフサイクル依存のためテスト免除
- `RescriptVariantFlowAction`: AnAction 単発呼び出しのみ（テスト免除）
