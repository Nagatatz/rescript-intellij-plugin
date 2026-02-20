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

```
src/main/
├── kotlin/com/rescript/plugin/
│   ├── RescriptLanguage.kt              # Language 定義
│   ├── RescriptFileTypes.kt             # .res / .resi FileType
│   ├── RescriptIcons.kt                 # アイコン
│   ├── lang/
│   │   ├── RescriptTokenTypes.kt        # IElementType トークン定義
│   │   ├── RescriptLexer.kt             # JFlex ラッパー (FlexAdapter)
│   │   ├── RescriptParser.kt            # 軽量パーサー (トップレベル宣言 + JSX)
│   │   ├── RescriptParserDefinition.kt  # ParserDefinition
│   │   ├── RescriptAstFactory.kt        # AST ファクトリ (文字列リテラルの言語インジェクション対応)
│   │   ├── RescriptFindUsagesProvider.kt  # Find Usages + WordsScanner
│   │   └── psi/
│   │       ├── RescriptPsi.kt           # PSI 要素クラス
│   │       ├── RescriptPsiUtils.kt      # PSI ユーティリティ (要素探索・判定)
│   │       └── RescriptStringLiteral.kt # 文字列リテラル PSI (PsiLanguageInjectionHost)
│   ├── highlight/
│   │   ├── RescriptSyntaxHighlighter.kt
│   │   ├── RescriptSyntaxHighlighterFactory.kt
│   │   ├── RescriptColorSettingsPage.kt # ハイライト色設定 UI
│   │   └── RescriptBraceMatcher.kt
│   ├── lsp/
│   │   ├── RescriptLspServerSupportProvider.kt  # LSP サーバー起動判定
│   │   ├── RescriptLspServerDescriptor.kt       # LSP サーバー設定
│   │   ├── RescriptLanguageServer.kt            # カスタム LSP リクエストインターフェース
│   │   ├── RescriptLsp4jClient.kt               # カスタム LSP 通知受信クライアント
│   │   ├── RescriptCompilationStatusService.kt  # コンパイル状態保持サービス
│   │   ├── RescriptSemanticTokensSupport.kt     # セマンティックトークン対応
│   │   ├── RescriptLspDetector.kt               # LSP・プロジェクト検出ユーティリティ
│   │   ├── RescriptPackageManagerDetector.kt    # パッケージマネージャ検出 (npm/yarn/pnpm)
│   │   ├── RescriptLspInstaller.kt              # LSP バックグラウンドインストール
│   │   └── RescriptLspStartupActivity.kt        # プロジェクト起動時 LSP インストール通知
│   ├── codestyle/
│   │   ├── RescriptCodeStyleSettingsProvider.kt  # コードスタイル設定
│   │   └── RescriptLineIndentProvider.kt         # インデント制御
│   ├── config/
│   │   ├── RescriptJsonIconProvider.kt  # rescript.json アイコン
│   │   └── RescriptJsonSchemaProviderFactory.kt  # JSON Schema 提供
│   ├── run/
│   │   ├── RescriptCliDetector.kt       # ReScript CLI 検出
│   │   ├── RescriptCommand.kt           # コマンド定義
│   │   ├── RescriptConfigurationFactory.kt
│   │   ├── RescriptConsoleFilterProvider.kt  # コンソール出力のファイルパスリンク化
│   │   ├── RescriptRunConfiguration.kt
│   │   ├── RescriptRunConfigurationOptions.kt
│   │   ├── RescriptRunConfigurationType.kt
│   │   ├── RescriptRunLineMarkerContributor.kt  # ガター実行アイコン
│   │   └── RescriptSettingsEditor.kt    # 実行構成 UI
│   ├── settings/
│   │   ├── RescriptProjectSettings.kt     # プロジェクト単位の設定永続化
│   │   └── RescriptConfigurable.kt        # Settings UI (Languages & Frameworks > ReScript)
│   ├── structure/
│   │   ├── RescriptStructureViewElement.kt
│   │   ├── RescriptStructureViewFactory.kt
│   │   └── RescriptStructureViewModel.kt
│   ├── indexing/
│   │   └── RescriptTodoIndexer.kt         # TODO インデクシング
│   ├── editor/
│   │   ├── RescriptQuoteHandler.kt        # スマート引用符補完
│   │   ├── RescriptEditorNotificationProvider.kt  # LSP 未検出時の案内バー
│   │   ├── RescriptStatementUpDownMover.kt  # 宣言の上下移動 (Alt+Shift+Up/Down)
│   │   ├── RescriptSmartEnterProcessor.kt   # Smart Enter (Shift+Enter)
│   │   ├── RescriptDeclarationRangeHandler.kt  # Context Info (スクロール時の宣言ヘッダー固定)
│   │   ├── RescriptUnwrapDescriptor.kt      # Unwrap/Remove (Ctrl+Shift+Delete)
│   │   └── RescriptTypedHandler.kt          # JSX 閉じタグ自動挿入
│   ├── formatter/
│   │   └── RescriptFormattingService.kt   # 外部フォーマッタ連携 (rescript format CLI)
│   ├── navigation/
│   │   ├── RescriptSymbolContributor.kt        # Go to Symbol (Cmd+Option+O)
│   │   ├── RescriptSwitchFileAction.kt         # .res/.resi ファイル切り替え (Alt+O)
│   │   ├── RescriptGotoRelatedProvider.kt      # Go to Related (.res/.resi/.js ジャンプ)
│   │   ├── RescriptCreateInterfaceAction.kt    # .resi インターフェース生成
│   │   ├── RescriptOpenCompiledJsAction.kt     # コンパイル済み JS を開く (Alt+Shift+J)
│   │   ├── RescriptQualifiedNameProvider.kt    # 完全修飾名コピー (Cmd+Shift+Alt+C)
│   │   └── RescriptTestCreator.kt              # Go to Test / Create Test (Ctrl+Shift+T)
│   ├── template/
│   │   └── RescriptCreateFileAction.kt    # New > ReScript File アクション
│   ├── spellcheck/
│   │   ├── RescriptSpellcheckingStrategy.kt  # スペルチェック対応
│   │   └── RescriptBundledDictionaryProvider.kt  # ReScript 用語バンドル辞書
│   ├── completion/
│   │   └── RescriptPostfixTemplateProvider.kt  # Postfix Completion (.switch, .pipe, .log 等)
│   ├── analysis/
│   │   ├── RescriptReanalyzeAnnotator.kt  # reanalyze デッドコード分析
│   │   ├── RescriptReanalyzeQuickFix.kt   # Quick Fix (プレフィックス付与・削除)
│   │   └── RescriptUnusedCodeInspection.kt  # Global Inspection (プロジェクト全体分析)
│   ├── test/
│   │   ├── RescriptTestRunConfigurationType.kt   # テスト実行構成タイプ
│   │   ├── RescriptTestRunConfiguration.kt       # テスト実行構成
│   │   ├── RescriptTestConfigurationFactory.kt   # テスト構成ファクトリ
│   │   ├── RescriptTestConfigurationProducer.kt  # コンテキストからの自動構成
│   │   ├── RescriptTestFrameworkDetector.kt      # jest/vitest 自動検出
│   │   ├── RescriptTestConsoleProperties.kt      # SMTRunner テストツリー
│   │   ├── RescriptTestLocator.kt                # compiled JS → .res パス変換
│   │   ├── RescriptTestRunConfigurationOptions.kt # テスト実行構成オプション永続化
│   │   ├── RescriptTestSettingsEditor.kt         # テスト設定 UI
│   │   └── RescriptTestSourcesFilter.kt          # テストファイル認識 (*_test.res, __tests__/)
│   ├── preview/
│   │   ├── RescriptCompiledJsPreviewToolWindowFactory.kt  # JS プレビューツールウィンドウ
│   │   └── RescriptCompiledJsPreviewPanel.kt              # プレビューパネル
│   ├── hierarchy/
│   │   ├── RescriptModuleHierarchyProvider.kt        # モジュール階層エントリポイント
│   │   ├── RescriptModuleHierarchyBrowser.kt         # 階層ビュー UI
│   │   ├── RescriptModuleHierarchyTreeStructure.kt   # ツリー構造
│   │   ├── RescriptModuleHierarchyNodeDescriptor.kt  # ノード記述子
│   │   └── RescriptDependencyAnalyzer.kt             # モジュール依存関係分析
│   ├── paste/
│   │   └── RescriptPasteAsJsonAction.kt   # Paste as JSON.t
│   ├── injection/
│   │   ├── RescriptRawJsInjector.kt    # %raw() 内 JavaScript ハイライト
│   │   └── RescriptMarkdownCodeFenceProvider.kt  # Markdown コードフェンスハイライト
│   ├── codevision/
│   │   └── RescriptCodeVisionProvider.kt  # Code Lens (LSP codeLens → CodeVision)
│   ├── statusbar/
│   │   └── RescriptCompilerStatusWidgetFactory.kt  # ビルドステータス表示
│   ├── errorlens/
│   │   ├── RescriptErrorLensEditorListener.kt      # FileEditorManagerListener
│   │   ├── RescriptErrorLensManager.kt             # エディタ単位の管理 (DaemonListener + Inlay Map)
│   │   ├── RescriptErrorLensRenderer.kt            # EditorCustomElementRenderer
│   │   ├── RescriptErrorLensSeverity.kt            # 重要度 → 色マッピング
│   │   └── RescriptErrorLensHighlighterInfo.kt     # RangeHighlighter → DiagnosticInfo 変換
│   ├── debug/
│   │   ├── RescriptDebugCompiledJsAction.kt        # Debug Compiled JS アクション
│   │   ├── RescriptDebugConfigurationType.kt       # Run Configuration タイプ
│   │   ├── RescriptDebugConfigurationFactory.kt    # ファクトリ
│   │   ├── RescriptDebugRunConfiguration.kt        # 実行構成
│   │   ├── RescriptDebugRunConfigurationOptions.kt # 永続化オプション
│   │   └── RescriptDebugSettingsEditor.kt          # 設定 UI
│   ├── imports/
│   │   ├── RescriptImportOptimizer.kt   # Import Optimizer (重複 + 未使用 open 削除)
│   │   ├── RescriptImportUtil.kt        # Import 操作ユーティリティ
│   │   └── RescriptUnusedOpenDetector.kt # LSP 診断から未使用 open を検出
│   ├── intention/
│   │   ├── RescriptWrapWithIntention.kt     # Wrap with Some/Ok/Error
│   │   ├── RescriptAddGenTypeIntention.kt   # Add @genType annotation
│   │   └── RescriptGenerateDocCommentIntention.kt  # KDoc コメント生成
│   ├── surround/
│   │   └── RescriptSurroundDescriptor.kt    # Surround With (if/switch/try/block)
│   ├── folding/
│   │   ├── RescriptFoldingBuilder.kt      # コード折りたたみ (CustomFoldingBuilder)
│   │   └── RescriptCustomFoldingProvider.kt  # //#region カスタム折りたたみ
│   ├── wizard/
│   │   ├── ProjectTemplate.kt                # 12 テンプレート enum + カテゴリ
│   │   ├── ProjectFileBuilders.kt            # 共有ファイル生成ユーティリティ
│   │   ├── RescriptModuleBuilder.kt          # Project Wizard (New Project)
│   │   ├── RescriptProjectWizardStep.kt      # テンプレート選択 UI
│   │   ├── RescriptProjectGenerator.kt       # プロジェクトファイル生成
│   │   └── templates/                        # テンプレート別ファイル生成
│   │       ├── BasicTemplateFiles.kt
│   │       ├── ViteReactTemplateFiles.kt
│   │       ├── NextjsTemplateFiles.kt
│   │       ├── ElectronTemplateFiles.kt
│   │       ├── HonoTemplateFiles.kt
│   │       ├── CloudflareWorkersTemplateFiles.kt
│   │       ├── AwsLambdaTemplateFiles.kt
│   │       ├── GoogleCloudRunTemplateFiles.kt
│   │       ├── ReactNativeTemplateFiles.kt
│   │       ├── NpmLibraryTemplateFiles.kt
│   │       ├── CliToolTemplateFiles.kt
│   │       └── MonorepoTemplateFiles.kt
│   ├── generate/
│   │   ├── RescriptGenerateGroup.kt              # Generate メニューグループ
│   │   ├── RescriptTypeDeclarationParser.kt      # テキストベース型宣言パーサー
│   │   ├── RescriptGenerateActionUtil.kt         # Generate アクション共通ユーティリティ
│   │   ├── RescriptGenerateSwitchAction.kt       # Switch Arms 生成
│   │   └── RescriptGenerateModuleTypeAction.kt   # Module Type 生成
│   ├── breadcrumb/
│   │   └── RescriptBreadcrumbsProvider.kt  # パンくずリストナビゲーション
│   ├── refactor/
│   │   ├── RescriptRenameHandler.kt     # LSP 経由リネームハンドラ
│   │   └── RescriptNamesValidator.kt    # 識別子バリデーション
│   ├── inspection/
│   │   ├── RescriptMissingConfigInspection.kt   # rescript.json 欠落警告
│   │   ├── RescriptDuplicateOpenInspection.kt   # 重複 open 検出
│   │   └── RescriptEmptyModuleInspection.kt     # 空モジュール検出
│   ├── binding/
│   │   ├── DtsJsonModel.kt              # .d.ts JSON 中間表現データモデル + Gson デシリアライザ
│   │   ├── DtsTypeMapper.kt             # TypeScript → ReScript 型マッピング
│   │   ├── DtsToRescriptConverter.kt    # JSON モデル → ReScript バインディングコード生成
│   │   ├── DtsNodeDetector.kt           # Node.js / TypeScript パッケージ検出
│   │   ├── DtsParserProcess.kt          # Node.js プロセス実行（dts-to-json.js 起動）
│   │   └── DtsGenerateBindingAction.kt  # .d.ts バインディング生成アクション
│   ├── projectview/
│   │   └── RescriptTreeStructureProvider.kt  # Project View .resi ネスト表示
│   └── commenter/RescriptCommenter.kt
├── java/com/rescript/plugin/lang/
│   └── Rescript.flex                    # JFlex レクサー定義 (ソース)
└── resources/
    ├── META-INF/
    │   ├── plugin.xml                   # プラグイン登録 (extension points)
    │   ├── rescript-json.xml            # JSON Schema (optional dep: com.intellij.modules.json)
    │   ├── rescript-js-injection.xml    # %raw() JS インジェクション (optional dep: JavaScript)
    │   ├── rescript-markdown.xml        # Markdown コードフェンス (optional dep: Markdown)
    │   ├── rescript-debug.xml           # デバッグ統合 (optional dep: JavaScriptDebugger)
    │   └── rescript-nodejs.xml          # Node.js 統合 (optional dep: NodeJS)
    ├── colorSchemes/
    │   ├── RescriptDarcula.xml          # Darcula テーマ用配色
    │   └── RescriptDefault.xml          # Default テーマ用配色
    ├── liveTemplates/
    │   └── ReScript.xml                 # Live Templates (15スニペット)
    ├── fileTemplates/internal/
    │   ├── ReScript Module.res.ft       # モジュールテンプレート
    │   ├── ReScript Interface.resi.ft   # インターフェーステンプレート
    │   └── ReScript Component.res.ft    # React コンポーネントテンプレート
    ├── dictionaries/
    │   └── rescript.dic                   # ReScript 用語スペルチェック辞書
    ├── scripts/
    │   └── dts-to-json.js               # バンドル Node.js パーサースクリプト
    ├── schemas/
    │   └── rescript.schema.json         # rescript.json 用 JSON Schema
    └── icons/                           # SVG アイコン
```

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
- **Error Lens** (`errorlens/`) — エディタ行内にインライン診断表示
- **JS プレビュー** (`preview/`) — コンパイル済み JS のリアルタイムプレビュー
- **モジュール階層** (`hierarchy/`) — モジュール依存関係のツリー表示
- **プロジェクトウィザード** (`wizard/`) — 12 テンプレートによる新規プロジェクト作成
- **コード検査** (`inspection/`, `analysis/`) — 重複 open、空モジュール、rescript.json 欠落、reanalyze デッドコード分析
- **リファクタリング** (`refactor/`) — LSP 経由リネーム、識別子バリデーション
- **Import 最適化** (`imports/`) — 重複・未使用 open の自動削除
- **Intention Actions** (`intention/`) — Wrap with Some/Ok/Error、@genType 追加、ドキュメントコメント生成
- **Surround With** (`surround/`) — if/switch/try/block で囲む
- **Postfix Completion** (`completion/`) — .switch, .pipe, .log 等
- **コード折りたたみ** (`folding/`) — ブロック折りたたみ、//#region カスタム折りたたみ
- **パンくずリスト** (`breadcrumb/`) — エディタ上部のナビゲーション
- **Generate アクション** (`generate/`) — Switch Arms / Module Type 生成
- **.d.ts バインディング生成** (`binding/`) — TypeScript 型定義から ReScript バインディングを自動生成
- **Unwrap/Remove** (`editor/`) — Some/Ok/Error/if/switch/try/ブレースの除去 (Ctrl+Shift+Delete)
- **JSX 閉じタグ自動挿入** (`editor/`) — `>` 入力時に閉じタグを自動補完
- **Context Info** (`editor/`) — スクロール時にトップレベル宣言のヘッダーを固定表示
- **Go to Test** (`navigation/`) — 実装⇔テストファイル間のナビゲーション・新規テスト作成 (Ctrl+Shift+T)
- **Find Usages** (`lang/`) — WordsScanner によるシンボルインデキシング + 使用箇所検索
- **バンドル辞書** (`spellcheck/`) — ReScript 固有用語のスペルチェック辞書
- **テストファイル認識** (`test/`) — `*_test.res`、`*.test.res`、`__tests__/` の自動認識
- **Project View ネスト** (`projectview/`) — `.resi` を対応する `.res` の下にネスト表示

## 開発規約

- パッケージ: `com.rescript.plugin.*`
- プラグイン ID: `com.rescript.plugin`
- extension point の登録は `plugin.xml` で行う（オプション依存は `META-INF/rescript-*.xml` に分離）
- 新しい言語機能を追加する場合は、既存のファイル構成（highlight/, lang/, lsp/ 等）に従う
- レクサーにトークンを追加する場合は `Rescript.flex` と `RescriptTokenTypes.kt` の両方を更新する
- テストは `src/test/` に配置する

詳細な規約:

@.claude/rules/testing.md
@.claude/rules/code-comments.md
@.claude/rules/git-conventions.md
@.claude/rules/steering-workflow.md
@.claude/rules/documentation.md

## 重要な注意事項

- `RescriptFlexLexer.java` は自動生成ファイル。直接編集せず、`Rescript.flex` を編集すること
- LSP 機能は `@rescript/language-server` が利用可能な環境でのみ動作する
- `pluginSinceBuild` は `gradle.properties` で管理（`pluginUntilBuild` は前方互換性のため意図的に未設定）
- Gradle Configuration Cache が有効化されている
- Task ツール（サブエージェント）を使用する場合、`run_in_background` は **明示的に指示された場合のみ** 使用すること
