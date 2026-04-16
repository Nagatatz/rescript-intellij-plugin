# Design — ACTIONABLE Test Gap Backfill

## 設計方針

本番コードを変更しない純粋なテスト追加のみのワークストリーム。既存のテストパターン（`RescriptModuleHierarchyProviderTest`, `RescriptCallAnalyzerTest`, `RescriptSwitchFileActionTest`, `RescriptDependencyDiagramModelTest`）に追従する。

### テスト分類と採用フレームワーク

| 種類 | フレームワーク | 用途 |
|---|---|---|
| 純ロジック / PSI スタブ | JUnit 5 + `RescriptTestUtils.stubPsiElement` / Mockito | PSI ツリー走査、ラベル整形、フィルタロジック |
| Fixture (light project) | JUnit 3 + `BasePlatformTestCase` | VirtualFile 生成 / FileType 判定 / FileTypeIndex / PsiManager |

`IntelliJPlatformExtension` (JUnit 5) も使えるが、ヒエラルキー系では `BasePlatformTestCase` の方が `addFileToProject` などのヘルパが揃っており、既存ヒエラルキー系テストとの整合性が良い。

### スタブ vs Mockito

- 既存テストは Mockito (`mock(PsiElement::class.java)`) と独自スタブ (`SimpleStubElement`) を混在使用している
- Group 1 ヒエラルキー系の TreeStructure / NodeDescriptor は Mockito でも動くが、`HierarchyNodeDescriptor.update()` は内部で大量の super 呼び出しがあるため、**実 PSI を fixture で生成して descriptor を組み立てる方が安全**。BasePlatformTestCase で書く方がよい
- Mockito スタブは `extractDependencies` / `findEnclosingDeclaration` 等の static utility 系に限定

### Group 2 ブラウザクラスのテスト戦略

`HierarchyBrowserBaseEx` のコンストラクタは `Project` と `PsiElement` を要求し、フィールド注入 / setUp が必要。`BasePlatformTestCase` で `myFixture.addFileToProject(...)` で PSI を作り、`RescriptModuleHierarchyBrowser(rescriptFile)` を直接インスタンス化する。

検証対象:
- `isApplicableElement(element)`: 各 typeName への applicable / not applicable 判定
- `createHierarchyTreeStructure(typeName, psiElement)`: typeName → 適切な TreeStructure クラスへのルーティング
- `getContentDisplayName`: 表示名抽出ロジック
- `getActionPlace`, `getPrevOccurenceActionNameImpl`, `getNextOccurenceActionNameImpl`: 定数返却
- `getComparator()`: 文字列比較ロジック (大文字小文字無視)

検証**しない**: createTrees (Swing JTree 構築), createLegendPanel (常に null), prependActions (no-op)

### Group 2 RescriptFormattingService

検証対象:
- `canFormat(PsiFile)`: `.res` / `.resi` / `.txt` ファイルでの判定
- `getName()`: `"rescript format"` を返す
- `getNotificationGroupId()`: `"ReScript"` を返す
- `getFeatures()`: 空セットを返す
- `createFormattingTask(request)`: `RescriptCliDetector.findCli` が null を返すケース → `request.onError` が呼ばれて null が返る

検証**しない**: 実際の `rescript format` プロセス起動・stdin/stdout/stderr 処理 — 外部プロセスのインテグレーションテスト相当

### Group 1 RescriptIcons

最小限の sanity test。`@JvmField val FILE`, `INTERFACE_FILE`, `CONFIG_FILE` の 3 フィールドが non-null であることを確認。`IconLoader.getIcon` は遅延ロードで例外を投げないが、ロードに失敗した場合は EmptyIcon が返る。`/icons/rescript-file.svg` 等のリソースが classpath に存在することは別途 `Class.getResourceAsStream` で確認。

### Group 1 RescriptDeclarationPsiElement

- `toString()`: `"RescriptDeclarationPsiElement(${type})"` 形式の検証 — fixture で実 LET_DECLARATION を作って toString() の prefix を確認
- `getDeclarationName()`: stub があれば stub.name、なければ `RescriptPsiUtils.extractName(this)` にフォールバック — fixture で `let foo = 1` を parse して getDeclarationName() == "foo" を確認

### Group 1 NodeDescriptor

`HierarchyNodeDescriptor.update()` は内部で `super.update()` を呼び `myHighlightedText` を更新する。Mockito では `super` 呼び出しが扱いにくい。fixture で実際のヒエラルキーノードを作る方が単純。代替として、PsiUtils.extractName / getIcon が返す値の正しさは PsiUtils 側で別途テスト済みなので、NodeDescriptor のテストはコンストラクタの引数受け渡しと、isBase フラグの伝播のみを検証する軽量版で十分。

→ **採用方針**: NodeDescriptor 系は **fixture テスト**で実 PSI を組み立て、descriptor を構築 → `update()` を呼んで `myHighlightedText.text` を確認、`getIcon()` 経由で icon が設定されたことを確認

### Group 1 TreeStructure

- `RescriptModuleHierarchyTreeStructure.buildChildren`: MODULE_DECLARATION の child のみフィルタ
- `RescriptModuleDependencyTreeStructure.buildChildren`: RescriptFile のみ展開、`extractModuleReferences` 結果を子ノード化
- `RescriptCallerTreeStructure.buildChildren`: `findCallers` 結果を子ノード化
- `RescriptCalleeTreeStructure.buildChildren`: `findCallees` 結果を子ノード化

→ **採用方針**: fixture で `module Inner { let x = 1 }` 等を含む `.res` を読み込み、TreeStructure を構築 → `getChildElements(rootDescriptor)` を呼んで件数と型を検証

## 新規ファイル一覧

すべて `src/test/kotlin/com/rescript/plugin/<package>/` 配下:

1. `RescriptIconsTest.kt`
2. `config/RescriptJsonSchemaProviderFactoryTest.kt`
3. `diagram/RescriptDependencyDiagramProviderTest.kt` (`buildDiagram` の fixture テスト追加。`extractDependencies` は `RescriptDependencyDiagramModelTest` で既にカバー済みなので重複しない)
4. `hierarchy/RescriptModuleHierarchyNodeDescriptorTest.kt`
5. `hierarchy/RescriptModuleHierarchyTreeStructureTest.kt` (両 TreeStructure をまとめてテスト)
6. `hierarchy/call/RescriptCallHierarchyNodeDescriptorTest.kt`
7. `hierarchy/call/RescriptCalleeTreeStructureTest.kt`
8. `hierarchy/call/RescriptCallerTreeStructureTest.kt`
9. `lang/psi/RescriptDeclarationPsiElementTest.kt`
10. `formatter/RescriptFormattingServiceTest.kt`
11. `hierarchy/RescriptModuleHierarchyBrowserTest.kt`
12. `hierarchy/call/RescriptCallHierarchyBrowserTest.kt`

## 修正ファイル

- `build.gradle.kts`: kover excludes から、新規テストでカバーされたクラス/パッケージを削除し、`minBound` をラチェット更新

### kover excludes 見直しの方針

excludes に列挙されたパッケージ全体（`com.rescript.plugin.hierarchy`, `com.rescript.plugin.hierarchy.call`, `com.rescript.plugin.diagram`, `com.rescript.plugin.formatter`, `com.rescript.plugin.config`）は、Workstream B のテスト追加後も**多数のクラスが未カバーで残る**（hierarchy パッケージには Browser 以外にも未テストの IDE-coupled クラスがある）。テスト追加後は、これらパッケージを exclude から外すと total 90% を割る可能性が高い。

→ **採用方針**: 安全側に倒し、パッケージ単位の exclude は据え置く。代わりに**新規テストで実カバー率が上がる**ことを `koverHtmlReport` で確認し、`minBound` のラチェットは `total` 実測ベースで決定する。新たに完全カバーされる個別クラスがあれば classes() exclude のリストから外す。

`com.rescript.plugin.lang.psi.RescriptDeclarationPsiElement*` の class exclude は、`RescriptDeclarationPsiElementTest` 追加後に削除を試みる（`com.rescript.plugin.lang.psi.RescriptFile` は引き続き exempt）。`com.rescript.plugin.RescriptIcons` の class exclude は、テスト追加後に削除を試みる。

## コミット粒度

`.claude/rules/git-conventions.md` に従い機能単位で分割。各コミット後にテストパスを確認。

1. `✅ Add RescriptIconsTest`
2. `✅ Add RescriptJsonSchemaProviderFactoryTest`
3. `✅ Add RescriptDeclarationPsiElementTest`
4. `✅ Add tests for module hierarchy descriptors and tree structures`
5. `✅ Add tests for call hierarchy descriptors and tree structures`
6. `✅ Add RescriptDependencyDiagramProviderTest for buildDiagram`
7. `✅ Add RescriptFormattingServiceTest`
8. `✅ Add RescriptModuleHierarchyBrowserTest`
9. `✅ Add RescriptCallHierarchyBrowserTest`
10. `🔧 Update kover excludes and ratchet minBound`

## 検証

```bash
./gradlew ktlintCheck
./gradlew test
./gradlew koverHtmlReport
# build/reports/kover/html/index.html で実測カバレッジ確認
./gradlew clean buildPlugin
```
