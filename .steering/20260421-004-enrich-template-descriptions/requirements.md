# 要求内容: プロジェクトテンプレート説明の拡充

## 背景

Project Wizard のテンプレート選択画面（`RescriptProjectWizardStep`）右側の説明パネルには、`ProjectTemplate.description` の 1 行文字列が表示される。現状の説明は「Lightweight web server with Hono framework on Node.js, SQLite (Drizzle), and OpenAPI/Scalar UI」のような単発のサマリで、どの依存が入っているか・何が含まれるか・どの前提 (Node バージョン、PM 対応) があるかが分からない。

## 目的

テンプレート説明を拡充し、ユーザーがテンプレート選択時に以下を把握できるようにする:

- テンプレートが何を実現するか（1 行のサマリ）
- 同梱される主要ライブラリ・ツール（箇条書き 3〜5 項目）
- 必須ランタイム・前提条件（Node バージョン、対応 PM 等）

## 受け入れ条件

- [ ] 全 15 テンプレート (`ProjectTemplate` enum の全エントリ) の `description` が複数行化されている
- [ ] 各説明は「1 行サマリ + 箇条書き (主要依存・主要機能)」の構造を持つ
- [ ] 各 `*TemplateFiles.kt` の KDoc / 実際の `generate` 内容と矛盾がない
- [ ] `ProjectTemplateTest` の `description.isNotBlank()` チェックが通る
- [ ] `./gradlew ktlintCheck clean buildPlugin test` が成功する

## スコープ外

- ウィザード UI レイアウトの変更（JTextArea は既に wordWrap 済み）
- 説明文の国際化
- テンプレート機能そのものの追加・変更
