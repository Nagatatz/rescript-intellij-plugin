# P3 Batch Tier 3 — Design

## ブランチ戦略

```
main
 └── feature/p3-batch-tier3
      ├── feature/test-runner
      ├── feature/js-preview
      ├── feature/unused-code
      └── feature/module-hierarchy
```

## Feature 1: Test Runner Integration

### アーキテクチャ
- `SMTRunnerConsoleProperties` でテストツリー UI を表示
- `SMTestLocator` で compiled JS → .res パス変換
- `ConfigurationTypeBase` + `ConfigurationFactory` でテスト実行構成を提供
- `LazyRunConfigurationProducer` でコンテキストからの自動構成

### 主要クラス
- `RescriptTestFrameworkDetector`: package.json から jest/vitest を検出
- `RescriptTestRunConfiguration`: テスト実行構成（GeneralCommandLine + SMTRunner）
- `RescriptTestLocator`: compiled JS パス → .res ソースファイル解決

## Feature 2: Compiled JS Preview

### アーキテクチャ
- `ToolWindowFactory` でツールウィンドウを登録
- `FileEditorManagerListener` でアクティブファイル変更を監視
- `EditorFactory.createEditor()` で読み取り専用 JS エディタを表示
- `RescriptCompilationStatusService` リスナーでコンパイル成功時にリフレッシュ

### 主要クラス
- `RescriptCompiledJsPreviewToolWindowFactory`: ツールウィンドウファクトリ
- `RescriptCompiledJsPreviewPanel`: プレビューパネル（エディタ + ツールバー）

## Feature 3: Unused Code Detection

### アーキテクチャ
- 既存 `RescriptReanalyzeAnnotator` の `ReanalyzeDiagnostic` に `name` フィールド追加
- `apply()` で Quick Fix を annotations に付与
- `GlobalInspectionTool` でプロジェクト全体分析

### 主要クラス
- `RescriptReanalyzeQuickFix`: _ プレフィックス付与 / 未使用コード削除
- `RescriptUnusedCodeInspection`: GlobalInspectionTool（reanalyze -json 実行）

## Feature 4: Module Hierarchy

### アーキテクチャ
- `HierarchyProvider` で階層ビューのエントリポイント
- `HierarchyBrowser` / `HierarchyTreeStructure` / `HierarchyNodeDescriptor` でツリー表示
- PSI から open/include 参照を抽出する純粋ユーティリティ

### 主要クラス
- `RescriptDependencyAnalyzer`: モジュール参照抽出・ツリー構築
- `RescriptModuleHierarchyProvider`: 階層ビューエントリポイント
- `RescriptModuleHierarchyBrowser`: 2つの階層ビュー（Nesting / Dependencies）
