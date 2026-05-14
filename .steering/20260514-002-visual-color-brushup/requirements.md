# Visual Color Brushup — 要求内容

## 背景

機能発掘調査 `.steering/20260514-001-feature-discovery/` で抽出された 21 件の候補のうち、バケット A の 5 件 (visual / panel の意味別色付け) を本ステアリングで実装する。最近実装した Visual 機能の box / edge が単色赤系で意味的な視覚分離がないという課題を解決する。

## スコープ

5 件の色付け改善 + ドキュメント同期 = 6 コミット。各コミットは model + view + tests を 1 つにまとめ、独立にビルド・テストが通過する粒度。

### 機能 1: Variant Flow Visual の arm 種別色分け

`flow/RescriptVariantFlowModel.kt` + `flow/RescriptVariantFlowGraphView.kt`

- `ArmKind` enum (`ROOT`, `CONSTRUCTOR`, `WILDCARD`, `PATTERN_BINDING`, `TODO_PLACEHOLDER`, `NESTED_SWITCH`) を model に追加
- `FlowNode.kind: ArmKind` フィールド追加
- 純関数 `classifyArm(patternSummary, bodyPreview, hasChildren): ArmKind` を model に追加。判定基準:
  - `hasChildren` → `NESTED_SWITCH`
  - `patternSummary == "_"` → `WILDCARD`
  - `patternSummary[0].isLowerCase()` → `PATTERN_BINDING`
  - `bodyPreview.startsWith("todo")` → `TODO_PLACEHOLDER` (※`todo` リテラルのみ、failwith 等は対象外)
  - default → `CONSTRUCTOR`
- `RescriptVariantFlowGraphView` の単色 palette を `Map<ArmKind, Pair<Color, Color>>` に置換 (Light/Dark 対応の `JBColor` で 5 色)
- 凡例 (`Constructor`, `Wildcard`, `Binding`, `Todo`, `Nested`) を canvas 下部に描画。`canvasSize.height` に `LEGEND_HEIGHT = 28` を加算
- `computeLayout` の geometry は変えない

### 機能 2: Module Dependency Visual の Kahn 分類色分け

`diagram/RescriptDependencyDiagramModel.kt` + `diagram/RescriptDependencyDiagramGraphView.kt`

- `NodeRole` enum (`ENTRY_POINT`, `INTERMEDIATE`, `LEAF`, `CYCLE_MEMBER`) を model に追加
- 純関数 `classifyNodes(nodes, edges): Map<String, NodeRole>` を model に追加 (Kahn の BFS、ドレイン不能ノードを `CYCLE_MEMBER`)
- `LayoutNode.role: NodeRole` フィールド追加
- `RescriptDependencyDiagramGraphView` の単色 palette を `Map<NodeRole, Pair<Color, Color>>` に置換 (4 色)
- 凡例 (`Entry point`, `Intermediate`, `Leaf`, `Cycle`) を canvas 下部に描画
- 既存の `assignLayers` から in-degree 計算を `classifyNodes` から再利用する形でリファクタ
- `EDGE_COLOR` は単色のまま (v1)

### 機能 3: Interop Risk panel の RiskLevel 色帯

`interop/RescriptInteropModel.kt` + `interop/RescriptInteropRiskPanel.kt`

- model に `internal val COLOR_BY_RISK: Map<RiskLevel, JBColor>` 追加 (HIGH 赤 / MEDIUM 黄 / LOW グレー)
- `EntryRenderer` を `JBLabel` から `JPanel(BorderLayout)` 構成に変更:
  - WEST: 4px 幅の `JPanel`、背景色 = `COLOR_BY_RISK.getValue(entry.risk)`
  - CENTER: 既存の `JBLabel` (テキストはそのまま)
- 選択時の背景処理は既存ロジック維持

### 機能 4: Type Impact panel の TypeRefKind 色ラベル

`impact/RescriptTypeImpactModel.kt` + `impact/RescriptTypeImpactPanel.kt`

- model に `internal fun colorForKind(kind: TypeRefKind): JBColor` 追加 (TYPE_REF 青 / CONSTRUCTOR 紫 / PATTERN 緑 / FIELD_ACCESS 橙 / UNKNOWN グレー)
- `EntryRenderer` を `ListCellRenderer<ReferenceEntry>` から `ColoredListCellRenderer<ReferenceEntry>` に変換:
  - `append("[$kind] ", SimpleTextAttributes(STYLE_BOLD, colorForKind(value.kind)))` で kind を色付き bold
  - 残り (file:line, previewLine) は REGULAR / GRAY
- マウスダブルクリック → ナビゲーション処理 (`list.selectedValue` 経由) は影響なし

### 機能 5: Notebook cell の `JBColor` 化バグ修正

`notebook/RescriptNotebookCellPanel.kt`

- 3 つのハードコード `Color` を `JBColor` 化:
  - `Color(0xCC0000)` (line 121, エラー出力 foreground) → `ERROR_FOREGROUND`
  - `Color(0xC0C0C0)` (line 129, セル border) → `BORDER_COLOR`
  - `Color(0xF5F5F5)` (line 130, output 背景) → `OUTPUT_BACKGROUND`
- 他のハードコード `Color` (`Color.GRAY` 等) も発見次第 `JBColor` 化

### 機能 6: ドキュメント同期

- `CLAUDE.md` レイヤー 3 の `flow/` `diagram/` `interop/` `impact/` `notebook/` 段落に色分けの言及を追加
- `README.md` Features の該当機能行に色分け追記
- `docs/repository-structure.md` の該当パッケージ行に新 enum を反映
- `sphinx-docs/user/features/advanced.md` の該当セクションに色凡例の説明を追記
- `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` を `make gettext && make update-po` 後に日本語訳を埋める

## 受け入れ条件

- 全 5 機能で「異なる種別/kind/severity に異なる JBColor 参照が割り当たる」ことをユニットテストで `assertNotEquals` または `distinctBy { it.rgb }` で検証
- 既存のレイアウト系テスト (`empty` / `single` / `linear` / `branching` / `cycle` / `self-loop` / `determinism` 等) はすべて緑のまま
- `./gradlew ktlintCheck buildPlugin test koverHtmlReport verifyPluginStructure` が全緑
- `./gradlew runIde` で Light/Dark テーマ両方で全 ToolWindow を開いてビジュアル確認 (本人による)
- `.claude/rules/definition-of-done.md` Phase 1〜5 すべて通過

## 制約

- `computeLayout` 純関数の geometry には触れない (色のみ追加、凡例分の高さ加算のみ許可)
- palette マップは `*Model.kt` に `internal` で抽出 (UI 免除回避でテストカバレッジ確保)
- 色定義は全て `JBColor(Color(lightHex), Color(darkHex))` で Light/Dark 対応
- 凡例ラベルは英語リテラル
- TODO placeholder 検出は `bodyPreview.startsWith("todo")` リテラルのみ (偽陽性ゼロ優先)
