# 要求定義: Dependency Diagram の Extension Point 登録

## 背景

`docs/20260427-006-docs-audit` の整合性監査で、`diagram/` パッケージ（`RescriptDependencyDiagramProvider` / `RescriptDependencyDiagramModel`）が `plugin.xml` および全 `META-INF/rescript-*.xml` に登録されておらず、ユーザーから利用不能な状態であることが判明した。

一方で以下のドキュメントは「実装済み機能」として記載しており、ユーザーがドキュメント通り操作しても動作しない:

- `docs/product-requirements.md:280-287` US-15（受け入れ条件 3 項目すべて `[x]`）
- `docs/repository-structure.md:78` `diagram/` パッケージ説明
- `README.md:74` Features `Dependency diagram — Visualize module dependency graph`
- `sphinx-docs/user/features/advanced.md:927-952` 完全な機能説明 + UI パスへの誘導
- `sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po` 翻訳済み

## ゴール

ドキュメントと実装の乖離を解消するため、**実装側を完成させて Extension Point として登録する**。

## ユーザーストーリー（US-15 を再確認）

**規模が大きくなったプロジェクトを保守する開発者として**、モジュール間の依存関係をグラフで俯瞰することで、循環依存や責務の集中を発見したい。

## 受け入れ条件

PRD に既に記載の 3 項目を実証可能な実装として満たす:

- [ ] `ReScript Dependencies` または専用 ToolWindow から依存関係ダイアグラムを開ける
- [ ] モジュール依存関係をダイアグラム（視覚化された形式）で確認できる
- [ ] DOT 形式エクスポート時に外部ツール（graphviz など）に渡せる安全な出力が得られる

加えて、以下を満たす:

- [ ] **Community / Ultimate 両対応**（CLAUDE.md「全 JetBrains IDE 対応」方針に従う）
- [ ] 既存の `RescriptDependenciesToolWindowFactory`（パッケージ依存ツリー）との混同を避ける ID/命名
- [ ] 既存テスト（`RescriptDependencyDiagramProviderTest` / `RescriptDependencyDiagramModelTest`）はそのまま通過する

## スコープ外

- IntelliJ UML プラグイン (`com.intellij.diagram`) を必須依存にすること（Community 非対応のため）
- 高度なグラフレイアウト機能（インタラクティブドラッグ、ズーム等）— 最小実装で受け入れ条件を満たす範囲
- 大規模プロジェクト（>1000 モジュール）でのパフォーマンス保証

## 制約

- `pluginSinceBuild = 253.0`（IntelliJ 2025.3+）の API のみ使用
- 既存の deprecated API ルール (`.claude/rules/deprecated-api.md`) に従う
- セキュリティ規約（外部入力検証、絶対パス露出禁止）に従う
- DOT エクスポート時のモジュール名エスケープは既存の `escapeDot()` 実装を維持
