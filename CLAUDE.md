# CLAUDE.md

## 強制的な行動指示

本ファイルおよび `@import` で読み込まれるルール、`.claude/skills/` 配下のスキル本文に書かれている規約は **すべて強制** であり、ユーザーから明示的に解除されない限り例外なく従うこと。違反した場合は即座に修正する。

## プロジェクト概要

ReScript 言語サポートを JetBrains IDE に提供する IntelliJ プラグイン。JFlex レクサーによるシンタックスハイライトと、rescript-language-server (LSP) による意味解析のハイブリッドアーキテクチャを採用。

- 言語: Kotlin + JFlex (Java 生成)
- ビルドシステム: Gradle (Kotlin DSL)
- 対象プラットフォーム: IntelliJ Platform 2026.1.4+（ビルドは 2026.2.0.1 に対して行う）
- JDK: 25+（2026.2 のバイトコードが Java 25 のため。JDK 21 では javac が読めない）

## ビルド・実行コマンド

```bash
# ビルド / クリーンビルド
./gradlew buildPlugin
./gradlew clean buildPlugin

# 開発用 IDE インスタンス起動（古い jar は prepareSandbox 時に自動除去。完全クリーンは clean runIde）
./gradlew runIde

# UI テスト（runIdeForUiTests で IDE 起動 → 別ターミナルで uiTest、Remote-Robot ポート 8082）
./gradlew runIdeForUiTests
./gradlew uiTest

# テストスイートの切り出し（PR フィードバック高速化用）
./gradlew test -Pscope=fast    # perf / *IntegrationTest / cli/ を除外した unit test 集合
./gradlew test -Pscope=perf    # perf/ 配下のスモークベンチマークのみ
./gradlew test -Pscope=cli     # cli/ 配下の mmdc / dot 結合テストのみ（CLI 不在時は skip）

# ローカルで CI を再現 / テスト + カバレッジ（build/reports/kover/html/index.html）
./gradlew ktlintCheck buildPlugin test koverHtmlReport verifyPluginStructure

# ドキュメント（sphinx-docs/ 内で実行）
cd sphinx-docs && uv sync && make build-all && make serve
```

JFlex レクサー (`RescriptFlexLexer.java`) は `generateRescriptLexer` タスクで自動生成される（`.gitignore` 対象・手動生成不要・直接編集禁止）。`Rescript.flex` を編集すること。

CI/CD は GitHub Actions で 10 ワークフロー（CI / Release / Docs / CodeQL / Integration Tests / OS Matrix / Monthly Verify / IntelliJ Platform Watch / Template Versions Outdated / Dependabot Discord Notify）を運用。詳細は `.github/workflows/` と `docs/repository-structure.md` を参照。

## アーキテクチャ

3 レイヤーのハイブリッド構成。完全な機能カタログ・パッケージ対応・Extension Point マップは必要時に以下を参照する（常時ロードしない）:

- パッケージ構成と各機能の責務: `docs/repository-structure.md`
- 機能カテゴリ別の設計解説と EP マップ: `docs/functional-design.md`
- ユーザー向け機能サマリ: `README.md` の Features セクション
- LSP 非接続時の機能別フォールバック: `docs/lsp-fallback-matrix.md`

- **レイヤー 1: 言語基盤（内蔵）** — JFlex レクサー (`Rescript.flex`)、軽量パーサー (`RescriptParser.kt`、トップレベル宣言 + JSX 構造のみ認識)、PSI ツリー、PSI Stub Index (`indexing/`)、ストラクチャービュー (`structure/`)。
- **レイヤー 2: LSP 統合** — IntelliJ Platform LSP API 経由で `@rescript/language-server` を stdio 起動。補完・診断・定義ジャンプ・ホバー・参照検索・インレイヒント・Signature Help・セマンティックトークン。モノレポ対応は `RescriptWorkspaceDiscovery`。カスタムリクエスト/通知は `RescriptLanguageServer.kt` / `RescriptLsp4jClient.kt`。
- **レイヤー 3: IDE 統合機能** — Intention / Inspection / 補完 / ナビゲーション / 各種 ToolWindow（Variant Flow・Module Dependency Diagram・Type Impact・Notebook・Interop Risk・Type Coverage 等）/ Project Wizard（22 テンプレート）。個々の機能の実装詳細は `docs/repository-structure.md` のパッケージ表を参照。

## 開発規約

- パッケージ: `com.rescript.plugin.*` / プラグイン ID: `com.rescript.plugin`
- Extension Point の登録は `plugin.xml`（オプション依存は `META-INF/rescript-*.xml` に分離）
- 新機能は既存のファイル構成（`highlight/`, `lang/`, `lsp/` 等）に従う
- レクサーにトークンを追加する場合は `Rescript.flex` と `RescriptTokenTypes.kt` の両方を更新する
- テストは `src/test/` に配置する
- **コミットは最低でも機能単位で分割する**（複数の独立した機能を 1 コミットにまとめることは禁止）

### 常時適用される規約 (rules)

以下は全セッションで `@import` され常に適用される（実装中に継続的に効く規約のみ）。

@.claude/rules/testing.md
@.claude/rules/code-comments.md
@.claude/rules/deprecated-api.md
@.claude/rules/git-conventions.md

### フェーズ起動時に必ず Read する規約（遅延ロード）

以下はワークフローの**特定フェーズでのみ**必要なため、常時 `@import` せず該当フェーズの直前に必ず Read すること。トリガーは強制であり、詳細チェックリストの本文は都度参照する（常時ロードのコンテキスト肥大を防ぐため）。

| トリガー | 必ず Read する規約 |
|---------|------------------|
| **コード変更を伴う指示を受けたら、1 行も書く前に** | `.claude/rules/steering-workflow.md`（`.steering/` 作成・requirements/design/tasklist 承認・worktree） |
| **コミット / マージの直前** | `.claude/rules/definition-of-done.md`（Phase 1〜5 の全チェック索引。`definition-of-done-check` スキルでも代替可） |

### 状況依存で参照する規約・スキル

以下は常時ロードせず、該当作業時に参照する（`.claude/rules/` を Read、または対応スキルが自動発火する）。索引は `.claude/rules/README.md`。

| 領域 | 参照先 |
|------|--------|
| ドキュメント同期・日本語訳 | `.claude/rules/documentation.md` / `docs-lint` / `sphinx-po-ja-sync` スキル |
| ロードマップ表のフォーマット | `.claude/rules/roadmap-format.md` |
| リリース手順 | `.claude/rules/release.md` / `intellij-release-flow` スキル |
| 図表作成 | `.claude/rules/diagram-rules.md` |
| GitHub Actions ピン留め | `.claude/rules/github-actions-pinning.md` |
| audit / カバレッジ調査の二段検証 | `.claude/rules/audit-tasks.md` |
| 英語/日本語の使い分け | `.claude/rules/language.md` |
| コンテキスト管理・自動化レシピ | `.claude/rules/context-management.md` / `automation-playbooks.md` |

## 重要な注意事項

- `RescriptFlexLexer.java` は自動生成ファイル。直接編集せず `Rescript.flex` を編集する
- LSP 機能は `@rescript/language-server` が利用可能な環境でのみ動作する
- `pluginSinceBuild` は `gradle.properties` で管理（`pluginUntilBuild` は前方互換性のため意図的に未設定）
- Gradle Configuration Cache が有効化されている
- ロードマップ（将来機能一覧）は `docs/product-requirements.md` を参照

## コンテキスト管理

コンパクション時は常に以下を保持すること:

- 現在の作業ブランチと worktree のパス
- 現在アクティブな `.steering/` ディレクトリのパスと `tasklist.md` の進捗
- 現在のセッション内で変更・新規作成したファイルの一覧
- 発生したビルドエラー・テスト失敗の内容

Task ツール（サブエージェント）使用時、`run_in_background` は **明示的に指示された場合のみ** 使用する。

### ビルド/テスト出力の抑制（コンテキスト保全）

`./gradlew buildPlugin` / `test` / `koverHtmlReport` 等は数千〜数万トークンの出力を生み、長セッションでコンテキストを急速に消費しコンパクションを早める。以下を徹底すること:

- 結果の成否だけ知りたい場合は、出力をファイルにリダイレクトし末尾のみ読む:
  `./gradlew test > /tmp/gradle-test.log 2>&1; tail -40 /tmp/gradle-test.log`
- 失敗時のみ詳細を追う。成功時は `tail` の数行で確認を終える
- 全テストではなくスコープを絞る（`./gradlew test -Pscope=fast`）
- 大量出力が予想されるコマンドは原則 `tail` / `grep` で必要箇所だけを取り込み、全文を会話に流し込まない

## セキュリティ

- 外部入力（LSP サーバーレスポンス、ファイルシステムパス、JSON 設定のパース結果）はすべて検証する
- 外部プロセスの実行には `ProcessBuilder` に明示的な引数リストを渡す。ユーザー入力をコマンド文字列に連結しない
- ユーザー向け UI 要素やエラーメッセージに絶対パスを露出させない
- LSP レスポンス由来のファイルパスは、ファイル操作前にサニタイズする
