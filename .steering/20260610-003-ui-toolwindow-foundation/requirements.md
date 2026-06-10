# 要求内容: ui/ 共通基盤 (完全リファクタリング Phase 2)

## 背景

完全リファクタリング計画の Phase 2。ロードマップ #124 / #125 / #127 と、一次調査で新発見された caret listener 重複を解消する。
新パッケージ `com.rescript.plugin.ui` を新設する (ロードマップが `ui/DualViewToolWindowPanel`, `ui/GraphViewPaintHelpers` を明示しているため)。

## 対象の重複 (実コードで確認済み)

### 1. GraphView paint helpers (#125)

`flow/RescriptVariantFlowGraphView.kt` と `diagram/RescriptDependencyDiagramGraphView.kt` で以下が重複:

- `paintEdges` (L127/L121): polyline 描画 + 終端矢印。**完全同一**
- `paintArrowHead` (L147/L139): 三角形描画。唯一の差分は分岐条件 `to.y > from.y` (flow) vs `to.y >= from.y` (diagram) — 等値は水平終端セグメントの縮退ケースで TD レイアウトでは発生しない
- `truncateToWidth` (L166/L157): 省略記号付き切り詰め。**完全同一**
- `paintLegend` (L82/L82): スウォッチ + ラベルの凡例描画。enum→色の PALETTE 参照以外**同一**
- 描画定数 MARGIN=12 / ARROW_HALF_WIDTH=5 / ARROW_HEIGHT=8 / LEGEND_HEIGHT=28 / LEGEND_SWATCH_SIZE=14 / LEGEND_ITEM_GAP=12 / EDGE_COLOR=JBColor(0xCB3939, 0xE6484F) が**全て同値**で二重定義

### 2. SimpleToolWindowPanel 定型 (#127)

flow / diagram / coverage / impact / interop の 5 panel で共通:

- `SimpleToolWindowPanel(true, true), Disposable` 継承
- `statusLabel = JBLabel(" ")` + centerPanel(BorderLayout, CENTER+SOUTH) + `setContent` + `setToolbar(buildToolbar())`
- `buildToolbar()`: DefaultActionGroup → `createActionToolbar(TOOLBAR_PLACE, group, true)` → `targetComponent = this`
- inner `RefreshAction` (BGT / AllIcons.Actions.Refresh / 説明文のみ相違)
- flow / impact のみ `Alarm(SWING_THREAD, this)` + `REFRESH_DEBOUNCE_MS = 200` のデバウンス付き

### 3. CardLayout Visual/Source トグル (#124)

flow / diagram の 2 panel で共通: `viewCards` / `viewSwitcher` / `@Volatile visualMode` / `switchView(toVisual)` / `VisualModeAction` / `SourceModeAction` (EDT ToggleAction、相互排他) / `CARD_VISUAL` / `CARD_SOURCE`

### 4. Caret tracker

flow (L130-158) / impact (L97-122) で共通: 全既存エディタ + EditorFactoryListener 経由の将来エディタへの CaretListener 装着 → `scheduleRefresh()`。
相違: flow のみ `editor !is EditorEx` ガードがあるが、listener 本体は EditorEx API を使わないため機能的に不活性。
typeinfo / ppx は `eventMulticaster.addCaretListener` の別方式 — 本フェーズでは統一対象外 (実機検証タスクで判断、デフォルトは現状維持)。

## 要求

1. `ui/GraphViewPaintHelpers` を新設し、両 GraphView の paint helper と描画定数を集約する
2. `ui/RescriptToolWindowPanelBase` を新設し、5 panel の toolbar / statusLabel / RefreshAction / デバウンス定型を集約する。**デバウンスは現状どおり Alarm のまま移し、`@Suppress("UnstableApiUsage")` を基盤 1 箇所に集約する** (coroutines 置換は Phase 4)
3. `ui/DualViewToolWindowPanel` を新設し (2 の基盤を継承)、flow / diagram の CardLayout トグルを集約する
4. `ui/RescriptEditorCaretTracker` を新設し、flow / impact の caret listener 装着を集約する
5. ロードマップ #124 / #125 / #127 の進捗管理 (着手時 🚧、完了時に実装済みへ移動)

## 受け入れ条件

- [ ] 既存テスト (両 GraphViewTest 含む) が **無変更で** green
- [ ] 新クラスに KDoc。テスト可能なもの (paint helpers / caret tracker の述語) にはユニットテスト、Swing 基盤クラスは免除理由を tasklist に明記 + kover 除外を理由コメント付きで追加
- [ ] `./gradlew ktlintCheck clean buildPlugin test koverVerify verifyPluginStructure` green (minBound 86 維持)
- [ ] `runIde` での手動スモーク (design.md のチェックリスト) 実施
- [ ] docs (repository-structure.md / CLAUDE.md / product-requirements.md) 同期。sphinx は更新なし (機能不変)

## スコープ外

- GraphView の computeLayout 共通化 (別アルゴリズムのため恒久的に対象外)
- Alarm → coroutines (Phase 4)
- typeinfo / ppx の multicaster 方式統一 (実機検証で安全と確認できた場合のみ追加コミット、できなければ現状維持)
- notebook panel の基盤化 (構造が異なる場合は対象外と記録)
