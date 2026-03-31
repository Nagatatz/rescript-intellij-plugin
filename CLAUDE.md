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

GitHub Actions で 4 つのワークフローを運用:

| ワークフロー | ファイル | トリガー | 内容 |
|-------------|---------|---------|------|
| CI | `ci.yml` | Push/PR to `main` | ビルド、テスト、ktlint、カバレッジ、プラグイン検証 |
| Release | `release.yml` | Tag `v*.*.*` | GitHub Release 作成 |
| Docs | `docs.yml` | Push/PR to `main` (`sphinx-docs/` 変更時) | Sphinx ドキュメントのビルド・デプロイ |
| Qodana | `qodana_code_quality.yml` | Push/PR to `main` | 静的コード分析 |

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
- **実行構成** (`run/`) — rescript.json 経由の ReScript ビルド実行
- **テスト実行** (`test/`) — jest/vitest 自動検出、SMTRunner テストツリー
- **デバッグ** (`debug/`) — コンパイル済み JS のデバッグ実行
- **コードフォーマッタ** (`formatter/`) — `rescript format` CLI による外部フォーマッタ連携（`Cmd+Option+L`）
- **コードスタイル** (`codestyle/`) — インデント設定
- **カラースキーム** (`colorSchemes/`) — Darcula / Default テーマ用の専用配色
- **rescript.json アイコン** (`config/`) — 設定ファイルへの専用アイコン表示
- **ビルドステータス** (`statusbar/`) — ステータスバーにコンパイル状態表示
- **Error Lens** (`errorlens/`) — エディタ行内にインライン診断表示、型ミスマッチ構造化ヒント（Expected/Actual 型のインライン表示）
- **JS プレビュー** (`preview/`) — コンパイル済み JS のリアルタイムプレビュー
- **モジュール階層** (`hierarchy/`) — モジュール依存関係のツリー表示
- **Call Hierarchy** (`hierarchy/call/`) — 関数の呼び出し階層（Callers/Callees）ツリー表示（Ctrl+Alt+H）
- **Project View** (`projectview/`) — コンパイル済み JS のネスト表示・灰色化
- **プロジェクトウィザード** (`wizard/`) — 12 テンプレートによる新規プロジェクト作成
- **コード検査** (`inspection/`, `analysis/`) — 重複 open、空モジュール、rescript.json 欠落、reanalyze デッドコード分析（サーバーモード対応）、.resi シグネチャ同期、Suggested Refactoring 提案
- **Reanalyze サーバーモード** (`analysis/`) — `rescript-tools reanalyze-server` デーモンプロセスによるデッドコード分析高速化（ReScript >= 12.1.0、自動起動・ヘルスチェック・自動再起動）
- **リファクタリング** (`refactor/`) — LSP 経由リネーム、識別子バリデーション、Extract Variable（Ctrl+Alt+V）、Extract Function（Ctrl+Alt+M）、Inline Variable/Function（Ctrl+Alt+N）、Introduce Constant、Change Signature（Ctrl+F6）、React コンポーネント抽出
- **Import 最適化** (`imports/`) — 重複・未使用 open の自動削除
- **Intention Actions** (`intention/`) — Wrap with Some/Ok/Error、@genType 追加、ドキュメントコメント生成、->ignore 追加、_ プレフィックス追加、冗長ブレース削除、識別子ケース修正、ラベル付き引数挿入、Switch ケース統合、Case Split、位置→ラベル引数変換、括弧削除、修飾子削除、Pipe⇔関数呼び出し変換、インターフェース公開/非公開、分割代入の展開
- **Quick Fix** (`quickfix/`) — 未解決参照の open 追加/修飾子付加、使用箇所からの関数生成、型ホール (`_`) 候補型提案
- **Surround With** (`surround/`) — if/switch/try/block で囲む
- **Postfix Completion** (`completion/`) — .switch, .pipe, .log, .promise, .await 等
- **Completion Weigher** (`completion/`) — コンテキストベースの補完候補重み付け
- **Completion Confidence** (`completion/`) — コメント・文字列内の補完ポップアップ抑制
- **Live Template コンテキスト** (`completion/`) — ReScript 専用コンテキスト + moduleName/componentName マクロ
- **コード折りたたみ** (`folding/`) — ブロック折りたたみ、//#region カスタム折りたたみ
- **パンくずリスト** (`breadcrumb/`) — エディタ上部のナビゲーション
- **ナビゲーションバー** (`navbar/`) — Structure View ベースのナビゲーションバー表示
- **Generate アクション** (`generate/`) — Switch Arms / Module Type / Make 関数 / Record Value / JSON エンコーダ・デコーダ / モジュールタイプ実装生成
- **.d.ts バインディング生成** (`binding/`) — TypeScript 型定義から ReScript バインディングを自動生成
- **Unwrap/Remove** (`editor/`) — Some/Ok/Error/if/switch/try/ブレースの除去 (Ctrl+Shift+Delete)
- **JSX 閉じタグ自動挿入** (`editor/`) — `>` 入力時に閉じタグを自動補完
- **Context Info** (`editor/`) — スクロール時にトップレベル宣言のヘッダーを固定表示
- **Go to Implementation** (`navigation/`) — .resi → .res 実装ジャンプ (Ctrl+Alt+B)
- **Search Everywhere** (`navigation/`) — Shift+Shift でファイル・シンボルの統合検索
- **Go to Test** (`navigation/`) — 実装⇔テストファイル間のナビゲーション・新規テスト作成 (Ctrl+Shift+T)
- **Find Usages** (`lang/`) — WordsScanner によるシンボルインデキシング + 使用箇所検索
- **バンドル辞書** (`spellcheck/`) — ReScript 固有用語のスペルチェック辞書
- **テストファイル認識** (`test/`) — `*_test.res`、`*.test.res`、`__tests__/` の自動認識
- **Project View ネスト** (`projectview/`) — `.resi` を対応する `.res` の下にネスト表示
- **コメンター** (`commenter/`) — 行コメント (`//`) / ブロックコメント (`/* */`) の Commenter 実装
- **パッケージ依存関係** (`dependencies/`) — rescript.json の npm 依存関係ツリー表示
- **Code Vision** (`codevision/`) — CodeVision API 経由で関数の型注釈表示
- **Enter Handler** (`editor/`) — ドキュメントコメント・行コメントの自動継続
- **Join Lines** (`editor/`) — let/pipe/arrow のスマート行結合
- **Word Selection** (`editor/`) — 文字列・括弧・コメントの選択拡大/縮小
- **Highlight Usages** (`highlight/`) — switch/if/try 等の対応キーワードハイライト
- **Goto Super** (`navigation/`) — .res → .resi 宣言ジャンプ (Ctrl+U)
- **External Documentation** (`documentation/`) — Belt/Js モジュールの外部ドキュメント URL (Shift+F1)
- **Run Anything** (`run/`) — Ctrl+Ctrl で ReScript CLI コマンド実行
- **Expression Type** (`lsp/`) — カーソル位置の式の型を LSP hover で表示 (Ctrl+Shift+P)
- **パイプチェーン型ヒント** (`lsp/`) — `->` パイプチェーンの中間型をインライン表示
- **Parameter Info Handler** (`completion/`) — Ctrl+P でラベル付き引数をネイティブ UI で表示
- **GitHub エラーレポート** — 未処理例外の GitHub Issues 自動レポート（`RescriptErrorReporter`）
- **Problem Highlight Filter** (`analysis/`) — node_modules 等のハイライト抑制
- **Format Check** (`analysis/`) — 未フォーマットコードの検出と Quick Fix によるフォーマット実行（設定で ON/OFF）
- **Type Info ToolWindow** (`typeinfo/`) — カーソル位置の式の型を常時表示するツールウィンドウ（LSP hover + debounce）
- **`%re()` RegExp インジェクション** (`injection/`) — `%re("/pattern/flags")` 内の正規表現にRegExp言語インジェクション
- **Framework Detector** (`config/`) — `rescript.json` によるReScript フレームワークの自動検出
- **Code Rearranger** (`codestyle/`) — トップレベル宣言の自動並べ替え（open/include → type → exception → module → external → let）
- **変更可能性の診断** (`inspection/`) — 不要な `ref` 使用の検出と Quick Fix による除去
- **スタイルリンティング** (`inspection/`) — 冗長ブール式・Belt API・ブール switch パターンの検出と改善提案
- **filter+map チェーン変換** (`intention/`) — `filter+map` チェーンを `filterMap` に変換 (Alt+Enter)
- **型注釈追加** (`intention/`) — LSP hover 情報を用いた let 束縛への型注釈挿入 (Alt+Enter)
- **PPX 可視化** (`lsp/`) — PPX アノテーションの効果をインレイヒントで表示
- **型ミスマッチ差分表示** (`errorlens/`) — 型エラーの不一致部分を色分けして差分表示
- **Strip Trailing Spaces Filter** (`editor/`) — 文字列リテラル内の空白を保護しつつ行末空白を除去
- **Injected Language Formatting** (`formatter/`) — インジェクトされた言語フラグメントのフォーマット対応
- **Grazie Text Extractor** (`grazie/`) — コメント・文字列からの自然言語テキスト抽出（Grazie 連携）
- **Element Signature Provider** (`navigation/`) — 折りたたみ状態の永続化のための要素シグネチャ
- **Index Pattern Builder** (`indexing/`) — コメント内 TODO/FIXME パターンのインデックス構築
- **File Include Provider** (`navigation/`) — open 文からのファイルインクルードナビゲーション
- **Floating Toolbar** (`editor/`) — ReScript ファイル用フローティングツールバー（Format/Open JS/Create Interface）
- **Scratch File** (`scratch/`) — ReScript スクラッチファイルの作成・実行
- **REPL** (`repl/`) — ReScript インタラクティブ実行環境ツールウィンドウ
- **JS/TS→ReScript 変換** (`paste/`) — JavaScript/TypeScript コードを ReScript に変換してペースト（型注釈除去、JSX パターン変換対応）
- **依存関係ダイアグラム** (`diagram/`) — モジュール依存関係のグラフ可視化
- **PPX 展開ビュー** (`ppx/`) — PPX マクロの展開結果をツールウィンドウに表示
- **コメント内コード評価** (`editor/`) — ドキュメントコメント内のコード例を評価・検証
- **Worksheet モード** (`worksheet/`) — `.resw` ファイル全体をインタラクティブに評価
- **型シグネチャ検索** (`navigation/`) — 型シグネチャから関数を逆引き検索（Search Everywhere 統合）
- **Restart LSP アクション** (`lsp/`) — Tools メニューから LSP サーバーを明示的に再起動
- **Dump LSP State** (`lsp/`) — LSP サーバーの内部状態をデバッグ出力するアクション（Tools メニュー）
- **LSP 初期化オプション** (`settings/`, `lsp/`) — signatureHelp/cache/inlayHints/compileStatus の6設定を LSP に送信
- **ビルド自動開始プロンプト** (`run/`) — プロジェクト起動時に `rescript build -w` の開始をバルーン通知で提案
- **offset↔position 変換ユーティリティ** (`util/`) — LSP Position とエディタ offset の相互変換共通化
- **共通 Regex パターン** (`util/`) — LIDENT/UIDENT/WHITESPACE/open 文パターン等の重複 Regex を `RescriptRegexPatterns` に集約
- **プロセス実行ユーティリティ** (`util/`) — 外部コマンド実行の共通パターン（タイムアウト、stdout キャプチャ）を `RescriptProcessUtils` に集約
- **ファイルユーティリティ** (`util/`) — `.res`/`.resi` 拡張子判定・対応ファイル検索を `RescriptFileUtil` に集約

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
