# Design: Code Lens（CodeVision API による型注釈表示）

## 新規ファイル

### `src/main/kotlin/com/rescript/plugin/codevision/RescriptCodeVisionProvider.kt`

`DaemonBoundCodeVisionProvider` を実装。

```kotlin
class RescriptCodeVisionProvider : DaemonBoundCodeVisionProvider {
    override val id: String = "rescript.codeLens"
    override val name: String = "ReScript Type Annotations"
    override val defaultAnchor: CodeVisionAnchorKind = CodeVisionAnchorKind.Top
    override val relativeOrderings: List<CodeVisionRelativeOrdering> = emptyList()

    override fun computeForEditor(editor: Editor, file: PsiFile): List<Pair<TextRange, CodeVisionEntry>> {
        // 1. .resi ファイルはスキップ
        // 2. LspServerManager から ReScript LSP サーバーを取得
        // 3. textDocumentService.codeLens() で CodeLens[] を取得
        // 4. CodeLens の range → TextRange, command.title → TextCodeVisionEntry にマッピング
    }
}
```

## 変更ファイル

### `lsp/RescriptLspServerDescriptor.kt`

`createInitializationOptions()` をオーバーライドして `{"codeLens": true}` を返す。

```kotlin
override fun createInitializationOptions(): Any =
    mapOf("extensionConfiguration" to mapOf("codeLens" to true))
```

### `plugin.xml`

```xml
<codeInsight.daemonBoundCodeVisionProvider
    implementation="com.rescript.plugin.codevision.RescriptCodeVisionProvider"/>
```

## LSP リクエスト

- 標準 LSP メソッド `textDocument/codeLens`（lsp4j の `TextDocumentService.codeLens()` を使用）
- カスタムインターフェースへの追加は不要

## テスト

- `RescriptCodeVisionProviderTest` — `.resi` ファイルスキップ、プロバイダのプロパティ確認
- LSP サーバーとの結合テストは省略（単体テスト困難）
