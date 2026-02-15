# Requirements: CLAUDE.md / README.md の更新

## 概要

CLAUDE.md と README.md の内容が現在のプロジェクト実態と乖離しているため、最新の状態に合わせて更新する。

## 変更対象

### CLAUDE.md

1. **プロジェクト構成図の更新** — 未記載の 4 パッケージと複数ファイルを追加
   - `codestyle/` (RescriptCodeStyleSettingsProvider.kt, RescriptLineIndentProvider.kt)
   - `config/` (RescriptJsonIconProvider.kt)
   - `run/` (7ファイル: RescriptRunConfigurationType 等)
   - `structure/` (3ファイル: RescriptStructureViewFactory 等)
   - `highlight/RescriptColorSettingsPage.kt`
   - `lsp/RescriptSemanticTokensSupport.kt`
   - `resources/colorSchemes/` ディレクトリ

2. **アーキテクチャ説明の更新** — 新機能（セマンティックハイライト、ストラクチャービュー、実行構成、コードスタイル等）を追記

### README.md

1. **対象プラットフォームバージョンの修正** — `2024.2+` → `2025.3+`
2. **機能一覧の更新** — ストラクチャービュー、実行構成、セマンティックハイライト、コードスタイル、rescript.json アイコン、カラースキームを追記
3. **Lexer 生成手順の更新** — Grammar-Kit 手動手順を Gradle タスク自動生成の説明に変更

## 受け入れ条件

- CLAUDE.md のプロジェクト構成図が実際のファイル構成と一致すること
- CLAUDE.md のアーキテクチャ説明が現在の機能を網羅していること
- README.md の対象プラットフォームが gradle.properties と一致すること
- README.md の機能一覧が plugin.xml の登録内容を反映していること
- README.md のビルド手順が現在の開発フローと一致すること

## 制約事項

- .md ファイルのみの変更（コード変更なし）
- 既存の文体・構成スタイルを維持する
