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
# サンドボックス内の古いプラグイン jar は prepareSandbox 時に自動で除去される
# （pluginVersion バンプ後の stale jar 起因の PluginException を防ぐため）。
# 完全クリーンが必要な場合のみ `./gradlew clean runIde` を使う。

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
| CI | `ci.yml` | Push/PR to `main` | ビルド、テスト、ktlint、カバレッジ（`koverVerify` で minBound 強制）、プラグイン検証。PR 時のみ `mutation-test` ジョブで PIT を `util/` `lang/` に対し実行 |
| Release | `release.yml` | Tag `v*.*.*` | GitHub Release 作成 |
| Docs | `docs.yml` | Push/PR to `main` (`sphinx-docs/` 変更時) | Sphinx ドキュメントのビルド・デプロイ |
| Monthly Verify | `monthly-verify.yml` | Cron（毎月 1 日） | `verifyPlugin` の実行と `plugin-verifier-ignored-problems.txt` の `Expires:` 期限切れエントリの警告 |

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
- **モノレポ対応** (`RescriptWorkspaceDiscovery`) — `rescript.json` がルート直下にない pnpm/npm/yarn ワークスペース構成を自動検出する。優先度は「Settings の `packageRoots` 手動指定 → `pnpm-workspace.yaml` / `package.json#workspaces` の glob 展開 → depth ≤ 4 の再帰スキャン → 親方向走査」。Inspection・ステータスバー・LSP startup notification・LSP バイナリ探索のすべてがこの検出結果を共有する
- **セマンティックトークンハイライト** (`RescriptSemanticTokensSupport.kt`) — LSP セマンティックトークンによる高精度な色分け
- **カスタム LSP リクエスト** (`RescriptLanguageServer.kt`) — `createInterface`, `openCompiled` 等の ReScript 固有リクエスト
- **カスタム LSP 通知** (`RescriptLsp4jClient.kt`) — `rescript/compilationStatus` 通知受信
- **Code Lens** (`RescriptCodeVisionProvider.kt`) — CodeVision API 経由で関数の型注釈を表示
- **Type Narrowing Visualizer** (`narrowing/`) — `switch` の各 arm でスクラティニーがどの型に絞り込まれたかを LSP hover の結果に基づいてインレイヒントで表示。さらに pattern binding (`| Some(x) =>` の `x` 等) の直後にも、その binding 単体の narrowing 後の型を別ヒントとして出す
- LSP 未接続時の機能ごとの振る舞い（フル機能 / 部分機能 / 不可）は `docs/lsp-fallback-matrix.md` を参照

### レイヤー 3: IDE 統合機能

IDE 統合機能の完全なカテゴリ一覧・パッケージ対応・Extension Point 対応は以下を参照:

- 機能カテゴリ別の解説: `docs/functional-design.md`
- パッケージ構成: `docs/repository-structure.md`
- ユーザー向けサマリ: `README.md` の Features セクション

Variant Flow Diagram (`flow/`) はカーソル位置の `switch` 式を decision tree として ToolWindow に表示する純構文ベースの機能で、`narrowing/` の `RescriptSwitchArmCollector` を再利用しつつ LSP には依存しない。ツールバーの **Visual / Source トグル** で表示モードを切替できる。Visual モードは `RescriptVariantFlowGraphView` (純粋な `computeLayout` + Java2D 描画) でルートとアームを赤系の角丸ボックスとオーソゴナル矢印で描く。Source モードは従来通り Mermaid `flowchart TD` ソーステキスト表示で、Copy Mermaid / Copy DOT のツールバーアクションで外部の Mermaid Live や graphviz `dot` に持ち出せる。

Module Dependency Diagram (`diagram/`) は `open` / `include` 関係を辿ってプロジェクト内の `.res` モジュールから有向依存グラフを組み立て、ToolWindow に表示する LSP 非依存の機能。ツールバーの **Visual / Source トグル** で表示モードを切替できる。Visual モードは `RescriptDependencyDiagramGraphView` (純粋な `computeLayout` + Java2D 描画) でモジュールを赤系の角丸ボックス、依存方向をオーソゴナル矢印で描く。レイヤー割当は Kahn の BFS で、in-degree 0 のモジュール（誰からも依存されていないエントリポイント候補）が一番上の layer 0 に置かれ、下流の依存が下に積まれる。サイクル内のノードは Kahn の残ノードとして最下位の追加 layer にまとめて描かれる。Source モードは従来通り Mermaid `flowchart TD` ソーステキスト表示で、Copy Mermaid / Copy DOT のツールバーアクションを両モードから利用できる。

Type Impact Preview (`impact/`) はカーソル位置の `type` 宣言に対するプロジェクト全体の参照箇所を ToolWindow に一覧表示し、型変更の波及範囲を事前に見積もれるようにする。`PsiSearchHelper` で word-index ベースの参照検索を行い、`RescriptReferenceClassifier` のトークン・ヒューリスティックで type-ref / constructor / pattern / field-access に分類する。LSP 不要・200 件のソフトキャップ付き。

Notebook 風 Worksheet (`notebook/`) は `.resnb` 拡張子の JSON ファイルを cell-based エディタで開き、各セルを独立に評価できるようにする。既存 `repl/RescriptReplExecutor` をセル評価のバックエンドとして再利用し、評価結果を `cell.lastOutput` としてファイルに persist する。Markdown エクスポートで PR / 設計書への共有も可能。LSP 不要。

JS Interop Risk Map (`interop/`) は `%raw` / `external` / `Obj.magic` / `@bs.*` などの「型システムから抜け出す」呼び出し箇所をプロジェクト全体でスキャンし、ToolWindow に一覧表示する。`RescriptInteropClassifier` のトークン・ヒューリスティックで `(kind, risk)` を判定し、HIGH → MEDIUM → LOW で並べ替え。`FileTypeIndex` ベースの project スコープ走査・LSP 不要・500 件のソフトキャップ付き。

Reason → ReScript Migration Pilot (`migration/`) はプロジェクト内の `.re` / `.rei` ファイルを ToolWindow にチェックボックス付きで一覧表示し、選択した複数ファイルを `rescript convert` CLI 経由で `.res` / `.resi` に一括変換する。`FilenameIndex.getAllFilesByExt` でファイル列挙、`ProcessBuilder` でサブプロセス実行（タイムアウト 30 秒）、成功時は VFS write action でリネーム。LSP 不要・並列実行は将来検討。`rescript convert` は **ReScript 11 系まで** の機能で、v12 (rewatch ベース CLI) では削除済み。Converter は実行前に `rescript --version` を probe し、major が 12 以上なら "Pin `rescript@^11` to use the Migration Pilot" の actionable エラーで早期に bail-out する。

Type Coverage Heat Map (`coverage/`) はプロジェクト内の `.res` ファイルごとに「トップレベル `let` 宣言のうち、どれだけが明示的に型注釈を持っているか」を表形式で可視化する。`RescriptTypeCoverageClassifier` の depth-0 `:` ヒューリスティック (パラメータリストや record literal 内の `:` は無視) でアノテーション有無を判定し、ToolWindow にファイル / 総数 / Annotated / Inferred / Coverage % をソート可能なテーブルで表示。デフォルトは coverage % 昇順なので「型を足したいファイル」が上に並ぶ。色分け: < 30% 赤 / 30〜69% 黄 / ≥ 70% 緑。`FileTypeIndex` ベースの project スコープ走査・LSP 不要・2,000 ファイルのハードキャップ付き。パラメータ単位の annotated 判定や LSP hover ベースの精度向上は将来検討。

Add Missing Switch Arms Intention (`intention/RescriptAddMissingSwitchArmsIntention`) は書きかけの `switch` 式に対して LSP hover でスクラティニーの型を取得し、`RescriptLspUtils.parseVariantConstructors` の constructor 集合と既存アームの差集合を `RescriptMissingArmsBuilder` で算出して、不足アーム (`| Name(_) => todo` / `| Name => todo`) を閉じ `}` 直前に挿入する Alt+Enter Intention。`_` ワイルドカードや LIDENT 単独 binding を含む switch では非表示。or-pattern (`| Foo | Bar`) は両 constructor をカバー済として認識し、ネストした switch ではカーソル位置の最内 switch のみが対象になる。LSP hover が `color` のような **bare type name** だけを返した場合は、`RescriptVariantTypeResolver` が `RescriptNameIndex` 経由でプロジェクト内の `type <name> = | ...` 宣言を検索し、`RescriptTypeDeclarationParser` で RHS を再パースする 2nd-pass で constructor 集合を補完する。

Rename Variant Constructor Intention (`intention/RescriptRenameVariantConstructorIntention`) は variant constructor (UIDENT) にキャレットを置いた状態の Alt+Enter で起動する LSP 非依存のリネーム機能。`RescriptConstructorOccurrenceClassifier` がトークンの前後関係から `CONSTRUCTOR` / `PATTERN` / `MODULE_QUALIFIED_TAIL` / `OTHER` を判定し、`RescriptConstructorOccurrenceFinder` が `PsiSearchHelper` の word-index 経由でプロジェクト全体の出現箇所を集めて分類器でフィルタする。出現件数とファイル数を確認ダイアログで提示してから単一の `WriteCommandAction` で一括リネームするので Undo も 1 ステップ。500 件のハードキャップを超えた場合は「Shift+F6 (LSP rename) を使うか対象を絞ってください」のメッセージで中止する。Shift+F6 の `RescriptRenameHandler` (LSP rename) と独立して動き、LSP 未起動環境でも動作する。

Hoogle-style Type Signature Search (`navigation/RescriptTypeSignatureSearchContributor`) は Search Everywhere の "ReScript Types" タブで、ユーザーが入力した型シグネチャ (`(int, string) => result<int, string>` や `=> option<'a>` など) をプロジェクト全体の `let` / `external` / `type` の `: T` 注釈と **構造的に** 照合する。`RescriptDeclarationSignatureExtractor` が候補ソースから binding 名と `: T` 注釈テキストを抽出し、`RescriptTypeParser` がクエリと候補の双方を `RescriptTypeAst` に変換し、`RescriptTypeUnifier` が EXACT / TVAR_MATCH / PARTIAL / MISMATCH を判定して `MISMATCH` 以外を結果リストに表示する。先頭が `=>` のクエリは "返り値 T を持つ関数" の検索モード (`PARTIAL` スコア)、クエリ側の `'a` は具体型に対応するワイルドカード (`TVAR_MATCH` スコア)。`RescriptTypeSignatureCellRenderer` が `name: signature  (path:line)` で結果を描画し、選択時に binding 位置にジャンプする。レコード型・ポリモーフィックバリアント・ラベル引数は v1 ではパースしない。LSP 不要。

Project Wizard (`wizard/`) は Package Manager と Validation Library (`zod` / `sury`) の選択 UI を備える。22 テンプレートのうち既存 18 件 (hono-inertia と tauri を含む) は選択に応じて `Validation.res` を `variants/<key>/` から生成する。検証対象はテンプレートごとに異なる: サーバー系 10 テンプレート（hono / hono-graphql / hono-inertia / aws-lambda / cloudflare-workers / google-cloud-run / nextjs / full-stack / monorepo / res-x）は HTTP 入力、CLI Tool は `init` サブコマンドのオプション、npm Library は public API 引数、Basic は `config.json` の shape、Electron / Tauri は IPC レスポンス、React Native (Expo / CLI) と Vite+React はフォーム入力を対象にする。res-x テンプレートは Bun + Vite + HTMX 前提で `package.json` の scripts に `bun` コマンドを直接書き込む。Hono + Inertia テンプレートは Hono バックエンド + `@inertiajs/react` v3 + Vite+ 統合 (`vp dev` / `vp build` / `vp test` / `vp check`) で server-driven SPA を提供する。SSR がデフォルト: 非 Inertia 訪問 (初回 GET / 検索ボット / OGP) では `src/Ssr.res` が `react-dom/server` の `renderToString` で React コンポーネントを事前レンダリングして `<div id="app">` に埋め込み、ブラウザ側 `Main.res` は `hydrateRoot` で hydration する。Inertia ナビゲーション (`X-Inertia: true`) は middleware が JSON で返すため SSR を迂回する。Tauri テンプレートは Tauri 2.x + Vite+ React renderer + `@rescript-tauri/core` で IPC を組み立て、`tauri.conf.json` の `beforeDevCommand` / `beforeBuildCommand` を選択された package manager (`pnpm dev` / `npm run dev` / `yarn dev` / `bun run dev`) に応じて書き出す。Rust 側は `src-tauri/` に `greet` と `get_info` の 2 つの `#[tauri::command]` を含む最小骨組みを生成する。新規 4 テンプレート（TanStack Start / Remix RR v7 / Astro / Waku）はフレームワークが独自のデータレイヤーを持つため `ProjectTemplate.supportsValidationSelection = false` を宣言し、Wizard Step UI は Validation コンボを非表示にする。

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

コンパクション時は常に以下を保持すること:
- 現在の作業ブランチと worktree のパス
- 現在アクティブな `.steering/` ディレクトリのパスと `tasklist.md` の進捗
- 現在のセッション内で変更・新規作成したファイルの一覧
- 発生したビルドエラー・テスト失敗の内容

Task ツール（サブエージェント）を使用する場合、`run_in_background` は **明示的に指示された場合のみ** 使用すること。

## セキュリティ

- 外部入力（LSP サーバーレスポンス、ファイルシステムパス、JSON 設定のパース結果）はすべて検証すること
- 外部プロセスの実行には `ProcessBuilder` に明示的な引数リストを渡すこと。ユーザー入力をコマンド文字列に連結してはならない
- ユーザー向け UI 要素やエラーメッセージに絶対パスを露出させないこと
- LSP レスポンス由来のファイルパスは、ファイル操作に利用する前にサニタイズすること

## ロードマップ

@docs/product-requirements.md
