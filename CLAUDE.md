# CLAUDE.md

## プロジェクト概要

ReScript 言語サポートを JetBrains IDE に提供する IntelliJ プラグイン。JFlex レクサーによるシンタックスハイライトと、rescript-language-server (LSP) による意味解析のハイブリッドアーキテクチャを採用。

- 言語: Kotlin + JFlex (Java 生成)
- ビルドシステム: Gradle (Kotlin DSL)
- 対象プラットフォーム: IntelliJ Platform 2025.3+
- JDK: 21+

## ビルド・実行コマンド

```bash
# ビルド
./gradlew buildPlugin

# クリーンビルド
./gradlew clean buildPlugin

# 開発用 IDE インスタンス起動
./gradlew runIde

# UI テスト用 IDE 起動（Remote-Robot サーバー付き、ポート 8082）
./gradlew runIdeForUiTests

# UI テスト実行（別ターミナルで、IDE 起動後に実行）
./gradlew uiTest
```

JFlex レクサー (`RescriptFlexLexer.java`) は `generateRescriptLexer` タスクで自動生成される（`compileJava` / `compileKotlin` の依存タスク）。生成ファイルは `.gitignore` に含まれており、手動生成は不要。

## CI/CD

GitHub Actions で 3 つのワークフローを運用:

| ワークフロー | ファイル | トリガー | 内容 |
|-------------|---------|---------|------|
| CI | `ci.yml` | Push/PR to `main` | ビルド、テスト、ktlint、カバレッジ、プラグイン検証 |
| Release | `release.yml` | Tag `v*.*.*` | GitHub Release 作成 |
| Docs | `docs.yml` | Push/PR to `main` (`sphinx-docs/` 変更時) | Sphinx ドキュメントのビルド・デプロイ |

```bash
# ローカルで CI を再現
./gradlew ktlintCheck buildPlugin test koverHtmlReport verifyPluginStructure

# テスト + カバレッジ
./gradlew test koverHtmlReport
# レポート: build/reports/kover/html/index.html

# ドキュメント（sphinx-docs/ 内で実行）
cd sphinx-docs && uv sync && make build-all && make serve
```

## プロジェクト構成

@docs/repository-structure.md

## アーキテクチャ

### レイヤー 1: 言語基盤 (プラグイン内蔵)
- **JFlex レクサー** (`Rescript.flex`) — トークン分解、シンタックスハイライト
- **軽量パーサー** (`RescriptParser.kt`) — トップレベル宣言 (`let`, `type`, `module`, `external`, `open`, `include`, `exception`) と JSX 構造 (`JSX_ELEMENT`, `JSX_SELF_CLOSING_ELEMENT`, `JSX_FRAGMENT`) を認識
- **PSI ツリー** — コード折りたたみ、ストラクチャービュー、JSX 構造認識向け
- **PSI Stub Index** (`indexing/`) — 5種の宣言型（let, type, module, external, exception）のスタブベースインデックスによる高速シンボル検索
- **ストラクチャービュー** (`structure/`) — モジュール・関数・型宣言のツリー表示

### レイヤー 2: LSP 統合
- IntelliJ Platform の LSP API (`com.intellij.platform.lsp`) を使用
- `@rescript/language-server` を stdio 経由で起動
- 補完、診断、定義ジャンプ、ホバー、参照検索、インレイヒント、Signature Help を提供
- **セマンティックトークンハイライト** (`RescriptSemanticTokensSupport.kt`) — LSP セマンティックトークンによる高精度な色分け
- **カスタム LSP リクエスト** (`RescriptLanguageServer.kt`) — `createInterface`, `openCompiled` 等の ReScript 固有リクエスト
- **カスタム LSP 通知** (`RescriptLsp4jClient.kt`) — `rescript/compilationStatus` 通知受信
- **Code Lens** (`RescriptCodeVisionProvider.kt`) — CodeVision API 経由で関数の型注釈を表示

### レイヤー 3: IDE 統合機能

IDE 統合機能の完全なカテゴリ一覧・パッケージ対応・Extension Point 対応は以下を参照:

- 機能カテゴリ別の解説: `docs/functional-design.md`
- パッケージ構成: `docs/repository-structure.md`
- ユーザー向けサマリ: `README.md` の Features セクション

## 開発規約

- パッケージ: `com.rescript.plugin.*`
- プラグイン ID: `com.rescript.plugin`
- extension point の登録は `plugin.xml` で行う（オプション依存は `META-INF/rescript-*.xml` に分離）
- 新しい言語機能を追加する場合は、既存のファイル構成（highlight/, lang/, lsp/ 等）に従う
- レクサーにトークンを追加する場合は `Rescript.flex` と `RescriptTokenTypes.kt` の両方を更新する
- テストは `src/test/` に配置する
- **コミットは最低でも機能単位で分割する**（複数の独立した機能を1コミットにまとめることは禁止）

詳細な規約:

@.claude/rules/testing.md
@.claude/rules/code-comments.md
@.claude/rules/deprecated-api.md
@.claude/rules/git-conventions.md
@.claude/rules/steering-workflow.md
@.claude/rules/documentation.md
@.claude/rules/roadmap-format.md
@.claude/rules/definition-of-done.md
@.claude/rules/release.md

## 重要な注意事項

- `RescriptFlexLexer.java` は自動生成ファイル。直接編集せず、`Rescript.flex` を編集すること
- LSP 機能は `@rescript/language-server` が利用可能な環境でのみ動作する
- `pluginSinceBuild` は `gradle.properties` で管理（`pluginUntilBuild` は前方互換性のため意図的に未設定）
- Gradle Configuration Cache が有効化されている

## コンテキスト管理

コンパクション時は常に以下を保持すること:
- 現在の作業ブランチと worktree のパス
- 現在アクティブな `.steering/` ディレクトリのパスと `tasklist.md` の進捗
- 現在のセッション内で変更・新規作成したファイルの一覧
- 発生したビルドエラー・テスト失敗の内容

Task ツール（サブエージェント）を使用する場合、`run_in_background` は **明示的に指示された場合のみ** 使用すること。

## セキュリティ

- 外部入力（LSP サーバーレスポンス、ファイルシステムパス、JSON 設定のパース結果）はすべて検証すること
- 外部プロセスの実行には `ProcessBuilder` に明示的な引数リストを渡すこと。ユーザー入力をコマンド文字列に連結してはならない
- ユーザー向け UI 要素やエラーメッセージに絶対パスを露出させないこと
- LSP レスポンス由来のファイルパスは、ファイル操作に利用する前にサニタイズすること

## ロードマップ

@docs/product-requirements.md
