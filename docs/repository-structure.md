# リポジトリ構造定義書 (Repository Structure)

## 1. トップレベル構成

```
rescript-intellij-plugin/
├── src/                          # ソースコード
│   ├── main/                     # プロダクションコード
│   └── test/                     # テストコード
├── docs/                         # 永続的ドキュメント（設計・要件）
├── sphinx-docs/                  # Sphinx ユーザー/開発者向けドキュメント
├── .steering/                    # 作業単位のステアリングドキュメント（履歴）
├── .claude/                      # Claude Code 設定・ルール
├── .github/                      # GitHub Actions ワークフロー
├── build.gradle.kts              # Gradle ビルド定義
├── gradle.properties             # プロジェクト設定値
├── settings.gradle.kts           # Gradle プロジェクト設定
├── gradlew / gradlew.bat         # Gradle Wrapper
├── .editorconfig                 # エディタ設定（インデント、行長等）
├── .gitignore                    # Git 除外設定
├── CLAUDE.md                     # Claude Code プロジェクト指示書
├── README.md                     # プロジェクト README
└── LICENSE                       # ライセンス
```

## 2. ソースコード (`src/`)

### 2.1 プロダクションコード (`src/main/`)

#### Kotlin ソース (`src/main/kotlin/com/rescript/plugin/`)

パッケージは機能ドメインごとに分割する。各パッケージは単一の責務を持つ。

| パッケージ | 責務 | 代表クラス |
|-----------|------|-----------|
| (ルート) | Language / FileType / Icons 定義、エラーレポート連携 | `RescriptLanguage`, `RescriptFileTypes`, `RescriptIcons`, `RescriptErrorReporter` |
| `lang/` | レクサー、パーサー、トークン定義、型宣言 RHS の再パース | `RescriptLexer`, `RescriptParser`, `RescriptTokenTypes`, `RescriptTypeDeclarationParser` |
| `lang/psi/` | PSI 要素クラス、ユーティリティ、JSX 開閉タグペア解決 | `RescriptPsi`, `RescriptStringLiteral`, `RescriptPsiUtils`, `RescriptJsxTagPairUtil` |
| `highlight/` | シンタックスハイライト、ブレースマッチング、使用箇所ハイライト（キーワード / JSX 開閉タグペア） | `RescriptSyntaxHighlighter`, `RescriptBraceMatcher`, `RescriptHighlightUsagesHandlerFactory`, `RescriptJsxTagHighlightHandler` |
| `lsp/` | LSP サーバー管理、カスタムプロトコル、LSP ユーティリティ、variant 型の bare-name 解決 | `RescriptLspServerSupportProvider`, `RescriptLspServerDescriptor`, `RescriptLspUtils`, `RescriptVariantTypeResolver` |
| `codestyle/` | コードスタイル、インデント設定 | `RescriptCodeStyleSettingsProvider` |
| `config/` | rescript.json アイコン、JSON Schema | `RescriptJsonIconProvider`, `RescriptJsonSchemaProviderFactory` |
| `run/` | 実行構成（ReScript ビルド）、実行共通ユーティリティ | `RescriptRunConfigurationType`, `RescriptRunConfiguration`, `RescriptRunUtils` |
| `test/` | テスト実行構成（jest/vitest）、describe/it/test 行の Run/Debug ガターマーカーと `-t` フィルタ検出 | `RescriptTestRunConfigurationType`, `RescriptTestRunLineMarkerContributor`, `RescriptTestCallDetector` |
| `debug/` | デバッグ実行構成 | `RescriptDebugConfigurationType` |
| `settings/` | プロジェクト設定 UI・永続化（スキーマ駆動） | `RescriptConfigurable`, `RescriptProjectSettings`, `RescriptSettingsSchema`, `RescriptSettingDescriptor`, `RescriptSettingsValidator` |
| `structure/` | ストラクチャービュー（宣言 + JSX 要素 / フラグメント。表示判定は `RescriptPsiUtils.STRUCTURE_VIEW_TYPES` = `NAVIGABLE_TYPES` + JSX 要素 / フラグメント。self-closing は除外） | `RescriptStructureViewFactory` |
| `indexing/` | PSI スタブインデックス（5 種の宣言型: let / type / module / external / exception）、`open` 文インデックス、識別子の名前インデックス、TODO インデクシング | `RescriptIndexPatternBuilder`, `RescriptModuleIndex`, `RescriptNameIndex`, `RescriptOpenStatementIndex`, `RescriptTodoIndexer` |
| `editor/` | エディタ補助（引用符、通知バー、Smart Enter、JSX 開閉タグ同期リネーム 等） | `RescriptQuoteHandler`, `RescriptSmartEnterProcessor`, `RescriptTypedHandler`, `RescriptBackspaceHandler` |
| `formatter/` | 外部フォーマッタ連携 | `RescriptFormattingService` |
| `navigation/` | ナビゲーション（Symbol、Related、Switch File、Hoogle-style 型シグネチャ検索、シグネチャトークン色付け 等） | `RescriptSymbolContributor`, `RescriptSwitchFileAction`, `RescriptTypeAst`, `RescriptTypeParser`, `RescriptTypeUnifier`, `RescriptDeclarationSignatureExtractor`, `RescriptTypeSignatureSearchContributor`, `RescriptSignatureTokenColorizer` |
| `template/` | ファイル作成テンプレート | `RescriptCreateFileAction` |
| `spellcheck/` | スペルチェック | `RescriptSpellcheckingStrategy` |
| `completion/` | Postfix Completion | `RescriptPostfixTemplateProvider` |
| `analysis/` | reanalyze デッドコード分析 | `RescriptReanalyzeAnnotator` |
| `inspection/` | コードインスペクション（重複 open、空モジュール、JSX 開閉タグ不一致、シグネチャ同期等） | `RescriptDuplicateOpenInspection`, `RescriptMismatchedJsxTagInspection`, `RescriptSignatureSyncInspection` |
| `quickfix/` | クイックフィックス（未解決参照、関数生成等） | `RescriptAddOpenQuickFix`, `RescriptGenerateFunctionQuickFix` |
| `preview/` | コンパイル済み JS プレビュー | `RescriptCompiledJsPreviewToolWindowFactory` |
| `hierarchy/` | モジュール階層ビュー | `RescriptModuleHierarchyProvider` |
| `paste/` | Paste as JSON.t | `RescriptPasteAsJsonAction` |
| `injection/` | 言語インジェクション（%raw JS、Markdown） | `RescriptRawJsInjector` |
| `codevision/` | Code Lens（CodeVision） | `RescriptCodeVisionProvider` |
| `narrowing/` | Type Narrowing Visualizer（switch arm の絞り込み型をインレイヒントで表示） | `RescriptNarrowingHintProvider`, `RescriptSwitchArmCollector`, `RescriptHoverTypeResolver`, `RescriptNarrowingPresenter` |
| `flow/` | Variant Flow Diagram（switch の decision tree を ToolWindow で可視化、Visual / Source トグル、arm 種別カラーパレット (`ArmKind`)、Source モード Mermaid 色付け (`MermaidSourceColorizer`、`diagram/` パッケージと共有)、Mermaid + DOT エクスポート） | `RescriptVariantFlowToolWindowFactory`, `RescriptVariantFlowPanel`, `RescriptVariantFlowAction`, `RescriptVariantFlowModel`, `ArmKind`, `RescriptVariantFlowGraphView`, `RescriptVariantFlowHints`, `RescriptVariantFlowMermaidExporter`, `RescriptVariantFlowDotExporter`, `MermaidSourceColorizer` |
| `impact/` | Type Impact Preview（カーソル位置の type 宣言に対するプロジェクト全体の参照を ToolWindow で一覧表示、`[kind]` ラベルを `colorForKind` で色付き bold 表示） | `RescriptTypeImpactToolWindowFactory`, `RescriptTypeImpactPanel`, `RescriptTypeImpactAction`, `RescriptTypeTargetResolver`, `RescriptTypeReferenceFinder`, `RescriptReferenceClassifier`, `RescriptTypeImpactModel` |
| `notebook/` | Notebook 風 Worksheet（`.resnb` cell-based エディタ + Markdown エクスポート、cell input は `EditorTextField` + `RescriptFileType` でフルハイライト、border / output 背景 / エラー前景は `JBColor` で Light/Dark 両対応） | `RescriptNotebookFileType`, `RescriptNotebookFileEditorProvider`, `RescriptNotebookFileEditor`, `RescriptNotebookPanel`, `RescriptNotebookCellPanel`, `RescriptNotebookSerializer`, `RescriptNotebookMarkdownExporter`, `RescriptNotebookModel` |
| `interop/` | JS Interop Risk Map（`%raw` / `external` / `Obj.magic` / `@bs.*` の使用箇所一覧 + 種別/リスクスコア、各行に `COLOR_BY_RISK` 由来の左端色帯） | `RescriptInteropRiskToolWindowFactory`, `RescriptInteropRiskPanel`, `RescriptInteropRiskAction`, `RescriptInteropClassifier`, `RescriptInteropScanner`, `RescriptInteropModel` |
| `coverage/` | Type Coverage Heat Map（`.res` ファイルごとの annotated/inferred 比率を表形式で可視化、color-coded sortable table） | `RescriptTypeCoverageToolWindowFactory`, `RescriptTypeCoveragePanel`, `RescriptTypeCoverageScanner`, `RescriptTypeCoverageClassifier`, `RescriptTypeCoverageModel` |
| `statusbar/` | ビルドステータスウィジェット | `RescriptCompilerStatusWidgetFactory` |
| `errorlens/` | Error Lens（行末インライン診断） | `RescriptErrorLensManager` |
| `imports/` | Import Optimizer、open 文ユーティリティ | `RescriptImportOptimizer`, `RescriptImportUtil` |
| `intention/` | Intention Actions（Wrap with、@genType 追加、Rename variant constructor 等） | `RescriptWrapWithIntention`, `RescriptRenameVariantConstructorIntention`, `RescriptConstructorOccurrenceClassifier`, `RescriptConstructorOccurrenceFinder` |
| `surround/` | Surround With | `RescriptSurroundDescriptor` |
| `folding/` | コード折りたたみ | `RescriptFoldingBuilder` |
| `wizard/` | Project Wizard（新規プロジェクト作成、Package Manager / Validation Library 選択 UI） | `RescriptModuleBuilder`, `PackageManager`, `ValidationLibrary` |
| `wizard/templates/` | 22 種類のプロジェクトテンプレートファイル生成（既存 18 件は zod/sury の `Validation.res` を variants/<key>/ から選択。TanStack Start / Remix RR v7 / Astro / Waku は Validation 選択を無効化） | `TemplateScaffold`, `BasicTemplateFiles`, `ViteReactTemplateFiles`, `HonoInertiaTemplateFiles`, `TauriTemplateFiles`, `TanstackStartTemplateFiles`, `RemixV7TemplateFiles`, `AstroTemplateFiles`, `WakuTemplateFiles` 等 |
| `generate/` | Code Generation（Generate メニュー） | `RescriptGenerateGroup`, `RescriptGenerateSwitchAction`, `RescriptJsonCodeGenerator` |
| `binding/` | .d.ts → ReScript バインディング生成 | `DtsGenerateBindingAction`, `DtsToRescriptConverter` |
| `breadcrumb/` | パンくずリストナビゲーション | `RescriptBreadcrumbsProvider` |
| `refactor/` | リネーム、識別子バリデーション | `RescriptRenameHandler` |
| `util/` | 共通ユーティリティ（セキュリティ、offset↔Position 変換、エディタ操作、ツールウィンドウ取り付け等、色 hex 変換、HTML エディタ pane factory、panel 用 EditorTextField 設定、プロジェクト全体ファイル走査ループ、coroutine デバウンス） | `RescriptSecurityUtils`, `RescriptOffsetUtils`, `RescriptEditorUtils`, `RescriptToolWindowContent`, `RescriptColorUtils`, `HtmlEditorPaneFactory`, `EditorTextFieldFactory`, `RescriptProjectFileScanner`, `RescriptCoroutineDebouncer` |
| `ui/` | ToolWindow panel 共通基盤（toolbar / status / debounce 付き refresh の `RescriptToolWindowPanelBase`、Visual ↔ Source カード切替の `DualViewToolWindowPanel`、GraphView 共有描画プリミティブ、caret 追跡） | `RescriptToolWindowPanelBase`, `DualViewToolWindowPanel`, `GraphViewPaintHelpers`, `RescriptEditorCaretTracker` |
| `commenter/` | コメントトグル | `RescriptCommenter` |
| `dependencies/` | パッケージ依存関係ツリー表示 | `RescriptDependenciesToolWindowFactory`, `RescriptDependenciesPanel` |
| `diagram/` | モジュール依存関係ダイアグラム（Visual / Source トグル + Mermaid + DOT エクスポート、`NodeRole` 別ノード色分けと凡例、Source モードは `flow/MermaidSourceColorizer` でトークン色付け） | `RescriptDependencyDiagramToolWindowFactory`, `RescriptDependencyDiagramPanel`, `RescriptDependencyDiagramAction`, `RescriptDependencyDiagramExportAction`, `RescriptDependencyDiagramGraphView`, `RescriptMermaidExporter`, `RescriptDependencyDiagramProvider`, `RescriptDependencyDiagramModel`, `NodeRole` |
| `documentation/` | ドキュメントプロバイダ（Quick Doc、External Doc） | `RescriptDocumentationProvider` |
| `grazie/` | Grazie テキスト抽出連携 | `RescriptGrazieTextExtractor` |
| `navbar/` | ナビゲーションバー | `RescriptStructureAwareNavbar` |
| `ppx/` | PPX 展開ビューツールウィンドウ（`@annotation` を `RescriptSyntaxHighlighter.ANNOTATION` の色で `JEditorPane` HTML レンダリング） | `RescriptPpxViewToolWindowFactory`, `RescriptPpxViewPanel` |
| `projectview/` | Project View ネスト表示・装飾 | `RescriptTreeStructureProvider`, `RescriptProjectViewNodeDecorator` |
| `repl/` | REPL ツールウィンドウ | `RescriptReplToolWindowFactory`, `RescriptReplPanel` |
| `scratch/` | スクラッチファイル | `RescriptScratchRootType`, `RescriptScratchCreationHelper` |
| `typeinfo/` | 型情報ツールウィンドウ（hover 由来の型シグネチャを `EditorTextField` + `RescriptFileType` の viewer モードで表示し、エディタと同じ色付け） | `RescriptTypeInfoToolWindowFactory`, `RescriptTypeInfoPanel` |
| `worksheet/` | Worksheet モード（.resw） | `RescriptWorksheetFileType`, `RescriptWorksheetRunner` |

#### Java ソース (`src/main/java/com/rescript/plugin/lang/`)

| ファイル | 説明 |
|---------|------|
| `Rescript.flex` | JFlex レクサー定義ファイル（手動編集対象） |
| `RescriptFlexLexer.java` | JFlex から自動生成（`.gitignore` 対象、直接編集禁止） |

#### リソース (`src/main/resources/`)

| パス | 説明 |
|------|------|
| `META-INF/plugin.xml` | メインプラグイン記述子（Extension Point 登録） |
| `META-INF/rescript-json.xml` | JSON Schema 提供（optional dep: `com.intellij.modules.json`） |
| `META-INF/rescript-js-injection.xml` | %raw() JS インジェクション（optional dep: `JavaScript`） |
| `META-INF/rescript-markdown.xml` | Markdown コードフェンス（optional dep: `org.intellij.plugins.markdown`） |
| `META-INF/rescript-debug.xml` | デバッグ統合（optional dep: `JavaScriptDebugger`） |
| `META-INF/rescript-nodejs.xml` | Node.js 統合（optional dep: `NodeJS`） |
| `colorSchemes/` | Darcula / Default テーマ用カラースキーム XML |
| `liveTemplates/ReScript.xml` | 21 種類の Live Template スニペット |
| `fileTemplates/internal/` | ファイル作成テンプレート（Module / Interface / Component） |
| `scripts/dts-to-json.js` | バンドル Node.js スクリプト（.d.ts パーサー） |
| `schemas/rescript.schema.json` | rescript.json 用 JSON Schema |
| `icons/` | SVG アイコン（.res / .resi / rescript.json 用、Light/Dark 対応） |
| `templates/` | Wizard テンプレートの静的ファイル（`.res` サンプル・README セクション・`drizzle.config.ts` 等）。`TemplateResourceLoader` 経由でロード |

### 2.2 テストコード (`src/test/`)

```
src/test/kotlin/com/rescript/plugin/
├── RescriptTestUtils.kt           # テスト共通ユーティリティ
├── lang/                          # レクサー、パーサー、トークンのテスト
├── highlight/                     # ハイライト、ブレースマッチングのテスト
├── lsp/                           # LSP 関連コンポーネントのテスト
├── ...                            # 各パッケージに対応するテストパッケージ
├── perf/                          # 純粋関数の smoke benchmark（collector / model / scanner / classifier）
├── cli/                           # 外部 CLI 結合テスト（mmdc / graphviz dot / rescript convert、CLI 不在時は skip）
└── wizard/templates/              # テンプレート依存バージョンのテスト
```

テストは対象クラスと同じパッケージ構造に配置し、`<対象クラス名>Test.kt` と命名する。`perf/` と `cli/` は単一のプロダクションパッケージに紐付かず、複数モジュールにまたがる実行時間ガード / 外部 CLI 結合検証を集約する役割を持つ。`cli/` のテストは `Assumptions.assumeTrue` で対応 CLI の可用性をゲートし、ローカル不在時は skip、CI（`ci.yml` の build ジョブで `mmdc` / `graphviz` をインストール）では実行される。

VFS write action のような「light fixture では駆動できない」挙動を要するテストは、`IntelliJPlatformExtensionWithContentRoot`（heavy fixture、`IdeaTestFixtureFactory.createFixtureBuilder` ベース）を使う。1 件あたり 3〜10 秒のセットアップコストがあるため、本当に必要なテストに限定して使う。

## 3. ドキュメント

### 3.1 永続的ドキュメント (`docs/`)

プロジェクト全体の設計・要件を定義する恒久的なドキュメント。

| ファイル | 内容 |
|---------|------|
| `product-requirements.md` | プロダクト要求定義書（ビジョン、ユーザーストーリー、機能一覧） |
| `functional-design.md` | 機能設計書（コンポーネント設計、Extension Point マップ） |
| `development-guidelines.md` | 開発ガイドライン（コーディング規約、テスト規約） |
| `architecture.md` | 技術仕様書（テクノロジースタック、制約、パフォーマンス要件） |
| `repository-structure.md` | リポジトリ構造定義書（本ドキュメント） |
| `glossary.md` | ユビキタス言語定義（用語集） |
| `ideas/concept.md` | アイデア・コンセプトメモ |

### 3.2 Sphinx ドキュメント (`sphinx-docs/`)

ユーザー向け・開発者向けのドキュメントサイト。GitHub Pages でホスティングされる。

```
sphinx-docs/
├── user/                     # ユーザー向けガイド
│   ├── installation.md       # インストール手順
│   ├── quickstart.md         # クイックスタート
│   ├── features/             # 機能別ガイド
│   ├── troubleshooting.md    # トラブルシューティング
│   └── changelog.md          # 変更履歴
├── dev/                      # 開発者向けガイド
│   ├── setup.md              # 開発環境セットアップ
│   ├── building.md           # ビルド手順
│   ├── project-structure.md  # プロジェクト構造
│   ├── extending.md          # 拡張方法
│   ├── testing.md            # テスト
│   └── contributing.md       # コントリビュートガイド
├── locale/ja/LC_MESSAGES/    # 日本語翻訳 (.po ファイル)
├── conf.py                   # Sphinx 設定
└── Makefile                  # ビルドコマンド
```

### 3.3 ステアリングドキュメント (`.steering/`)

作業単位の一時的なドキュメント。作業完了後は履歴として保持される。

```
.steering/[YYYYMMDD]-[NNN]-[開発タイトル]/
├── requirements.md           # 要求内容
├── design.md                 # 設計
└── tasklist.md               # タスクリスト
```

## 4. 設定ファイル

### 4.1 Claude Code (`.claude/`)

```
.claude/
├── rules/                    # プロジェクト固有のルール
│   ├── testing.md            # テスト規約
│   ├── code-comments.md      # コードコメント規約（KDoc）
│   ├── git-conventions.md    # Git コミット・ブランチ規約
│   ├── steering-workflow.md  # ステアリングワークフロー
│   └── documentation.md      # ドキュメント管理規約
└── settings.json             # Claude Code ローカル設定
```

### 4.2 GitHub (`.github/`)

```
.github/
├── workflows/
│   ├── ci.yml                # CI（ビルド、テスト、カバレッジ）
│   ├── release.yml           # リリース（タグベース）
│   └── docs.yml              # Sphinx ドキュメントビルド・デプロイ
└── dependabot.yml            # Dependabot 設定
```

## 5. ファイル配置ルール

### 新しい機能を追加する場合

1. **パッケージ選択**: 既存パッケージの責務に該当するものがあればそこに配置する。該当しない場合は新しいパッケージを作成する
2. **Extension Point 登録**: `src/main/resources/META-INF/plugin.xml` に登録する。オプション依存の場合は `rescript-*.xml` に分離する
3. **テスト作成**: `src/test/kotlin/com/rescript/plugin/<パッケージ>/` に `<クラス名>Test.kt` を作成する
4. **ドキュメント更新**: 必要に応じて `CLAUDE.md`、`docs/functional-design.md`（Extension Point マップ）を更新する

### 命名規則

| 対象 | 規則 | 例 |
|------|------|-----|
| Kotlin ファイル | PascalCase、`Rescript` プレフィックス | `RescriptFoldingBuilder.kt` |
| テストファイル | 対象クラス名 + `Test` サフィックス | `RescriptFoldingBuilderTest.kt` |
| リソースファイル | kebab-case | `rescript-json.xml` |
| パッケージ | lowercase、単一単語推奨 | `folding/`, `errorlens/`, `codevision/` |

### 自動生成ファイル

以下のファイルは自動生成されるため、手動編集しないこと:

| ファイル | 生成元 | 生成タスク |
|---------|--------|-----------|
| `src/main/java/.../RescriptFlexLexer.java` | `Rescript.flex` | `generateRescriptLexer` |
| `build/` | ソースコード全体 | `buildPlugin` |
| `sphinx-docs/_build/` | Sphinx ソース | `make build-all` |
