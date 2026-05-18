# ReScript Syntax-Based Coloring — 設計

## 共通基盤

`RescriptSyntaxHighlighter` の `ATTR_MAP` (`src/main/kotlin/com/rescript/plugin/highlight/RescriptSyntaxHighlighter.kt`) は以下を返す:

- `getHighlightingLexer(): Lexer = RescriptLexer()`
- `getTokenHighlights(IElementType): Array<TextAttributesKey>` — 各トークン種別に対し 0 個または 1 個の TextAttributesKey

色解決経路: `EditorColorsManager.getInstance().globalScheme.getAttributes(key): TextAttributes` → `SimpleTextAttributes.fromTextAttributes(ta)` で `SimpleTextAttributes` に変換。

## 機能 1: Hoogle 検索結果

### 新規ヘルパー `navigation/RescriptSignatureTokenColorizer.kt`

```kotlin
internal object RescriptSignatureTokenColorizer {
    data class Token(val text: String, val attributes: SimpleTextAttributes)

    /**
     * Tokenises a type signature via RescriptLexer and resolves each
     * token to a SimpleTextAttributes by consulting the global colour
     * scheme through RescriptSyntaxHighlighter.
     *
     * Falls back to REGULAR_ATTRIBUTES when:
     *   - the highlighter returns no key for the token type
     *   - the scheme returns null TextAttributes for the resolved key
     */
    fun tokenize(signature: String): List<Token> {
        val lexer = RescriptLexer().apply { start(signature) }
        val highlighter = RescriptSyntaxHighlighter()
        val scheme = EditorColorsManager.getInstance().globalScheme
        val result = mutableListOf<Token>()
        while (lexer.tokenType != null) {
            val tokenType = lexer.tokenType!!
            val text = signature.substring(lexer.tokenStart, lexer.tokenEnd)
            val keys = highlighter.getTokenHighlights(tokenType)
            val attrs = resolveAttributes(keys, scheme)
            result.add(Token(text, attrs))
            lexer.advance()
        }
        return result
    }

    private fun resolveAttributes(
        keys: Array<TextAttributesKey>,
        scheme: EditorColorsScheme,
    ): SimpleTextAttributes {
        if (keys.isEmpty()) return SimpleTextAttributes.REGULAR_ATTRIBUTES
        val resolved = scheme.getAttributes(keys.first())
            ?: return SimpleTextAttributes.REGULAR_ATTRIBUTES
        return SimpleTextAttributes.fromTextAttributes(resolved)
    }
}
```

### Renderer 変更 `RescriptTypeSignatureCellRenderer.kt`

```kotlin
override fun customizeCellRenderer(...) {
    if (value == null) return
    append("${value.name}: ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
    for (token in RescriptSignatureTokenColorizer.tokenize(value.signatureDisplay)) {
        append(token.text, token.attributes)
    }
    append("  (${value.relativePath}:${value.line})", SimpleTextAttributes.GRAYED_ATTRIBUTES)
}
```

### テスト

新規 `navigation/RescriptSignatureTokenColorizerTest.kt`:
- "string => int" は 3 トークンに分かれる (LIDENT, OPERATOR =>, LIDENT) — 各トークンが non-null attrs を返す
- "'a => 'a" は TYPE_ARGUMENT トークンを含み、その attrs は REGULAR と異なる
- 空文字列はトークン 0 を返す (defensive)
- LIDENT トークンは highlighter 未登録なので REGULAR_ATTRIBUTES に fallback

## 機能 2: Type Info panel

`JBLabel` から `EditorTextField` (viewer モード) に置換:

```kotlin
private val typeField: EditorTextField =
    EditorTextField(
        EditorFactory.getInstance().createDocument(NO_RESCRIPT_FILE),
        project,
        RescriptFileType,
        true,  // isViewer
        true,  // oneLineMode
    ).apply {
        addSettingsProvider { editor ->
            editor.settings.isLineNumbersShown = false
            editor.settings.isFoldingOutlineShown = false
            editor.settings.isRightMarginShown = false
            editor.settings.isCaretRowShown = false
            editor.setBorder(JBUI.Borders.empty(8))
        }
    }
```

`showMessage(text: String)` は `ApplicationManager.getApplication().invokeLater { typeField.text = text }` に変更。

REPL output (`RescriptReplPanel`) の `EditorEx` 構築パターンを参考にするが、こちらは `EditorTextField` のシンプルな viewer モード。

### テスト

`RescriptTypeInfoPanel` 自体は UI 免除。`showMessage` のテキスト変更ロジックは fixture テストで検証 (既存テストがあれば拡張、なければ skip)。

## 機能 3: PPX View panel

`JTextArea` のまま維持し、`infoArea.highlighter.addHighlight()` で `@annotation` 部分にカラー Range を追加する:

```kotlin
private fun updatePpxInfo(sourceText: String) {
    val annotations = findPpxAnnotations(sourceText)
    if (annotations.isEmpty()) {
        infoArea.text = "No PPX annotations found in this file."
        return
    }

    val info = buildString { /* same as before */ }
    infoArea.text = info

    // Highlight every "@xxx" run in the rendered text using the
    // ANNOTATION TextAttributesKey from RescriptSyntaxHighlighter.
    highlightAnnotations()
}

private fun highlightAnnotations() {
    val scheme = EditorColorsManager.getInstance().globalScheme
    val ta = scheme.getAttributes(RescriptSyntaxHighlighter.ANNOTATION) ?: return
    val color = ta.foregroundColor ?: return
    val painter = DefaultHighlighter.DefaultHighlightPainter(null)
    // Actually, we need a foreground colour painter, not background.
    // Use a custom HighlightPainter that overrides paint() to render
    // the underlying text in `color`. Alternative simpler approach:
    // Render via JEditorPane with HTML <span style="color:..."> wrapping.
}
```

実装方針確定: JTextArea ではフォアグラウンド色の Range 上書きが煩雑なので **JEditorPane (HTML) に切替** が現実的:

```kotlin
private val infoArea = JEditorPane().apply {
    contentType = "text/html"
    isEditable = false
    putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
    font = Font(Font.MONOSPACED, Font.PLAIN, 13)
    border = JBUI.Borders.empty(8)
}

private fun updatePpxInfo(sourceText: String) {
    val annotations = findPpxAnnotations(sourceText)
    if (annotations.isEmpty()) {
        infoArea.text = "<html><body style='font-family:monospace'>No PPX annotations found in this file.</body></html>"
        return
    }
    val color = annotationColorHex()
    val html = buildString {
        append("<html><body style='font-family:monospace;white-space:pre'>")
        for ((annotation, line) in annotations) {
            val expansion = getPpxExpansionInfo(annotation)
            val escaped = escapeHtml(annotation)
            val coloredAnnotation = "<span style='color:$color;font-weight:bold'>$escaped</span>"
            append("Line $line: ").append(coloredAnnotation).append("<br>")
            append("&nbsp;&nbsp;→ ").append(escapeHtml(expansion)).append("<br><br>")
        }
        append("</body></html>")
    }
    infoArea.text = html
}

private fun annotationColorHex(): String {
    val ta = EditorColorsManager.getInstance().globalScheme.getAttributes(RescriptSyntaxHighlighter.ANNOTATION)
    val c = ta?.foregroundColor ?: return "#888888"
    return String.format("#%02X%02X%02X", c.red, c.green, c.blue)
}
```

### テスト

`findPpxAnnotations` / `getPpxExpansionInfo` の既存テストは維持。HTML 生成ロジックは `internal fun renderHtml(annotations, color): String` を抽出してユニットテスト (アノテーション数 0 / 1 / 複数で正しい HTML が生成されることを構造的に検証)。

## 機能 4: Notebook cell 入力

`JTextArea` を `EditorTextField` に置換 (REPL input のパターン):

```kotlin
private val codeArea: EditorTextField =
    EditorTextField(
        EditorFactory.getInstance().createDocument(initialCell.code),
        project,
        RescriptFileType,
        false,  // isViewer
        false,  // oneLineMode
    ).apply {
        setPreferredSize(Dimension(0, 4 * 18))  // ~4 rows of editor font
        addSettingsProvider { editor ->
            editor.settings.isLineNumbersShown = false
            editor.settings.isFoldingOutlineShown = false
            editor.settings.isRightMarginShown = false
        }
        addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                onChanged()
            }
        })
    }
```

注意: `RescriptNotebookCellPanel` のコンストラクタは現在 `Project` を受け取っていない。`projectPath: String` のみ。`Project` を追加するか、`RescriptNotebookPanel` 側で持つ `Project` を渡せるようにする必要がある。

確認: `RescriptNotebookPanel` の `RescriptNotebookCellPanel` 生成箇所を Read してから `Project` 引数を追加する。

### テスト

`RescriptNotebookCellPanel` 自体は UI 免除。`toCell()` ロジックは既存テストがあれば維持、なければ追加。

## ファイル変更まとめ

| ファイル | 変更 |
|---|---|
| `navigation/RescriptSignatureTokenColorizer.kt` (新規) | トークン化 + 属性解決ヘルパー |
| `navigation/RescriptTypeSignatureCellRenderer.kt` | tokenize ヘルパー呼出に変更 |
| `typeinfo/RescriptTypeInfoPanel.kt` | JBLabel → EditorTextField (viewer) |
| `ppx/RescriptPpxViewPanel.kt` | JTextArea → JEditorPane (HTML 着色) |
| `notebook/RescriptNotebookCellPanel.kt` | JTextArea → EditorTextField |
| `notebook/RescriptNotebookPanel.kt` | Project 引数を CellPanel に渡す調整 (必要なら) |

## リスク

1. **PPX View HTML 描画パフォーマンス** — JEditorPane の HTML は重い。アノテーション数 < 50 なら問題なし、それ以上で重ければ Highlighter 経由に戻す
2. **EditorTextField のメモリ消費** — Notebook で大量のセルがあると EditorTextField が重くなる可能性。1 セルあたりの追加コストは数 KB なので 100 セル程度までは問題なし
3. **`EditorColorsManager.globalScheme` の Light/Dark 切替時の再描画** — テーマ変更時にトークン色を更新する仕組みが必要。`EditorColorsManager.addEditorColorsListener` でリスナを張る ... ただし v1 では panel 再オープンで反映するに留める
4. **`RescriptLexer` の thread safety** — `start()` / `advance()` は 1 インスタンスで sequential のみ。Renderer から呼ぶ際は毎回 new するか、ThreadLocal で持つ
5. **`SimpleTextAttributes.fromTextAttributes` の null fallback** — `TextAttributes` の foreground が null だと結果も null になる可能性 → 防御的に `REGULAR_ATTRIBUTES` にフォールバック
