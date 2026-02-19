# Design: テストカバレッジ 100% 到達

## 1. 並列実装戦略（git worktree）

### ブランチ構成

```
main
 └── feature/test-coverage-100          ← バッチブランチ
      ├── test/unit-tests               ← Worktree 1: 純粋ユニットテスト
      ├── test/inspection-analysis      ← Worktree 2: Inspection & Analysis
      ├── test/editor-intention         ← Worktree 3: Editor & Intention
      └── test/navigation-misc          ← Worktree 4: Navigation & Misc
```

### Worktree ディレクトリ

```
../rescript-wt-unit-tests/
../rescript-wt-inspection-analysis/
../rescript-wt-editor-intention/
../rescript-wt-navigation-misc/
```

### 競合リスク分析

各 worktree は異なるパッケージのテストファイルのみ操作するため、ファイル競合は発生しない:
- `RescriptTestUtils.kt` は既存で変更不要
- `plugin.xml` の変更なし
- `build.gradle.kts` の変更なし
- 各 worktree のテストファイルはパッケージが完全に独立

## 2. テストアプローチ

### テストパターン（既存プロジェクトに準拠）

1. **JUnit 4 + バッククォート命名**: `@Test fun \`description\`() { ... }`
2. **直接インスタンス化**: モッキングフレームワーク不使用
3. **Dynamic Proxy スタブ**: `RescriptTestUtils.stubAstNode()`, `SimpleStubElement` 使用
4. **TestableFoldingBuilder パターン**: protected メソッドを公開するサブクラス
5. **`org.junit.Assert`**: アサーション標準ライブラリ

### カバレッジ向上の方針

- **純粋ロジックのテスト**: IDE 依存なしで直接テスト（最高 ROI）
- **PSI スタブテスト**: `SimpleStubElement` + `stubAstNodeWithChildren()` で PSI ツリー構築
- **内部メソッド直接テスト**: `internal` メソッドは同一パッケージからテスト可能
- **メソッド抽出**: プラットフォーム依存メソッド内のロジックを抽出してテスト可能にする（最小限）

## 3. Worktree 1: 純粋ユニットテスト（test/unit-tests）

### 3.1 `RescriptNamesValidatorTest.kt`
- **対象**: `refactor/RescriptNamesValidator.kt`
- **方式**: 直接インスタンス化、`project` 引数は `null`
- **テストケース**:
  - `isIdentifier`: lident (`foo`, `_bar`, `camelCase`, `x'`), uident (`Foo`, `Belt`), 不正 (`""`, `123`, `foo bar`, `@attr`, `-invalid`)
  - `isKeyword`: 全46キーワード → true、非キーワード → false
  - エッジケース: 単独 `_`, 数字始まり, 特殊文字

### 3.2 `RescriptCommandTest.kt`
- **対象**: `run/RescriptCommand.kt`
- **方式**: 直接呼び出し
- **テストケース**:
  - `fromId("build")` → BUILD, `fromId("clean")` → CLEAN, `fromId("unknown")` → BUILD（デフォルト）
  - 各エントリの `args` プロパティ
  - `entries` のサイズ検証

### 3.3 `RescriptCliDetectorTest.kt`
- **対象**: `run/RescriptCliDetector.kt`
- **方式**: 一時ディレクトリに node_modules/.bin/rescript を作成
- **テストケース**:
  - `findCli` で workingDirectory に CLI がある場合
  - `findCli` で projectBasePath に CLI がある場合
  - 親ディレクトリ探索のフォールバック
  - どこにも見つからない場合 → null
  - null 引数の処理

### 3.4 `RescriptSemanticTokensSupportTest.kt`
- **対象**: `lsp/RescriptSemanticTokensSupport.kt`
- **方式**: 直接インスタンス化
- **テストケース**:
  - 各 tokenType ("variable", "type", "namespace" 等) → 対応する TextAttributesKey
  - 未知の tokenType → null
  - modifiers パラメータの影響なし確認

### 3.5 `RescriptCompilationStatusServiceTest.kt`
- **対象**: `lsp/RescriptCompilationStatusService.kt`
- **方式**: Project スタブ + Disposable スタブ
- **テストケース**:
  - 初期状態 = UNKNOWN
  - `updateStatus()` で状態変更
  - リスナーが通知を受信
  - Disposer 登録後にリスナーが削除される
  - CompilationStatus データクラスのプロパティ

### 3.6 `RescriptSpellcheckingStrategyTest.kt`
- **対象**: `spellcheck/RescriptSpellcheckingStrategy.kt`
- **方式**: PsiElement スタブ + ASTNode スタブ
- **テストケース**:
  - `SINGLE_COMMENT`, `MULTI_COMMENT`, `STRING_VALUE` → TEXT_TOKENIZER
  - `LIDENT`, `UIDENT` → identifier tokenizer
  - `LBRACE`, `LET` 等 → EMPTY_TOKENIZER
  - `element.node == null` → EMPTY_TOKENIZER

### 3.7 `RescriptPsiUtilsTest.kt`
- **対象**: `lang/psi/RescriptPsiUtils.kt`
- **方式**: `stubAstNodeWithChildren()` + `SimpleStubElement`
- **テストケース**:
  - `extractName`: LET_DECLARATION + LIDENT → 名前抽出
  - `extractName`: MODULE_DECLARATION + UIDENT → 名前抽出
  - `extractName`: `rec` キーワードスキップ
  - `extractName`: 子要素なし → "(anonymous)"
  - `extractName`: node == null → "(unknown)"
  - `getIcon`: 各宣言タイプ → 対応アイコン
  - `getElementDescription`: 各宣言タイプ → 対応説明文
  - `NAVIGABLE_TYPES` の内容検証

### 3.8 `RescriptTypeDeclarationParserTest.kt`
- **対象**: `generate/RescriptTypeDeclarationParser.kt`
- **方式**: 直接呼び出し（純粋関数）
- **テストケース**:
  - Variant パース: `type t = | A | B(int)` → Variant(2 constructors)
  - Record パース: `type t = {name: string, age: int}` → Record(2 fields)
  - mutable フィールド: `mutable count: int` → isMutable = true
  - `extractTypeName`: `type foo = ...` → "foo"
  - 空ボディ → Unknown
  - `=` なし → Unknown
  - エッジケース: 空コンストラクタ, ペイロード付き, ネスト型

### 3.9 `RescriptRunConfigurationTypeTest.kt`
- **対象**: `run/RescriptRunConfigurationType.kt`
- **方式**: 直接インスタンス化
- **テストケース**: ID, displayName 検証

### 3.10 `RescriptConfigurationFactoryTest.kt`
- **対象**: `run/RescriptConfigurationFactory.kt`
- **方式**: 直接インスタンス化
- **テストケース**: ID, optionsClass 検証

### 3.11 `RescriptTestRunConfigurationTypeTest.kt`
- **対象**: `test/RescriptTestRunConfigurationType.kt`
- **テストケース**: ID, displayName 検証

### 3.12 `RescriptTestConfigurationFactoryTest.kt`
- **対象**: `test/RescriptTestConfigurationFactory.kt`
- **テストケース**: ID, optionsClass 検証

## 4. Worktree 2: Inspection & Analysis（test/inspection-analysis）

### 4.1 `RescriptEmptyModuleInspectionTest.kt`（新規）
- **対象**: `inspection/RescriptEmptyModuleInspection.kt`
- **方式**: PSI スタブで検査ロジック検証
- **テストケース**:
  - メタ情報（displayName, groupDisplayName, isEnabledByDefault）
  - QuickFix familyName
  - `hasDeclarationChildren()` 相当ロジック: 空モジュール vs 非空モジュール
  - モジュールエイリアス（`=` あり）のスキップ確認

### 4.2 `RescriptDuplicateOpenInspectionTest.kt`（新規）
- **対象**: `inspection/RescriptDuplicateOpenInspection.kt`
- **方式**: PSI スタブ
- **テストケース**:
  - メタ情報検証
  - QuickFix familyName
  - 重複検出ロジック（同一スコープ内の同名 open）

### 4.3 `RescriptMissingConfigInspectionTest.kt`（新規）
- **対象**: `inspection/RescriptMissingConfigInspection.kt`
- **方式**: メタ情報テスト
- **テストケース**:
  - メタ情報（displayName, groupDisplayName）
  - 検証対象ファイル名リスト

### 4.4 `RescriptReanalyzeAnnotatorTest.kt`（改善）
- **追加テストケース**:
  - `parseJsonOutput` 完全パスマッチ（`file == filePath`）
  - `parseJsonOutput` 逆方向マッチ（`filePath.endsWith(file)`）
  - `parseAllDiagnostics` range.size < 4
  - `parseJsonOutput` "name" フィールド欠損 → "unknown" デフォルト
  - `doAnnotate(null)` → null 返却

### 4.5 `RescriptReanalyzeQuickFixTest.kt`（改善）
- **追加テストケース**:
  - `isAvailable()` → true
  - `findWordStart` 空文字列
  - `findWordEnd` テキスト末尾超過
  - `findWordStart/End` アポストロフィ含み識別子 (`x'`)
  - `startInWriteAction` 各サブクラス

### 4.6 `RescriptUnusedCodeInspectionTest.kt`（改善）
- **追加テストケース**:
  - `isGraphNeeded` = false
  - `getDisplayName`, `getGroupDisplayName` 検証
  - ファイル解決ロジック（メソッド抽出後テスト可能にする場合）

### 4.7 `RescriptDependencyAnalyzerTest.kt`（改善）
- **追加テストケース**:
  - `extractModulePath` PSI スタブ: OPEN + UIDENT("Belt") + DOT + UIDENT("Array") → "Belt.Array"
  - `extractModulePath` 単一モジュール: UIDENT("Belt") → "Belt"
  - `extractModulePath` 子要素なし → ""
  - `getReferencedModuleNames` テスト

## 5. Worktree 3: Editor & Intention（test/editor-intention）

### 5.1 `RescriptBreadcrumbsProviderTest.kt`（新規）
- **対象**: `breadcrumb/RescriptBreadcrumbsProvider.kt`
- **方式**: PSI スタブ
- **テストケース**:
  - `getLanguages()` に RescriptLanguage 含む
  - `acceptElement()`: NAVIGABLE_TYPES → true、それ以外 → false
  - `getElementInfo()` が `RescriptPsiUtils.extractName()` と一致

### 5.2 `RescriptStructureViewElementTest.kt`（新規）
- **対象**: `structure/RescriptStructureViewElement.kt`
- **方式**: NavigatablePsiElement スタブ
- **テストケース**:
  - `getAlphaSortKey()` が要素名を返す
  - `getPresentation()` のテキストとアイコン
  - `getChildren()` が NAVIGABLE_TYPES のみ返す

### 5.3 `RescriptPostfixTemplateProviderTest.kt`（新規）
- **対象**: `completion/RescriptPostfixTemplateProvider.kt`
- **方式**: 直接インスタンス化
- **テストケース**:
  - テンプレート数 = 7
  - 各テンプレートの key 検証 (switch, pipe, log, some, ok, error, ignore)
  - `isTerminalSymbol('.')` → true
  - `isTerminalSymbol(',')` → false

### 5.4 `RescriptSmartEnterProcessorTest.kt`（改善）
- **追加テストケース**:
  - `analyzeLine` の追加エッジケース（空行、コメントのみ行）
  - テンプレートリテラル内の括弧

### 5.5 `RescriptStatementUpDownMoverTest.kt`（改善）
- **追加テストケース**:
  - `findDeclaration` PSI スタブ: LET_DECLARATION 要素 → 自身を返す
  - `findDeclaration` PSI スタブ: 宣言内の子要素 → 親宣言を返す
  - `findDeclaration` PSI スタブ: 宣言外 → null
  - `findNextDeclaration` PSI スタブ: 次の兄弟が宣言 → その宣言
  - `findNextDeclaration` PSI スタブ: 次の兄弟がアノテーション → その先の宣言
  - `findPreviousDeclaration` 同様のテスト

### 5.6 `RescriptWrapWithIntentionTest.kt`（改善）
- **追加テストケース**:
  - `startInWriteAction()` = true
  - 各サブクラスの `wrapper` プロパティ
  - `isAvailable()` ロジック（PSI スタブ）

### 5.7 `RescriptAddGenTypeIntentionTest.kt`（改善）
- **追加テストケース**:
  - `startInWriteAction()` = true
  - `findParentDeclaration` PSI スタブ: 各宣言タイプ
  - `hasGenTypeAnnotation` PSI スタブ: アノテーション有無

### 5.8 `RescriptSurroundDescriptorTest.kt`（改善）
- **追加テストケース**:
  - `isApplicable` 空配列 → false
  - `isApplicable` 非空配列 → true

### 5.9 `RescriptFoldingBuilderTest.kt`（改善）
- **追加テストケース**:
  - `getLanguagePlaceholderText` JSX_ELEMENT スタブ（extractJsxTagName テスト）
  - `getLanguagePlaceholderText` JSX_FRAGMENT
  - `extractJsxTagName` ドット付きタグ名（例: React.Fragment）
  - `isCustomFoldingCandidate` 追加タイプ

### 5.10 `RescriptCustomFoldingProviderTest.kt`（改善）
- **追加テストケース**:
  - `isCustomRegionEnd` 先頭空白付き
  - `getPlaceholderText` `"// #region"` 空名前バリアント

### 5.11 `RescriptLineIndentProviderTest.kt`（改善）
- **追加テストケース**:
  - `isSuitableFor(RescriptLanguage)` → true
  - `isSuitableFor(null)` → false
  - `findLastSignificantToken` 追加エッジケース

## 6. Worktree 4: Navigation & Misc（test/navigation-misc）

### 6.1 `RescriptRawJsInjectorTest.kt`（新規）
- **対象**: `injection/RescriptRawJsInjector.kt`
- **方式**: PSI スタブ + SimpleStubElement
- **テストケース**:
  - `getInjectionRange`: `"content"` → TextRange(1, 8)
  - `getInjectionRange`: `""` → null（空文字列）
  - `getInjectionRange`: テンプレート文字列（引用符なし）→ フルレンジ
  - `isInsideRawBlock`: `% raw ( string` パターン → true
  - `isInsideRawBlock`: パターン不一致 → false

### 6.2 `RescriptQualifiedNameProviderTest.kt`（改善）
- **追加テストケース**:
  - `findDeclarationElement` PSI スタブ: 各宣言タイプ
  - `buildModulePath` PSI スタブ: ネストモジュール
  - `getQualifiedName` PSI スタブ: トップレベル宣言
  - `qualifiedNameToElement` → null 確認

### 6.3 `RescriptGotoRelatedProviderTest.kt`（改善）
- **追加テストケース**:
  - `.res` ファイルで `.mjs` JS 出力
  - `.res` ファイルで `.js` JS 出力
  - サブディレクトリ内の `.res` ファイル

### 6.4 `RescriptImportOptimizerTest.kt`（改善）
- **追加テストケース**:
  - `extractModulePath` で `child.node == null` → 空文字列
  - `supports()` 追加ケース

### 6.5 `RescriptPasteAsJsonActionTest.kt`（改善）
- **追加テストケース**:
  - `getActionUpdateThread()` → BGT
  - `escapeString` バックスラッシュ入力
  - `convertJsonToRescript` 追加エッジケース

### 6.6 `RescriptTestFrameworkDetectorTest.kt`（改善）
- **追加テストケース**:
  - 未カバー分岐の追加テスト

### 6.7 `RescriptTestLocatorTest.kt`（改善）
- **追加テストケース**:
  - 未カバー分岐の追加テスト

### 6.8 `RescriptCompilerStatusWidgetFactoryTest.kt`（改善）
- **追加テストケース**:
  - 未カバー分岐の追加テスト

## 7. プロダクションコード変更（最小限）

テスタビリティ向上のために必要な場合のみ:
- メソッドの `private` → `internal` 変更（テスト対象メソッドが private の場合）
- 複雑なメソッドからのロジック抽出（`RescriptUnusedCodeInspection.runInspection()` 等）

変更は各 worktree の担当パッケージ内に限定する。

## 8. 影響範囲

### 新規ファイル（19 ファイル）
- Worktree 1: 12 テストファイル
- Worktree 2: 3 テストファイル
- Worktree 3: 3 テストファイル
- Worktree 4: 1 テストファイル

### 改善ファイル（19 ファイル）
- Worktree 2: 4 テストファイル
- Worktree 3: 8 テストファイル
- Worktree 4: 7 テストファイル

### プロダクションコード変更
- 最小限（必要に応じてメソッド可視性変更のみ）
