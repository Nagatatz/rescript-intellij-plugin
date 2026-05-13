# 設計: Switch Flow 空状態ヒント

## 1. 空状態の理由を分類

`refresh()` で diagram が描画できない 3 つのケース:

- **NoEditor** — `currentEditorContext()` が null（ReScript ファイルどころか、開いているエディタがない）
- **NotResScript** — エディタは開いているが、ファイルタイプが ReScript でない
- **NoSwitch** — ReScript ファイルが開いているがキャレット周辺に `switch` 式がない

それぞれに対して使い方ヒントを差し替える。

## 2. 新規 pure helper `RescriptVariantFlowHints`

```kotlin
internal object RescriptVariantFlowHints {
    internal enum class Reason {
        NO_EDITOR,
        NOT_RESCRIPT,
        NO_SWITCH,
    }

    fun emptyStateMessage(reason: Reason): String = when (reason) {
        Reason.NO_EDITOR -> """
            No ReScript file is currently open.

            How to use:
              1. Open a `.res` file.
              2. Place the caret on a `switch` expression.
              3. The decision tree appears here automatically.

            The toolbar also offers `Copy Mermaid` / `Copy DOT` exports.
        """.trimIndent()
        Reason.NOT_RESCRIPT -> """
            The active editor is not a ReScript file.

            Switch flow diagrams are rendered for `.res` and `.resi` files.
            Switch to a ReScript file and place the caret on a `switch`
            expression to see its decision tree.
        """.trimIndent()
        Reason.NO_SWITCH -> """
            No `switch` expression at the caret.

            Move the caret onto (or inside) a `switch` keyword and the
            decision tree will refresh within ~200 ms. Nested switches up
            to depth 3 are expanded inline.
        """.trimIndent()
    }

    fun shortStatusLabel(reason: Reason): String = when (reason) {
        Reason.NO_EDITOR -> "Open a ReScript file to see its switch flow."
        Reason.NOT_RESCRIPT -> "Switch to a ReScript file."
        Reason.NO_SWITCH -> "No switch under caret."
    }
}
```

ヒント本文と short status を分け、テキストエリアに本文、ステータスバーに short を出す。両方に同じ Reason を渡すので食い違わない。

## 3. `RescriptVariantFlowPanel.refresh()` の改修

```kotlin
private fun refresh() {
    val ctx = currentEditorContext()
    if (ctx == null) {
        renderEmpty(Reason.NO_EDITOR)
        return
    }
    if (ctx.file.fileType != RescriptFileType && ctx.file.fileType != RescriptInterfaceFileType) {
        renderEmpty(Reason.NOT_RESCRIPT)
        return
    }
    val diagram =
        ApplicationManager.getApplication().runReadAction<FlowDiagram?> {
            RescriptVariantFlowModel.buildAtOffset(ctx.source, ctx.offset)
        }
    currentDiagram = diagram
    if (diagram == null) {
        renderEmpty(Reason.NO_SWITCH)
    } else {
        currentJumpTarget = JumpTarget(ctx.file, ctx.offset)
        textArea.text = RescriptVariantFlowMermaidExporter.toMermaid(diagram)
        textArea.caretPosition = 0
        statusLabel.text = " Arms: ${countArms(diagram)}"
    }
}

private fun renderEmpty(reason: Reason) {
    textArea.text = RescriptVariantFlowHints.emptyStateMessage(reason)
    statusLabel.text = " ${RescriptVariantFlowHints.shortStatusLabel(reason)}"
    currentDiagram = null
    currentJumpTarget = null
}
```

旧 `renderEmpty(message: String)` は削除（代わりに enum 駆動の新版）。

## 4. fixture コメントの追記

`manual-test-projects/main/src/VariantUsage.res` の先頭に「これは Rename Variant Constructor intention のクロスファイル fixture であって、`Variant Usage` という機能ではない」旨を 2 行追記。

```rescript
// Cross-file occurrences for the Rename Variant Constructor intention.
// This is a fixture — there is no plugin feature literally named
// "Variant Usage". For variant decision trees, use the
// `ReScript Switch Flow` tool window.
// ...
```

## 5. テスト

新規 `RescriptVariantFlowHintsTest.kt`:

- 各 Reason について `emptyStateMessage` が非空かつ「How to use」または「Switch to」または「Move the caret」を含む
- 各 Reason について `shortStatusLabel` が `" "` 1 文字以上を返す
- NO_EDITOR メッセージは Copy Mermaid / Copy DOT に言及している（toolbar の機能を案内するため）

Panel 本体は Swing UI 免除のためテスト追加なし。

## 後方互換性

- public API シグネチャ無変更
- plugin.xml 変更なし
- sphinx-docs 変更なし（既存の説明で十分）
