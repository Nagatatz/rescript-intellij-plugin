# タスクリスト: ui/ 共通基盤 (Phase 2)

セクション間依存: 3 は 2 の後 (継承)。1・4 は独立。5 (docs) は最後。
各セクション = 1 コミット = 独立にビルド・テスト通過可能な単位。

## セクション 0: セットアップ

- [x] `git fetch origin` + main の ahead/behind 確認 (0/0)
- [x] `EnterWorktree` で worktree 作成、`pwd` / `git rev-parse --show-toplevel` で編集パス確認
- [x] `docs/product-requirements.md` の #124 / #125 / #127 に 🚧 マーク (最初のコミットに含める)

## セクション 1: GraphViewPaintHelpers (#125)

- [x] `ui/GraphViewPaintHelpers.kt` 新規作成 (KDoc 付き、paintEdges / paintArrowHead / truncateToWidth / paintLegend / LegendItem / 共有定数)
- [x] `ui/GraphViewPaintHelpersTest.kt` 新規作成 (BufferedImage 描画 + ピクセル検証 + truncateToWidth assert)
- [x] `flow/RescriptVariantFlowGraphView.kt` から重複メソッド・定数を削除して helper 呼び出しに置換 (LEGEND_ITEMS は PALETTE 依存のため companion 初期化順を考慮して PALETTE の後に移動)
- [x] `diagram/RescriptDependencyDiagramGraphView.kt` 同上
- [x] 既存両 GraphViewTest が無変更 green
- [x] `./gradlew ktlintCheck test` green
- [ ] コミット: `♻️ Extract GraphViewPaintHelpers shared by flow and diagram views`

## セクション 2: RescriptToolWindowPanelBase (#127)

- [x] `ui/RescriptToolWindowPanelBase.kt` 新規作成 (KDoc 付き、installUi / scheduleRefresh / createRefreshAction / Alarm 集約)
  - 補足: `@Suppress("UnstableApiUsage")` は不要と判明 — Internal なのは typeinfo の `POOLED_THREAD` のみで、基盤が使う `SWING_THREAD` は flow/impact でも suppress なしで使用していた
- [x] flow / impact (debounce 200)、diagram / coverage / interop (debounce 0) の 5 panel を基盤継承に書き換え (refresh/refreshAsync → doRefresh override、inner RefreshAction → createRefreshAction、未使用 Font import の残骸も削除)
- [x] notebook が対象外であることを再確認: `RescriptNotebookCellPanel` は `JPanel(BorderLayout())` ベースの cell コンポーネントで SimpleToolWindowPanel 構造を持たない → 対象外で確定
- [x] build.gradle.kts に kover クラス除外 `ui.RescriptToolWindowPanelBase*` を理由コメント付きで追加
- [x] テスト省略 (基盤クラス): Swing UI コンポーネント免除 — toolbar / Alarm は IDE UI 結合のためヘッドレス検証不可。スモークで担保
- [x] `./gradlew ktlintCheck test` green
- [x] コミット: `♻️ Add RescriptToolWindowPanelBase and migrate five tool window panels` (14dfa0d)

## セクション 3: DualViewToolWindowPanel (#124)

- [x] `ui/DualViewToolWindowPanel.kt` 新規作成 (KDoc 付き、buildDualView / switchView / createVisualModeAction / createSourceModeAction / CARD 定数)
- [x] flow / diagram の 2 panel から CardLayout / トグル定型を削除して基盤利用に書き換え
- [x] build.gradle.kts に kover クラス除外 `ui.DualViewToolWindowPanel*` を理由コメント付きで追加
- [x] テスト省略 (基盤クラス): 同上 Swing UI 免除 (ToggleAction の isSelected は AnActionEvent が必要でヘッドレス単体 assert は不成立 → スモーク 1・2 のトグル往復で担保)
- [x] `./gradlew ktlintCheck test` green
- [x] コミット: `♻️ Add DualViewToolWindowPanel for the Visual/Source card toggle` (8eb304d)

## セクション 4: RescriptEditorCaretTracker

- [x] `ui/RescriptEditorCaretTracker.kt` 新規作成 (KDoc 付き、install / shouldTrack)
- [x] `ui/RescriptEditorCaretTrackerTest.kt` 新規作成 (fixture でエディタ作成 → caret 移動 → callback 発火 / 後から作られたエディタの追跡 / project なしエディタの除外、の 4 ケース — install 経路を実フィクスチャで検証できたため免除不要)
- [x] flow / impact の attachEditorListeners / attachCaretListener を置換 (flow の不活性 EditorEx ガードは削除 — listener 本体が EditorEx API 非依存のため)
- [x] typeinfo / ppx の multicaster 統一: **本フェーズでは統一せず現状維持で確定**。multicaster 方式は全 project のイベントを受けて callback 内で project フィルタする別セマンティクスであり、観察だけで等価性を保証できない。typeinfo は Phase 4 (Alarm 置換) で必ず触るため、そこで再評価する
- [x] `./gradlew ktlintCheck test` green
- [ ] コミット: `♻️ Extract RescriptEditorCaretTracker for caret-driven panels`

## セクション 5: ドキュメント同期

- [x] `docs/repository-structure.md` に `ui/` パッケージ行を追加
- [x] `docs/product-requirements.md` の #124 / #125 / #127 を将来機能テーブルから削除 (リファクタリング候補は残り #128 のみ)
- [x] CLAUDE.md: 変更不要を確認 — レイヤー 3 の Variant Flow / Module Dependency 記述は機能説明でクラス名・挙動とも不変
- [x] sphinx-docs: 更新なしの確認のみ (機能不変)
- [ ] コミット: `📝 Sync docs for Phase 2 ui foundation`

## マージ前検証 (DoD Phase 3〜4)

- [ ] `./gradlew ktlintCheck clean buildPlugin test koverVerify verifyPluginStructure` green (minBound 86 維持)
- [ ] `koverHtmlReport` で GraphViewPaintHelpers / RescriptEditorCaretTracker のカバレッジ確認
- [ ] `./gradlew runIde` で design.md の手動スモークチェックリストを実施し結果を記録
- [ ] tasklist 全項目 `[x]` 更新をマージ前最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否確認
- [ ] main へマージ → ブランチ削除 → push

## テスト免除の記載

- `RescriptToolWindowPanelBase` / `DualViewToolWindowPanel`: Swing UI コンポーネント (SimpleToolWindowPanel 継承、toolbar / CardLayout / Alarm の IDE UI 結合) のため免除。kover クラス除外 + 手動スモークで担保
- 5 panel (flow / diagram / coverage / impact / interop): 既存の免除済みクラス。変更は基盤への定型移譲のみ
