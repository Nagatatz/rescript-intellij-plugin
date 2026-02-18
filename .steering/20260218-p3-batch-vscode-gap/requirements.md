# Requirements: P3 rescript-vscode ギャップ 5機能バッチ実装

## 概要

rescript-vscode（公式 VS Code 拡張）との機能ギャップを埋める P3 の 5 機能を git worktree による並列実装で一括追加する。

全機能が独立しており、共有インフラは不要。

## 対象機能

### 1. reanalyze 統合（難易度: 高）

**説明:** reanalyze（デッドコード分析・未処理例外分析）ツールをIDE内で実行し、結果をエディタ上に警告として表示する。

**受け入れ条件:**
- [ ] `rescript-tools.exe` を `node_modules/rescript/` から自動検出する
- [ ] ファイル保存時に reanalyze を実行し、デッドコード・未処理例外を警告表示する
- [ ] 警告は黄色い波線でインライン表示される
- [ ] reanalyze バイナリが見つからない場合はサイレントに機能を無効化する

**備考:** rescript-vscode は `rescript-tools.exe reanalyze -json` を実行し、JSON 出力をパースして diagnostics に変換している。IntelliJ では `ExternalAnnotator` API を使用する。

### 2. Markdown ReScript ハイライト（難易度: 低）

**説明:** Markdown ファイル内の ` ```rescript ` コードブロックで ReScript シンタックスハイライトを有効にする。

**受け入れ条件:**
- [ ] Markdown ファイル内の ` ```rescript ` ブロックが ReScript としてハイライトされる
- [ ] ` ```res ` / ` ```resi ` もサポートする
- [ ] Markdown プラグインがインストールされていない場合は機能が無効化される（optional dependency）
- [ ] ブロック内でコード補完・ナビゲーションが動作する

### 3. Paste as JSON.t（難易度: 中）

**説明:** クリップボードの JSON をReScript の `JSON.t` 型表現に変換してペーストする。

**受け入れ条件:**
- [ ] Edit メニューまたは右クリックメニューに「Paste as JSON.t」アクションが表示される
- [ ] ReScript ファイルでのみアクション有効
- [ ] クリップボードの JSON を `JSON.Object(dict{"key": JSON.String("value"), ...})` 形式に変換する
- [ ] ネストした JSON 構造を再帰的に変換する
- [ ] 不正な JSON の場合はエラー通知を表示する

**備考:** rescript-vscode の "Paste as JSON.t" コマンドに相当する。JSX 変換（Paste as ReScript JSX）は外部ライブラリ依存のため今回はスコープ外とする。

### 4. `//#region` 折りたたみ（難易度: 低）

**説明:** `//#region` と `//#endregion` コメントマーカーによるカスタム折りたたみ領域をサポートする。

**受け入れ条件:**
- [ ] `//#region [名前]` と `//#endregion` で囲まれた領域が折りたたみ可能になる
- [ ] `// #region` / `// #endregion`（スペースあり）もサポートする
- [ ] 折りたたみ時に region 名がプレースホルダーとして表示される
- [ ] ネストした region をサポートする
- [ ] 既存の折りたたみ（モジュール、宣言、コメント）に影響しない

### 5. Incremental Type Checking 設定（難易度: 低〜中）

**説明:** LSP サーバーの Incremental Type Checking 機能の有効/無効を IDE 設定画面から切り替える。

**受け入れ条件:**
- [ ] Settings > Languages & Frameworks > ReScript に「Enable incremental type checking」チェックボックスが表示される
- [ ] 設定変更が LSP 初期化オプションに反映される（サーバー再起動で適用）
- [ ] デフォルトで有効
- [ ] 設定変更後に LSP サーバーが自動再起動される

## 実装アプローチ

| 機能 | ブランチ名 | worktree パス |
|------|-----------|--------------|
| reanalyze 統合 | `feature/reanalyze` | `../rescript-wt-reanalyze` |
| Markdown ReScript ハイライト | `feature/markdown-highlight` | `../rescript-wt-markdown` |
| Paste as JSON.t | `feature/paste-as-json` | `../rescript-wt-paste-json` |
| `//#region` 折りたたみ | `feature/region-folding` | `../rescript-wt-region-fold` |
| Incremental Type Checking 設定 | `feature/incremental-tc` | `../rescript-wt-incremental-tc` |

## 制約事項

- 各機能は完全に独立しており、共有インフラは不要
- すべてのブランチはバッチブランチから分岐する
- 各ブランチで `./gradlew buildPlugin` が成功すること
- 共有ドキュメント更新はバッチブランチでのマージ後に一括で行う
- Paste as ReScript JSX（HTML→JSX変換）は外部ライブラリ依存のため今回はスコープ外
