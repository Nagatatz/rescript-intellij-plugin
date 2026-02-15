# Design: テストカバレッジ拡充（Phase 2）

## 実装アプローチ

### 方針

- 全テストは JUnit 4 単体テストで完結させる（IntelliJ Platform ランタイム不要）
- プロダクションコードのパッケージ構造に合わせてテストクラスを配置する
- 既存テストのヘルパーメソッド（`tokenize`, `tokenizeNoWs` 等）パターンを踏襲する
- `RescriptFoldingBuilder` は `getPlaceholderText` / `isCollapsedByDefault` に ASTNode が必要だが、`isCollapsedByDefault` は引数を使わず常に `false` を返すため `null` で呼び出せないか確認し、不可能な場合はモックを使用する

## ファイル構成

### 新規作成するファイル

| ファイル | パッケージ | 内容 |
|---------|-----------|------|
| `src/test/.../lang/RescriptTokenTypesTest.kt` | `com.rescript.plugin.lang` | TokenSet の内容・要素数検証 |
| `src/test/.../lang/RescriptParserDefinitionTest.kt` | `com.rescript.plugin.lang` | ファクトリメソッド検証 |
| `src/test/.../highlight/RescriptSyntaxHighlighterTest.kt` | `com.rescript.plugin.highlight` | トークン→属性マッピング検証 |
| `src/test/.../highlight/RescriptBraceMatcherTest.kt` | `com.rescript.plugin.highlight` | ブレースペア検証 |
| `src/test/.../highlight/RescriptColorSettingsPageTest.kt` | `com.rescript.plugin.highlight` | 設定ページ属性検証 |
| `src/test/.../commenter/RescriptCommenterTest.kt` | `com.rescript.plugin.commenter` | コメント文字列検証 |
| `src/test/.../folding/RescriptFoldingBuilderTest.kt` | `com.rescript.plugin.folding` | プレースホルダー・折りたたみデフォルト検証 |

### 変更するファイル

| ファイル | 変更内容 |
|---------|---------|
| `src/test/.../lang/RescriptLexerTest.kt` | エッジケーステストメソッド追加 |
| `src/test/.../codestyle/RescriptLineIndentProviderTest.kt` | 追加テストメソッド追加 |
| `build.gradle.kts` | `test-local` の `sourceSets` 参照削除 |

## テスト設計詳細

### 1. RescriptTokenTypesTest

```kotlin
class RescriptTokenTypesTest {
    // KEYWORDS TokenSet: 57要素（キーワード48 + キーワード演算子7 + ビルトイン9 + BOOL_VALUE）
    // ※ 実際にソースを数えて正確な数を使用
    fun testKeywordsCount()
    fun testKeywordsContainsLet()
    fun testKeywordsContainsBoolValue()

    // OPERATORS TokenSet: 31要素
    fun testOperatorsCount()
    fun testOperatorsContainsPipeForward()

    // TOP_LEVEL_KEYWORDS TokenSet: 7要素
    fun testTopLevelKeywordsCount()
    fun testTopLevelKeywordsContainsAllDeclarationStarters()

    // COMMENTS TokenSet: 2要素
    fun testCommentsCount()

    // NUMBERS TokenSet: 2要素
    fun testNumbersCount()

    // STRINGS TokenSet: 3要素
    fun testStringsCount()
}
```

### 2. RescriptSyntaxHighlighterTest

```kotlin
class RescriptSyntaxHighlighterTest {
    private val highlighter = RescriptSyntaxHighlighter()

    // キーワードマッピング: KEYWORDS 内全トークン → KEYWORD 属性
    fun testAllKeywordsMapToKeywordAttribute()

    // 演算子マッピング: OPERATORS 内全トークン → OPERATOR 属性
    fun testAllOperatorsMapToOperatorAttribute()

    // 文字列マッピング
    fun testStringValueMapsToString()
    fun testJsStringOpenMapsToString()
    fun testCharValueMapsToString()

    // 数値マッピング
    fun testIntValueMapsToNumber()
    fun testFloatValueMapsToNumber()

    // コメントマッピング
    fun testSingleCommentMapsToLineComment()
    fun testMultiCommentMapsToBlockComment()

    // 括弧マッピング
    fun testBracesMapsCorrectly()
    fun testBracketsMapsCorrectly()
    fun testParensMapsCorrectly()

    // 句読点マッピング
    fun testDotMapsToDot()
    fun testCommaMapping()
    fun testSemiMapping()

    // 特殊トークン
    fun testTypeArgumentMapsToTypeArg()
    fun testPolyVariantMapsToPolyVariant()
    fun testUidentMapsToModuleName()
    fun testAnnotationMapping()

    // JSX マッピング
    fun testJsxTagNameMapsToMarkupTag()
    fun testJsxComponentNameMapsToModuleName()
    fun testTagBracketsMapToMarkupTagBracket()

    // BAD_CHARACTER マッピング
    fun testBadCharacterMapping()

    // 未知トークン
    fun testUnmappedTokenReturnsEmptyArray()

    // レクサー
    fun testGetHighlightingLexerReturnsRescriptLexer()
}
```

### 3. RescriptBraceMatcherTest

```kotlin
class RescriptBraceMatcherTest {
    private val matcher = RescriptBraceMatcher()

    fun testPairsContainsThreeEntries()
    fun testBracePairExists()
    fun testBracketPairExists()
    fun testParenPairExists()
    fun testBracePairIsStructural()
    fun testBracketPairIsNotStructural()
    fun testParenPairIsNotStructural()
    fun testIsPairedBracesAllowedBeforeTypeAlwaysTrue()
    fun testGetCodeConstructStartReturnsOffset()
}
```

### 4. RescriptCommenterTest

```kotlin
class RescriptCommenterTest {
    private val commenter = RescriptCommenter()

    fun testLineCommentPrefix()
    fun testBlockCommentPrefix()
    fun testBlockCommentSuffix()
    fun testCommentedBlockCommentPrefixIsNull()
    fun testCommentedBlockCommentSuffixIsNull()
}
```

### 5. RescriptParserDefinitionTest

```kotlin
class RescriptParserDefinitionTest {
    private val definition = RescriptParserDefinition()

    fun testCreateLexerReturnsRescriptLexer()
    fun testCreateParserReturnsRescriptParser()
    fun testGetFileNodeType()
    fun testGetCommentTokensContainsSingleComment()
    fun testGetCommentTokensContainsMultiComment()
    fun testGetStringLiteralElementsContainsStringValue()
}
```

### 6. RescriptColorSettingsPageTest

```kotlin
class RescriptColorSettingsPageTest {
    private val page = RescriptColorSettingsPage()

    fun testDisplayName()
    fun testIconNotNull()
    fun testHighlighterType()
    fun testAttributeDescriptorsNotEmpty()
    fun testAttributeDescriptorsContainKeyword()
    fun testColorDescriptorsEmpty()
    fun testDemoTextContainsLetKeyword()
    fun testDemoTextContainsModuleKeyword()
    fun testAdditionalTagMapNotEmpty()
    fun testAdditionalTagMapContainsVar()
}
```

### 7. RescriptFoldingBuilderTest

`isCollapsedByDefault(node: ASTNode): Boolean` は引数を無視して `false` を返すため、モックの ASTNode を渡してテスト可能。`getPlaceholderText` も同様に elementType のみ参照するため、モック可能。

ただし ASTNode はインターフェースのため、モックライブラリなしではインスタンス化が困難。Mockito を追加するか、簡易的なアプローチを検討する。

**方針:** テスト依存に Mockito を追加せず、`getPlaceholderText` / `isCollapsedByDefault` のテストは ASTNode のシンプルなスタブを手書きする。

```kotlin
class RescriptFoldingBuilderTest {
    private val builder = RescriptFoldingBuilder()

    fun testIsCollapsedByDefaultReturnsFalse()
    fun testGetPlaceholderTextForMultiComment()
    fun testGetPlaceholderTextForModuleDeclaration()
    fun testGetPlaceholderTextForOtherElements()
}
```

### 8. RescriptLexerTest 追加分

```kotlin
// ── Edge Cases: String ──
fun testUnclosedString()
fun testStringWithNewline()

// ── Edge Cases: Template ──
fun testUnclosedTemplate()
fun testTemplateDollarWithoutBrace()
fun testTemplateNestedBraces()

// ── Edge Cases: Comments ──
fun testDeeplyNestedBlockComment()
fun testUnclosedNestedComment()

// ── Edge Cases: Numeric ──
fun testLeadingZeroInteger()
fun testFloatDotWithoutTrailingDigits()

// ── Edge Cases: Lexer State ──
fun testLexerResetBetweenUses()
```

### 9. RescriptLineIndentProviderTest 追加分

```kotlin
fun testFindLastSignificantTokenCommentOnlyLine()
fun testFindLastSignificantTokenBlockCommentOnlyLine()
fun testFindLastSignificantTokenStringContainingKeywords()
fun testFindLastSignificantTokenComplexExpression()
```

### 10. build.gradle.kts クリーンアップ

25行目の以下を削除：

```kotlin
kotlin.srcDir("src/test-local/kotlin")
```

## テスト想定数

| テストクラス | 新規テスト数 |
|-------------|------------|
| RescriptTokenTypesTest | 10 |
| RescriptSyntaxHighlighterTest | 22 |
| RescriptBraceMatcherTest | 9 |
| RescriptCommenterTest | 5 |
| RescriptParserDefinitionTest | 6 |
| RescriptColorSettingsPageTest | 10 |
| RescriptFoldingBuilderTest | 4 |
| RescriptLexerTest（追加分） | 10 |
| RescriptLineIndentProviderTest（追加分） | 4 |
| **合計** | **80** |

**既存169テスト + 新規80テスト = 249テスト（目標）**

## 影響範囲

- テストコードの追加のみ。プロダクションコードへの変更なし
- `build.gradle.kts` の `sourceSets` 変更は `test-local` の除去のみ（ディレクトリが存在しないため影響なし）
- テスト実行時間への影響は軽微（単体テストのため ms 単位）

## リスク

| リスク | 対策 |
|-------|------|
| ASTNode モック作成が困難 | 必要最小限のメソッドのみオーバーライドする簡易スタブを作成。それでも困難な場合は FoldingBuilder テストをスキップ |
| `RescriptColorSettingsPage` のコンストラクタが IntelliJ Platform に依存 | 事前に `new RescriptColorSettingsPage()` の直接インスタンス化を確認。依存する場合はテストをスキップ |
| `getHighlightingLexer()` が FlexAdapter を返すため instanceof チェックが必要 | `RescriptLexer` は `FlexAdapter` を継承しているため、型チェックは問題ない |
