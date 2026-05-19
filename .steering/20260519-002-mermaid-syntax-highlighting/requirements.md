# Mermaid Syntax Highlighting — 要求内容

## 背景

機能発掘調査のバケット D の 1 件 (旧バケット C) として、Variant Flow と Module Dependency の Source モード (Mermaid `flowchart TD` テキスト表示) に Mermaid 構文ベースのハイライトを追加する。現状は `JTextArea` での plain text 表示。

## スコープ

2 件のパネル + 1 件の共通 colorizer + ドキュメント = 3 コミット。

### 機能 1: 共通 Mermaid colorizer ヘルパー (`flow/MermaidSourceColorizer.kt`)

- 入力: Mermaid `flowchart TD` ソーステキスト
- 出力: HTML 文字列 (JEditorPane 用) — 各 Mermaid トークンに色付き `<span>` を付与
- 認識する Mermaid 要素:
  - キーワード: `flowchart`, `graph`, `TD`, `LR`, `subgraph`, `end`
  - アロー: `-->`, `---`, `-.->`, `==>`
  - 引用文字列: `["..."]` `["..."]` 内のラベル
  - コメント: `%%` で始まる行
  - ノード ID: 単独の identifier-like トークン
- 色のソース: `EditorColorsManager.getInstance().globalScheme.getAttributes(key)` 経由で:
  - キーワード → `RescriptSyntaxHighlighter.KEYWORD`
  - アロー → `RescriptSyntaxHighlighter.OPERATOR`
  - 引用文字列 → `RescriptSyntaxHighlighter.STRING`
  - コメント → `RescriptSyntaxHighlighter.LINE_COMMENT`
- 純関数 `render(source: String): String` でテスト可能

### 機能 2: Variant Flow Source モード (`flow/RescriptVariantFlowPanel.kt`)

- 現状: `JTextArea` で Mermaid `flowchart TD` ソースを plain text 表示
- 変更: `JTextArea` → `JEditorPane` (HTML)、`MermaidSourceColorizer.render()` 経由

### 機能 3: Module Dependency Source モード (`diagram/RescriptDependencyDiagramPanel.kt`)

- 同上のパターン

### 機能 4: ドキュメント同期

- `CLAUDE.md` の `flow/` `diagram/` 段落に Mermaid colorizer 言及追加
- `README.md` Features 該当行
- `docs/repository-structure.md` の `flow/` 行に `MermaidSourceColorizer` を追加
- `sphinx-docs/user/features/advanced.md` の該当セクションに Mermaid 色付け説明
- `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` 同期

## 受け入れ条件

- Variant Flow と Module Dependency の Source モードで Mermaid のキーワード・アロー・引用文字列・コメントが色分けされる
- 色付けロジックは `MermaidSourceColorizerTest` で構造的に検証 (HTML タグの存在を assert)
- 既存テスト緑、`./gradlew ktlintCheck buildPlugin test koverHtmlReport koverVerify verifyPluginStructure` 全緑

## 制約

- 新規 JFlex lexer は作成しない (v1 では line-based 正規表現で十分)
- 色値の具体的な assertion はテスト対象外 (ColorScheme 依存)
- Copy Mermaid アクションが返す文字列に HTML タグが混入してはならない (UI 描画と export 経路は分離)
