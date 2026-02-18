# Design: P3 rescript-vscode ギャップ 5機能バッチ実装

全機能がローカル実装で独立しているため、共有インフラは不要。各 worktree で新規ファイル作成 + 既存ファイル変更のみ。

---

## 1. reanalyze 統合

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/analysis/RescriptReanalyzeAnnotator.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**

`ExternalAnnotator<CollectedInfo, AnnotationResult>` の3フェーズモデルを使用:

```kotlin
class RescriptReanalyzeAnnotator : ExternalAnnotator<RescriptReanalyzeAnnotator.CollectedInfo, RescriptReanalyzeAnnotator.AnnotationResult>() {

    data class CollectedInfo(
        val filePath: String,
        val projectBasePath: String,
    )

    data class ReanalyzeDiagnostic(
        val file: String,
        val range: IntArray, // [startLine, startChar, endLine, endChar]
        val message: String,
        val kind: String,   // "warning", "error"
    )

    data class AnnotationResult(
        val diagnostics: List<ReanalyzeDiagnostic>,
    )

    // フェーズ 1: 情報収集（Read Action）
    override fun collectInformation(file: PsiFile): CollectedInfo? {
        if (file !is RescriptFile) return null
        val vFile = file.virtualFile ?: return null
        val basePath = file.project.basePath ?: return null
        return CollectedInfo(vFile.path, basePath)
    }

    // フェーズ 2: 外部ツール実行（バックグラウンドスレッド）
    override fun doAnnotate(info: CollectedInfo): AnnotationResult? {
        val toolPath = findReanalyzeTool(info.projectBasePath) ?: return null
        val commandLine = GeneralCommandLine(toolPath, "reanalyze", "-json")
            .withWorkDirectory(info.projectBasePath)
        // プロセス実行、stdout をJSONパース
        // 当該ファイルに関連する diagnostics のみフィルタ
        return AnnotationResult(diagnostics)
    }

    // フェーズ 3: アノテーション適用（Read Action）
    override fun apply(file: PsiFile, result: AnnotationResult, holder: AnnotationHolder) {
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return
        for (diag in result.diagnostics) {
            val startOffset = document.getLineStartOffset(diag.range[0]) + diag.range[1]
            val endOffset = document.getLineStartOffset(diag.range[2]) + diag.range[3]
            holder.newAnnotation(HighlightSeverity.WARNING, diag.message)
                .range(TextRange(startOffset, endOffset))
                .create()
        }
    }
}
```

**rescript-tools.exe の検出:**
- `node_modules/rescript/rescript-tools.exe` を検索
- 親ディレクトリを遡って monorepo 対応
- 見つからない場合はサイレントに null を返す

**JSON 出力フォーマット（rescript-vscode 準拠）:**
```json
[
  {
    "name": "unusedVariable",
    "kind": "warning",
    "file": "/path/to/file.res",
    "range": [10, 4, 10, 20],
    "message": "unused variable x"
  }
]
```

**plugin.xml:**
```xml
<externalAnnotator language="ReScript"
    implementationClass="com.rescript.plugin.analysis.RescriptReanalyzeAnnotator"/>
```

**テスト:** ツール検出ロジックのテスト + JSON パーステスト

---

## 2. Markdown ReScript ハイライト

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/injection/RescriptMarkdownCodeFenceProvider.kt`
- `src/main/resources/META-INF/rescript-markdown.xml`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`（optional dependency 追加）
- `build.gradle.kts`（bundledPlugin 追加）

**設計:**

```kotlin
class RescriptMarkdownCodeFenceProvider : CodeFenceLanguageProvider {
    override fun getLanguageByInfoString(infoString: String): Language? {
        val trimmed = infoString.trim().lowercase()
        return if (trimmed in RESCRIPT_IDENTIFIERS) {
            RescriptLanguage.INSTANCE
        } else null
    }

    override fun getCompletionVariantsForInfoString(
        completionParameters: CompletionParameters
    ): List<LookupElement> =
        listOf(LookupElementBuilder.create("rescript"))

    companion object {
        private val RESCRIPT_IDENTIFIERS = setOf("rescript", "res", "resi")
    }
}
```

**rescript-markdown.xml:**
```xml
<idea-plugin>
    <extensions defaultExtensionNs="org.intellij.plugins.markdown">
        <fenceLanguageProvider
                implementation="com.rescript.plugin.injection.RescriptMarkdownCodeFenceProvider"/>
    </extensions>
</idea-plugin>
```

**plugin.xml に追加:**
```xml
<depends optional="true" config-file="rescript-markdown.xml">org.intellij.plugins.markdown</depends>
```

**build.gradle.kts に追加:**
```kotlin
bundledPlugin("org.intellij.plugins.markdown")
```

**テスト:** `getLanguageByInfoString()` のマッピングテスト

---

## 3. Paste as JSON.t

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/paste/RescriptPasteAsJsonAction.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**

```kotlin
class RescriptPasteAsJsonAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return

        val clipboardText = CopyPasteManager.getInstance()
            .getContents<String>(DataFlavor.stringFlavor)
            ?: return

        val jsonText = clipboardText.trim()
        if (!isLikelyJson(jsonText)) {
            showNotification(project, "Clipboard does not contain valid JSON")
            return
        }

        val rescriptCode = try {
            convertJsonToRescript(JsonParser.parseString(jsonText))
        } catch (ex: Exception) {
            showNotification(project, "Failed to parse JSON: ${ex.message}")
            return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            val offset = editor.caretModel.offset
            editor.document.insertString(offset, rescriptCode)
            editor.caretModel.moveToOffset(offset + rescriptCode.length)
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabledAndVisible = file is RescriptFile
    }

    companion object {
        fun isLikelyJson(text: String): Boolean {
            val trimmed = text.trimStart()
            return trimmed.startsWith("{") || trimmed.startsWith("[")
        }

        fun convertJsonToRescript(element: JsonElement): String {
            return when {
                element.isJsonNull -> "JSON.Null"
                element.isJsonPrimitive -> {
                    val prim = element.asJsonPrimitive
                    when {
                        prim.isBoolean -> "JSON.Boolean(${prim.asBoolean})"
                        prim.isNumber -> {
                            val num = prim.asDouble
                            val formatted = if (num == num.toLong().toDouble()) {
                                "${num.toLong()}."  // ReScript float notation
                            } else {
                                num.toString()
                            }
                            "JSON.Number($formatted)"
                        }
                        prim.isString -> "JSON.String(\"${escapeString(prim.asString)}\")"
                        else -> "JSON.Null"
                    }
                }
                element.isJsonArray -> {
                    val items = element.asJsonArray.joinToString(", ") { convertJsonToRescript(it) }
                    "JSON.Array([$items])"
                }
                element.isJsonObject -> {
                    val entries = element.asJsonObject.entrySet().joinToString(", ") { (key, value) ->
                        "\"$key\": ${convertJsonToRescript(value)}"
                    }
                    "JSON.Object(dict{$entries})"
                }
                else -> "JSON.Null"
            }
        }

        private fun escapeString(s: String): String =
            s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t")
    }
}
```

**備考:** `com.google.gson` は IntelliJ Platform にバンドルされているため、追加依存なしで使用可能。

**plugin.xml:**
```xml
<action id="ReScript.PasteAsJsonT"
        class="com.rescript.plugin.paste.RescriptPasteAsJsonAction"
        text="Paste as JSON.t"
        description="Convert clipboard JSON to ReScript JSON.t">
    <add-to-group group-id="EditorPopupMenu" anchor="last"/>
    <add-to-group group-id="EditMenu" anchor="after" relative-to-action="EditorPaste"/>
</action>
```

**テスト:** JSON → ReScript 変換ロジックのテスト（各 JSON 型、ネスト、エスケープ）

---

## 4. `//#region` 折りたたみ

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/folding/RescriptCustomFoldingProvider.kt`

**変更ファイル:**
- `src/main/kotlin/com/rescript/plugin/folding/RescriptFoldingBuilder.kt`（`FoldingBuilderEx` → `CustomFoldingBuilder` に変更）
- `src/main/resources/META-INF/plugin.xml`
- `src/test/kotlin/com/rescript/plugin/folding/RescriptFoldingBuilderTest.kt`（メソッド名変更に対応）

**設計:**

### RescriptCustomFoldingProvider

```kotlin
class RescriptCustomFoldingProvider : CustomFoldingProvider() {
    override fun isCustomRegionStart(elementText: String): Boolean {
        val trimmed = elementText.trimStart()
        return trimmed.startsWith("//#region") || trimmed.startsWith("// #region")
    }

    override fun isCustomRegionEnd(elementText: String): Boolean {
        val trimmed = elementText.trimStart()
        return trimmed.startsWith("//#endregion") || trimmed.startsWith("// #endregion")
    }

    override fun getPlaceholderText(elementText: String): String {
        val trimmed = elementText.trimStart()
        val name = trimmed
            .removePrefix("//#region").removePrefix("// #region")
            .trim()
        return name.ifEmpty { "..." }
    }

    override fun getDescription(): String = "//#region ... //#endregion"
    override fun getStartString(): String = "//#region"
    override fun getEndString(): String = "//#endregion"
}
```

### RescriptFoldingBuilder の変更

`FoldingBuilderEx` を `CustomFoldingBuilder` に変更:

```kotlin
class RescriptFoldingBuilder : CustomFoldingBuilder() {
    // buildFoldRegions() → buildLanguageFoldRegions()
    override fun buildLanguageFoldRegions(
        descriptors: MutableList<FoldingDescriptor>,
        root: PsiElement,
        document: Document,
        quick: Boolean,
    ) {
        // 既存ロジックをそのまま移行（descriptors に追加）
    }

    // getPlaceholderText() → getLanguagePlaceholderText()
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? =
        when (node.elementType) { ... }

    // isCollapsedByDefault() → isRegionCollapsedByDefault()
    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean = false

    // CustomFoldingBuilder に必要
    override fun isCustomFoldingCandidate(node: ASTNode): Boolean =
        node.elementType == RescriptTokenTypes.SINGLE_COMMENT
}
```

**plugin.xml:**
```xml
<customFoldingProvider
    implementation="com.rescript.plugin.folding.RescriptCustomFoldingProvider"/>
```

**テスト:** 既存テストのメソッド名変更 + region 折りたたみのテスト追加

---

## 5. Incremental Type Checking 設定

**新規ファイル:** なし

**変更ファイル:**
- `src/main/kotlin/com/rescript/plugin/settings/RescriptProjectSettings.kt`
- `src/main/kotlin/com/rescript/plugin/settings/RescriptConfigurable.kt`
- `src/main/kotlin/com/rescript/plugin/lsp/RescriptLspServerDescriptor.kt`

**設計:**

### RescriptProjectSettings.kt の変更

```kotlin
class State {
    var lspServerPath: String = ""
    var nodePath: String = ""
    var incrementalTypecheckingEnabled: Boolean = true  // NEW
}

// プロパティ追加
var incrementalTypecheckingEnabled: Boolean
    get() = state.incrementalTypecheckingEnabled
    set(value) { state.incrementalTypecheckingEnabled = value }
```

### RescriptConfigurable.kt の変更

```kotlin
private var incrementalTypecheckingCheckbox: JCheckBox? = null

// createComponent() に追加:
val incrementalCheckbox = JCheckBox("Enable incremental type checking", true)
incrementalTypecheckingCheckbox = incrementalCheckbox

// FormBuilder に追加:
.addSeparator()
.addComponent(incrementalCheckbox)
.addTooltip("When enabled, the LSP server uses incremental type checking for faster feedback. Requires LSP server restart.")

// isModified() に追加:
|| incrementalTypecheckingCheckbox?.isSelected != settings.incrementalTypecheckingEnabled

// apply() に追加:
settings.incrementalTypecheckingEnabled = incrementalTypecheckingCheckbox?.isSelected ?: true
// 設定変更時に LSP サーバーを再起動
LspServerManager.getInstance(project)
    .stopAndRestartIfNeeded(RescriptLspServerSupportProvider::class.java)

// reset() に追加:
incrementalTypecheckingCheckbox?.isSelected = settings.incrementalTypecheckingEnabled

// disposeUIResources() に追加:
incrementalTypecheckingCheckbox = null
```

### RescriptLspServerDescriptor.kt の変更

```kotlin
override fun createInitializationOptions(): Any {
    val settings = RescriptProjectSettings.getInstance(project)
    return mapOf(
        "extensionConfiguration" to mapOf(
            "codeLens" to true,
            "incrementalTypechecking" to mapOf(
                "enabled" to settings.incrementalTypecheckingEnabled,
            ),
        ),
    )
}
```

**テスト:** 設定値の読み書きテスト + 初期化オプション生成テスト

---

## ファイル競合分析

- 共有インフラなし → LSP 関連ファイルの競合なし
- `plugin.xml` のみ競合（各機能が行追加、マージ時手動解決）
- `build.gradle.kts` は Markdown ハイライト機能のみが変更
- `//#region` 折りたたみは `RescriptFoldingBuilder.kt` を変更するが、他の機能はこのファイルを変更しないため競合なし
- Incremental TC 設定は `RescriptProjectSettings.kt`, `RescriptConfigurable.kt`, `RescriptLspServerDescriptor.kt` を変更するが、他の機能はこれらを変更しないため競合なし
