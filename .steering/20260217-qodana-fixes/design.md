# Design: 静的解析指摘事項の修正

## 修正方針

既存の動作を維持しつつ、最小限の変更で各指摘事項を修正する。

---

## 1. CRITICAL: `RescriptTodoIndexer` コンパイルエラー

**問題:** `IdAndTodoScannerBasedOnFilterLexer` が IntelliJ Platform 2025.3 で削除された。

**修正方針:** `BaseFilterLexer` を継承したカスタムフィルタレキサーを作成し、TODO トークンスキャンを行う。

**変更ファイル:**
- `src/main/kotlin/com/rescript/plugin/indexing/RescriptTodoIndexer.kt`

**実装:**
```kotlin
class RescriptFilterLexer(
    originalLexer: Lexer,
    occurrenceConsumer: OccurrenceConsumer,
) : BaseFilterLexer(originalLexer, occurrenceConsumer) {
    override fun advance() {
        scanWordsInToken(UsageSearchContext.IN_COMMENTS, false, false)
        advanceTodoItemCountsInToken()
        myDelegate.advance()
    }
}

class RescriptTodoIndexer : LexerBasedTodoIndexer() {
    override fun createLexer(consumer: OccurrenceConsumer): Lexer =
        RescriptFilterLexer(RescriptLexer(), consumer)
}
```

---

## 2. HIGH: 演算子優先順位の明示化

**問題:** `RescriptParser.kt:416-419` で `&&` と `||` の優先順位が曖昧。

**変更ファイル:**
- `src/main/kotlin/com/rescript/plugin/lang/RescriptParser.kt`

**修正:**
```kotlin
private fun isTopLevelStart(token: IElementType?): Boolean =
    token != null &&
        (RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(token) ||
            token == RescriptTokenTypes.ARROBASE)
```

---

## 3. MEDIUM: `it.node.elementType` の安全アクセス

**問題:** `PsiElement.node` は null になり得るが、安全アクセス演算子を使っていない。

**変更ファイル:**
- `src/main/kotlin/com/rescript/plugin/structure/RescriptStructureViewElement.kt:30`
- `src/main/kotlin/com/rescript/plugin/inspection/RescriptDuplicateOpenInspection.kt:29,46,58`
- `src/main/kotlin/com/rescript/plugin/inspection/RescriptEmptyModuleInspection.kt:42,45,48,73`

**修正:** `it.node.elementType` → `it.node?.elementType` に統一。

---

## 4. LOW (パフォーマンス): ホットパスでのオブジェクト生成の定数化

### 4a. `RescriptParser.kt:74-76`
`listOf(...)` を companion object の定数に抽出。

### 4b. `RescriptFoldingBuilder.kt:30-34, 46-49`
`setOf(...)` を companion object の定数に抽出。`PsiTreeUtil.findChildrenOfAnyType(root, PsiElement::class.java)` はそのまま維持（再帰ビジター化は影響範囲が大きいため）。

### 4c. `RescriptRunConfiguration.kt:79`
`"\\s+".toRegex()` を companion object の定数に抽出。

---

## 5. LOW (エラーハンドリング): 例外処理の改善

### 5a. `RescriptLspServerDescriptor.kt:110-120`
- `runCatching` → `try-catch` に変更し、`InterruptedException` を再スロー
- `inputStream` を `.use {}` でクローズ

### 5b. `RescriptFormattingService.kt:86-88`
- `proc.waitFor()` → `proc.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS)` に変更
- タイムアウト時は `proc.destroyForcibly()`

### 5c. `RescriptRenameHandler.kt:102`
- `catch (e: Exception)` → `catch (e: Exception)` のまま、ただし `CancellationException` を再スロー

---

## 6. LOW (コードスタイル): FQN を import に統一

**変更ファイル:**
- `RescriptDuplicateOpenInspection.kt` — `com.intellij.psi.PsiFile`, `com.intellij.psi.PsiElement` を import に
- `RescriptEmptyModuleInspection.kt` — 同上
- `RescriptMissingConfigInspection.kt` — 同上
- `RescriptRenameHandler.kt` — `org.eclipse.lsp4j.TextDocumentIdentifier`, `com.intellij.openapi.editor.Document` を import に
- `RescriptRunConfiguration.kt` — `com.intellij.execution.process.ProcessHandler`, `com.intellij.execution.ExecutionException` を import に

---

## 7. LOW (コードスタイル): `RESCRIPT_EXTENSIONS` 定数の共通化

**方針:** `RescriptLspServerSupportProvider` の companion object 定数を `internal` に公開し、他の箇所から参照する。

**変更ファイル:**
- `src/main/kotlin/com/rescript/plugin/lsp/RescriptLspServerSupportProvider.kt` — `RESCRIPT_EXTENSIONS` を `internal` に変更
- `src/main/kotlin/com/rescript/plugin/lsp/RescriptLspServerDescriptor.kt` — `RescriptLspServerSupportProvider.RESCRIPT_EXTENSIONS` を参照
- `src/main/kotlin/com/rescript/plugin/refactor/RescriptRenameHandler.kt` — 同上

---

## 8. LOW (冗長コード): 冗長なオーバーライドの削除

- `RescriptLanguage.kt:6` — `getDisplayName()` を削除（`Language("ReScript")` で自動設定）
- `RescriptFileTypes.kt:19` — `RescriptInterfaceFileType.getDisplayName()` を削除（`getName()` と同値）

---

## 9. LOW (ベストプラクティス): `RescriptFile.getFileType()` の修正

**変更ファイル:**
- `src/main/kotlin/com/rescript/plugin/lang/psi/RescriptPsi.kt:43`

**修正:** `RescriptFileType` → `viewProvider.fileType` に変更。

---

## 10. LOW (ベストプラクティス): `VirtualFileManager.findFileByUrl` の修正

**変更ファイル:**
- `src/main/kotlin/com/rescript/plugin/inspection/RescriptMissingConfigInspection.kt:20`

**修正:** `VirtualFileManager.getInstance().findFileByUrl("file://$basePath")` → `LocalFileSystem.getInstance().findFileByPath(basePath)` に変更。

---

## 11. LOW (ベストプラクティス): `saveAllDocuments()` の個別化

**変更ファイル:**
- `src/main/kotlin/com/rescript/plugin/refactor/RescriptRenameHandler.kt:240`

**修正:** 変更したドキュメントを追跡し、個別に `saveDocument()` を呼び出す。

---

## 影響範囲

- レクサー・パーサーの動作変更なし
- LSP 統合の動作変更なし
- ビルドが成功するようになる（CRITICAL 修正）
- 既存テストへの影響なし
