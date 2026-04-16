# 設計: テストコンパイラ警告クリーンアップ

## 方針の決定

警告 6 種類ごとに固定の置換レシピを定め、30 ファイルに機械的に適用する。ただし「Check for instance is always 'true'」と「No cast needed + return」の 2 種はテスト意図が文脈依存のため、ケースごとに判断する。

## 警告別の置換レシピ

### 1. `Check for instance is always 'true'` (45 件)

`assertTrue(instance is SuperType)` 形式で、`instance` の静的型がすでに `SuperType` を満たすケース。3 パターンに分岐:

#### 1a. 継承契約の明示的アサーション (例: `inspection is LocalInspectionTool`)

```kotlin
// Before
private val inspection = RescriptDuplicateOpenInspection()
@Test fun `inspection is a LocalInspectionTool`() {
    assertTrue(inspection is com.intellij.codeInspection.LocalInspectionTool)
}
```

**採用策**: ローカル変数の型を `Any` に広げ、`is` チェックを意味のある実行時検証にする。テスト本来の意図（継承契約のリグレッション検知）を維持する。

```kotlin
// After
@Test fun `inspection is a LocalInspectionTool`() {
    val inspection: Any = RescriptDuplicateOpenInspection()
    assertTrue(inspection is com.intellij.codeInspection.LocalInspectionTool)
}
```

採用理由: `@Suppress("KotlinConstantConditions")` では「契約が守られている」という検証が型チェッカー任せになり、リフレクションや動的ロードで置き換えられた場合を検出できない。明示的な実行時チェックを残す価値がある。

#### 1b. enum / sealed / `object` シングルトンの型判定 (例: `RescriptElementTypes.LET_DECLARATION is RescriptDeclarationElementType`)

これらの宣言は `object` / `val` で 1 インスタンス固定のため、型チェックは実質シングルトンの型タグ検証。書き換えが難しく、冗長だが削除は価値を失う。

**採用策**: `@Suppress("KotlinConstantConditions")` をテスト関数単位で付与。コメントで「registry のリグレッション検知」と付記する。

```kotlin
// After
@Test
@Suppress("KotlinConstantConditions") // Guards against accidental type change in registry
fun testElementTypesAreStubBased() {
    assertTrue(RescriptElementTypes.LET_DECLARATION is RescriptDeclarationElementType)
    ...
}
```

#### 1c. 値が `Any?` などからキャストで入手できるケース

該当なし（今回の警告群には含まれない）。

### 2. `No cast needed` (2 件)

`null as? X ?: return` パターン。テストが即 `return` するため本来のアサーションが実行されない **壊れたテスト**。

- `RescriptFindUsagesProviderTest.kt:17` — `getHelpId(null ...)` 結果を検証しようとして返す
- `RescriptQualifiedNameProviderTest.kt:182` — `qualifiedNameToElement(..., null ...)` 結果を検証しようとして返す

**採用策**: これらは実質テストしていない。LightPlatformTestCase の `project` / ダミー `PsiElement` を使い、非 null の値で呼び出して null 戻り値を検証するように書き換える。

```kotlin
// Before (RescriptFindUsagesProviderTest.kt:17)
@Test fun `help id is null`() {
    assertEquals(null, provider.getHelpId(null as? com.intellij.psi.PsiElement ?: return))
}

// After — PSI ファイル要素を生成して渡す
@Test fun `help id is null for any element`() {
    val file = RescriptTestUtils.createFile(project, "sample.res", "let x = 1")
    val element = file.findElementAt(0) ?: fail("Expected a PSI element")
    assertEquals(null, provider.getHelpId(element))
}
```

`RescriptQualifiedNameProviderTest.kt:182` は `BasePlatformTestCase` 継承のため `project` が利用可能。`null as? Project ?: return` を `project` 直接渡しに変更する。

### 3. Deprecated API: `AnActionEvent.createFromDataContext` (3 件)

IntelliJ Platform 2025.3 で利用可能な代替:

```kotlin
AnActionEvent.createEvent(dataContext, presentation, place, ActionUiKind.NONE, null /* InputEvent */)
```

対象ファイル:

- `RescriptOpenCompiledJsActionTest.kt:180`
- `RescriptSwitchFileActionTest.kt:122`
- `RescriptGenerateActionUtilTest.kt:40`

**採用策**: 3 箇所とも一律に `createEvent(ctx, presentation, place, ActionUiKind.NONE, null)` に置換。`import com.intellij.openapi.actionSystem.ActionUiKind` を追加する。

### 4. Deprecated override 注釈欠落 (1 件)

`RescriptCallHierarchyProviderTest.kt:49` の `override fun getData(dataId: String): Any? = null` は `DataContext.getData(String)`（非推奨）のオーバーライド。

**採用策**: `@Suppress("OVERRIDE_DEPRECATION")` をメソッドに付与。新 API `getData(DataKey<T>)` も同時に提供しているため、旧 API のオーバーライドを「意図的」として残す必要がある（IntelliJ の DataContext は旧メソッドも現役で呼ばれ得る）。

### 5. `Unchecked cast` (1 件)

`RescriptDependencyAnalyzerTest.kt:262` の `childArray as Array<PsiElement>`。`childArray` は `Array<SimpleStubElement>`（`PsiElement` サブ型）。

**採用策**: `@Suppress("UNCHECKED_CAST")` をラムダ行に付与。`Array<SimpleStubElement>` から `Array<PsiElement>` へのコピーは非効率かつテスト純度を落とすため、コストに見合わない。

```kotlin
@Suppress("UNCHECKED_CAST")
override fun getChildren(): Array<PsiElement> = childArray as Array<PsiElement>
```

### 6. Windows 不正文字を含むテスト名 (1 件)

`RescriptLspSignatureParserTest.kt:44` の `` `parseSignatureLabels parses optional param with =?` ``。

**採用策**: `?` をスペース + 英字に置換し、テスト名を `` `parseSignatureLabels parses optional param with trailing question mark` `` に変更。

## 影響ファイル一覧 (30 ファイル)

| ファイル | 警告種類 | 該当行数 |
|---|---|---|
| analysis/RescriptUnusedCodeInspectionTest.kt | 1a | 131 |
| completion/RescriptCompletionConfidenceTest.kt | 1a | 16, 39 |
| completion/RescriptLiveTemplateMacrosTest.kt | 1a | 36, 42 |
| completion/RescriptTemplateContextTypeTest.kt | 1a | 19 |
| config/RescriptFileTypeRecoveryStartupActivityTest.kt | 1a | 29 |
| config/RescriptFrameworkDetectorTest.kt | 1a | 19 |
| documentation/RescriptDocumentationProviderTest.kt | 1a | 25 |
| editor/RescriptEnterHandlerTest.kt | 1a | 18 |
| editor/RescriptJoinLinesHandlerTest.kt | 1a | 18 |
| generate/RescriptBaseGenerateActionTest.kt | 1a | 34 |
| generate/RescriptGenerateActionUtilTest.kt | 3 | 40 |
| hierarchy/RescriptDependencyAnalyzerTest.kt | 5 | 262 |
| hierarchy/call/RescriptCallHierarchyProviderTest.kt | 4 | 49 |
| highlight/RescriptHighlightUsagesHandlerFactoryTest.kt | 1a | 18 |
| inspection/RescriptDuplicateOpenInspectionTest.kt | 1a | 18 |
| inspection/RescriptEmptyModuleInspectionTest.kt | 1a | 18 |
| inspection/RescriptMissingConfigInspectionTest.kt | 1a | 18 |
| inspection/RescriptMutabilityInspectionTest.kt | 1a | 78 |
| inspection/RescriptStyleLintInspectionTest.kt | 1a | 77 |
| inspection/RescriptSuggestedRefactoringInspectionTest.kt | 1a | 89 |
| intention/RescriptBaseIntentionTest.kt | 1a | 85 |
| lang/RescriptFindUsagesProviderTest.kt | 2 | 17 |
| lang/psi/RescriptDeclarationElementTypeTest.kt | 1b | 40, 45-49, 54-59 |
| lang/psi/RescriptPsiTest.kt | 1b | 53-58 |
| lsp/RescriptExpressionTypeProviderTest.kt | 1a | 18 |
| lsp/RescriptLspSignatureParserTest.kt | 6 | 44 |
| navigation/RescriptGotoSuperHandlerTest.kt | 1a | 19 |
| navigation/RescriptOpenCompiledJsActionTest.kt | 3 | 180 |
| navigation/RescriptQualifiedNameProviderTest.kt | 2 | 182 |
| navigation/RescriptSwitchFileActionTest.kt | 3 | 122 |
| navigation/RescriptSymbolContributorTest.kt | 1a | 27 |
| quickfix/RescriptTypeHoleQuickFixTest.kt | 1a | 85 |
| refactor/RescriptExtractVariableHandlerTest.kt | 1a | 18 |
| refactor/RescriptRefactoringSupportProviderTest.kt | 1a | 23 |
| run/RescriptRunAnythingProviderTest.kt | 1a | 53 |

合計 35 件（一部ファイルは複数の警告を含む）。警告件数 53 と一致しない差分は、`RescriptDeclarationElementTypeTest.kt` (11 行) と `RescriptPsiTest.kt` (6 行) の多重警告によるもの。

## テスト戦略

各置換は以下のいずれかの検証で安全性を担保:

1. **1a / 1b / 4 / 5**: `@Suppress` 追加または型変更のみ。ロジック変更なし → ビルド + 既存テスト通過で十分
2. **2 (壊れたテスト書き換え)**: 書き換え後もアサーションが成立することを確認 → `./gradlew test` 全テスト通過
3. **3 (API 置換)**: 置換後のテストが同じシナリオを検証していることを確認 → 書き換え対象テストを個別実行で green 確認
4. **6 (テスト名変更)**: テスト名以外は不変 → 全テスト通過

## コミット分割

警告カテゴリ単位で 6 コミットに分割する:

1. `🐛 Fix broken tests in FindUsagesProvider/QualifiedNameProvider` (category 2)
2. `♻️ Replace deprecated AnActionEvent.createFromDataContext with createEvent` (category 3)
3. `♻️ Suppress deprecated DataContext.getData override warning` (category 4)
4. `♻️ Suppress unchecked cast in stub element test scaffolding` (category 5)
5. `♻️ Rename test case to remove Windows-unsafe question mark` (category 6)
6. `♻️ Clarify runtime type assertions in 25 test classes` (category 1a + 1b 一括)

カテゴリ 1 を最後に回すのは、他カテゴリが壊れたテストの書き換えで、まずそれらを片付けてから単純な型・Suppress 作業に集中するため。

## リスクと軽減

| リスク | 軽減策 |
|---|---|
| `ActionUiKind.NONE` が 2025.3 で未公開の可能性 | API 存在確認済み（`app.jar` の `createEvent(DataContext, Presentation, String, ActionUiKind, InputEvent)` シグネチャ確認済み） |
| カテゴリ 2 の書き換えが既存インフラ (`RescriptTestUtils.createFile`) と整合しない | 実装時に `RescriptTestUtils` を確認、必要なら `BasePlatformTestCase.myFixture.configureByText` を使用 |
| コミット単位が大きすぎてレビュー困難 | カテゴリ 6 ("1a 一括") は 25 ファイルだが、編集内容が「ローカル変数宣言の型変更」で一貫しているため粒度を優先 |
