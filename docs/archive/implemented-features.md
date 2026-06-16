# 当初ロードマップ完了項目（履歴アーカイブ）

本ドキュメントは `docs/product-requirements.md` から分離したロードマップ完了項目の履歴記録である。現在の実装状況の要約は以下を参照:

- ユーザー向けサマリ: `README.md` の Features セクション
- パッケージ/クラス単位の対応表: `docs/functional-design.md` の Extension Point マップ
- 機能カテゴリ別の解説: `sphinx-docs/user/features/`

---

| 機能 | 説明 | 実装状況 |
|---|---|---|
| カラースキーム設定 UI | トークンごとの色を IDE 設定画面からカスタマイズ | `RescriptColorSettingsPage` + Darcula/Default テーマ |
| rescript.json 自動検出 | rescript.json の検出とプロジェクト設定の自動構成 | `RescriptJsonIconProvider` + `RescriptMissingConfigInspection` |
| コンパイラ実行・ビルドツール統合 | IDE 内から ReScript コンパイラを実行し、ビルド結果を表示 | `RescriptRunConfigurationType` + Run Configuration |
| ストラクチャービュー | ファイル内のシンボル一覧をツリー表示 | `RescriptStructureViewFactory` |
| コード折りたたみ | モジュール・宣言・コメントの折りたたみ | `RescriptFoldingBuilder` |
| ブレースマッチング | `{}`、`[]`、`()` の自動対応表示 | `RescriptBraceMatcher` |
| コメントトグル | 行コメント・ブロックコメントの切り替え | `RescriptCommenter` |
| セマンティックハイライト | LSP セマンティックトークンによる高精度な色分け | `RescriptSemanticTokensSupport` |
| コードスタイル設定 | インデント・タブ設定 | `RescriptCodeStyleSettingsProvider` + `RescriptLineIndentProvider` |
| スマート引用符補完 | 引用符の自動ペアリング | `RescriptQuoteHandler` |
| パンくずナビゲーション | エディタ上部にスコープパス表示 | `RescriptBreadcrumbsProvider` |
| リネームリファクタリング | LSP 連携によるシンボルリネーム | `RescriptRenameHandler` + `RescriptNamesValidator` |
| TODO インデクシング | ReScript ファイル内の TODO/FIXME 認識 | `RescriptTodoIndexer` |
| Go to Symbol | `Cmd+Option+O` でシンボル検索 | `RescriptSymbolContributor` |
| 外部フォーマッタ連携 | `rescript format` CLI によるコードフォーマット | `RescriptFormattingService` |
| コードインスペクション | 重複 open、空モジュール、設定ファイル未検出の警告 | `RescriptDuplicateOpenInspection` 等 |
| プロジェクト設定 UI | Languages & Frameworks > ReScript 設定画面 | `RescriptConfigurable` + `RescriptProjectSettings` |
| `.res`/`.resi` 切り替え | `Alt+O` で実装/インターフェースファイル切り替え | `RescriptSwitchFileAction` |
| Live Templates | `let`, `mod`, `switch`, `if` 等15種のスニペット | `liveTemplates/ReScript.xml` |
| File Templates | New > ReScript File からテンプレートファイル作成（Module, Interface, Component） | `RescriptCreateFileAction` + `internalFileTemplate` |
| スペルチェック | コメント・文字列・識別子のスペルチェック | `RescriptSpellcheckingStrategy` |
| Postfix Completion | `.switch`, `.pipe`, `.log`, `.some`, `.ok`, `.error`, `.ignore` の式後方補完 | `RescriptPostfixTemplateProvider` |
| Console Filter | コンパイルエラー出力のファイルパス:行番号をクリック可能リンクに変換 | `RescriptConsoleFilterProvider` |
| Editor Notification Bar | LSP 未検出時のエディタ上部案内バー表示（Install ボタン付き） | `RescriptEditorNotificationProvider` |
| LSP 自動インストール促進 | プロジェクト起動時のバルーン通知 + パッケージマネージャ自動検出によるワンクリックインストール | `RescriptLspStartupActivity` + `RescriptLspInstaller` + `RescriptPackageManagerDetector` |
| JSON Schema 提供 | `rescript.json`/`bsconfig.json` の補完・バリデーション | `RescriptJsonSchemaProviderFactory` |
| `%raw()` JS ハイライト | `%raw()` 内の JavaScript をハイライト | `RescriptRawJsInjector` + `RescriptAstFactory` |
| Go to Related | `Navigate > Related Symbol` で `.res`/`.resi`/`.js` 間の関連ファイルジャンプ | `RescriptGotoRelatedProvider` |
| Signature Help | `(` 入力時に関数シグネチャ（パラメータ情報）をポップアップ表示 | IntelliJ 2025.3+ LSP API で自動サポート |
| Code Lens | 関数定義の上に推論された型シグネチャをインライン表示 | `RescriptCodeVisionProvider` (CodeVision API) |
| インターフェースファイル生成 | `.res` から `.resi` を自動生成 | `RescriptCreateInterfaceAction` (LSP `textDocument/createInterface`) |
| コンパイル済み JS を開く | `.res` に対応するコンパイル済み `.js` を開く (Alt+Shift+J) | `RescriptOpenCompiledJsAction` (LSP `textDocument/openCompiled`) |
| ビルドステータス表示 | ステータスバーにコンパイル状態（成功/エラー/警告）表示 | `RescriptCompilerStatusWidgetFactory` (LSP `rescript/compilationStatus`) |
| Quick Fix (LSP Code Actions) | LSP コードアクション 9 種の自動受領（`simpleAddMissingCases` / `wrapInSome` / `unwrapOptional` / `addUndefinedRecordFields` / `simpleConversion` / `applyUncurried` / `didYouMean` / `removeUnusedCode` / `extractLocalModuleToFile` / `expandCatchAllPatterns`） | IntelliJ 2024.1+ LSP API のデフォルト `LspCodeActionsSupport` で自動サポート（`docs/lsp-fallback-matrix.md` §5 参照） |
| Intention Actions | 式を Some/Ok/Error でラップ、@genType 追加 (Alt+Enter) | `RescriptWrapWithIntention` + `RescriptAddGenTypeIntention` |
| Surround With | 選択コードを if/switch/try/block で囲む (Ctrl+Alt+T) | `RescriptSurroundDescriptor` |
| Import Optimizer | 重複 open 文の自動削除 (Ctrl+Alt+O) | `RescriptImportOptimizer` |
| Gutter Run Icons | .res ファイルのガターに▶実行アイコン表示 | `RescriptRunLineMarkerContributor` |
| reanalyze 統合 | デッドコード分析、未処理例外分析 | `RescriptReanalyzeAnnotator` (ExternalAnnotator) |
| Markdown ReScript ハイライト | ` ```rescript ` コードブロックのシンタックスハイライト | `RescriptMarkdownCodeFenceProvider` (CodeFenceLanguageProvider) |
| Paste as JSON.t | クリップボード JSON を ReScript `JSON.t` に変換ペースト | `RescriptPasteAsJsonAction` (AnAction) |
| `//#region` 折りたたみ | カスタム折りたたみマーカー | `RescriptCustomFoldingProvider` + `CustomFoldingBuilder` |
| Incremental Type Checking 設定 | LSP の incremental typechecking トグル | `RescriptProjectSettings` + `RescriptConfigurable` |
| Statement Up/Down Mover | Alt+Shift+Up/Down でトップレベル宣言を上下移動 | `RescriptStatementUpDownMover` |
| Qualified Name Copy | Cmd+Shift+Alt+C で完全修飾名コピー | `RescriptQualifiedNameProvider` |
| Smart Enter | Shift+Enter で文を補完して改行（括弧閉じ、switch ブレース補完等） | `RescriptSmartEnterProcessor` |
| Project Wizard | New Project ダイアログから 15 種類のプロジェクトテンプレート選択・作成。テンプレート一覧・同梱要素・統合テストの詳細は [docs/templates.md](templates.md) 参照 | `RescriptModuleBuilder` + `RescriptProjectWizardStep` + `ProjectTemplate` + `ProjectFileBuilders` + `templates/*` + `TemplateIntegrationTest` |
| Code Generation | variant switch arms / module type スケルトン / make 関数 / JSON エンコーダ・デコーダ自動生成 (Cmd+N) | `RescriptGenerateGroup` + `RescriptTypeDeclarationParser` + `RescriptJsonCodeGenerator` |
| .d.ts → ReScript Binding 生成 | TypeScript `.d.ts` 定義ファイルから ReScript `external` バインディングコードを自動生成 | `DtsGenerateBindingAction` + `DtsToRescriptConverter` |
| Bundled Dictionary | ReScript 固有用語（genType, Belt, functor 等）のスペルチェック辞書 | `RescriptBundledDictionaryProvider` + `rescript.dic` |
| Test Source Filter | `*_test.res`、`*.test.res`、`__tests__/` ディレクトリの自動テストファイル認識 | `RescriptTestSourcesFilter` |
| Context Info | スクロール時にトップレベル宣言のヘッダーをエディタ上部に固定表示 | `RescriptDeclarationRangeHandler` |
| Find Usages | WordsScanner によるシンボルインデキシング + 使用箇所検索 UI | `RescriptFindUsagesProvider` |
| Unwrap/Remove | Some/Ok/Error/if/switch/try/ブレースの除去 (Ctrl+Shift+Delete) | `RescriptUnwrapDescriptor` |
| JSX 閉じタグ自動挿入 | `>` 入力時に対応する閉じタグ `</tag>` を自動補完 | `RescriptTypedHandler` |
| Go to Test / Create Test | 実装⇔テストファイル間のナビゲーション・新規テスト作成 (Ctrl+Shift+T) | `RescriptTestCreator` |
| Project View .resi ネスト | `.resi` インターフェースファイルを対応する `.res` の下にネスト表示 | `RescriptTreeStructureProvider` |
| Project View ファイルネスト | コンパイル済み JS（`.res.js`/`.mjs`/`.cjs`、`.bs.js`/`.mjs`/`.cjs`）を `.res` の子としてネスト表示し、灰色で表示 | `RescriptTreeStructureProvider` + `RescriptCompiledJsNodeDecorator` |
| Completion Confidence | コメント・文字列内での補完ポップアップ抑制 | `RescriptCompletionConfidence` |
| Live Template コンテキスト | ReScript 専用コンテキスト + moduleName/componentName マクロ | `RescriptTemplateContextType` + `RescriptLiveTemplateMacros` |
| Additional Snippets | 6 新 Live Templates (`@module`, `@val`, `@send`, `@get`, `@set`, `comp`) + 2 新 Postfix (`.promise`, `.await`) | `ReScript.xml` + `RescriptPostfixTemplateProvider` |
| Problem Highlight Filter | `node_modules/`, `lib/bs/`, `lib/ocaml/` 内のハイライト抑制 | `RescriptProblemHighlightFilter` |
| Enter Handler | ドキュメントコメント (`/** */`) と行コメント (`//`) の自動継続 | `RescriptEnterHandler` |
| Join Lines | let バインディング・パイプチェーン・アロー関数のスマート行結合 | `RescriptJoinLinesHandler` |
| Word Selection | 文字列・括弧・コメントの選択拡大/縮小 | `RescriptWordSelectionHandler` (3ハンドラ) |
| Highlight Usages | switch/if/try 等の対応キーワードハイライト | `RescriptHighlightUsagesHandlerFactory` |
| Goto Super | .res → .resi 宣言ジャンプ (Ctrl+U) | `RescriptGotoSuperHandler` |
| External Documentation + Quick Documentation | Belt/Js モジュールの外部ドキュメント URL (Shift+F1) + PSI ベースのフォールバックドキュメント (Ctrl+Q / hover) | `RescriptDocumentationProvider` (generateDoc/generateHoverDoc + getUrlFor) |
| Run Anything | Ctrl+Ctrl で ReScript CLI コマンド実行 (build/clean/format) | `RescriptRunAnythingProvider` |
| Expression Type Info | カーソル位置の式の型を LSP hover で表示 (Ctrl+Shift+P) | `RescriptExpressionTypeProvider` |
| Predefined Code Style | "ReScript Standard" プリセットコードスタイル | `RescriptPredefinedCodeStyle` |
| Element Description | リファクタリングダイアログ・Find Usages での要素説明 | `RescriptElementDescriptionProvider` |
| Reader Mode | node_modules 内 .res/.resi のリーダーモード表示 | `RescriptReaderModeMatcher` |
| Lookup Char Filter | 補完候補選択時の文字フィルタリング | `RescriptLookupCharFilter` |
| Inspection Suppressor | `// noinspection` コメントによるインスペクション抑制 | `RescriptInspectionSuppressor` |
| Backspace Handler | JSX タグペアのバックスペース削除 | `RescriptBackspaceHandler` |
| Color Provider | エディタガターのカラープレビュースウォッチ (#hex, rgb, hsl) | `RescriptColorProvider` |
| VCS Code Vision | 宣言上の著者・最終変更 VCS アノテーション | `RescriptVcsCodeVisionContext` |
| Project View Decorator | "has interface" サフィックス、rescript.json バージョン表示 | `RescriptProjectViewNodeDecorator` |
| Copy/Paste Pre-Processor | 文字列リテラル内ペースト時のエスケープ処理 | `RescriptCopyPastePreProcessor` |
| Open Statement Index | open 文のファイルベースインデックス（高速モジュール検索） | `RescriptOpenStatementIndex` |
| Paste as JSX | HTML → ReScript JSX 変換ペースト | `RescriptPasteAsJsxProcessor` |
| Package Dependencies View | rescript.json 依存パッケージのツールウィンドウ表示 | `RescriptDependenciesToolWindowFactory` + `RescriptDependenciesPanel` |
| Auto Import Options | Settings > Editor > Auto Import のオプション UI | `RescriptAutoImportOptionsProvider` |
| Move Element Left/Right | カンマ区切り要素の左右移動 (Alt+Shift+Cmd+Left/Right) | `RescriptMoveElementHandler` |
| Usage Type Provider | Find Usages グルーピング用の使用タイプ分類 | `RescriptUsageTypeProvider` |
| Code Block Handler | コードブロック境界検出 (Ctrl+Shift+[ / ]) | `RescriptCodeBlockHandler` |
| Split/Join List | 1行↔複数行リストの変換 | `RescriptListSplitJoinContext` |
| Quick Documentation | PSI ベースのフォールバックドキュメント (Ctrl+Q / hover) | `RescriptDocumentationProvider` (generateDoc/generateHoverDoc) |
| Safe Delete | 使用箇所チェック付き Safe Delete (Refactor > Safe Delete) | `RescriptSafeDeleteProcessor` |
| Name Suggestion | リネーム時の名前候補提案（型・ファイル名ベース） | `RescriptNameSuggestionProvider` |
| Search Everywhere | Shift+Shift でファイル・シンボルの統合検索 | `RescriptSearchEverywhereContributor` |
| Unresolved Reference Quick Fix | 未解決参照に対する open 追加/修飾子付加クイックフィックス (Alt+Enter) | `RescriptAddOpenQuickFix` + `RescriptQualifyReferenceQuickFix` |
| Completion Weigher | コンテキストベースの補完候補重み付け | `RescriptCompletionWeigher` |
| パイプチェーン中間型ヒント | `->` パイプチェーンの各ステップで中間型をインライン表示 | `RescriptPipeChainTypeHintsProvider` |
| ラベル付き引数の一括挿入 | 関数のラベル付き引数を一括で挿入 (Alt+Enter) | `RescriptInsertLabeledArgsIntention` |
| Make 関数生成 | レコード型からコンストラクタ関数を自動生成 (Cmd+N) | `RescriptGenerateMakeAction` |
| Switch ケース統合 | 同じボディの switch ケースを `\| A \| B => body` に統合 (Alt+Enter) | `RescriptMergeSwitchCasesIntention` |
| 使用箇所からの関数生成 | 未定義関数の使用箇所からスタブ関数を生成 (Alt+Enter) | `RescriptGenerateFunctionQuickFix` |
| .resi シグネチャ同期 | `.res` と `.resi` の宣言シグネチャ不一致を検出 | `RescriptSignatureSyncInspection` |
| ケースの変数分割 | パターンマッチの変数を全コンストラクタに展開 (Alt+Enter) | `RescriptCaseSplitIntention` |
| 位置引数→ラベル付き引数変換 | `foo(1, "hello")` → `foo(~id=1, ~name="hello")` の変換 (Alt+Enter) | `RescriptConvertToLabeledArgsIntention` |
| 不要な括弧の削除 | 式を囲む不要な括弧を自動削除 (Alt+Enter) | `RescriptRemoveParenthesesIntention` |
| 不要な修飾子の削除 | モジュールパスの冗長な修飾子を削除 (Alt+Enter) | `RescriptRemoveQualifierIntention` |
| Go to Implementation | .resi → .res の実装宣言ジャンプ (Ctrl+Alt+B) | `RescriptGotoImplementationAction` |
| Pipe ⇔ 関数呼び出し変換 | `arr->Array.map(f)` ⇔ `Array.map(arr, f)` の相互変換 (Alt+Enter) | `RescriptConvertPipeToFunctionCallIntention` + `RescriptConvertFunctionCallToPipeIntention` |
| インターフェース公開/非公開 | `.res` の宣言を `.resi` に追加/削除して公開を制御 (Alt+Enter) | `RescriptAddToInterfaceIntention` + `RescriptRemoveFromInterfaceIntention` |
| 型ミスマッチインラインヒント | 型エラー箇所に Expected/Actual 型を構造化してインライン表示 | `RescriptTypeMismatchParser` + `RescriptErrorLensRenderer` 拡張 |
| Parameter Info Handler | Ctrl+P でラベル付き引数をネイティブ UI で表示 | IntelliJ 2025.3+ LSP API で自動サポート |
| GitHub エラーレポート | 未処理例外の GitHub Issues 自動レポート（ブラウザベース） | `RescriptErrorReporter` |
| Extract Variable | 式を `let` 束縛に抽出 (Ctrl+Alt+V) | `RescriptRefactoringSupportProvider` + `RescriptExtractVariableHandler` |
| Navigation Bar Model | ナビゲーションバーにファイル構造表示 | `RescriptStructureAwareNavbar` |
| Format Check | 未フォーマットコードの検出と Quick Fix によるフォーマット実行（設定で ON/OFF） | `RescriptFormatCheckAnnotator` + `RescriptFormatQuickFix` |
| Call Hierarchy | 関数の呼び出し階層（Callers/Callees）ツリー表示 (Ctrl+Alt+H) | `RescriptCallHierarchyProvider` + `RescriptCallHierarchyBrowser` + `RescriptCallAnalyzer` |
| Stub Index | PSI スタブベースのインデックスによる高速シンボル検索 | `RescriptStubElementTypes` + `RescriptDeclarationElementType` + `RescriptNameIndex` + `RescriptModuleIndex` |
| Type Info ToolWindow | カーソル位置の式の型を常時表示するツールウィンドウ（LSP hover + debounce） | `RescriptTypeInfoToolWindowFactory` + `RescriptTypeInfoPanel` |
| Record Value Generation | レコード型の全フィールドにデフォルト値を自動挿入 (Cmd+N) | `RescriptGenerateRecordValueAction` |
| %re RegExp Injection | `%re("/pattern/flags")` 内の正規表現に RegExp 言語インジェクション | `RescriptRawJsInjector` 拡張 |
| Expand Destructuring | `let {name, age} = user` を個別 let 束縛に展開 (Alt+Enter) | `RescriptExpandDestructuringIntention` |
| Framework Detector | rescript.json によるフレームワーク自動検出 | `RescriptFrameworkDetector` + `RescriptFrameworkType` |
| Code Rearranger | トップレベル宣言の自動並べ替え（open → type → exception → module → external → let） | `RescriptRearranger` |
| 変更可能性の診断 | 不要な `ref` 使用の検出と Quick Fix による除去 | `RescriptMutabilityInspection` |
| スタイルリンティング | 冗長ブール式・Belt API・ブール switch パターンの検出と改善提案 | `RescriptStyleLintInspection` |
| filter+map チェーン変換 | `filter+map` チェーンを `filterMap` に変換 (Alt+Enter) | `RescriptFilterMapChainIntention` |
| 型注釈追加 | LSP hover 情報を用いた let 束縛への型注釈挿入 (Alt+Enter) | `RescriptAddTypeAnnotationIntention` |
| PPX 可視化 | PPX アノテーションの効果をインレイヒントで表示 | `RescriptPpxVisualizationProvider` |
| 型ミスマッチ差分表示 | 型エラーの不一致部分を色分けして差分表示 | `RescriptTypeDiffComputer` + `RescriptErrorLensRenderer` 拡張 |
| Strip Trailing Spaces | 文字列リテラル内の空白を保護しつつ行末空白を除去 | `RescriptStripTrailingSpacesFilterFactory` + `RescriptStripTrailingSpacesFilter` |
| Formatting for Injected | インジェクトされた言語フラグメントのフォーマット対応 | `RescriptInjectedFormattingModelBuilder` |
| Grazie Text Extractor | コメント・文字列からの自然言語テキスト抽出（Grazie 連携） | `RescriptGrazieTextExtractor` + `rescript-grazie.xml` |
| Element Signature Provider | 折りたたみ状態永続化のための要素シグネチャ | `RescriptElementSignatureProvider` |
| Index Pattern Builder | コメント内 TODO/FIXME パターンのインデックス構築 | `RescriptIndexPatternBuilder` |
| File Include Provider | open 文からのファイルインクルードナビゲーション | `RescriptFileIncludeProvider` |
| Editor Floating Toolbar | ReScript ファイル用フローティングツールバー（Format/Open JS/Create Interface） | `RescriptFloatingToolbarProvider` |
| Scratch File | ReScript スクラッチファイルの作成・実行 | `RescriptScratchRootType` + `RescriptScratchCreationHelper` |
| REPL | インタラクティブ実行環境ツールウィンドウ | `RescriptReplToolWindowFactory` + `RescriptReplPanel` + `RescriptReplExecutor` |
| Suggested Refactoring | コード品質改善のリファクタリングを提案 | `RescriptSuggestedRefactoringInspection` |
| JS/TS→ReScript 変換 | JavaScript/TypeScript コードを ReScript に変換してペースト（型注釈除去、interface/enum コメントアウト、JSX パターン変換） | `RescriptPasteAsRescriptProcessor` |
| Inline Variable/Function | 変数・関数をインライン展開 (Ctrl+Alt+N) | `RescriptInlineHandler` |
| Introduce Constant | リテラル値を定数に抽出 | `RescriptIntroduceConstantHandler` |
| Dependency Diagram | モジュール依存関係のダイアグラム生成 | `RescriptDependencyDiagramProvider` + `RescriptDependencyDiagramModel` |
| React コンポーネント抽出 | JSX を新しい React コンポーネントに抽出 | `RescriptExtractComponentHandler` |
| PPX 展開ビュー | PPX マクロの展開結果をツールウィンドウに表示 | `RescriptPpxViewToolWindowFactory` + `RescriptPpxViewPanel` |
| モジュールタイプ実装生成 | モジュールタイプのスケルトン実装を生成 (Cmd+N) | `RescriptGenerateModuleImplAction` |
| 型ホール支援 | `_` 型ホールに対する候補型の提案 (Alt+Enter) | `RescriptTypeHoleQuickFix` |
| コメント内コード評価 | ドキュメントコメント内のコード例を評価・検証 | `RescriptCommentEvalProvider` |
| Worksheet モード | `.resw` ファイル全体をインタラクティブに評価 | `RescriptWorksheetFileType` + `RescriptWorksheetRunner` |
| Extract Function | 選択コードを新しい関数に抽出 (Ctrl+Alt+M) | `RescriptExtractFunctionHandler` |
| Change Signature | 関数シグネチャの変更と呼び出し側の自動修正 (Ctrl+F6) | `RescriptChangeSignatureHandler` + `RescriptChangeSignatureAction` |
| 型シグネチャ検索 | 型シグネチャから関数を逆引き検索 (Shift+Shift) | `RescriptTypeSignatureSearchContributor` |
| Restart LSP アクション | Tools メニューから LSP サーバーを明示的に再起動 | `RescriptRestartLspAction` |
| LSP 初期化オプション補完 | signatureHelp/cache/inlayHints/compileStatus の6設定を LSP に送信 | `RescriptProjectSettings` + `RescriptLspServerDescriptor` |
| ビルド自動開始プロンプト | プロジェクト起動時に `rescript build -w` の開始をバルーン通知で提案 | `RescriptBuildWatchStartupActivity` |
| Dump LSP State | LSP サーバーの内部状態をデバッグ出力するアクション（Tools メニュー） | `RescriptDumpLspStateAction` |
| offset↔position 変換共通化 | 重複する offset↔LSP Position 変換ロジックを `RescriptOffsetUtils` に集約 | `RescriptOffsetUtils` |
| Reanalyze サーバーモード | reanalyze をデーモンモードで常駐させ差分分析を高速化（ReScript >= 12.1.0） | `RescriptReanalyzeServerService` + `RescriptReanalyzeServerStartupActivity` + `RescriptReanalyzeVersionDetector` |
| Regex インスタンスキャッシュ | 121箇所の `Regex(...)` を companion object 定数に置換 | 各ファイルの companion object 定数 |
| 重複 Regex パターン統一 | LIDENT/UIDENT/WHITESPACE の重複 Regex 定義を一元管理 | `RescriptRegexPatterns` |
| 長大ファイル分割 | 300行超ファイル5つをファサードパターンで責務分離（9ファイル抽出） | `RescriptJsonEncoderGenerator` + `RescriptJsonDecoderGenerator` + `RescriptDeclarationParser` + `RescriptJsxParser` + `RescriptOperatorDocumentation` + `RescriptExternalDocUrls` + `RescriptUnwrappers` + `RescriptLspSignatureParser` + `RescriptLspDiagnosticParser` |
| Intention 基底クラス抽出 | 20 Intention の `getFamilyName()` / `RescriptFile` ガードを共通基底クラスに集約 | `RescriptBaseIntention` |
| Generate Action 基底クラス抽出 | 6 Generate Action の `ActionUpdateThread.BGT` ポリシーを共通基底クラスに集約 | `RescriptBaseGenerateAction` |
| エディタユーティリティ抽出 | `WriteCommandAction` ラッパーと `Document` 行アクセスヘルパーの共通化 | `RescriptEditorUtils` |
| Pipeline Hints | `->` パイプ各段の中間型を LSP hover 経由で InlayHint 表示（ロードマップ #110） | `RescriptPipeChainTypeHintsProvider` |
| Call Hierarchy ToolWindow | 関数の呼び出し元・呼び出し先を階層ツリーで表示（PSI ベース実装、Ctrl+Alt+H。ロードマップ #118） | `RescriptCallHierarchyProvider` + `RescriptCallAnalyzer` |
