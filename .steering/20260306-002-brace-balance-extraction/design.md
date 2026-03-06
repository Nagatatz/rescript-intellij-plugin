# Design: Brace Balance Utility Extraction

## 新規ファイル

### `src/main/kotlin/com/rescript/plugin/util/RescriptBraceBalanceUtil.kt`

```kotlin
object RescriptBraceBalanceUtil {
    // ── テキストベース ──────────────────────────────────

    /** 指定位置の開き括弧に対応する閉じ括弧の位置を返す */
    fun findMatchingBracket(text: String, openIndex: Int, openChar: Char, closeChar: Char): Int?

    /** findMatchingBracket の '(' / ')' 版 */
    fun findMatchingParen(text: String, openIndex: Int): Int?

    /** findMatchingBracket の '{' / '}' 版 */
    fun findMatchingBrace(text: String, openIndex: Int): Int?

    // ── PSI ベース ──────────────────────────────────────

    /** ホワイトスペース要素をスキップして次の意味ある兄弟要素を返す */
    fun skipWhitespace(element: PsiElement?): PsiElement?

    /** ホワイトスペース要素をスキップして前の意味ある兄弟要素を返す */
    fun skipWhitespaceBackward(element: PsiElement?): PsiElement?
}
```

## 変更ファイル

| ファイル | 変更内容 |
|---------|---------|
| `RescriptUnwrapDescriptor.kt` | `findMatchingBracket`/`findMatchingBrace`/`findMatchingParen` を `RescriptBraceBalanceUtil` への委譲に置換 |
| `RescriptInsertLabeledArgsIntention.kt` | ローカル `findMatchingParen` を `RescriptBraceBalanceUtil.findMatchingParen` に置換 |
| `RescriptConvertToLabeledArgsIntention.kt` | ローカル `findMatchingParen` を `RescriptBraceBalanceUtil.findMatchingParen` に置換 |
| `RescriptHighlightUsagesHandlerFactory.kt` | companion object の `skipWhitespace`/`skipWhitespaceBackward` を `RescriptBraceBalanceUtil` への委譲に置換 |

## 対象外

- `RescriptWordSelectionHandler.kt` の PSI 版 `findMatchingBracket` — PsiElement ベースで API が異なるため今回は対象外
- `RescriptParser.kt` の `braceDepth` — PsiBuilder API 使用のため別物
- `RescriptHighlightUsagesHandlerFactory.kt` の6つの `collect*Related` メソッド — 各メソッド固有のロジックが多く、共通化のメリットが少ない

## テスト

### `src/test/kotlin/com/rescript/plugin/util/RescriptBraceBalanceUtilTest.kt`

- `findMatchingBracket`: 正常系（ネスト括弧含む）、不一致、範囲外
- `findMatchingParen`: 基本、ネスト
- `findMatchingBrace`: 基本、ネスト
- `skipWhitespace` / `skipWhitespaceBackward`: PSI 依存のため IDE テスト環境が必要。テスト省略理由: PSI 要素のモックが必要で、既存の `RescriptHighlightUsagesHandlerFactory` が IDE テストを持たない。代わりにテキストベースメソッドのテストに注力する。
