# Requirements: Custom Agents

## 概要

Claude Code のカスタムサブエージェント機能を活用し、IntelliJ Plugin 開発に特化した2つの専門エージェントを定義する。

## 背景

Claude Code は `.claude/agents/` ディレクトリにマークダウンファイルを配置することで、カスタムサブエージェントを定義できる。プロジェクト固有の反復的タスクを専門エージェントとして定義することで、品質チェックやビルドエラー解決の効率化を図る。

## 機能要件

### FR-1: Code Reviewer エージェント (`code-reviewer.md`)

IntelliJ Plugin のコード品質レビューを行う読み取り専用エージェント。

- **ツール制限:** `Read`, `Glob`, `Grep` のみ（コード変更不可）
- **モデル:** `sonnet`（コスト効率重視）
- **レビュー観点:**
  - KDoc コメントの有無チェック
  - `plugin.xml` の Extension Point 登録確認
  - テストファイルの存在確認
  - `RescriptFlexLexer.java` への直接編集がないか確認
  - パッケージ構成（`com.rescript.plugin.*`）の遵守確認
- **出力形式:** マークダウン表形式でレビュー結果を出力

### FR-2: Build Resolver エージェント (`build-resolver.md`)

Gradle ビルドエラーの解析と修正提案を行うエージェント。

- **ツール制限:** `Read`, `Glob`, `Grep`, `Bash`
- **モデル:** `sonnet`（コスト効率重視）
- **機能:**
  - `./gradlew buildPlugin` のエラー出力を解析
  - Kotlin コンパイルエラー、Gradle 設定エラー、依存関係エラーを分類
  - 修正提案を具体的なコード変更として提示
  - IntelliJ Platform API の互換性問題を検出

## 非機能要件

- エージェント定義ファイルは `.claude/agents/` ディレクトリに配置
- YAML フロントマターで `allowed-tools` と `model` を指定
- プロンプト内容はプロジェクト固有の規約（CLAUDE.md）に準拠

## 制約事項

- カスタムエージェントはコード変更を直接行わない（code-reviewer は読み取り専用、build-resolver は提案のみ）
- エージェント定義はマークダウン形式のみ
