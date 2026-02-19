# Tasklist: テストカバレッジ 100% 到達

## Phase 1: バッチブランチ & Worktree 準備

- [x] 1.1 バッチブランチ `feature/test-coverage-100` を main から作成
- [x] 1.2 ステアリングドキュメントをバッチブランチにコミット
- [x] 1.3 4 つの worktree を作成
  - `../rescript-wt-unit-tests/` (test/unit-tests)
  - `../rescript-wt-inspection-analysis/` (test/inspection-analysis)
  - `../rescript-wt-editor-intention/` (test/editor-intention)
  - `../rescript-wt-navigation-misc/` (test/navigation-misc)
- [x] 1.4 window-instructions.md を作成

## Phase 2: 並列実装（4 worktree）

### Worktree 1: test/unit-tests（純粋ユニットテスト 12 ファイル）
- [ ] 2.1.1 RescriptNamesValidatorTest.kt
- [ ] 2.1.2 RescriptCommandTest.kt
- [ ] 2.1.3 RescriptCliDetectorTest.kt
- [ ] 2.1.4 RescriptSemanticTokensSupportTest.kt
- [ ] 2.1.5 RescriptCompilationStatusServiceTest.kt
- [ ] 2.1.6 RescriptSpellcheckingStrategyTest.kt
- [ ] 2.1.7 RescriptPsiUtilsTest.kt
- [ ] 2.1.8 RescriptTypeDeclarationParserTest.kt
- [ ] 2.1.9 RescriptRunConfigurationTypeTest.kt
- [ ] 2.1.10 RescriptConfigurationFactoryTest.kt
- [ ] 2.1.11 RescriptTestRunConfigurationTypeTest.kt
- [ ] 2.1.12 RescriptTestConfigurationFactoryTest.kt
- [ ] 2.1.13 ビルド確認 & コミット

### Worktree 2: test/inspection-analysis（3 新規 + 4 改善）
- [ ] 2.2.1 RescriptEmptyModuleInspectionTest.kt（新規）
- [ ] 2.2.2 RescriptDuplicateOpenInspectionTest.kt（新規）
- [ ] 2.2.3 RescriptMissingConfigInspectionTest.kt（新規）
- [ ] 2.2.4 RescriptReanalyzeAnnotatorTest.kt（改善）
- [ ] 2.2.5 RescriptReanalyzeQuickFixTest.kt（改善）
- [ ] 2.2.6 RescriptUnusedCodeInspectionTest.kt（改善）
- [ ] 2.2.7 RescriptDependencyAnalyzerTest.kt（改善）
- [ ] 2.2.8 ビルド確認 & コミット

### Worktree 3: test/editor-intention（3 新規 + 8 改善）
- [ ] 2.3.1 RescriptBreadcrumbsProviderTest.kt（新規）
- [ ] 2.3.2 RescriptStructureViewElementTest.kt（新規）
- [ ] 2.3.3 RescriptPostfixTemplateProviderTest.kt（新規）
- [ ] 2.3.4 RescriptSmartEnterProcessorTest.kt（改善）
- [ ] 2.3.5 RescriptStatementUpDownMoverTest.kt（改善）
- [ ] 2.3.6 RescriptWrapWithIntentionTest.kt（改善）
- [ ] 2.3.7 RescriptAddGenTypeIntentionTest.kt（改善）
- [ ] 2.3.8 RescriptSurroundDescriptorTest.kt（改善）
- [ ] 2.3.9 RescriptFoldingBuilderTest.kt（改善）
- [ ] 2.3.10 RescriptCustomFoldingProviderTest.kt（改善）
- [ ] 2.3.11 RescriptLineIndentProviderTest.kt（改善）
- [ ] 2.3.12 ビルド確認 & コミット

### Worktree 4: test/navigation-misc（1 新規 + 7 改善）
- [ ] 2.4.1 RescriptRawJsInjectorTest.kt（新規）
- [ ] 2.4.2 RescriptQualifiedNameProviderTest.kt（改善）
- [ ] 2.4.3 RescriptGotoRelatedProviderTest.kt（改善）
- [ ] 2.4.4 RescriptImportOptimizerTest.kt（改善）
- [ ] 2.4.5 RescriptPasteAsJsonActionTest.kt（改善）
- [ ] 2.4.6 RescriptTestFrameworkDetectorTest.kt（改善）
- [ ] 2.4.7 RescriptTestLocatorTest.kt（改善）
- [ ] 2.4.8 RescriptCompilerStatusWidgetFactoryTest.kt（改善）
- [ ] 2.4.9 ビルド確認 & コミット

## Phase 3: マージ & 最終確認

- [ ] 3.1 test/unit-tests → feature/test-coverage-100 マージ
- [ ] 3.2 test/inspection-analysis → feature/test-coverage-100 マージ
- [ ] 3.3 test/editor-intention → feature/test-coverage-100 マージ
- [ ] 3.4 test/navigation-misc → feature/test-coverage-100 マージ
- [ ] 3.5 worktree クリーンアップ
- [ ] 3.6 `./gradlew test` 全テスト通過確認
- [ ] 3.7 `./gradlew buildPlugin` 成功確認

## Phase 4: カバレッジ確認 & main マージ

- [ ] 4.1 `./gradlew koverHtmlReport` でカバレッジレポート生成
- [ ] 4.2 カバレッジ向上を確認
- [ ] 4.3 feature/test-coverage-100 → main マージ
- [ ] 4.4 バッチブランチ削除
