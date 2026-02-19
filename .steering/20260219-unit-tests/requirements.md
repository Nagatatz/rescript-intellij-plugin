# Requirements: 純粋ユニットテスト追加

## 概要

IDE 依存なしの純粋ユニットテスト 12 ファイルを新規作成し、未テストクラスのカバレッジを向上させる。

## 対象クラスと要件

| # | テストファイル | 対象クラス | テスト内容 |
|---|--------------|-----------|-----------|
| 1 | RescriptNamesValidatorTest.kt | RescriptNamesValidator | isIdentifier(), isKeyword() テスト（project引数はnull渡し） |
| 2 | RescriptCommandTest.kt | RescriptCommand | fromId(), enum プロパティ, args 検証 |
| 3 | RescriptCliDetectorTest.kt | RescriptCliDetector | findCli() テスト（一時ディレクトリにnode_modules/.bin/rescript作成） |
| 4 | RescriptSemanticTokensSupportTest.kt | RescriptSemanticTokensSupport | getTextAttributesKey() 全トークンタイプマッピング |
| 5 | RescriptCompilationStatusServiceTest.kt | RescriptCompilationStatusService | updateStatus(), addListener(), 初期状態UNKNOWN（ProjectスタブはstubProxy使用） |
| 6 | RescriptSpellcheckingStrategyTest.kt | RescriptSpellcheckingStrategy | getTokenizer() トークンタイプ→トークナイザーマッピング |
| 7 | RescriptPsiUtilsTest.kt | RescriptPsiUtils | extractName(), getIcon(), getElementDescription()（stubAstNodeWithChildren使用） |
| 8 | RescriptTypeDeclarationParserTest.kt | RescriptTypeDeclarationParser | 既存テスト確認（既にテスト済みの場合はスキップ） |
| 9 | RescriptRunConfigurationTypeTest.kt | RescriptRunConfigurationType | ID, displayName 検証 |
| 10 | RescriptConfigurationFactoryTest.kt | RescriptConfigurationFactory | ID, optionsClass 検証 |
| 11 | RescriptTestRunConfigurationTypeTest.kt | RescriptTestRunConfigurationType | ID, displayName 検証 |
| 12 | RescriptTestConfigurationFactoryTest.kt | RescriptTestConfigurationFactory | ID, optionsClass 検証 |

## 制約事項

- 既存テストパターン（JUnit 4、スタブベース、RescriptTestUtils）に準拠
- IDE テストフレームワーク（BasePlatformTestCase等）は使用しない
- 全テストが `./gradlew test` で通過すること

## 受け入れ条件

- 12 テストファイルが作成されていること（既存テストはスキップ可）
- 全テストがグリーンであること
- 既存テストが壊れていないこと
