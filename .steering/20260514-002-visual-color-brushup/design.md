# Visual Color Brushup — 設計

## 共通ユーティリティと依存

| 既存 API | 用途 |
|---|---|
| `com.intellij.ui.JBColor(Color, Color)` | Light/Dark 自動切替の Color。既に `RescriptErrorLensSeverity.kt`, `RescriptTypeCoveragePanel.kt` で使用例あり |
| `com.intellij.ui.ColoredListCellRenderer<T>` | 色付き list cell。既に `navigation/RescriptTypeSignatureCellRenderer.kt` で使用例あり |
| `com.intellij.ui.SimpleTextAttributes` | `STYLE_BOLD` 等のフラグ + Color で attribute 生成 |
| `JBUI.Borders` | レイアウト用余白 |

## 機能 1: Variant Flow Visual

### Model 変更 (`RescriptVariantFlowModel.kt`)

```kotlin
enum class ArmKind {
    ROOT,
    CONSTRUCTOR,
    WILDCARD,
    PATTERN_BINDING,
    TODO_PLACEHOLDER,
    NESTED_SWITCH,
}

data class FlowNode(
    val id: String,
    val patternSummary: String,
    val bodyPreview: String,
    val children: List<FlowNode>,
    val kind: ArmKind = ArmKind.CONSTRUCTOR,
)

internal fun classifyArm(
    patternSummary: String,
    bodyPreview: String,
    hasChildren: Boolean,
): ArmKind = when {
    hasChildren -> ArmKind.NESTED_SWITCH
    patternSummary.trim() == "_" -> ArmKind.WILDCARD
    patternSummary.trim().firstOrNull()?.isLowerCase() == true -> ArmKind.PATTERN_BINDING
    bodyPreview.trim().startsWith("todo") -> ArmKind.TODO_PLACEHOLDER
    else -> ArmKind.CONSTRUCTOR
}
```

`buildArmNode` で `classifyArm(...)` を呼び `FlowNode.kind` をセット。truncate 用 `(deeper switch hidden)` ノードは `ArmKind.NESTED_SWITCH` 固定。

### View 変更 (`RescriptVariantFlowGraphView.kt`)

```kotlin
companion object {
    private val PALETTE: Map<ArmKind, Pair<Color, Color>> = mapOf(
        ArmKind.ROOT             to (JBColor(Color(0xFFE7E8), Color(0x7A2226)) to JBColor(Color(0xCB3939), Color(0xE6484F))),
        ArmKind.CONSTRUCTOR      to (JBColor(Color(0xE7F0FF), Color(0x223A6E)) to JBColor(Color(0x3E72C2), Color(0x6B9CE6))),
        ArmKind.WILDCARD         to (JBColor(Color(0xEEEEEE), Color(0x3C3C3C)) to JBColor(Color(0x9A9A9A), Color(0x6F6F6F))),
        ArmKind.PATTERN_BINDING  to (JBColor(Color(0xE7FBE7), Color(0x254B25)) to JBColor(Color(0x3E9E3E), Color(0x68C268))),
        ArmKind.TODO_PLACEHOLDER to (JBColor(Color(0xFFF6D9), Color(0x6E4F12)) to JBColor(Color(0xC79A2B), Color(0xE6BC55))),
        ArmKind.NESTED_SWITCH    to (JBColor(Color(0xF1E7FB), Color(0x4A2A6B)) to JBColor(Color(0x8C56C2), Color(0xB07AE0))),
    )
    private const val LEGEND_HEIGHT = 28
}

data class Layout(
    val rootBox: Rectangle,
    val armBoxes: List<Pair<Rectangle, String>>,
    val armKinds: List<ArmKind>,
    val edges: List<List<Point>>,
    val canvasSize: Dimension,
)
```

`paintComponent` での描画ループは `armBoxes.zip(armKinds)` で arm 種別を参照しつつ `PALETTE.getValue(kind)` で fill/border を引く。`canvasSize.height` は `computeFlatLayout` / `computeTreeLayout` 内で `LEGEND_HEIGHT` 分だけ最終加算。

凡例:

```kotlin
private fun paintLegend(g: Graphics2D, canvasWidth: Int, baseY: Int) {
    val items = listOf(
        "Constructor" to ArmKind.CONSTRUCTOR,
        "Wildcard"    to ArmKind.WILDCARD,
        "Binding"     to ArmKind.PATTERN_BINDING,
        "Todo"        to ArmKind.TODO_PLACEHOLDER,
        "Nested"      to ArmKind.NESTED_SWITCH,
    )
    var x = MARGIN
    val y = baseY + 4
    val fm = g.fontMetrics
    for ((label, kind) in items) {
        val (fill, border) = PALETTE.getValue(kind)
        g.color = fill
        g.fillRoundRect(x, y, 14, 14, 4, 4)
        g.color = border
        g.drawRoundRect(x, y, 14, 14, 4, 4)
        g.color = JBColor.foreground()
        g.drawString(label, x + 18, y + fm.ascent)
        x += 18 + fm.stringWidth(label) + 16
    }
}
```

### テスト追加

`RescriptVariantFlowModelTest.kt`:
- arm with bare `_` → `WILDCARD`
- arm with lowercase identifier pattern → `PATTERN_BINDING`
- arm whose body starts with `todo` → `TODO_PLACEHOLDER`
- arm with children → `NESTED_SWITCH`
- default arm → `CONSTRUCTOR`

`RescriptVariantFlowGraphViewTest.kt`:
- `layout.armKinds.size == layout.armBoxes.size`
- `palette assigns 6 distinct fill colours` (companion object accessor via `@TestOnly`)

## 機能 2: Module Dependency Visual

### Model 変更 (`RescriptDependencyDiagramModel.kt`)

```kotlin
enum class NodeRole {
    ENTRY_POINT,
    INTERMEDIATE,
    LEAF,
    CYCLE_MEMBER,
}

fun classifyNodes(
    nodes: List<ModuleNode>,
    edges: List<ModuleEdge>,
): Map<String, NodeRole> {
    val inDeg = nodes.associate { it.name to 0 }.toMutableMap()
    val outDeg = nodes.associate { it.name to 0 }.toMutableMap()
    for (e in edges) {
        inDeg[e.to] = (inDeg[e.to] ?: 0) + 1
        outDeg[e.from] = (outDeg[e.from] ?: 0) + 1
    }
    // Kahn BFS: nodes that can be drained
    val drained = mutableSetOf<String>()
    val queue: ArrayDeque<String> = ArrayDeque()
    val workingIn = inDeg.toMutableMap()
    for (n in nodes) if (workingIn[n.name] == 0) queue.add(n.name)
    while (queue.isNotEmpty()) {
        val cur = queue.removeFirst()
        drained.add(cur)
        for (e in edges.filter { it.from == cur }) {
            workingIn[e.to] = (workingIn[e.to] ?: 0) - 1
            if (workingIn[e.to] == 0) queue.add(e.to)
        }
    }
    return nodes.associate { node ->
        val name = node.name
        val role = when {
            name !in drained -> NodeRole.CYCLE_MEMBER
            inDeg[name] == 0 -> NodeRole.ENTRY_POINT
            outDeg[name] == 0 -> NodeRole.LEAF
            else -> NodeRole.INTERMEDIATE
        }
        name to role
    }
}
```

### View 変更 (`RescriptDependencyDiagramGraphView.kt`)

```kotlin
data class LayoutNode(
    val name: String,
    val layer: Int,
    val box: Rectangle,
    val role: NodeRole,
)

companion object {
    private val PALETTE: Map<NodeRole, Pair<Color, Color>> = mapOf(
        NodeRole.ENTRY_POINT  to (JBColor(Color(0xE7F0FF), Color(0x223A6E)) to JBColor(Color(0x3E72C2), Color(0x6B9CE6))),
        NodeRole.INTERMEDIATE to (JBColor(Color(0xFFF3F4), Color(0x4A1518)) to JBColor(Color(0xCB3939), Color(0xE6484F))),
        NodeRole.LEAF         to (JBColor(Color(0xE7FBE7), Color(0x254B25)) to JBColor(Color(0x3E9E3E), Color(0x68C268))),
        NodeRole.CYCLE_MEMBER to (JBColor(Color(0xFFE0B3), Color(0x5A3A12)) to JBColor(Color(0xD2691E), Color(0xE89E55))),
    )
    private const val LEGEND_HEIGHT = 28
}
```

`computeLayout` 内で `classifyNodes` を呼び出して各 `LayoutNode` の `role` を埋める。`assignLayers` の in-degree カウンタロジックは `classifyNodes` の中に統合 (`assignLayers` 自体は層割り当てのみ担当)。

凡例は機能 1 と同じパターンで `Entry point` / `Intermediate` / `Leaf` / `Cycle`。

### テスト追加

`RescriptDependencyDiagramModelTest.kt`:
- `classifyNodes marks in-degree-0 nodes as ENTRY_POINT`
- `classifyNodes marks out-degree-0 leaf nodes as LEAF`
- `classifyNodes marks Kahn-undrainable nodes as CYCLE_MEMBER`
- `classifyNodes marks intermediate nodes as INTERMEDIATE`
- `classifyNodes empty input returns empty map`

`RescriptDependencyDiagramGraphViewTest.kt`:
- `LayoutNode carries role consistent with classifyNodes`
- `palette assigns 4 distinct fill colours`

## 機能 3: Interop Risk panel

### Model 変更 (`RescriptInteropModel.kt`)

```kotlin
internal val COLOR_BY_RISK: Map<RiskLevel, JBColor> = mapOf(
    RiskLevel.HIGH   to JBColor(Color(0xCC3333), Color(0xFF6666)),
    RiskLevel.MEDIUM to JBColor(Color(0xCC8800), Color(0xFFCC44)),
    RiskLevel.LOW    to JBColor(Color(0x808080), Color(0xA0A0A0)),
)
```

### View 変更 (`RescriptInteropRiskPanel.kt`)

```kotlin
private class EntryRenderer : ListCellRenderer<InteropEntry> {
    private val text = JBLabel()
    private val band = JPanel().apply {
        preferredSize = Dimension(4, 1)
        isOpaque = true
    }
    private val row = JPanel(BorderLayout()).apply {
        add(band, BorderLayout.WEST)
        add(text, BorderLayout.CENTER)
        border = JBUI.Borders.emptyLeft(2)
    }

    override fun getListCellRendererComponent(
        list: JList<out InteropEntry>,
        value: InteropEntry?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean,
    ): Component {
        if (value == null) {
            text.text = ""
            return row
        }
        val risk = value.risk.name.lowercase()
        val kind = value.kind.name.lowercase()
        text.text = "[$risk/$kind] ${value.file.name}:${value.lineNumber}  ${value.previewLine}"
        band.background = COLOR_BY_RISK.getValue(value.risk)
        val bg = if (isSelected) list.selectionBackground else list.background
        row.background = bg
        text.background = bg
        text.foreground = if (isSelected) list.selectionForeground else list.foreground
        return row
    }
}
```

### テスト追加

`RescriptInteropModelTest.kt`:
- `COLOR_BY_RISK has entry for every RiskLevel`
- `COLOR_BY_RISK each level maps to a distinct color reference`

## 機能 4: Type Impact panel

### Model 変更 (`RescriptTypeImpactModel.kt`)

```kotlin
internal fun colorForKind(kind: TypeRefKind): JBColor = when (kind) {
    TypeRefKind.TYPE_REF     -> JBColor(Color(0x3E72C2), Color(0x6B9CE6))
    TypeRefKind.CONSTRUCTOR  -> JBColor(Color(0x8C56C2), Color(0xB07AE0))
    TypeRefKind.PATTERN      -> JBColor(Color(0x3E9E3E), Color(0x68C268))
    TypeRefKind.FIELD_ACCESS -> JBColor(Color(0xC79A2B), Color(0xE6BC55))
    TypeRefKind.UNKNOWN      -> JBColor(Color(0x9A9A9A), Color(0x6F6F6F))
}
```

### View 変更 (`RescriptTypeImpactPanel.kt`)

```kotlin
private class EntryRenderer : ColoredListCellRenderer<ReferenceEntry>() {
    override fun customizeCellRenderer(
        list: JList<out ReferenceEntry>,
        value: ReferenceEntry?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean,
    ) {
        if (value == null) return
        val kindLabel = value.kind.name.lowercase()
        append(
            "[$kindLabel] ",
            SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, colorForKind(value.kind)),
        )
        append(
            "${value.file.name}:${value.lineNumber}  ",
            SimpleTextAttributes.REGULAR_ATTRIBUTES,
        )
        append(value.previewLine, SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }
}
```

### テスト追加

`RescriptTypeImpactModelTest.kt` (新規):
- `colorForKind returns distinct color for each TypeRefKind value`
- `colorForKind UNKNOWN returns muted grey`

## 機能 5: Notebook cell

### View 変更 (`RescriptNotebookCellPanel.kt`)

```kotlin
private companion object {
    val ERROR_FOREGROUND: JBColor = JBColor(Color(0xCC0000), Color(0xE74C3C))
    val BORDER_COLOR: JBColor = JBColor(Color(0xC0C0C0), Color(0x3C3C3C))
    val OUTPUT_BACKGROUND: JBColor = JBColor(Color(0xF5F5F5), Color(0x2B2B2B))
}
```

- line 121 の `Color(0xCC0000)` → `ERROR_FOREGROUND`
- line 129 の `Color(0xC0C0C0)` → `BORDER_COLOR`
- line 130 の `Color(0xF5F5F5)` → `OUTPUT_BACKGROUND`

他の `Color.GRAY` 等は発見次第 `JBColor.GRAY` 化。

### テスト追加

UI 免除 (`RescriptNotebookCellPanel` は Swing UI コンポーネント)。テスト省略。

## 機能 6: ドキュメント同期

各機能の commit 後、最後に 1 commit でまとめて:

- `CLAUDE.md` レイヤー 3 — `flow/` `diagram/` `interop/` `impact/` `notebook/` 段落に色分け言及追記
- `README.md` Features セクションの該当機能行 (Variant Flow / Module Dependency / Interop Risk / Type Impact / Notebook) に色分け追記
- `docs/repository-structure.md` の `flow/` `diagram/` `interop/` `impact/` `notebook/` 行に新 enum (`ArmKind`, `NodeRole`) を追加
- `sphinx-docs/user/features/advanced.md` の該当セクション (Variant Flow Diagram / Module Dependency Diagram / JS Interop Risk Map / Type Impact Preview / Notebook 風 Worksheet) に色凡例の説明
- `cd sphinx-docs && make gettext && make update-po && make build-ja` を実行し、新規/変更 `msgid` の日本語 `msgstr` を埋める

## リスク

1. **`JBColor` の equals** — `JBColor` は `equals` を override していない。palette テストでは `it.rgb`/`it.color` 比較 (Color Scheme 解決後の値) または reference 同一性 (`distinctBy { System.identityHashCode(it) }`) を使う
2. **TODO placeholder の偽陰性** — `todo` リテラルのみマッチ、`failwith("TBD")` 等は CONSTRUCTOR 色。ユーザー判断どおり
3. **`LayoutNode` `==` 比較** — `role` フィールド追加で既存 `assertEquals(a.nodes, b.nodes)` テストは `classifyNodes` 決定性により壊れない (要動作確認)
4. **凡例領域** — `canvasSize.height` を増やす方向の変更なので、既存の包含チェック `assertTrue(box.y + box.height <= canvasSize.height)` は壊れない
5. **Notebook fixture テスト** — UI 免除でテストなし。手動検証のみ
