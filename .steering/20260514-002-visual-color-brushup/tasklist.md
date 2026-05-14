# Visual Color Brushup — タスクリスト

各セクションは「実装 + テスト + (該当する) ドキュメント = マージ可能 1 単位」の粒度。緑になったセクションから順に main に反映できる構成。

## セクション A: Variant Flow Visual の arm 種別色分け

- [x] `RescriptVariantFlowModel.kt` に `ArmKind` enum と `FlowNode.kind` 追加
- [x] `classifyArm(...)` 純関数追加
- [x] `buildArmNode` で `classifyArm` を呼び `FlowNode.kind` をセット
- [x] `RescriptVariantFlowGraphView.kt` の単色 palette を `Map<ArmKind, Pair<Color, Color>>` に置換
- [x] `Layout` データクラスに `armKinds: List<ArmKind>` 追加 (`armBoxes` と並列)
- [x] `paintComponent` の arm 描画ループで `PALETTE.getValue(kind)` を引く
- [x] `paintLegend(g, canvasWidth, baseY)` を追加し canvas 下部に 5 種の凡例
- [x] `canvasSize.height` を `LEGEND_HEIGHT = 28` 分加算
- [x] `RescriptVariantFlowModelTest.kt` に `classifyArm` の 5 ケース追加
- [x] `RescriptVariantFlowGraphViewTest.kt` に palette 一意性 + `armKinds` size assertion 追加
- [x] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.flow.*"` が緑
- [x] コミット: `🎨 Add ArmKind classification and colour-coded Variant Flow visual`

## セクション B: Module Dependency Visual の Kahn 分類色分け

- [x] `RescriptDependencyDiagramModel.kt` に `NodeRole` enum 追加
- [x] 純関数 `classifyNodes(nodes, edges): Map<String, NodeRole>` 追加 (Kahn BFS)
- [x] `RescriptDependencyDiagramGraphView.kt` の `LayoutNode` に `role: NodeRole` フィールド追加
- [x] 単色 palette を `Map<NodeRole, Pair<Color, Color>>` に置換
- [x] `computeLayout` で `classifyNodes` を呼び各 `LayoutNode.role` を埋める
- [x] `assignLayers` の in-degree カウンタを `classifyNodes` から再利用 (DRY)
- [x] `paintLegend` で canvas 下部に 4 種の凡例 (`Entry point`, `Intermediate`, `Leaf`, `Cycle`)
- [x] `canvasSize.height` を `LEGEND_HEIGHT` 分加算
- [x] `RescriptDependencyDiagramModelTest.kt` に `classifyNodes` の 5 ケース追加
- [x] `RescriptDependencyDiagramGraphViewTest.kt` に role 整合性 + palette 一意性 assertion 追加
- [x] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.diagram.*"` が緑
- [x] コミット: `🎨 Add NodeRole classification and colour-coded Module Dependency visual`

## セクション C: Interop Risk panel の RiskLevel 色帯

- [ ] `RescriptInteropModel.kt` に `internal val COLOR_BY_RISK: Map<RiskLevel, JBColor>` 追加
- [ ] `RescriptInteropRiskPanel.kt` の `EntryRenderer` を `JPanel(BorderLayout)` 構成に変更 (WEST: 4px 色帯、CENTER: 既存ラベル)
- [ ] `RescriptInteropModelTest.kt` に `COLOR_BY_RISK` の網羅性 + 一意性 assertion 追加
- [ ] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.interop.*"` が緑
- [ ] コミット: `🎨 Add RiskLevel colour band to Interop Risk Map rows`

## セクション D: Type Impact panel の TypeRefKind 色ラベル

- [ ] `RescriptTypeImpactModel.kt` に `internal fun colorForKind(kind: TypeRefKind): JBColor` 追加
- [ ] `RescriptTypeImpactPanel.kt` の `EntryRenderer` を `ColoredListCellRenderer<ReferenceEntry>` に変換
- [ ] `RescriptTypeImpactModelTest.kt` を新規作成 (`colorForKind` の網羅性 + 一意性 assertion)
- [ ] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.impact.*"` が緑
- [ ] コミット: `🎨 Add TypeRefKind colour coding to Type Impact references`

## セクション E: Notebook cell の `JBColor` 化バグ修正

- [ ] `RescriptNotebookCellPanel.kt` の 3 つのハードコード `Color` を `JBColor` 化 (`ERROR_FOREGROUND`, `BORDER_COLOR`, `OUTPUT_BACKGROUND`)
- [ ] 他のハードコード `Color` も発見次第 `JBColor` 化
- [ ] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.notebook.*"` が緑
- [ ] コミット: `🎨 Migrate Notebook cell colours to JBColor for Dark theme support`

## セクション F: ドキュメント同期

- [ ] `CLAUDE.md` レイヤー 3 — `flow/` `diagram/` `interop/` `impact/` `notebook/` 段落に色分け言及追記
- [ ] `README.md` Features の該当機能行に色分け追記
- [ ] `docs/repository-structure.md` に新 enum (`ArmKind`, `NodeRole`) を追加
- [ ] `sphinx-docs/user/features/advanced.md` に該当セクションの色凡例説明
- [ ] `cd sphinx-docs && make gettext && make update-po && make build-ja` 実行
- [ ] 新規/変更 `msgid` の日本語 `msgstr` を埋める
- [ ] コミット: `📝 Update CLAUDE.md/README/sphinx-docs for visual color brushup`

## セクション G: 仕上げとマージ

- [ ] `./gradlew ktlintCheck buildPlugin test koverHtmlReport verifyPluginStructure` が緑
- [ ] DoD Phase 3 自己検証 (KDoc、deprecated API、セキュリティ)
- [ ] 手動 visual verification (`./gradlew runIde` で Light/Dark テーマ両方)
- [ ] 本ファイルのすべてのチェックボックスを `[x]` に更新してコミット
- [ ] `AskUserQuestion` でマージ可否を確認
- [ ] 承認後: `git checkout main && git merge worktree-20260514-002-visual-color-brushup && git branch -d worktree-20260514-002-visual-color-brushup`
- [ ] セッション終了 (worktree 自動クリーンアップ)

## 依存関係

- セクション A → B → C → D → E → F → G の順で進める
- A〜E は独立 (各 commit 単体で main にマージ可能だが、worktree 内で順序を維持)
- F は A〜E 完了後 (ドキュメント記述が全機能の実装を前提とする)
- G は F 完了後

## テスト省略の理由

- `RescriptInteropRiskPanel` / `RescriptTypeImpactPanel` / `RescriptNotebookCellPanel` 自体は Swing UI コンポーネントで、`.claude/rules/testing.md` の免除カテゴリ「Swing UI コンポーネント」に該当。代わりに `*Model.kt` 側の palette 関数を `internal` で抽出してユニットテスト
- Visual 機能の `paintComponent` の色描画は Java2D `Graphics2D` 直接操作で UI 免除。`computeLayout` の純関数部分のみテスト
