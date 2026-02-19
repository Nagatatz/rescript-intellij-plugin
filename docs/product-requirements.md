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
| Project Wizard | New Project ダイアログから ReScript プロジェクトテンプレート作成 | `RescriptModuleBuilder` + `RescriptProjectWizardStep` |
| Code Generation | variant switch arms / module type スケルトン自動生成 (Cmd+N) | `RescriptGenerateGroup` + `RescriptTypeDeclarationParser` |

### 将来機能（ロードマップ） — ギャップ分析

rescript-vscode（公式 VS Code 拡張）および他の JetBrains 言語プラグイン（JS/TS, Kotlin, Elm, Dart, CoffeeScript, Svelte, ReasonML 等）との機能比較に基づき、未実装機能を優先度別に整理する。

#### P1（高優先度） — ユーザー体験に大きく影響

**他の JetBrains 言語プラグインとのギャップ（低コスト・高インパクト）:**

| 機能 | 説明 | 実装アプローチ | 難易度 | 参考プラグイン |
|---|---|---|---|---|
(全 P1 機能が実装済み)

#### P2（中優先度） — あると便利

**rescript-vscode とのギャップ:**

| 機能 | 説明 | 実装アプローチ | 難易度 |
|---|---|---|---|
(全 P2 rescript-vscode ギャップ機能が実装済み)

**他の JetBrains 言語プラグインとのギャップ:**

| 機能 | 説明 | 実装アプローチ | 難易度 | 参考プラグイン |
|---|---|---|---|---|
(全 P2 JetBrains ギャップ機能が実装済み)

#### P3（低優先度） — nice-to-have

**rescript-vscode とのギャップ:**

| 機能 | 説明 | 実装アプローチ | 難易度 |
|---|---|---|---|
| ~~reanalyze 統合~~ | ~~デッドコード分析、未処理例外分析~~ | ~~実装済み~~ | ~~高~~ |
| ~~Markdown ReScript ハイライト~~ | ~~` ```rescript ` コードブロックのハイライト~~ | ~~実装済み~~ | ~~低~~ |
| ~~Paste as JSON.t~~ | ~~クリップボード変換ペースト~~ | ~~実装済み~~ | ~~中~~ |
| ~~`//#region` 折りたたみ~~ | ~~カスタム折りたたみマーカー~~ | ~~実装済み~~ | ~~低~~ |
| ~~Incremental Type Checking 設定~~ | ~~LSP の incremental typechecking 設定~~ | ~~実装済み~~ | ~~低〜中~~ |
| JetBrains Marketplace 公開 | プラグインを Marketplace に公開 | Gradle `publishPlugin` タスク設定 | 中 |

**他の JetBrains 言語プラグインとのギャップ:**

| 機能 | 説明 | 実装アプローチ | 難易度 | 参考プラグイン |
|---|---|---|---|---|
| ~~Test Runner Integration~~ | ~~IDE 内テスト実行・結果表示（GUI テストランナー）~~ | ~~実装済み~~ | ~~中〜高~~ | ~~Elm, Svelte, Dart, Kotlin~~ |
| ~~Compiled JS Preview~~ | ~~ReScript ⇔ 生成 JS のツールウィンドウ表示~~ | ~~実装済み~~ | ~~中~~ | ~~CoffeeScript (split view), Kotlin (decompile)~~ |
| ~~Project Wizard~~ | ~~新規 ReScript プロジェクトテンプレート~~ | ~~実装済み~~ | ~~中~~ | ~~Svelte, Dart, Kotlin~~ |
| ~~Smart Enter~~ | ~~文の自動補完と改行（括弧閉じ等）~~ | ~~実装済み~~ | ~~中~~ | ~~Kotlin, JS/TS~~ |
| ~~Statement Up/Down Mover~~ | ~~宣言単位の上下移動~~ | ~~実装済み~~ | ~~低~~ | ~~Kotlin, JS/TS~~ |
| ~~Unused Code Detection~~ | ~~未使用変数・関数・open の検出（Quick Fix + Global Inspection）~~ | ~~実装済み~~ | ~~中~~ | ~~Elm, Dart, Kotlin~~ |
| ~~Module Hierarchy~~ | ~~モジュールネスト・依存関係ビュー（open/include）~~ | ~~実装済み（Type/Call Hierarchy の代替）~~ | ~~高~~ | ~~Kotlin, JS/TS, Dart~~ |
| ~~Qualified Name Copy~~ | ~~完全修飾名（`Module.subModule.name`）のコピー~~ | ~~実装済み~~ | ~~低~~ | ~~Kotlin, JS/TS~~ |
| ~~Code Generation~~ | ~~variant arms 自動生成、module type 生成等~~ | ~~実装済み~~ | ~~中〜高~~ | ~~Elm (JSON enc/dec), Dart, Kotlin~~ |

## 4. 成功の定義

### 定量指標

| 指標 | 目標値 |
|---|---|
| JetBrains Marketplace 公開 | v1.0 リリース |
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
| 対象 IDE | IntelliJ IDEA Ultimate 2025.3+（将来的に Community Edition 対応） |
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
- [ ] `.res` / `.resi` ファイルを開くと自動的に ReScript として認識される
- [ ] キーワード（`let`、`type`、`module`、`switch`、`if` 等）が正しくハイライトされる
- [ ] 文字列リテラル（通常文字列、テンプレート文字列）が正しくハイライトされる
- [ ] コメント（行コメント、ブロックコメント、ドキュメントコメント）が正しくハイライトされる
- [ ] 数値リテラル（整数、浮動小数点）が正しくハイライトされる
- [ ] 演算子が正しくハイライトされる
- [ ] デコレータ（`@`）が正しくハイライトされる

### US-02: コード補完

**ユーザーとして**、ReScript コードを入力中に適切な補完候補が表示されることで、効率的にコードを書きたい。

**受け入れ条件:**
- [ ] 変数名、関数名、モジュール名の補完候補が表示される
- [ ] 型情報に基づいた適切な候補が優先表示される
- [ ] `@rescript/language-server` が利用可能な環境で動作する

### US-03: 定義ジャンプ

**ユーザーとして**、シンボルの定義元に素早く移動できることで、コードの理解を深めたい。

**受け入れ条件:**
- [ ] Ctrl+Click（または Ctrl+B）でシンボルの定義元にジャンプできる
- [ ] 同一ファイル内、他ファイル、他モジュールの定義にジャンプできる

### US-04: リアルタイム診断

**ユーザーとして**、コード編集中にエラーや警告がリアルタイムで表示されることで、問題を早期に発見したい。

**受け入れ条件:**
- [ ] コンパイルエラーが赤い波線でインライン表示される
- [ ] 警告が黄色い波線でインライン表示される
- [ ] エラー・警告の詳細がホバーで確認できる
- [ ] Problems パネルにエラー・警告の一覧が表示される

### US-05: コード折りたたみ

**ユーザーとして**、モジュールや関数定義を折りたたむことで、大きなファイルの構造を把握しやすくしたい。

**受け入れ条件:**
- [ ] `module` ブロックを折りたたみ・展開できる
- [ ] `let` バインディングのブロックを折りたたみ・展開できる
- [ ] `type` 定義を折りたたみ・展開できる
- [ ] ブロックコメントを折りたたみ・展開できる

### US-06: ホバードキュメント

**ユーザーとして**、シンボルにカーソルを合わせた際に型情報やドキュメントが表示されることで、コードの理解を効率化したい。

**受け入れ条件:**
- [ ] シンボルにカーソルを合わせると型情報が表示される
- [ ] ドキュメントコメントがある場合はその内容も表示される

### US-07: ブレースマッチング

**ユーザーとして**、対応するブレースが強調表示されることで、ネストした構造を正しく把握したい。

**受け入れ条件:**
- [ ] `{}`、`[]`、`()` の対応がハイライト表示される
- [ ] カーソル位置のブレースに対応するブレースが強調される

### US-08: コメントトグル

**ユーザーとして**、キーボードショートカットでコメントの切り替えができることで、デバッグ作業を効率化したい。

**受け入れ条件:**
- [ ] Ctrl+/ で行コメントをトグルできる
- [ ] Ctrl+Shift+/ でブロックコメントをトグルできる

### US-09: 参照検索

**ユーザーとして**、シンボルの使用箇所を一覧で確認できることで、変更の影響範囲を把握したい。

**受け入れ条件:**
- [ ] シンボルを選択して「Find Usages」で使用箇所一覧が表示される
- [ ] 検索結果から各使用箇所にジャンプできる

### US-10: インレイヒント

**ユーザーとして**、推論された型がインラインで表示されることで、明示的な型注釈なしでも型を確認したい。

**受け入れ条件:**
- [ ] 型推論された変数やパラメータの横に型ヒントが薄く表示される
- [ ] インレイヒントの表示/非表示を設定で切り替えられる

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

- コードベースを小さく保つ（目標: Kotlin 1,000行以下 + JFlex 定義 500行以下）
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
