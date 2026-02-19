# Requirements: Editor & Intention Tests

## 概要

Editor / Intention / Structure / Completion / Folding / Codestyle 領域のテストカバレッジを向上させる。新規テスト 3 ファイルの追加と、既存テスト 8 ファイルの改善を行う。

## 対象

### 新規テスト (3 ファイル)

1. **RescriptBreadcrumbsProviderTest.kt** — `getLanguages()`, `acceptElement()` (NAVIGABLE_TYPES → true/false), `getElementInfo()`
2. **RescriptStructureViewElementTest.kt** — `getAlphaSortKey()`, `getPresentation()`, `getChildren()` (NavigatablePsiElement スタブ)
3. **RescriptPostfixTemplateProviderTest.kt** — テンプレート数=7, 各 key 検証, `isTerminalSymbol`

### 既存テスト改善 (8 ファイル)

4. **RescriptSmartEnterProcessorTest.kt** — `analyzeLine` 追加エッジケース (空行、コメント行、テンプレートリテラル)
5. **RescriptStatementUpDownMoverTest.kt** — `findDeclaration`/`findNextDeclaration`/`findPreviousDeclaration` PSI スタブテスト
6. **RescriptWrapWithIntentionTest.kt** — `startInWriteAction()`, wrapper プロパティ, `isAvailable` ロジック
7. **RescriptAddGenTypeIntentionTest.kt** — `startInWriteAction()`, `findParentDeclaration`/`hasGenTypeAnnotation` PSI スタブ
8. **RescriptSurroundDescriptorTest.kt** — `isApplicable` 空配列/非空配列
9. **RescriptFoldingBuilderTest.kt** — JSX_ELEMENT/JSX_FRAGMENT プレースホルダー, `extractJsxTagName` ドット付き
10. **RescriptCustomFoldingProviderTest.kt** — `isCustomRegionEnd` 空白付き, `getPlaceholderText` 空名前バリアント
11. **RescriptLineIndentProviderTest.kt** — `isSuitableFor(RescriptLanguage/null)`, `findLastSignificantToken` 追加

## 受け入れ条件

- 全テストが `./gradlew test` で通過すること
- IntelliJ Platform テストフレームワークに依存せず、純粋なユニットテストであること
- PSI スタブは Java Proxy を使用してモック化すること
