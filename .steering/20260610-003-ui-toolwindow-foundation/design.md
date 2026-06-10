# 設計: ui/ 共通基盤 (Phase 2)

リスク低→高の順に 4 セクション + docs。各セクション = 1 コミット = 独立マージ可能。

## セクション 1: `ui/GraphViewPaintHelpers` (#125、リスク低)

```kotlin
internal object GraphViewPaintHelpers {
    val DEFAULT_EDGE_COLOR: Color = JBColor(Color(0xCB3939), Color(0xE6484F))
    const val LEGEND_HEIGHT = 28          // 凡例帯の高さ (layout 計算からも参照される)
    // 内部定数: ARROW_HALF_WIDTH=5, ARROW_HEIGHT=8, LEGEND_SWATCH_SIZE=14, LEGEND_ITEM_GAP=12

    data class LegendItem(val label: String, val fill: Color, val border: Color)

    fun paintEdges(g: Graphics2D, edges: List<List<Point>>, color: Color = DEFAULT_EDGE_COLOR)
    fun paintArrowHead(g: Graphics2D, from: Point, to: Point)
    fun truncateToWidth(text: String, fm: FontMetrics, maxWidth: Int): String
    fun paintLegend(g: Graphics2D, baseY: Int, items: List<LegendItem>, margin: Int)
}
```

- 両 GraphView は `LEGEND_ITEMS` (enum) + `PALETTE` を `List<LegendItem>` に変換して渡す。PALETTE 自体は各 View に残す (セマンティック色は機能固有)
- `paintArrowHead` は diagram 側の `to.y >= from.y` に統一。差分は等値 (水平終端) の縮退ケースのみで、flow の TD レイアウトでは発生しない (発生しても矢印向きが変わるだけの cosmetic な差)
- 両 View から重複定数 (EDGE_COLOR / ARROW_* / LEGEND_SWATCH_SIZE / LEGEND_ITEM_GAP / LEGEND_HEIGHT) を削除。MARGIN は layout 定数として各 View に残し paintLegend に引数で渡す
- テスト: `GraphViewPaintHelpersTest` — BufferedImage への描画で例外なし + `truncateToWidth` の純ロジック assert。**ui/ は kover 対象、このクラスはテスト必須**
- 受け入れ: 既存 `RescriptVariantFlowGraphViewTest` / `RescriptDependencyDiagramGraphViewTest` が無変更 green

## セクション 2: `ui/RescriptToolWindowPanelBase` (#127、リスク中)

```kotlin
abstract class RescriptToolWindowPanelBase(
    private val toolbarPlace: String,
    debounceMs: Int = 0,
) : SimpleToolWindowPanel(true, true), Disposable {
    protected val statusLabel: JBLabel = JBLabel(" ")

    // UnstableApiUsage: Alarm(SWING_THREAD) — Phase 4 でここ 1 箇所だけ coroutines 化する
    private val refreshAlarm: Alarm? = debounceMs > 0 のとき Alarm(SWING_THREAD, this)

    /** subclass init から呼ぶ: centerPanel(CENTER+statusLabel SOUTH) + setContent + setToolbar */
    protected fun installUi(center: JComponent, actions: DefaultActionGroup)

    /** debounceMs > 0 なら cancel+addRequest、0 なら直接 doRefresh() */
    protected fun scheduleRefresh()

    protected abstract fun doRefresh()

    /** BGT + AllIcons.Actions.Refresh + actionPerformed → scheduleRefresh() */
    protected fun createRefreshAction(description: String): AnAction

    override fun dispose() { /* Alarm は child disposable で自動解放 */ }
}
```

- 適用 5 panel: flow (debounce 200) / impact (debounce 200) / diagram (0) / coverage (0) / interop (0)
- **挙動非変更の根拠**: coverage / interop / diagram の RefreshAction は現在 `refresh()` 直呼びで、debounce 0 の `scheduleRefresh()` は直接 `doRefresh()` を呼ぶため同一。toolbar 構築は文字通り同じコードの移動。ActionGroup の中身 (separator 配置含む) は各 panel が組み立てて渡す
- `@Suppress("UnstableApiUsage")` + 理由コメントは基盤クラスに集約 (typeinfo の POOLED_THREAD Alarm は本フェーズ対象外)
- kover: `com.rescript.plugin.ui.RescriptToolWindowPanelBase*` をクラス除外 (Swing UI 免除)。テスト省略理由を tasklist に記載
- notebook は SimpleToolWindowPanel 構造でない (JPanel ベースの cell リスト) ため対象外と tasklist に記録する (実装時に再確認)

## セクション 3: `ui/DualViewToolWindowPanel` (#124、リスク中)

```kotlin
abstract class DualViewToolWindowPanel(
    toolbarPlace: String,
    debounceMs: Int = 0,
) : RescriptToolWindowPanelBase(toolbarPlace, debounceMs) {
    @Volatile private var visualMode: Boolean = true

    /** JBScrollPane で包んで CardLayout に登録した viewSwitcher を返す。installUi の center に渡す */
    protected fun buildDualView(visual: JComponent, source: JComponent): JComponent

    protected fun switchView(toVisual: Boolean)  // visualMode 更新 + viewCards.show

    /** EDT ToggleAction ペア (相互排他)。説明文は panel ごとに引数で渡す */
    protected fun createVisualModeAction(description: String): ToggleAction
    protected fun createSourceModeAction(description: String): ToggleAction
}
```

- 適用: flow / diagram の 2 panel のみ。CARD_VISUAL / CARD_SOURCE 定数は基盤に移動
- アイコン (ToolWindowHierarchy / FileTypes.Text)・"Visual"/"Source" ラベル・初期カード = visual は両者同一なので基盤に固定
- kover: 同上のクラス除外
- 受け入れ: トグルの相互排他 (`isSelected` が排他) は可能ならヘッドレス assert、不可なら手動スモークで担保

## セクション 4: `ui/RescriptEditorCaretTracker` (リスク中)

```kotlin
internal object RescriptEditorCaretTracker {
    /** 既存全エディタ + 将来エディタ (EditorFactoryListener) に CaretListener を装着 */
    fun install(project: Project, parentDisposable: Disposable, onCaretMoved: () -> Unit)

    /** project 一致フィルタ。internal でテスト可能に */
    internal fun shouldTrack(editor: Editor, project: Project): Boolean
}
```

- 適用: flow / impact の 2 panel (`attachEditorListeners` + `attachCaretListener` を置換)
- flow の `editor !is EditorEx` ガードは **削除して統一**: listener 本体は EditorEx API を一切使わず `scheduleRefresh()` を呼ぶだけで、ガードは機能的に不活性。万一 non-EditorEx エディタが存在しても refresh が 1 回多く走るだけ (描画結果は editor context から再取得されるため不変)
- typeinfo / ppx (multicaster 方式) は対象外。`runIde` スモーク時に multicaster 方式との挙動差を観察し、安全と確認できた場合のみ追加コミットで統一 — 確認できなければ tasklist に残課題として記録
- テスト: fixture で `install` 後にエディタ作成 → caret 移動 → callback 発火を assert (light fixture で EditorFactory ベースのエディタ作成は可能)。困難なら `shouldTrack` の述語テスト + 手動スモークに切替え、免除理由を記載

## セクション 5: docs 同期

- repository-structure.md: `ui/` パッケージ行を新規追加、flow/diagram/coverage/impact/interop 行の代表クラスは変更なし
- CLAUDE.md: レイヤー 3 の Variant Flow / Module Dependency 記述は機能説明のため変更不要 (実装クラス名は変わらない)。`docs/repository-structure.md` 参照で吸収
- product-requirements.md: #124 / #125 / #127 を将来機能テーブルから削除
- build.gradle.kts: kover 除外 2 クラス追加は該当セクションのコミットに含める (docs コミットではない)

## 手動スモークチェックリスト (`./gradlew runIde`)

マージ前に以下を実施し、結果を tasklist に記録する:

1. **Variant Flow**: ToolWindow を開く → `.res` の switch にカーソル → 図が出る → カーソル連打でデバウンス → Visual/Source トグル往復 → Refresh → Jump to Switch → Copy Mermaid / Copy DOT
2. **Module Dependency**: ToolWindow を開く → 図が出る → Visual/Source トグル → Refresh → Export DOT / Mermaid
3. **Type Coverage / Interop Risk / Type Impact**: 各 ToolWindow を開く → 一覧表示 → Refresh → (impact はカーソル移動で追従)
4. 凡例・矢印・ラベル省略 (`…`) が従来どおり描画される (Light/Dark 両方)
5. プロジェクトクローズ → 例外がログに出ない

## リスクと緩和

| リスク | 緩和策 |
|---|---|
| 5 panel は kover 除外 + テスト免除で自動回帰網が薄い | 上記スモークチェックリストを必須化。panel の public API は不変、コンパイラが参照を守る |
| paintArrowHead の `>` → `>=` 統一 | 等値ケースは縮退 (水平終端) のみ。両 GraphViewTest green + スモーク 4 で目視確認 |
| flow の EditorEx ガード削除 | listener 本体が EditorEx 非依存である事実をコードで確認済み。スモーク 1 で確認 |
| 基盤クラス導入による panel の挙動退行 | 各セクション独立コミットで bisect 可能に。既存テスト無変更 green を受け入れ条件に |
