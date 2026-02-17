# Design: P2 JetBrains ギャップ 5機能バッチ実装

全機能がローカル実装（PSI/ドキュメント操作）で独立しているため、共有インフラは不要。各 worktree で新規ファイル作成 + `plugin.xml` 登録のみ。

---

## 1. Quick Fix: Add Import

**変更なし（ゼロコード）。**

IntelliJ 2024.1+ の LSP API が `textDocument/codeAction` を自動サポート。rescript-language-server が提供するコードアクション（import 追加、型注釈追加等）は追加コード不要で Quick Fix / Intention として表示される。

**実装内容:**
- 動作確認（`runIde` で手動テスト）
- ステアリングドキュメント作成

**新規ファイル:** なし
**変更ファイル:** なし
**テスト省略理由:** LSP サーバーとの結合が必須で単体テスト困難

---

## 2. Intention Actions

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/intention/RescriptWrapWithIntention.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**

共通の基底クラスで式のラップパターンを実装し、各バリエーションをサブクラスで定義する。

```kotlin
abstract class RescriptWrapWithIntention(
    private val wrapper: String,     // e.g. "Some", "Ok", "Error"
) : PsiElementBaseIntentionAction() {
    override fun getFamilyName(): String = "ReScript"
    override fun getText(): String = "Wrap with $wrapper(...)"

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        return element.containingFile?.language == RescriptLanguage.INSTANCE
    }

    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val document = editor.document
        val selectionModel = editor.selectionModel
        if (selectionModel.hasSelection()) {
            val start = selectionModel.selectionStart
            val end = selectionModel.selectionEnd
            val selectedText = document.getText(TextRange(start, end))
            WriteCommandAction.runWriteCommandAction(project) {
                document.replaceString(start, end, "$wrapper($selectedText)")
            }
        }
    }
}

class RescriptWrapWithSomeIntention : RescriptWrapWithIntention("Some")
class RescriptWrapWithOkIntention : RescriptWrapWithIntention("Ok")
class RescriptWrapWithErrorIntention : RescriptWrapWithIntention("Error")
```

`@genType` 追加:

```kotlin
class RescriptAddGenTypeIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Add @genType annotation"
    override fun getFamilyName(): String = "ReScript"

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        // カーソルが let/type/module 宣言上にあるか確認
    }

    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        // 宣言の直前の行に @genType を挿入
    }
}
```

**plugin.xml:**
```xml
<intentionAction>
    <language>ReScript</language>
    <className>com.rescript.plugin.intention.RescriptWrapWithSomeIntention</className>
    <category>ReScript</category>
    <skipBeforeAfter>true</skipBeforeAfter>
</intentionAction>
<!-- 同様に Ok, Error, AddGenType -->
```

---

## 3. Surround With

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/surround/RescriptSurroundDescriptor.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**

```kotlin
class RescriptSurroundDescriptor : SurroundDescriptor {
    override fun getElementsToSurround(
        file: PsiFile, startOffset: Int, endOffset: Int
    ): Array<PsiElement> {
        // 選択範囲内の PSI 要素を返す
        // ReScript ファイルでなければ空配列
    }

    override fun getSurrounders(): Array<Surrounder> = arrayOf(
        RescriptIfSurrounder(),
        RescriptSwitchSurrounder(),
        RescriptTrySurrounder(),
        RescriptBlockSurrounder(),
    )

    override fun isExclusive(): Boolean = false
}
```

各 Surrounder の実装:

| Surrounder | テンプレート | カーソル位置 |
|------------|------------|------------|
| `RescriptIfSurrounder` | `if (condition) {\n  <selection>\n}` | `condition` |
| `RescriptSwitchSurrounder` | `switch expr {\n| _ => <selection>\n}` | `expr` |
| `RescriptTrySurrounder` | `try {\n  <selection>\n} catch {\n| exn => ()\n}` | `exn => ()` の `()` |
| `RescriptBlockSurrounder` | `{\n  <selection>\n}` | ブロック末尾 |

**plugin.xml:**
```xml
<lang.surroundDescriptor language="ReScript"
    implementationClass="com.rescript.plugin.surround.RescriptSurroundDescriptor"/>
```

---

## 4. Import Optimizer

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/imports/RescriptImportOptimizer.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**

```kotlin
class RescriptImportOptimizer : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean =
        file.language == RescriptLanguage.INSTANCE

    override fun processFile(file: PsiFile): ImportOptimizer.CollectingInfoRunnable {
        // READ action フェーズ: ファイルを解析
        val openStatements = findOpenStatements(file)
        val duplicates = findDuplicateOpens(openStatements)
        val toRemove = duplicates.map { it.textRange }

        return object : ImportOptimizer.CollectingInfoRunnable {
            override fun run() {
                // WRITE action フェーズ: 重複を削除（逆順でオフセット保持）
                val document = PsiDocumentManager.getInstance(file.project)
                    .getDocument(file) ?: return
                toRemove.sortedByDescending { it.startOffset }.forEach { range ->
                    // 行ごと削除（改行含む）
                    val lineNum = document.getLineNumber(range.startOffset)
                    val lineStart = document.getLineStartOffset(lineNum)
                    val lineEnd = document.getLineEndOffset(lineNum)
                    val deleteEnd = minOf(lineEnd + 1, document.textLength)
                    document.deleteString(lineStart, deleteEnd)
                }
            }

            override fun getUserNotificationInfo(): String =
                "Removed ${toRemove.size} duplicate open statement(s)"
        }
    }
}
```

**`open` 文の検出ロジック:**
- PSI ツリーから `OPEN_STATEMENT` ノードを収集
- 各ノードからモジュール名を抽出（`open` キーワードの後の識別子列）
- 同じモジュール名の重複を検出（2回目以降を削除対象に）

**plugin.xml:**
```xml
<lang.importOptimizer language="ReScript"
    implementationClass="com.rescript.plugin.imports.RescriptImportOptimizer"/>
```

---

## 5. Gutter Run Icons

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/run/RescriptRunLineMarkerContributor.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**

```kotlin
class RescriptRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        // リーフ要素のみ処理（重複回避）
        if (element.node?.elementType != RescriptTokenTypes.LET_KEYWORD) return null

        // ファイルの最初の let 宣言にのみアイコンを表示
        // （ReScript にはエントリポイントの概念がないため、ファイル単位のビルドアイコン）
        val file = element.containingFile ?: return null
        if (file.language != RescriptLanguage.INSTANCE) return null

        // プロジェクトに rescript.json が存在するか確認
        val projectDir = element.project.guessProjectDir() ?: return null
        if (projectDir.findChild("rescript.json") == null) return null

        // ファイル内最初のトップレベル宣言かチェック
        val parent = element.parent
        if (parent?.node?.elementType !in TOP_LEVEL_DECLARATIONS) return null
        val firstDeclaration = file.children.firstOrNull {
            it.node?.elementType in TOP_LEVEL_DECLARATIONS
        }
        if (parent != firstDeclaration) return null

        return withExecutorActions(AllIcons.RunConfigurations.TestState.Run)
    }
}
```

**plugin.xml:**
```xml
<runLineMarkerContributor language="ReScript"
    implementationClass="com.rescript.plugin.run.RescriptRunLineMarkerContributor"/>
```

---

## ファイル競合分析

- 共有インフラなし → LSP 関連ファイルの競合なし
- `plugin.xml` のみ競合（各機能が `<extensions>` / `<intentionAction>` に行追加、マージ時手動解決）
