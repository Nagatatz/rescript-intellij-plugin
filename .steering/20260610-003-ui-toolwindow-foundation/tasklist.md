# タスクリスト: ui/ 共通基盤 (Phase 2)

セクション間依存: 3 は 2 の後 (継承)。1・4 は独立。5 (docs) は最後。
各セクション = 1 コミット = 独立にビルド・テスト通過可能な単位。

## セクション 0: セットアップ

- [ ] `git fetch origin` + main の ahead/behind 確認
- [ ] `EnterWorktree` で worktree 作成、`pwd` / `git rev-parse --show-toplevel` で編集パス確認
- [ ] `docs/product-requirements.md` の #124 / #125 / #127 に 🚧 マーク (最初のコミットに含める)

## セクション 1: GraphViewPaintHelpers (#125)

- [ ] `ui/GraphViewPaintHelpers.kt` 新規作成 (KDoc 付き、paintEdges / paintArrowHead / truncateToWidth / paintLegend / LegendItem / 共有定数)
- [ ] `ui/GraphViewPaintHelpersTest.kt` 新規作成 (BufferedImage 描画 + truncateToWidth assert)
- [ ] `flow/RescriptVariantFlowGraphView.kt` から重複メソッド・定数を削除して helper 呼び出しに置換
- [ ] `diagram/RescriptDependencyDiagramGraphView.kt` 同上
- [ ] 既存両 GraphViewTest が無変更 green
- [ ] `./gradlew ktlintCheck test` green
- [ ] コミット: `♻️ Extract GraphViewPaintHelpers shared by flow and diagram views`

## セクション 2: RescriptToolWindowPanelBase (#127)

- [ ] `ui/RescriptToolWindowPanelBase.kt` 新規作成 (KDoc 付き、installUi / scheduleRefresh / createRefreshAction / Alarm 集約 + `@Suppress` 1 箇所化)
- [ ] flow / impact (debounce 200)、diagram / coverage / interop (debounce 0) の 5 panel を基盤継承に書き換え
- [ ] notebook が対象外であることを実装時に再確認し、判断を本 tasklist に記録
- [ ] build.gradle.kts に kover クラス除外 `ui.RescriptToolWindowPanelBase*` を理由コメント付きで追加
- [ ] テスト省略 (基盤クラス): Swing UI コンポーネント免除 — toolbar / CardLayout / Alarm は IDE UI 結合のためヘッドレス検証不可。スモークで担保
- [ ] `./gradlew ktlintCheck test` green
- [ ] コミット: `♻️ Add RescriptToolWindowPanelBase and migrate five tool window panels`

## セクション 3: DualViewToolWindowPanel (#124)

- [ ] `ui/DualViewToolWindowPanel.kt` 新規作成 (KDoc 付き、buildDualView / switchView / Visual・Source ToggleAction / CARD 定数)
- [ ] flow / diagram の 2 panel から CardLayout / トグル定型を削除して基盤利用に書き換え
- [ ] build.gradle.kts に kover クラス除外 `ui.DualViewToolWindowPanel*` を理由コメント付きで追加
- [ ] テスト省略 (基盤クラス): 同上 Swing UI 免除。トグル相互排他はヘッドレス assert を試み、不可ならスモーク担保
- [ ] `./gradlew ktlintCheck test` green
- [ ] コミット: `♻️ Add DualViewToolWindowPanel for the Visual/Source card toggle`

## セクション 4: RescriptEditorCaretTracker

- [ ] `ui/RescriptEditorCaretTracker.kt` 新規作成 (KDoc 付き、install / shouldTrack)
- [ ] `ui/RescriptEditorCaretTrackerTest.kt` 新規作成 (fixture でエディタ作成 → caret 移動 → callback 発火、不可なら shouldTrack 述語テスト + 免除理由記載)
- [ ] flow / impact の attachEditorListeners / attachCaretListener を置換 (flow の不活性 EditorEx ガードは削除)
- [ ] typeinfo / ppx の multicaster 統一はスモーク観察後に判断 (統一しない場合は残課題として記録)
- [ ] `./gradlew ktlintCheck test` green
- [ ] コミット: `♻️ Extract RescriptEditorCaretTracker for caret-driven panels`

## セクション 5: ドキュメント同期

- [ ] `docs/repository-structure.md` に `ui/` パッケージ行を追加
- [ ] `docs/product-requirements.md` の #124 / #125 / #127 を将来機能テーブルから削除
- [ ] CLAUDE.md: 変更要否を確認 (実装クラス名不変のため原則不要)
- [ ] sphinx-docs: 更新なしの確認のみ (機能不変)
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
