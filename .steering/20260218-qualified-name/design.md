# Design: Qualified Name Copy

## API
- `com.intellij.ide.actions.QualifiedNameProvider`
- Extension point: `<qualifiedNameProvider>` in plugin.xml

## 実装
- `getQualifiedName(element)`: ファイル名.モジュールパス.要素名 を生成
- `qualifiedNameToElement()`: null（LSP が担当）
- `adjustElementToCopy()`: NAVIGABLE_TYPES の宣言ノードに調整

## ファイル
- 新規: `src/main/kotlin/com/rescript/plugin/navigation/RescriptQualifiedNameProvider.kt`
- 新規: `src/test/kotlin/com/rescript/plugin/navigation/RescriptQualifiedNameProviderTest.kt`
- 変更: `plugin.xml`
