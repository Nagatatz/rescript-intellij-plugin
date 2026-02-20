# ユビキタス言語定義 (Glossary)

## 1. ドメイン用語

### ReScript 言語

| 用語 | 英語 | 定義 | コード上の表現 |
|------|------|------|---------------|
| バインディング | Binding | `let` で定義される値の束縛。関数定義もバインディングの一種 | `LET_DECLARATION` |
| モジュール | Module | ReScript のモジュール。ファイル単位（ファイルモジュール）またはネスト定義（`module M = { ... }`） | `MODULE_DECLARATION` |
| インターフェース | Interface | `.resi` ファイルで定義されるモジュールの公開シグネチャ | `RescriptInterfaceFileType` |
| バリアント | Variant | 直和型のコンストラクタ（`type t = A \| B(int)`） | `UIDENT`（大文字開始識別子） |
| ポリモーフィックバリアント | Polymorphic Variant | `#Tag` 形式の構造的バリアント | `POLY_VARIANT` トークン |
| レコード | Record | `{ field: type }` 形式の積型 | — (LSP で型推論) |
| パイプ演算子 | Pipe Operator | `\|>` による関数合成（`x \|> f` は `f(x)` と等価） | `PIPE` トークン |
| デコレータ | Decorator | `@annotation` 形式のメタデータ（`@module`, `@genType` 等） | `ANNOTATION` PSI 要素 |
| 外部宣言 | External | JavaScript FFI 宣言（`external name: type = "jsName"`） | `EXTERNAL_DECLARATION` |
| JSX | JSX | React コンポーネント記法（`<Component prop={value} />`） | `JSX_ELEMENT`, `JSX_SELF_CLOSING_ELEMENT`, `JSX_FRAGMENT` |
| テンプレートリテラル | Template Literal | バッククォート文字列（`` `Hello ${name}` ``） | `TEMPLATE_STRING_*` トークン |
| 型引数 | Type Parameter | `'a`, `'b` 形式の多相型パラメータ | `TYPE_ARG` トークン |

### rescript-language-server

| 用語 | 英語 | 定義 | コード上の表現 |
|------|------|------|---------------|
| LSP サーバー | LSP Server | `@rescript/language-server` Node.js プロセス | `RescriptLspServerDescriptor` |
| セマンティックトークン | Semantic Token | LSP が返す意味解析ベースのトークン情報 | `RescriptSemanticTokensSupport` |
| コンパイルステータス | Compilation Status | LSP のカスタム通知 `rescript/compilationStatus` | `RescriptCompilationStatusService` |
| Code Lens | Code Lens | LSP `textDocument/codeLens` による型注釈表示 | `RescriptCodeVisionProvider` |
| インレイヒント | Inlay Hint | 推論された型のインライン表示 | LSP `textDocument/inlayHint` |

## 2. IntelliJ Platform 用語

### 基盤概念

| 用語 | 英語 | 定義 | コード上の表現 |
|------|------|------|---------------|
| PSI | Program Structure Interface | ソースコードの構文木表現。パース結果のノードツリー | `RescriptPsi`, `PsiElement` |
| PSI ツリー | PSI Tree | ファイル全体の構文木。ルートは `PsiFile` | `RescriptFile` |
| レクサー | Lexer | ソースコードをトークン列に分解するコンポーネント | `RescriptLexer` (FlexAdapter) |
| パーサー | Parser | トークン列から PSI ツリーを構築するコンポーネント | `RescriptParser` |
| トークン | Token / IElementType | レクサーが出力するソースコードの最小単位 | `RescriptTokenTypes` |
| TokenSet | TokenSet | 関連するトークンのグループ | `RescriptTokenTypes.KEYWORDS` |
| TextAttributesKey | TextAttributesKey | ハイライト色を識別するキー | `RescriptSyntaxHighlighter` |
| Extension Point | Extension Point | プラットフォームの拡張ポイント（`plugin.xml` で登録） | `plugin.xml` |

### IDE 機能

| 用語 | 英語 | 定義 | コード上の表現 |
|------|------|------|---------------|
| ストラクチャービュー | Structure View | ファイル内のシンボルツリー表示 | `RescriptStructureViewFactory` |
| コード折りたたみ | Code Folding | ブロックの展開/折りたたみ | `RescriptFoldingBuilder` |
| ブレースマッチング | Brace Matching | 対応する括弧のハイライト | `RescriptBraceMatcher` |
| Live Template | Live Template | コードスニペット補完 | `liveTemplates/ReScript.xml` |
| Postfix Completion | Postfix Completion | 式の後方からのコード補完（`.switch`, `.pipe` 等） | `RescriptPostfixTemplateProvider` |
| Intention Action | Intention Action | Alt+Enter で表示される提案アクション | `RescriptWrapWithIntention` |
| Surround With | Surround With | 選択コードを構文で囲む（Ctrl+Alt+T） | `RescriptSurroundDescriptor` |
| Code Vision | Code Vision | エディタ行上部に表示される追加情報（Code Lens の JetBrains 実装） | `RescriptCodeVisionProvider` |
| Run Configuration | Run Configuration | IDE 内のプログラム実行設定 | `RescriptRunConfigurationType` |
| External Annotator | External Annotator | 外部ツールからの注釈表示 | `RescriptReanalyzeAnnotator` |
| Local Inspection | Local Inspection | ファイル単位のコード検査 | `RescriptDuplicateOpenInspection` |
| Global Inspection | Global Inspection | プロジェクト全体のコード検査 | `RescriptUnusedCodeInspection` |
| Module Builder | Module Builder | New Project ウィザードのプロジェクト作成 | `RescriptModuleBuilder` |

## 3. ビルド・CI 用語

| 用語 | 英語 | 定義 |
|------|------|------|
| Gradle Wrapper | Gradle Wrapper | プロジェクト固有の Gradle バージョンを保証するラッパースクリプト |
| Configuration Cache | Configuration Cache | Gradle のビルド構成キャッシュ（再構成をスキップ） |
| GrammarKit | GrammarKit | JFlex/BNF からレクサー/パーサーを生成する Gradle プラグイン |
| ktlint | ktlint | Kotlin コードスタイルチェッカー/フォーマッター |
| Kover | Kover | Kotlin コードカバレッジツール |
| Qodana | Qodana | JetBrains の静的解析プラットフォーム |
| Plugin Verifier | Plugin Verifier | IntelliJ プラグインの API 互換性検証ツール |

## 4. プロジェクト固有の略語

| 略語 | 正式名称 | 説明 |
|------|---------|------|
| LSP | Language Server Protocol | エディタと言語サーバー間の通信プロトコル |
| PSI | Program Structure Interface | IntelliJ Platform の構文木 API |
| FFI | Foreign Function Interface | ReScript から JavaScript を呼び出すインターフェース |
| JSX | JavaScript XML | React のコンポーネント記法 |
| DTS | Declaration Type Script (`.d.ts`) | TypeScript 型定義ファイル |
| SMTRunner | Service Message Test Runner | JetBrains のテスト結果表示フレームワーク |

## 5. コード上の命名規則

### プレフィックス

| プレフィックス | 用途 | 例 |
|--------------|------|-----|
| `Rescript` | プラグインのクラス | `RescriptLanguage`, `RescriptLexer` |
| `RESCRIPT_` | 定数、TextAttributesKey | `RESCRIPT_SEMANTIC_VARIABLE` |
| `Dts` | .d.ts バインディング生成関連 | `DtsJsonModel`, `DtsTypeMapper` |

### サフィックス

| サフィックス | 用途 | 例 |
|-------------|------|-----|
| `Provider` | データ/サービスの提供者 | `RescriptBreadcrumbsProvider` |
| `Factory` | インスタンス生成 | `RescriptConfigurationFactory` |
| `Action` | ユーザーアクション | `RescriptSwitchFileAction` |
| `Detector` | 検出・判定ロジック | `RescriptLspDetector` |
| `Installer` | インストール処理 | `RescriptLspInstaller` |
| `Contributor` | Platform への貢献（検索結果、マーカー等） | `RescriptSymbolContributor` |
| `Descriptor` | 設定・記述の定義 | `RescriptLspServerDescriptor` |
| `Service` | 永続的な状態保持サービス | `RescriptCompilationStatusService` |
| `Support` | 機能サポート実装 | `RescriptSemanticTokensSupport` |
| `Test` | テストクラス | `RescriptLexerTest` |
