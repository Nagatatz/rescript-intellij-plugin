# Design: 純粋ユニットテスト追加

## アプローチ

既存テストパターンに準拠し、JUnit 4 + スタブベースの純粋ユニットテストを作成する。IDE テストフレームワークは使用せず、`RescriptTestUtils` のスタブユーティリティを活用する。

## テスト設計

### 1. RescriptNamesValidatorTest.kt
- **パッケージ**: `com.rescript.plugin.refactor`
- **方式**: 直接インスタンス化、project=null
- **テストケース**: lident/uident の正常系、空文字・数字始まり等の異常系、全キーワード判定、非キーワード判定

### 2. RescriptCommandTest.kt
- **パッケージ**: `com.rescript.plugin.run`
- **方式**: enum 値の直接検証
- **テストケース**: 各コマンドのid/displayName/args、fromId() 正常系/不明ID

### 3. RescriptCliDetectorTest.kt
- **パッケージ**: `com.rescript.plugin.run`
- **方式**: `@Rule TemporaryFolder` で一時ディレクトリ作成、`node_modules/.bin/rescript` ファイルを配置
- **テストケース**: ファイル発見、未存在時null、親ディレクトリ探索、null引数

### 4. RescriptSemanticTokensSupportTest.kt
- **パッケージ**: `com.rescript.plugin.lsp`
- **方式**: 直接インスタンス化
- **テストケース**: 全8トークンタイプ（variable/type/namespace/enumMember/property/interface/operator/modifier）のマッピング、unknownトークンでnull

### 5. RescriptCompilationStatusServiceTest.kt
- **パッケージ**: `com.rescript.plugin.lsp`
- **方式**: `RescriptTestUtils.stubProxy<Project>()` でProjectスタブ作成
- **テストケース**: 初期状態UNKNOWN、updateStatus反映、リスナー通知

### 6. RescriptSpellcheckingStrategyTest.kt
- **パッケージ**: `com.rescript.plugin.spellcheck`
- **方式**: `RescriptTestUtils.SimpleStubElement` でPsiElementスタブ作成
- **テストケース**: コメント→TEXT_TOKENIZER、文字列→TEXT_TOKENIZER、識別子→identifierTokenizer、その他→EMPTY_TOKENIZER

### 7. RescriptPsiUtilsTest.kt
- **パッケージ**: `com.rescript.plugin.lang.psi`
- **方式**: `RescriptTestUtils.stubAstNodeWithChildren` でASTノードスタブ作成、PsiElementスタブ
- **テストケース**: extractName各宣言タイプ、getIcon各宣言タイプ、getElementDescription各宣言タイプ

### 8. RescriptTypeDeclarationParserTest.kt
- **既存テスト確認**: 既に存在する場合はスキップ

### 9-12. 実行構成タイプ/ファクトリテスト
- **方式**: 直接インスタンス化
- **テストケース**: ID定数値、displayName、optionsClass

## ファイル配置

```
src/test/kotlin/com/rescript/plugin/
├── RescriptTestUtils.kt (新規 — スタブユーティリティ)
├── refactor/RescriptNamesValidatorTest.kt
├── run/RescriptCommandTest.kt
├── run/RescriptCliDetectorTest.kt
├── run/RescriptRunConfigurationTypeTest.kt
├── run/RescriptConfigurationFactoryTest.kt
├── lsp/RescriptSemanticTokensSupportTest.kt
├── lsp/RescriptCompilationStatusServiceTest.kt
├── spellcheck/RescriptSpellcheckingStrategyTest.kt
├── lang/psi/RescriptPsiUtilsTest.kt
├── test/RescriptTestRunConfigurationTypeTest.kt
└── test/RescriptTestConfigurationFactoryTest.kt
```

## 依存関係

- RescriptTestUtils.kt が他の全テストの前提（スタブユーティリティ提供）
- 各テストファイルは互いに独立
