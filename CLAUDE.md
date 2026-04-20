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

IDE 統合機能は以下のカテゴリで実装されている。詳細な機能一覧・クラス対応・キーバインドは `README.md` の Features セクション、Extension Point とクラス単位の対応は `docs/functional-design.md` を参照。

- **エディタ体験** (`editor/`, `formatter/`, `highlight/`, `folding/`, `commenter/`, `breadcrumb/`, `navbar/`) — 外部フォーマッタ連携、JSX 閉じタグ自動挿入、Enter/Join Lines/Word Selection ハンドラ、対応キーワードハイライト、ブロック/`//#region` 折りたたみ、パンくずリスト、Floating Toolbar など
- **コード補完・テンプレート** (`completion/`) — Postfix / Live Template コンテキスト / Completion Weigher / Parameter Info / Completion Confidence
- **ナビゲーション** (`navigation/`, `lang/`, `hierarchy/`, `search/`) — Go to Implementation、Go to Test、Search Everywhere、Find Usages、モジュール階層、Call Hierarchy、型シグネチャ検索、File Include Provider
- **リファクタリング・Intention・Quick Fix** (`refactor/`, `intention/`, `quickfix/`, `generate/`, `surround/`, `imports/`, `binding/`) — Extract/Inline/Change Signature、Wrap/Unwrap、filter+map→filterMap、識別子/ケース変換、Generate メニュー、Surround With、Import Optimizer、.d.ts → ReScript 変換
- **分析・診断** (`analysis/`, `inspection/`, `errorlens/`, `codevision/`) — reanalyze（サーバーモード対応）、Format Check、重複 open/空モジュール/設定欠落、変更可能性・スタイルリンティング、Error Lens、Code Vision、Problem Highlight Filter
- **実行・デバッグ・プロジェクト統合** (`run/`, `debug/`, `test/`, `statusbar/`, `config/`, `projectview/`, `dependencies/`, `wizard/`) — Run Configuration、jest/vitest テスト実行、デバッグ、ビルドステータス、rescript.json アイコン、Project View ネスト、依存関係ツリー、Project Wizard（15 テンプレート — 詳細は `docs/templates.md` 参照）
- **LSP 拡張機能** (`lsp/`, `settings/`, `typeinfo/`) — Restart/Dump LSP State、Expression Type、パイプチェーン型ヒント、PPX 可視化、Type Info ToolWindow、LSP 初期化オプション。設定 UI はスキーマ駆動（`RescriptSettingsSchema` + `RescriptSettingDescriptor` + `RescriptSettingsValidator`）で、項目追加時の変更箇所を 1 箇所に集約
- **ツールウィンドウ・対話機能** (`preview/`, `repl/`, `scratch/`, `worksheet/`, `ppx/`, `diagram/`, `typeinfo/`) — JS プレビュー、REPL、Scratch File、Worksheet モード、PPX 展開ビュー、依存関係ダイアグラム
- **言語インジェクション・ペースト** (`injection/`, `paste/`) — `%raw()` JS / `%re()` RegExp / Markdown コードフェンス、Paste as JSON.t、JS/TS → ReScript 変換
- **補助機能** (`spellcheck/`, `grazie/`, `indexing/`, `documentation/`) — スペルチェック、Grazie 連携、TODO インデックス、External Documentation
- **共通ユーティリティ** (`util/`, `wizard/templates/`) — offset↔Position 変換、正規表現パターン集約、プロセス実行、ファイル/エディタヘルパー、Intention/Generate 基底クラス、Wizard テンプレートリソースローダー (`TemplateResourceLoader` で `resources/templates/` 配下の静的コンテンツと `{{key}}` プレースホルダをロード)

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

When compacting, always preserve:
- Current working branch and worktree path
- The active `.steering/` directory path and current tasklist.md progress
- List of modified/created files in the current session
- Build errors or test failures encountered

Task ツール（サブエージェント）を使用する場合、`run_in_background` は **明示的に指示された場合のみ** 使用すること。

## セキュリティ

- Validate all external inputs (LSP server responses, file system paths, JSON config parsing)
- Use ProcessBuilder with explicit argument lists for external process execution; never concatenate user input into command strings
- Never expose absolute file system paths in user-facing UI elements or error messages
- Sanitize file paths from LSP responses before using in file operations

## ロードマップ

@docs/product-requirements.md
