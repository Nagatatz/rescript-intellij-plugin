# Mermaid Syntax Highlighting — 設計

## 機能 1: `flow/MermaidSourceColorizer.kt`

```kotlin
internal object MermaidSourceColorizer {
    /** Renders Mermaid flowchart source as HTML with token coloring. */
    fun render(source: String): String {
        val keywordHex = hexFor(RescriptSyntaxHighlighter.KEYWORD)
        val operatorHex = hexFor(RescriptSyntaxHighlighter.OPERATOR)
        val stringHex = hexFor(RescriptSyntaxHighlighter.STRING)
        val commentHex = hexFor(RescriptSyntaxHighlighter.LINE_COMMENT)
        return buildString {
            append("<html><body style='font-family:monospace;white-space:pre'>")
            for (line in source.lineSequence()) {
                append(renderLine(line, keywordHex, operatorHex, stringHex, commentHex))
                append("<br>")
            }
            append("</body></html>")
        }
    }

    internal fun renderLine(line: String, keywordHex: String, operatorHex: String, stringHex: String, commentHex: String): String {
        val trimmed = line.trimStart()
        if (trimmed.startsWith("%%")) return span(escapeHtml(line), commentHex)
        // Tokenize via regex
        val regex = Regex("""("[^"]*"|-->|---|-\.->|==>|\bflowchart\b|\bgraph\b|\bTD\b|\bLR\b|\bsubgraph\b|\bend\b)""")
        val sb = StringBuilder()
        var last = 0
        for (m in regex.findAll(line)) {
            sb.append(escapeHtml(line.substring(last, m.range.first)))
            val tok = m.value
            val hex = when {
                tok.startsWith("\"") -> stringHex
                tok.startsWith("-") || tok.startsWith("=") -> operatorHex
                else -> keywordHex
            }
            sb.append(span(escapeHtml(tok), hex))
            last = m.range.last + 1
        }
        sb.append(escapeHtml(line.substring(last)))
        return sb.toString()
    }

    private fun span(text: String, hex: String): String =
        "<span style='color:$hex'>$text</span>"

    private fun hexFor(key: TextAttributesKey): String {
        val ta = EditorColorsManager.getInstance().globalScheme.getAttributes(key)
        val c = ta?.foregroundColor ?: return DEFAULT_HEX
        return String.format("#%02X%02X%02X", c.red, c.green, c.blue)
    }

    private fun escapeHtml(s: String): String =
        s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    private const val DEFAULT_HEX = "#888888"
}
```

### テスト戦略

`MermaidSourceColorizerTest` (`flow/`):
- `renderLine` を直接呼び、トークン認識を assertion (キーワード/アロー/文字列/コメントが含まれること)
- 空行・コメント行・通常ノード行の各ケース
- 色は `hexFor` 経由で解決するため、ColorScheme 依存部分は引数で hex を渡す形にしてテスト可能

## 機能 2 & 3: Panel 変更

両 panel ( `RescriptVariantFlowPanel`, `RescriptDependencyDiagramPanel`) で:

```kotlin
private val sourceArea: JEditorPane =
    JEditorPane().apply {
        contentType = "text/html"
        isEditable = false
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        font = Font(Font.MONOSPACED, Font.PLAIN, 13)
        border = JBUI.Borders.empty(8)
    }

private fun refreshSource(mermaid: String) {
    sourceArea.text = MermaidSourceColorizer.render(mermaid)
    sourceArea.caretPosition = 0
}
```

Copy Mermaid アクションは元の生の `mermaid` 文字列を保持して直接 clipboard に渡す (HTML タグが入らないように)。

## ファイル変更

| ファイル | 変更 |
|---|---|
| `flow/MermaidSourceColorizer.kt` (新規) | colorizer + renderLine |
| `flow/RescriptVariantFlowPanel.kt` | JTextArea → JEditorPane (HTML) |
| `diagram/RescriptDependencyDiagramPanel.kt` | 同上 |
| `flow/MermaidSourceColorizerTest.kt` (新規) | renderLine の構造テスト |

## リスク

1. **JEditorPane の HTML 表示性能** — 大規模 Mermaid グラフ (100+ ノード) で重くなる可能性。v1 では問題なし、必要なら EditorTextField + 自作 lexer に置換可能
2. **Copy Mermaid action との分離** — 必ず生ソースを clipboard に渡し、HTML タグが混入しないように
3. **色テーマ切替時の追従** — ツールウィンドウを再オープンで反映 (acceptable for v1)
