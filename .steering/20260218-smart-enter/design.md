# Design: Smart Enter

## API
- `com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor`
- Extension point: `<lang.smartEnterProcessor>` in plugin.xml

## 実装
- `process()` で caret 行をレキサーで解析
- 優先順位: switch ブレース → パイプ `=>` → 未閉じ括弧 → デフォルト
- `RescriptLexer` でトークン化し、括弧バランスを計算

## ファイル
- 新規: `src/main/kotlin/com/rescript/plugin/editor/RescriptSmartEnterProcessor.kt`
- 新規: `src/test/kotlin/com/rescript/plugin/editor/RescriptSmartEnterProcessorTest.kt`
- 変更: `plugin.xml`
