# P3 Batch Tier 3 — Task List

## セットアップ
- [x] ステアリングドキュメント作成
- [ ] バッチブランチ `feature/p3-batch-tier3` 作成

## Feature 1: Test Runner Integration
- [ ] `RescriptTestFrameworkDetector.kt` 実装
- [ ] `RescriptTestRunConfigurationType.kt` 実装
- [ ] `RescriptTestRunConfigurationOptions.kt` 実装
- [ ] `RescriptTestConfigurationFactory.kt` 実装
- [ ] `RescriptTestRunConfiguration.kt` 実装
- [ ] `RescriptTestSettingsEditor.kt` 実装（テスト省略: Swing UI）
- [ ] `RescriptTestConsoleProperties.kt` 実装
- [ ] `RescriptTestLocator.kt` 実装
- [ ] `RescriptTestConfigurationProducer.kt` 実装（テスト省略: IDE コンテキスト必須）
- [ ] plugin.xml に extension point 登録
- [ ] `RescriptTestFrameworkDetectorTest.kt` テスト作成
- [ ] `RescriptTestLocatorTest.kt` テスト作成
- [ ] ビルド確認・コミット

## Feature 2: Compiled JS Preview
- [ ] `RescriptCompiledJsPreviewToolWindowFactory.kt` 実装
- [ ] `RescriptCompiledJsPreviewPanel.kt` 実装（テスト省略: Swing + EditorEx）
- [ ] plugin.xml に extension point 登録
- [ ] `RescriptCompiledJsPreviewToolWindowFactoryTest.kt` テスト作成
- [ ] ビルド確認・コミット

## Feature 3: Unused Code Detection
- [ ] `RescriptReanalyzeAnnotator.kt` 拡張（name フィールド + Quick Fix）
- [ ] `RescriptReanalyzeQuickFix.kt` 実装
- [ ] `RescriptUnusedCodeInspection.kt` 実装
- [ ] plugin.xml に extension point 登録
- [ ] `RescriptReanalyzeQuickFixTest.kt` テスト作成
- [ ] `RescriptUnusedCodeInspectionTest.kt` テスト作成
- [ ] ビルド確認・コミット

## Feature 4: Module Hierarchy
- [ ] `RescriptDependencyAnalyzer.kt` 実装
- [ ] `RescriptModuleHierarchyProvider.kt` 実装
- [ ] `RescriptModuleHierarchyBrowser.kt` 実装（テスト省略: UI）
- [ ] `RescriptModuleHierarchyTreeStructure.kt` 実装（テスト省略: IDE ツリー）
- [ ] `RescriptModuleHierarchyNodeDescriptor.kt` 実装（テスト省略: UI）
- [ ] plugin.xml に extension point 登録
- [ ] `RescriptDependencyAnalyzerTest.kt` テスト作成
- [ ] ビルド確認・コミット

## マージ・仕上げ
- [ ] 全機能ブランチをバッチブランチにマージ
- [ ] 最終ビルド確認
- [ ] 共有ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [ ] main にマージ
