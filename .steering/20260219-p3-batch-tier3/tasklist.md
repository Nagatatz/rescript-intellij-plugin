# P3 Batch Tier 3 — Task List

## セットアップ
- [x] ステアリングドキュメント作成
- [x] バッチブランチ `feature/p3-batch-tier3` 作成

## Feature 1: Test Runner Integration
- [x] `RescriptTestFrameworkDetector.kt` 実装
- [x] `RescriptTestRunConfigurationType.kt` 実装
- [x] `RescriptTestRunConfigurationOptions.kt` 実装
- [x] `RescriptTestConfigurationFactory.kt` 実装
- [x] `RescriptTestRunConfiguration.kt` 実装
- [x] `RescriptTestSettingsEditor.kt` 実装（テスト省略: Swing UI）
- [x] `RescriptTestConsoleProperties.kt` 実装
- [x] `RescriptTestLocator.kt` 実装
- [x] `RescriptTestConfigurationProducer.kt` 実装（テスト省略: IDE コンテキスト必須）
- [x] plugin.xml に extension point 登録
- [x] `RescriptTestFrameworkDetectorTest.kt` テスト作成
- [x] `RescriptTestLocatorTest.kt` テスト作成
- [x] ビルド確認・コミット

## Feature 2: Compiled JS Preview
- [x] `RescriptCompiledJsPreviewToolWindowFactory.kt` 実装
- [x] `RescriptCompiledJsPreviewPanel.kt` 実装（テスト省略: Swing + EditorEx）
- [x] plugin.xml に extension point 登録
- [x] `RescriptCompiledJsPreviewToolWindowFactoryTest.kt` テスト作成
- [x] ビルド確認・コミット

## Feature 3: Unused Code Detection
- [x] `RescriptReanalyzeAnnotator.kt` 拡張（name フィールド + Quick Fix）
- [x] `RescriptReanalyzeQuickFix.kt` 実装
- [x] `RescriptUnusedCodeInspection.kt` 実装
- [x] plugin.xml に extension point 登録
- [x] `RescriptReanalyzeQuickFixTest.kt` テスト作成
- [x] `RescriptUnusedCodeInspectionTest.kt` テスト作成
- [x] ビルド確認・コミット

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
