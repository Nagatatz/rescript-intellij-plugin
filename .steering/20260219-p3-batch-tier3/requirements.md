# P3 Batch Tier 3 — Requirements

## 概要

P3 Tier 2 完了後の残りタスクから4機能を並列実装する。

## 機能一覧

### Feature 1: Test Runner Integration
- jest/vitest の SMTRunner 統合
- テストツリー UI 表示
- package.json からのフレームワーク自動検出
- compiled JS → .res パスのソースロケーション

### Feature 2: Compiled JS Preview
- アクティブな .res ファイルに対応する compiled JS を表示するツールウィンドウ
- ファイル切り替え時に自動更新
- コンパイル完了時にリフレッシュ

### Feature 3: Unused Code Detection
- 既存の RescriptReanalyzeAnnotator を拡張
- Quick Fix（_ プレフィックス付与、未使用コード削除）
- GlobalInspectionTool によるプロジェクト全体分析

### Feature 4: Module Hierarchy
- HierarchyProvider でモジュールネスト構造を表示
- open/include 依存関係ビュー
- Module Nesting と Module Dependencies の2つの階層ビュー

## 受け入れ条件

- 各機能のビルドが成功すること
- テスト対象クラスのユニットテストが PASS すること
- plugin.xml に必要な extension point が登録されていること
