# Design: Statement Up/Down Mover

## API
- `com.intellij.codeInsight.editorActions.moveUpDown.StatementUpDownMover`
- Extension point: `<statementUpDownMover>` in plugin.xml

## 実装
- `checkAvailable()` で caret 位置の PSI 要素からトップレベル宣言を特定
- 先行する ANNOTATION ノードも宣言に含める
- 隣接する宣言を探し、`info.toMove` / `info.toMove2` に行範囲を設定

## ファイル
- 新規: `src/main/kotlin/com/rescript/plugin/editor/RescriptStatementUpDownMover.kt`
- 新規: `src/test/kotlin/com/rescript/plugin/editor/RescriptStatementUpDownMoverTest.kt`
- 変更: `plugin.xml`
