# プロダクト要求定義書 (Product Requirements Document)

## 1. プロダクトビジョンと目的

### ビジョン

ReScript 開発者が JetBrains IDE で快適に開発できる、高品質な言語サポートプラグインを提供する。

### 目的

- 既存の reasonml-idea-plugin がメンテナンス停止状態にあるため、ReScript 専用のクリーンな代替プラグインを提供する
- JFlex レクサーによる高速なネイティブ機能と、rescript-language-server（LSP）による意味解析機能のハイブリッドアーキテクチャにより、軽量かつ高機能な開発体験を実現する
- VSCode 拡張と同等の LSP 機能を JetBrains IDE で利用可能にする

### 背景

既存の reasonml-idea-plugin は Java 47,200行・561ファイルの巨大なコードベースで、ReScript/Reason/OCaml の3言語がコア層188ファイルを共有する複雑な構造だった。特に手書きの再帰下降パーサーにおける JSX 処理が65箇所に分散しており、メンテナンスが困難になっていた。LSP に意味解析を委譲することで、この複雑さを根本的に回避しつつ、rescript-language-server のアップデートに自動追従できる設計とした。

## 2. ターゲットユーザーと課題・ニーズ

### ターゲットユーザー

| ユーザー層 | 説明 |
|---|---|
| ReScript 開発者（JetBrains ユーザー） | 普段 IntelliJ IDEA や WebStorm 等の JetBrains IDE を使用しており、ReScript プロジェクトでも同じ IDE を使いたい開発者 |
| ReScript 開発者（VSCode からの移行） | VSCode の ReScript 拡張を使用しているが、JetBrains IDE の高度な機能（リファクタリング、データベースツール等）を活用したい開発者 |
| フルスタック開発者 | ReScript をフロントエンドに採用しつつ、バックエンド（Java/Kotlin/TypeScript 等）も同一 IDE で開発したい開発者 |

### 課題

1. **代替手段の不在** — 既存プラグインがメンテナンス停止状態で、最新の ReScript バージョンに対応していない
2. **IDE 分断** — ReScript 開発のためだけに VSCode に切り替える必要がある
3. **開発効率の低下** — シンタックスハイライトやコード補完がない環境での ReScript 開発は非効率

### ニーズ

- `.res` / `.resi` ファイルの正確なシンタックスハイライト
- コード補完、定義ジャンプ、ホバードキュメント等の IDE 標準機能
- リアルタイムのエラー・警告表示
- JetBrains IDE のエコシステム（キーマップ、テーマ、他プラグイン）との統合

## 3. 主要な機能一覧

### ネイティブ機能（プラグイン内蔵）

| 機能 | 説明 | 優先度 |
|---|---|---|
| シンタックスハイライト | JFlex レクサーによるトークンベースのカラーリング | P0 |
| コード折りたたみ | モジュール、let/type 宣言、ブロックコメントの折りたたみ | P0 |
| ブレースマッチング | `{}`、`[]`、`()` の自動対応表示 | P0 |
| コメントトグル | 行コメント (`//`) とブロックコメント (`/* */`) の切り替え | P0 |
| ファイルタイプ認識 | `.res` / `.resi` ファイルの自動認識とアイコン表示 | P0 |

### LSP 連携機能（rescript-language-server 経由）

| 機能 | 説明 | 優先度 |
|---|---|---|
| コード補完 | 型情報に基づくインテリジェントな補完候補表示 | P0 |
| 定義ジャンプ | シンボルの定義元への移動 | P0 |
| ホバードキュメント | カーソル位置のシンボルに対する型情報・ドキュメント表示 | P0 |
| 参照検索 | シンボルの使用箇所一覧表示 | P1 |
| リアルタイム診断 | コンパイルエラー・警告のインライン表示 | P0 |
| インレイヒント | 推論された型の注釈表示 | P1 |

### 実装済み機能（当初ロードマップから完了）

以下の機能は当初ロードマップに含まれていたが、既に実装済みである。

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
| Quick Fix (LSP Code Actions) | LSP コードアクションによる自動修正（import 追加、型注釈追加等） | IntelliJ 2024.1+ LSP API で自動サポート |
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
| Project Wizard | New Project ダイアログから 12 種類のプロジェクトテンプレート選択・作成（Basic, Vite+React, Next.js, Electron, Hono, CF Workers, Lambda, Cloud Run, React Native, npm Library, CLI Tool, Monorepo） | `RescriptModuleBuilder` + `RescriptProjectWizardStep` + `ProjectTemplate` + `ProjectFileBuilders` + `templates/*` |
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
| Project View ファイルネスト | コンパイル済み `.res.js` を `.res` の子としてネスト表示し、灰色で表示 | `RescriptFileNestingProvider` + `RescriptCompiledJsNodeDecorator` |
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
| JS→ReScript 変換 | JavaScript コードを ReScript に変換してペースト | `RescriptPasteAsRescriptProcessor` |
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

### 将来機能（ロードマップ） — ギャップ分析

3回の機能調査（初回調査・追加調査・関数型言語調査）で109件の未実装機能候補を収集し、全109件を実装済み（S/A/B 優先度42件 + Phase 1 Quick Wins 7件 + S 優先度6件 + A 優先度13件 + B 優先度18件 + C 優先度23件）。#79 (MultiLang Commenter) は ReScript/JS のコメント構文が同一のため不要と判断。#110, #111 は S 優先度として実装済み。#112, #113, #114 は A 優先度として実装済み。#118 は C 優先度として実装済み。#115, #116, #117 はインフラ改善として実装済み。

JetBrains Marketplace には v0.1.2 で申請済み（審査中）。

全機能実装済み。未実装機能なし。

## 4. 成功の定義

### 定量指標

| 指標 | 目標値 |
|---|---|
| JetBrains Marketplace 公開 | v0.1.7 公開済み |
| シンタックスハイライトの正確性 | ReScript の全トークンタイプを正しくカラーリング |
| LSP 機能の動作率 | VSCode 拡張と同等の機能カバレッジ |
| エディタ入力時のレスポンス | ハイライト更新が体感上即座（< 16ms） |
| プラグインの起動時間 | IDE 起動に対する影響が無視できるレベル |

### 定性指標

- ReScript 開発者が JetBrains IDE を主要な開発環境として選択できるようになる
- VSCode の ReScript 拡張と遜色ない開発体験を提供できる
- プラグインのコードベースが小さく、メンテナンスが容易である

## 5. ビジネス要件

### ライセンス

- MIT ライセンスでオープンソース公開

### プラットフォーム対応

| 要件 | 詳細 |
|---|---|
| 対象 IDE | IntelliJ IDEA (Ultimate / Community) 2025.3+、および全 JetBrains IDE |
| JDK | 21+ |
| OS | Windows / macOS / Linux（JetBrains IDE が動作する全 OS） |
| 依存ツール | Node.js（PATH で利用可能）、`@rescript/language-server` |

### リリース戦略

1. **フェーズ 1（MVP）** — ネイティブ機能 + 基本 LSP 機能の動作確認
2. **フェーズ 2（安定版）** — 全 P0/P1 機能の完成、エッジケースの修正
3. **フェーズ 3（公開）** — JetBrains Marketplace への公開、ドキュメント整備

## 6. ユーザーストーリー

### US-01: シンタックスハイライト

**ユーザーとして**、`.res` ファイルを開いた際に ReScript のコードが適切にハイライトされることで、コードの構造を視覚的に把握したい。

**受け入れ条件:**
- [x] `.res` / `.resi` ファイルを開くと自動的に ReScript として認識される
- [x] キーワード（`let`、`type`、`module`、`switch`、`if` 等）が正しくハイライトされる
- [x] 文字列リテラル（通常文字列、テンプレート文字列）が正しくハイライトされる
- [x] コメント（行コメント、ブロックコメント、ドキュメントコメント）が正しくハイライトされる
- [x] 数値リテラル（整数、浮動小数点）が正しくハイライトされる
- [x] 演算子が正しくハイライトされる
- [x] デコレータ（`@`）が正しくハイライトされる

### US-02: コード補完

**ユーザーとして**、ReScript コードを入力中に適切な補完候補が表示されることで、効率的にコードを書きたい。

**受け入れ条件:**
- [x] 変数名、関数名、モジュール名の補完候補が表示される
- [x] 型情報に基づいた適切な候補が優先表示される
- [x] `@rescript/language-server` が利用可能な環境で動作する

### US-03: 定義ジャンプ

**ユーザーとして**、シンボルの定義元に素早く移動できることで、コードの理解を深めたい。

**受け入れ条件:**
- [x] Ctrl+Click（または Ctrl+B）でシンボルの定義元にジャンプできる
- [x] 同一ファイル内、他ファイル、他モジュールの定義にジャンプできる

### US-04: リアルタイム診断

**ユーザーとして**、コード編集中にエラーや警告がリアルタイムで表示されることで、問題を早期に発見したい。

**受け入れ条件:**
- [x] コンパイルエラーが赤い波線でインライン表示される
- [x] 警告が黄色い波線でインライン表示される
- [x] エラー・警告の詳細がホバーで確認できる
- [x] Problems パネルにエラー・警告の一覧が表示される

### US-05: コード折りたたみ

**ユーザーとして**、モジュールや関数定義を折りたたむことで、大きなファイルの構造を把握しやすくしたい。

**受け入れ条件:**
- [x] `module` ブロックを折りたたみ・展開できる
- [x] `let` バインディングのブロックを折りたたみ・展開できる
- [x] `type` 定義を折りたたみ・展開できる
- [x] ブロックコメントを折りたたみ・展開できる

### US-06: ホバードキュメント

**ユーザーとして**、シンボルにカーソルを合わせた際に型情報やドキュメントが表示されることで、コードの理解を効率化したい。

**受け入れ条件:**
- [x] シンボルにカーソルを合わせると型情報が表示される
- [x] ドキュメントコメントがある場合はその内容も表示される

### US-07: ブレースマッチング

**ユーザーとして**、対応するブレースが強調表示されることで、ネストした構造を正しく把握したい。

**受け入れ条件:**
- [x] `{}`、`[]`、`()` の対応がハイライト表示される
- [x] カーソル位置のブレースに対応するブレースが強調される

### US-08: コメントトグル

**ユーザーとして**、キーボードショートカットでコメントの切り替えができることで、デバッグ作業を効率化したい。

**受け入れ条件:**
- [x] Ctrl+/ で行コメントをトグルできる
- [x] Ctrl+Shift+/ でブロックコメントをトグルできる

### US-09: 参照検索

**ユーザーとして**、シンボルの使用箇所を一覧で確認できることで、変更の影響範囲を把握したい。

**受け入れ条件:**
- [x] シンボルを選択して「Find Usages」で使用箇所一覧が表示される
- [x] 検索結果から各使用箇所にジャンプできる

### US-10: インレイヒント

**ユーザーとして**、推論された型がインラインで表示されることで、明示的な型注釈なしでも型を確認したい。

**受け入れ条件:**
- [x] 型推論された変数やパラメータの横に型ヒントが薄く表示される
- [x] インレイヒントの表示/非表示を設定で切り替えられる

## 7. 機能要件

### FR-01: 言語登録

- ReScript 言語を IntelliJ Platform に登録する
- `.res` ファイルを ReScript ソースファイルとして認識する
- `.resi` ファイルを ReScript インターフェースファイルとして認識する
- ファイルタイプに応じたアイコンを表示する

### FR-02: レキシカル解析

- JFlex レクサーにより ReScript ソースコードをトークンに分解する
- 対応トークンタイプ: キーワード、識別子、数値リテラル、文字列リテラル、コメント、演算子、区切り文字、デコレータ
- テンプレートリテラル（バッククォート文字列、`${...}` 補間）を正しくトークン化する
- 不正な文字列を BAD_CHARACTER として処理する

### FR-03: 軽量パース

- トップレベル宣言（`let`、`type`、`module`、`external`、`open`、`include`、`exception`）を認識する
- 宣言のボディ部分を波括弧のバランスに基づいて特定する
- コード折りたたみとストラクチャービュー用の PSI ツリーを構築する
- 式や JSX の詳細なパースは行わない（LSP に委譲）

### FR-04: シンタックスハイライト

- レクサーのトークンタイプに基づいてテキスト属性を適用する
- IDE のカラースキームと統合する
- エディタ入力に対して即座にハイライトを更新する

### FR-05: LSP サーバー管理

- `@rescript/language-server` の存在をプロジェクトルートから検索する
- 検索順序: `node_modules/@rescript/language-server/out/server.js` → グローバルインストール
- LSP サーバーを stdio 経由で起動・管理する
- サーバーの異常終了時に適切にハンドリングする
- LSP 未検出時、エディタ通知バーおよびプロジェクト起動時バルーンでインストールを促進する
- パッケージマネージャ（npm/yarn/pnpm）をロックファイルから自動検出し、ワンクリックでバックグラウンドインストールを実行する

### FR-06: LSP 機能連携

- IntelliJ Platform の LSP API を使用して以下の機能を提供する:
  - `textDocument/completion` — コード補完
  - `textDocument/definition` — 定義ジャンプ
  - `textDocument/hover` — ホバードキュメント
  - `textDocument/references` — 参照検索
  - `textDocument/publishDiagnostics` — リアルタイム診断
  - `textDocument/inlayHint` — インレイヒント

## 8. 非機能要件

### NFR-01: パフォーマンス

| 要件 | 目標値 |
|---|---|
| シンタックスハイライト更新 | < 16ms（60fps 相当） |
| コード補完の表示 | LSP サーバーのレスポンス時間に依存（プラグイン側のオーバーヘッドは最小限） |
| IDE 起動への影響 | 無視できるレベル（遅延ロード） |
| メモリ使用量 | プラグイン単体で 50MB 以下 |

### NFR-02: 互換性

| 要件 | 詳細 |
|---|---|
| IntelliJ Platform | 2025.3 以降 |
| JDK | 21 以降 |
| OS | Windows / macOS / Linux |
| ReScript バージョン | rescript-language-server が対応する全バージョン |

### NFR-03: 保守性

- コードベースを適正規模に保つ（機能追加に伴い成長するが、不要な複雑さを避ける）
- 複雑な式パースを避け、LSP に委譲する設計を維持する
- IntelliJ Platform の公式 API のみを使用する（internal API の使用禁止）

### NFR-04: 信頼性

- LSP サーバーが利用不可の場合でもネイティブ機能（ハイライト、折りたたみ等）は正常動作する
- LSP サーバーの異常終了時に IDE 全体に影響を与えない
- 不正な ReScript コードに対してもレクサー・パーサーがクラッシュしない

### NFR-05: ユーザビリティ

- プラグインインストール後、追加設定なしでネイティブ機能が動作する
- LSP 機能は `@rescript/language-server` のインストールのみで動作する（プラグインがワンクリックインストールを提供）
- エラーメッセージは原因と対処法を含む明確な内容とする
