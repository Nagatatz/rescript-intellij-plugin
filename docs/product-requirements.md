# プロダクト要求定義書 (Product Requirements Document)

## 1. プロダクトビジョンと目的

### ビジョン

ReScript 開発者が JetBrains IDE で快適に開発できる、高品質な言語サポートプラグインを提供する。

### 目的

- 既存の reasonml-idea-plugin は 2025-09-01 の v0.131 リリース以降コードコミットが README 更新と dependabot bump 中心になり低頻度メンテに移行しているため、ReScript 専用のクリーンな代替プラグインを提供する
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

1. **活発な代替手段の不在** — 既存プラグイン (reasonml-idea-plugin) は 2025-09 を最後にコードリリースが停滞しており、最新の ReScript バージョンへの追従が遅い
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

### 実装済み機能一覧

実装済み機能の完全なリストは以下を参照:

- ユーザー向けサマリ: `README.md` の Features セクション
- パッケージ/クラス単位の対応表: `docs/functional-design.md` の Extension Point マップ
- 機能カテゴリ別の解説: `sphinx-docs/user/features/`

当初ロードマップ完了項目の履歴記録は [archive/implemented-features.md](archive/implemented-features.md) を参照。

### 将来機能（ロードマップ）

直近 (`.steering/20260514-001-feature-discovery/` / `.steering/20260519-004-maintenance-cleanup/`) の発掘調査・監査で抽出された残候補をここに登録する。優先度は ROI (既存資産の再利用度) と「単独セッションで完結可能か」で評価。

#### 新機能候補

| # | 機能 | カテゴリ | 説明 | 難易度 | 優先度 |
|---|------|----------|------|--------|--------|
| 110 | Pipeline Hints | InlayHints | `->` パイプ各段の中間型を LSP hover 経由で InlayHint 表示 (F# Ionide 由来) | 中 | A |
| 111 | Test Code Lens | その他 | `describe` / `it` / `test` 行に Run / Debug の CodeVision を表示し既存 `RescriptTestRunConfigurationType` に橋渡し | 中 | A |
| 112 | `open` qualifier 展開 intention | Intention | `open Belt` を `Belt.Array.map` 形に書き戻す Alt+Enter (HLS importLens / rust-analyzer 由来) | 中 | A |
| 113 | doc コメント内評価 | その他 | `// > expr` 形式コメントを `RescriptReplExecutor` で評価し inline 注釈で結果表示 (HLS Eval plugin 由来) | 中 | B |
| 114 | ネスト switch 平坦化 intention | Intention | `switch x { \| Some(y) => switch y ... }` を 1 階層に統合 (Gleam LS 由来) | 中 | B |
| 115 | Wingman 風 type hole 補完 | 補完 | 型穴 `_` を target type と local binding から自動充填、case split サポート | 中〜高 | B |
| 116 | record / variant placeholder 補完 | 補完 | record literal 生成時に全フィールドを `_` で雛形化、variant matching wrapper も同様 | 中 | B |
| 117 | inferred 型注釈の一括挿入 quick fix | Quick Fix | Type Coverage Heat Map で低 coverage と判定された file の全 `let` に LSP hover 由来の `: T` を一括挿入 | 中 | B |
| 118 | Call Hierarchy ToolWindow | ナビゲーション | LSP `callHierarchy/incomingCalls` / `outgoingCalls` を ToolWindow に表示 (要 LSP サポート確認) | 中 | B |
| 119 | Build Console 専用 ToolWindow | ToolWindow | `rescript build --watch` の stdout を構造化表示し、エラー行クリックで該当箇所へジャンプ | 中〜高 | B |
| 120 | 追加 stub index | インデキシング | variant constructor / record field / object field / parameter の 4 種を stub index 化し Find Usages とリネームを高速化 | 中 | B |
| 121 | `.cmt` / `.cmti` バイナリ読取 | 分析 | LSP 非依存のホバー型表示 (NFR-04 強化、reasonml-idea-plugin の目玉機能を移植) | 高 | B |
| 122 | `if/match` 相互変換 intention | Intention | `if Option.isSome(x)` ⇔ `switch x { Some/None }` を Alt+Enter で相互変換 (rust-analyzer 由来) | 中 | C |
| 123 | Structural Search and Replace | リファクタリング | `switch $x { \| Some($y) => $y \| None => $z }` のような AST ワイルドカード一括置換 (rust-analyzer 由来) | 非常に高 | C |

#### リファクタリング候補

| # | 機能 | カテゴリ | 説明 | 難易度 | 優先度 |
|---|------|----------|------|--------|--------|
| 🚧 131 | wizard テンプレート scaffold 化 | リファクタリング | 22 個の *TemplateFiles の標準フレーム (common tail / リソース load / validation variant / 依存切替) を TemplateScaffold に集約 (golden テスト先行・バイト等価) | 中 | B |

新規機能の提案は GitHub Issues で受け付ける。ロードマップの表記方法は `.claude/rules/roadmap-format.md` を参照。

## 4. 成功の定義

### 定量指標

| 指標 | 目標値 |
|---|---|
| JetBrains Marketplace 公開 | 公開済み（現行バージョンは [docs/versions.md](versions.md) 参照） |
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

### プラットフォーム互換性戦略

JetBrains IDE は年 3 回（春・夏・秋）メジャーバージョンをリリースする。本プラグインは以下のポリシーで互換性を維持する。

#### サポートポリシー

| 項目 | 方針 |
|---|---|
| `pluginSinceBuild` | 直近 LTS（現在 2025.3）を最低互換とする。新メジャー版が出ても **既存ユーザーが取り残されない限り** 最低互換は据え置き |
| `pluginUntilBuild` | 通常未設定（前方互換性を維持）。破壊的変更で動作不能と判明した場合のみ、緊急パッチで一時設定し、修正版で再度外す |
| 新メジャー版対応 | リリース後 1 か月以内に `verifyPlugin` で互換性を検証し、問題があれば `verifierIdeVersions` に追加 |
| サポート終了 | `pluginSinceBuild` 引き上げは **3 メジャーバージョン以上前** の IDE のみを対象とする |

#### Verifier ブロッカー対応

`./gradlew verifyPlugin` および JetBrains Marketplace の自動互換性チェックで問題が報告された場合、次の優先度で対応する:

1. **Critical**（クラッシュ、API 削除）: 緊急パッチをリリースし、影響バージョンを `pluginUntilBuild` で除外
2. **Warning**（deprecated 警告）: 次回リリースまでに代替 API へ移行（`.claude/rules/deprecated-api.md` 参照）
3. **Info**（非推奨予定の通知）: `plugin-verifier-ignored-problems.txt` で抑制し、計画的に解消

**現在の既知ブロッカー**:

- IntelliJ Platform **2026.2 EAP** への自動互換性検証は、`verifier-cli` 1.403 がまだ 2026.2 の bundled-plugin layout を解釈できず `ClosedFileSystemException` で落ちるため保留。`build.gradle.kts` の `pluginVerification.ides` は `recommended()` を使わず `IntellijIdea 2026.1.2` を明示 pin している。新しい `verifier-cli` リリースが 2026.2 layout に対応したら `recommended()` に戻す。

#### 月次互換性検証

CI に月次の `verifyPlugin` ジョブを追加することを推奨する（`.github/workflows/ci.yml` の `schedule:` トリガー）。これにより、新リリースされた IDE バージョンへの追従漏れを早期発見できる。

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

### US-11: Project Wizard でのプロジェクト雛形生成

**ReScript を新規導入する開発者として**、IDE の New Project ダイアログから用途別のテンプレートを選んでプロジェクト雛形を生成できることで、ビルド設定や `package.json` を手書きする初期コストを省きたい。

**受け入れ条件:**
- [x] New Project ダイアログに ReScript カテゴリが表示される
- [x] 22 種類のテンプレート（Basic / Vite+React / Next.js / Hono / Hono GraphQL / Hono Inertia / Cloudflare Workers / AWS Lambda / Google Cloud Run / Electron / Tauri / React Native (Expo) / React Native (CLI) / npm Library / CLI Tool / Monorepo / Full-Stack / ReS-X / TanStack Start / Remix RR v7 / Astro / Waku）が選択できる
- [x] パッケージマネージャー（npm / yarn / pnpm / bun）と Validation ライブラリ（zod / sury）を選択できる。ただし TanStack Start / Remix RR v7 / Astro / Waku 選択時は Validation 選択 UI を非表示にする
- [x] 生成されたプロジェクトはそのまま `<package-manager> install && rescript build` が通る
- [x] 既存 18 テンプレートでは用途に合わせた `Validation.res` が `variants/<key>/` から書き出される

### US-12: Worksheet / REPL でのインタラクティブ評価

**コードを試行錯誤したい開発者として**、ファイル単位の評価（`.resw` Worksheet）と式単位の評価（REPL ツールウィンドウ）でコードをインタラクティブに走らせ、結果をその場で確認したい。

**受け入れ条件:**
- [x] `.resw` ファイルを開くと Worksheet モードで実行できる
- [x] Worksheet モードでコメント形式の式評価結果がインレイ表示される
- [x] REPL ツールウィンドウから ReScript コードを送って即座に評価結果を得られる
- [x] REPL の入力エリアではシンタックスハイライトが効き、履歴ナビゲーションが使える

### US-13: PPX 展開ビューによるマクロ効果の可視化

**`@deriving` や `@react.component` 等の PPX を使用する開発者として**、注釈が実際にどのコードへ展開されるかを確認することで、生成コードの挙動と外部 API への影響を理解したい。

**受け入れ条件:**
- [x] `ReScript PPX` ツールウィンドウから現在のファイルの PPX 展開結果を表示できる
- [x] PPX 注釈がついた宣言の行にインレイヒントで展開要約が表示される
- [x] 展開の対象が変わるたびにビューが追従する

### US-14: Type Info ツールウィンドウでの常時型表示

**型推論の結果を頻繁に確認したい開発者として**、ホバーを毎回操作するのではなく、ツールウィンドウでカーソル位置の型を常時表示しておきたい。

**受け入れ条件:**
- [x] `ReScript Type` ツールウィンドウを開くとカーソル位置のシンボルの型が表示される
- [x] エディタ内でカーソルを動かすたびに表示が更新される
- [x] LSP が利用可能な状態で動作する

### US-15: 依存関係ダイアグラムによるモジュール関係把握

**規模が大きくなったプロジェクトを保守する開発者として**、モジュール間の依存関係をグラフで俯瞰することで、循環依存や責務の集中を発見したい。

**受け入れ条件:**
- [x] `ReScript Dependencies` ツールウィンドウから依存関係ツールウィンドウを開ける
- [x] モジュール依存関係をダイアグラムで可視化できる
- [x] DOT 形式エクスポート時に外部ツール（graphviz など）に渡せる安全な出力が得られる

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

各指標の計測手段、リリース時の検証手順、退化ガードのラチェット案は [docs/performance-validation.md](performance-validation.md) を参照。

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

機能ごとの LSP 依存有無と非接続時の動作は [docs/lsp-fallback-matrix.md](lsp-fallback-matrix.md) を参照。

### NFR-05: ユーザビリティ

- プラグインインストール後、追加設定なしでネイティブ機能が動作する
- LSP 機能は `@rescript/language-server` のインストールのみで動作する（プラグインがワンクリックインストールを提供）
- エラーメッセージは原因と対処法を含む明確な内容とする
