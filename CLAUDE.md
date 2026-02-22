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
```

JFlex レクサー (`RescriptFlexLexer.java`) は `generateRescriptLexer` タスクで自動生成される（`compileJava` / `compileKotlin` の依存タスク）。生成ファイルは `.gitignore` に含まれており、手動生成は不要。

## プロジェクト構成

@docs/repository-structure.md

## アーキテクチャ

### レイヤー 1: 言語基盤 (プラグイン内蔵)
- **JFlex レクサー** (`Rescript.flex`) — トークン分解、シンタックスハイライト
- **軽量パーサー** (`RescriptParser.kt`) — トップレベル宣言 (`let`, `type`, `module`, `external`, `open`, `include`, `exception`) と JSX 構造 (`JSX_ELEMENT`, `JSX_SELF_CLOSING_ELEMENT`, `JSX_FRAGMENT`) を認識
- **PSI ツリー** — コード折りたたみ、ストラクチャービュー、JSX 構造認識向け
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
- **Project View** (`projectview/`) — コンパイル済み JS のネスト表示・灰色化
- **プロジェクトウィザード** (`wizard/`) — 12 テンプレートによる新規プロジェクト作成
- **コード検査** (`inspection/`, `analysis/`) — 重複 open、空モジュール、rescript.json 欠落、reanalyze デッドコード分析、.resi シグネチャ同期
- **リファクタリング** (`refactor/`) — LSP 経由リネーム、識別子バリデーション
- **Import 最適化** (`imports/`) — 重複・未使用 open の自動削除
- **Intention Actions** (`intention/`) — Wrap with Some/Ok/Error、@genType 追加、ドキュメントコメント生成、->ignore 追加、_ プレフィックス追加、冗長ブレース削除、識別子ケース修正、ラベル付き引数挿入、Switch ケース統合、Case Split、位置→ラベル引数変換、括弧削除、修飾子削除、Pipe⇔関数呼び出し変換、インターフェース公開/非公開
- **Quick Fix** (`quickfix/`) — 未解決参照の open 追加/修飾子付加、使用箇所からの関数生成
- **Surround With** (`surround/`) — if/switch/try/block で囲む
- **Postfix Completion** (`completion/`) — .switch, .pipe, .log, .promise, .await 等
- **Completion Weigher** (`completion/`) — コンテキストベースの補完候補重み付け
- **Completion Confidence** (`completion/`) — コメント・文字列内の補完ポップアップ抑制
- **Live Template コンテキスト** (`completion/`) — ReScript 専用コンテキスト + moduleName/componentName マクロ
- **コード折りたたみ** (`folding/`) — ブロック折りたたみ、//#region カスタム折りたたみ
- **パンくずリスト** (`breadcrumb/`) — エディタ上部のナビゲーション
- **Generate アクション** (`generate/`) — Switch Arms / Module Type / Make 関数 / JSON エンコーダ・デコーダ生成
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
