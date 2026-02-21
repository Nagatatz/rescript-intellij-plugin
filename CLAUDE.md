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
│   │   ├── RescriptElementDescriptionProvider.kt  # 要素説明プロバイダ
│   │   ├── RescriptUsageTypeProvider.kt   # 使用タイプ分類
│   │   └── psi/
│   │       ├── RescriptPsi.kt           # PSI 要素クラス
│   │       ├── RescriptPsiUtils.kt      # PSI ユーティリティ (要素探索・判定)
│   │       └── RescriptStringLiteral.kt # 文字列リテラル PSI (PsiLanguageInjectionHost)
│   ├── highlight/
│   │   ├── RescriptSyntaxHighlighter.kt
│   │   ├── RescriptSyntaxHighlighterFactory.kt
│   │   ├── RescriptColorSettingsPage.kt # ハイライト色設定 UI
│   │   ├── RescriptBraceMatcher.kt
│   │   └── RescriptHighlightUsagesHandlerFactory.kt  # switch/if/try キーワードハイライト
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
│   │   ├── RescriptLspStartupActivity.kt        # プロジェクト起動時 LSP インストール通知
│   │   └── RescriptExpressionTypeProvider.kt    # 式の型表示 (Ctrl+Shift+P)
│   ├── codestyle/
│   │   ├── RescriptCodeStyleSettingsProvider.kt  # コードスタイル設定
│   │   ├── RescriptLineIndentProvider.kt         # インデント制御
│   │   └── RescriptPredefinedCodeStyle.kt        # "ReScript Standard" プリセット
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
│   │   ├── RescriptRunAnythingProvider.kt  # Run Anything (Ctrl+Ctrl) コマンド
│   │   └── RescriptSettingsEditor.kt    # 実行構成 UI
│   ├── settings/
│   │   ├── RescriptProjectSettings.kt     # プロジェクト単位の設定永続化
│   │   └── RescriptConfigurable.kt        # Settings UI (Languages & Frameworks > ReScript)
│   ├── structure/
│   │   ├── RescriptStructureViewElement.kt
│   │   ├── RescriptStructureViewFactory.kt
│   │   └── RescriptStructureViewModel.kt
│   ├── indexing/
│   │   ├── RescriptTodoIndexer.kt         # TODO インデクシング
│   │   └── RescriptOpenStatementIndex.kt  # open 文インデックス
│   ├── editor/
│   │   ├── RescriptQuoteHandler.kt        # スマート引用符補完
│   │   ├── RescriptEditorNotificationProvider.kt  # LSP 未検出時の案内バー
│   │   ├── RescriptStatementUpDownMover.kt  # 宣言の上下移動 (Alt+Shift+Up/Down)
│   │   ├── RescriptSmartEnterProcessor.kt   # Smart Enter (Shift+Enter)
│   │   ├── RescriptDeclarationRangeHandler.kt  # Context Info (スクロール時の宣言ヘッダー固定)
│   │   ├── RescriptUnwrapDescriptor.kt      # Unwrap/Remove (Ctrl+Shift+Delete)
│   │   ├── RescriptTypedHandler.kt          # JSX 閉じタグ自動挿入
│   │   ├── RescriptEnterHandler.kt          # ドキュメントコメント・行コメント自動継続
│   │   ├── RescriptJoinLinesHandler.kt      # スマート行結合 (let/pipe/arrow)
│   │   ├── RescriptWordSelectionHandler.kt  # 文字列・括弧・コメントの選択拡大/縮小
│   │   ├── RescriptBackspaceHandler.kt      # JSX タグペアのバックスペース削除
│   │   ├── RescriptColorProvider.kt         # カラープレビュースウォッチ
│   │   ├── RescriptCopyPastePreProcessor.kt # 文字列ペースト時エスケープ
│   │   ├── RescriptMoveElementHandler.kt    # 要素の左右移動
│   │   ├── RescriptCodeBlockHandler.kt      # コードブロック境界検出
│   │   ├── RescriptListSplitJoinContext.kt  # リスト分割/結合
│   │   └── RescriptReaderModeMatcher.kt     # node_modules リーダーモード
│   ├── formatter/
│   │   └── RescriptFormattingService.kt   # 外部フォーマッタ連携 (rescript format CLI)
│   ├── navigation/
│   │   ├── RescriptSymbolContributor.kt        # Go to Symbol (Cmd+Option+O)
│   │   ├── RescriptSwitchFileAction.kt         # .res/.resi ファイル切り替え (Alt+O)
│   │   ├── RescriptGotoRelatedProvider.kt      # Go to Related (.res/.resi/.js ジャンプ)
│   │   ├── RescriptCreateInterfaceAction.kt    # .resi インターフェース生成
│   │   ├── RescriptOpenCompiledJsAction.kt     # コンパイル済み JS を開く (Alt+Shift+J)
│   │   ├── RescriptQualifiedNameProvider.kt    # 完全修飾名コピー (Cmd+Shift+Alt+C)
│   │   ├── RescriptTestCreator.kt              # Go to Test / Create Test (Ctrl+Shift+T)
│   │   └── RescriptGotoSuperHandler.kt         # .res → .resi 宣言ジャンプ (Ctrl+U)
│   ├── template/
│   │   └── RescriptCreateFileAction.kt    # New > ReScript File アクション
│   ├── spellcheck/
│   │   ├── RescriptSpellcheckingStrategy.kt  # スペルチェック対応
│   │   └── RescriptBundledDictionaryProvider.kt  # ReScript 用語バンドル辞書
│   ├── completion/
│   │   ├── RescriptCompletionConfidence.kt    # 補完ポップアップ制御 (コメント・文字列内抑制)
│   │   ├── RescriptTemplateContextType.kt     # Live Template コンテキスト (ReScript 専用)
│   │   ├── RescriptLiveTemplateMacros.kt      # Live Template マクロ (moduleName, componentName)
│   │   ├── RescriptPostfixTemplateProvider.kt  # Postfix Completion (.switch, .pipe, .log, .promise, .await 等)
│   │   ├── RescriptLookupCharFilter.kt        # 補完文字フィルタ
│   │   └── RescriptDecoratorCompletionContributor.kt  # デコレータ補完 (@genType, @module 等)
│   ├── analysis/
│   │   ├── RescriptReanalyzeAnnotator.kt  # reanalyze デッドコード分析
│   │   ├── RescriptReanalyzeQuickFix.kt   # Quick Fix (プレフィックス付与・削除)
│   │   ├── RescriptUnusedCodeInspection.kt  # Global Inspection (プロジェクト全体分析)
│   │   └── RescriptProblemHighlightFilter.kt  # node_modules 等のハイライト抑制
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
│   ├── projectview/
│   │   ├── RescriptFileNestingProvider.kt       # .res.js を .res の子にネスト表示
│   │   ├── RescriptCompiledJsNodeDecorator.kt   # コンパイル済み JS を灰色表示
│   │   ├── RescriptTreeStructureProvider.kt     # Project View .resi ネスト表示
│   │   └── RescriptProjectViewNodeDecorator.kt  # Project View ノード装飾
│   ├── paste/
│   │   ├── RescriptPasteAsJsonAction.kt       # Paste as JSON.t
│   │   └── RescriptPasteAsJsxProcessor.kt     # HTML → JSX 変換ペースト
│   ├── injection/
│   │   ├── RescriptRawJsInjector.kt    # %raw() 内 JavaScript ハイライト
│   │   └── RescriptMarkdownCodeFenceProvider.kt  # Markdown コードフェンスハイライト
│   ├── codevision/
│   │   ├── RescriptCodeVisionProvider.kt      # Code Lens (LSP codeLens → CodeVision)
│   │   └── RescriptVcsCodeVisionContext.kt    # VCS Code Vision アノテーション
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
│   │   ├── RescriptUnusedOpenDetector.kt # LSP 診断から未使用 open を検出
│   │   └── RescriptAutoImportOptionsProvider.kt  # Auto Import 設定 UI
│   ├── intention/
│   │   ├── RescriptWrapWithIntention.kt     # Wrap with Some/Ok/Error
│   │   ├── RescriptAddGenTypeIntention.kt   # Add @genType annotation
│   │   ├── RescriptGenerateDocCommentIntention.kt  # KDoc コメント生成
│   │   ├── RescriptAddIgnoreIntention.kt    # 未使用結果に ->ignore 追加
│   │   ├── RescriptAddUnderscorePrefixIntention.kt  # 未使用変数に _ プレフィックス追加
│   │   ├── RescriptRemoveRedundantBracesIntention.kt  # 冗長ブロック { expr } 削除
│   │   └── RescriptFixIdentifierCaseIntention.kt  # 識別子ケース修正 (PascalCase/camelCase)
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
│   │   ├── RescriptRenameHandler.kt         # LSP 経由リネームハンドラ
│   │   ├── RescriptNamesValidator.kt        # 識別子バリデーション
│   │   ├── RescriptSafeDeleteProcessor.kt   # Safe Delete プロセッサ
│   │   └── RescriptNameSuggestionProvider.kt  # リネーム名前候補
│   ├── inspection/
│   │   ├── RescriptMissingConfigInspection.kt   # rescript.json 欠落警告
│   │   ├── RescriptDuplicateOpenInspection.kt   # 重複 open 検出
│   │   ├── RescriptEmptyModuleInspection.kt     # 空モジュール検出
│   │   └── RescriptInspectionSuppressor.kt      # インスペクション抑制
│   ├── binding/
│   │   ├── DtsJsonModel.kt              # .d.ts JSON 中間表現データモデル + Gson デシリアライザ
│   │   ├── DtsTypeMapper.kt             # TypeScript → ReScript 型マッピング
│   │   ├── DtsToRescriptConverter.kt    # JSON モデル → ReScript バインディングコード生成
│   │   ├── DtsNodeDetector.kt           # Node.js / TypeScript パッケージ検出
│   │   ├── DtsParserProcess.kt          # Node.js プロセス実行（dts-to-json.js 起動）
│   │   └── DtsGenerateBindingAction.kt  # .d.ts バインディング生成アクション
│   ├── dependencies/
│   │   ├── RescriptDependenciesToolWindowFactory.kt  # パッケージ依存ツールウィンドウ
│   │   └── RescriptDependenciesPanel.kt              # 依存パッケージパネル
│   ├── documentation/
│   │   └── RescriptDocumentationProvider.kt  # 外部ドキュメント URL + Quick Documentation (Ctrl+Q / Shift+F1)
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
    │   └── ReScript.xml                 # Live Templates (21スニペット、ReScript 専用コンテキスト)
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
- **Project View** (`projectview/`) — コンパイル済み JS のネスト表示・灰色化
- **プロジェクトウィザード** (`wizard/`) — 12 テンプレートによる新規プロジェクト作成
- **コード検査** (`inspection/`, `analysis/`) — 重複 open、空モジュール、rescript.json 欠落、reanalyze デッドコード分析
- **リファクタリング** (`refactor/`) — LSP 経由リネーム、識別子バリデーション
- **Import 最適化** (`imports/`) — 重複・未使用 open の自動削除
- **Intention Actions** (`intention/`) — Wrap with Some/Ok/Error、@genType 追加、ドキュメントコメント生成、->ignore 追加、_ プレフィックス追加、冗長ブレース削除、識別子ケース修正
- **Surround With** (`surround/`) — if/switch/try/block で囲む
- **Postfix Completion** (`completion/`) — .switch, .pipe, .log, .promise, .await 等
- **Completion Confidence** (`completion/`) — コメント・文字列内の補完ポップアップ抑制
- **Live Template コンテキスト** (`completion/`) — ReScript 専用コンテキスト + moduleName/componentName マクロ
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
- **Enter Handler** (`editor/`) — ドキュメントコメント・行コメントの自動継続
- **Join Lines** (`editor/`) — let/pipe/arrow のスマート行結合
- **Word Selection** (`editor/`) — 文字列・括弧・コメントの選択拡大/縮小
- **Highlight Usages** (`highlight/`) — switch/if/try 等の対応キーワードハイライト
- **Goto Super** (`navigation/`) — .res → .resi 宣言ジャンプ (Ctrl+U)
- **External Documentation** (`documentation/`) — Belt/Js モジュールの外部ドキュメント URL (Shift+F1)
- **Run Anything** (`run/`) — Ctrl+Ctrl で ReScript CLI コマンド実行
- **Expression Type** (`lsp/`) — カーソル位置の式の型を LSP hover で表示 (Ctrl+Shift+P)
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

## 重要な注意事項

- `RescriptFlexLexer.java` は自動生成ファイル。直接編集せず、`Rescript.flex` を編集すること
- LSP 機能は `@rescript/language-server` が利用可能な環境でのみ動作する
- `pluginSinceBuild` は `gradle.properties` で管理（`pluginUntilBuild` は前方互換性のため意図的に未設定）
- Gradle Configuration Cache が有効化されている
- Task ツール（サブエージェント）を使用する場合、`run_in_background` は **明示的に指示された場合のみ** 使用すること

## 将来実装予定の機能（ロードマップ）

3回の機能調査（初回調査・追加調査・関数型言語調査）で109件の未実装機能候補を収集し、S/A/B 優先度の42件は実装済み。残りの機能を以下のように分類。

### 実装予定（Phase 2-4: 15件）

**Phase 1 実装済み:** #71 (->ignore), #91 (_ プレフィックス), #72 (冗長ブレース削除), #90 (デコレータ補完), #92 (演算子優先順位), #80 (Long Line 抑制), #73 (識別子ケース修正) — 7件実装、#79 (MultiLang Commenter) は ReScript/JS コメント構文同一のため不要

#### Phase 2: ReScript らしさ（6件）

| # | 機能 | 種類 | 難易度 |
|---|------|------|--------|
| 70 | Pipe ⇔ 関数呼び出し変換 | Intention | 中 |
| 93 | 常時型表示パネル (Sticky Type Info) | ToolWindow | 中 |
| 74 | パイプチェーン中間型ヒント | InlayHints | 中 |
| 78 | Switch ケース統合 | Intention | 中 |
| 95 | ケースの変数分割 (Case Split) | Intention | 中 |
| 97 | map/filter チェーン変換 | Intention | 中 |

#### Phase 3: .resi 管理 + コード生成（4件）

| # | 機能 | 種類 | 難易度 |
|---|------|------|--------|
| 76 | インターフェース公開/非公開切り替え | Intention | 中 |
| 94 | .resi シグネチャ同期 | Editor | 中 |
| 77 | Make 関数生成 | Generate | 中 |
| 96 | レコードスタブ生成 | Generate | 中 |

#### Phase 4: 補完・分析の強化（5件）

| # | 機能 | 種類 | 難易度 |
|---|------|------|--------|
| 98 | 位置引数→ラベル付き引数変換 | Intention | 中 |
| 84 | Parameter Info Handler (Ctrl+P) | Completion | 中 |
| 102 | スタイルリンティング | Inspection | 中〜高 |
| 83 | 型ミスマッチインラインヒント | InlayHints | 中 |
| 99 | 型ミスマッチ差分表示 | InlayHints | 中〜高 |

### 将来検討（C 優先度: 36件）

低優先度または高難度の機能。必要に応じて個別に実装検討する。

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 43 | Extract Variable | リファクタリング | 高 |
| 44 | Call Hierarchy | ナビゲーション | 高 |
| 45 | Go to Implementation | ナビゲーション | 中 |
| 46 | Search Everywhere | ナビゲーション | 中 |
| 47 | Navigation Bar Model | ナビゲーション | 低〜中 |
| 48 | External Annotator (Format Check) | 分析 | 中 |
| 49 | Unresolved Reference Quick Fix | 分析 | 中〜高 |
| 50 | Completion Weigher | 補完 | 中 |
| 51 | Stub Index | インデキシング | 高 |
| 52 | Code Rearranger | その他 | 中 |
| 53 | Strip Trailing Spaces | 編集 | 低 |
| 54 | IntelliLang | インジェクション | 低〜中 |
| 55 | Formatting for Injected | インジェクション | 低 |
| 56 | Framework Detector | プロジェクトビュー | 中 |
| 57 | Scratch File | 実行 | 中〜高 |
| 58 | REPL | 実行 | 中〜高 |
| 59 | Grazie Text Extractor | その他 | 低 |
| 60 | Element Signature Provider | その他 | 低 |
| 61 | Index Pattern Builder | インデキシング | 低 |
| 75 | ラベル付き引数の一括挿入 | Intention | 中 |
| 81 | JSON エンコーダ/デコーダ生成 | Generate | 中〜高 |
| 82 | 分割代入の導入/解除 | Intention | 中 |
| 85 | 型注釈一括追加 | Intention | 中 |
| 86 | React コンポーネント抽出 | リファクタリング | 高 |
| 87 | PPX 展開ビュー | ToolWindow | 高 |
| 88 | モジュールタイプ実装生成 | Generate | 高 |
| 89 | 使用箇所からの関数生成 | Quick Fix | 中〜高 |
| 100 | 不要な括弧の削除 | Intention | 中 |
| 101 | 不要な修飾子の削除 | Intention | 中 |
| 103 | 変更可能性の診断 | Inspection | 中 |
| 104 | JS→ReScript 変換 | Paste | 中〜高 |
| 105 | 型ホール支援 | Quick Fix | 高 |
| 106 | コメント内コード評価 | Editor | 高 |
| 107 | Worksheet モード | Editor | 高 |
| 108 | 型シグネチャ検索 | ナビゲーション | 非常に高 |
| 109 | Implicit/PPX 可視化 | InlayHints | 中 |

### 長期検討（D 優先度: 8件）

パーサーの大幅拡張が前提となる高難度機能。

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 62 | Extract Function | リファクタリング | 非常に高 |
| 63 | Inline Variable/Function | リファクタリング | 高 |
| 64 | Change Signature | リファクタリング | 非常に高 |
| 65 | Introduce Constant | リファクタリング | 高 |
| 66 | Suggested Refactoring | リファクタリング | 中〜高 |
| 67 | Dependency Diagram | その他 | 高 |
| 68 | File Include Provider | ナビゲーション | 中 |
| 69 | Editor Floating Toolbar | その他 | 中 |
