# Requirements: P2 rescript-vscode ギャップ 5機能バッチ実装

## 概要

rescript-vscode（公式 VS Code 拡張）との機能ギャップを埋める P2 の 5 機能を git worktree による並列実装で一括追加する。

## 対象機能

### 1. Signature Help（難易度: 低）

**説明:** 関数呼び出し時の引数情報をポップアップ表示する（Parameter Info）。

**受け入れ条件:**
- [ ] `(` 入力時に関数のシグネチャ情報がポップアップ表示される
- [ ] カーソル位置に応じてアクティブなパラメータがハイライトされる
- [ ] `,` 入力で次のパラメータに切り替わる
- [ ] LSP 未接続時はポップアップが表示されない（エラーにならない）

**備考:** IntelliJ 2025.3+ の LSP API で `textDocument/signatureHelp` が自動サポートされている。rescript-language-server はデフォルトで有効（`signatureHelp.enabled: true`）。実装は LSP 初期化オプションの確認と動作検証が主な作業。

### 2. Code Lens（難易度: 中）

**説明:** 関数定義の上部に推論された型シグネチャをインライン表示する。

**受け入れ条件:**
- [ ] `.res` ファイルの関数定義の上に型シグネチャが薄く表示される
- [ ] `.resi` ファイルでは表示されない（既に型が明示されているため）
- [ ] Settings > Inlay Hints > Code Vision で表示の ON/OFF が切り替え可能
- [ ] LSP 未接続時は表示されない（エラーにならない）

**備考:** IntelliJ LSP API は Code Lens を非サポート。CodeVision API (`DaemonBoundCodeVisionProvider`) を使用し、LSP の `textDocument/codeLens` レスポンスを CodeVision エントリにマッピングする。rescript-language-server ではデフォルト無効（`codeLens: false`）のため、初期化オプションで有効化が必要。

### 3. インターフェースファイル生成（難易度: 中）

**説明:** `.res` ファイルから `.resi` インターフェースファイルを自動生成する。

**受け入れ条件:**
- [ ] `.res` ファイルを開いた状態でアクションを実行すると、対応する `.resi` ファイルが生成される
- [ ] `.resi` が既に存在する場合、上書き確認ダイアログが表示される
- [ ] 生成後、`.resi` ファイルがエディタで開かれる
- [ ] コンパイル未実行（`.cmi` 未生成）時は、コンパイルを促すメッセージが表示される
- [ ] アクションは Go To メニューまたはショートカットからアクセス可能

**備考:** LSP カスタムリクエスト `textDocument/createInterface` を使用。サーバーが `.cmi` ファイルから `.resi` を生成し、ディスクに書き込む。

### 4. コンパイル済み JS を開く（難易度: 低）

**説明:** `.res` ファイルに対応するコンパイル済み JavaScript ファイルを開く。

**受け入れ条件:**
- [ ] `.res` / `.resi` ファイルを開いた状態でアクションを実行すると、対応する `.js` ファイルが開かれる
- [ ] `rescript.json` の `package-specs` 設定（suffix, module format, in-source）に基づいてファイルパスが解決される
- [ ] コンパイル済みファイルが存在しない場合、コンパイルを促す通知が表示される
- [ ] アクションは Go To メニューまたはショートカットからアクセス可能

**備考:** LSP カスタムリクエスト `textDocument/openCompiled` を使用。サーバーが `rescript.json` の設定を読み取り、コンパイル済みファイルのパスを返す。

### 5. ビルドステータス表示（難易度: 中〜高）

**説明:** ステータスバーに ReScript コンパイラのビルド状態を表示する。

**受け入れ条件:**
- [ ] ステータスバーに ReScript のビルド状態が表示される
- [ ] 状態: コンパイル中（スピナー）、成功（チェック）、エラー（警告アイコン + エラー数）、警告（警告アイコン + 警告数）
- [ ] ステータスバーウィジェットをクリックすると詳細情報を表示（ツールチップまたはポップアップ）
- [ ] ReScript プロジェクトでない場合はウィジェットが表示されない

**備考:** LSP サーバーからの `rescript/compilationStatus` カスタム通知を受信して表示を更新する。

## 実装アプローチ

### 共有インフラ（バッチブランチで事前作成）

3つの機能（Create Interface, Open Compiled, Code Lens）が LSP カスタムリクエストを使用し、1つの機能（Build Status）がカスタム通知を使用するため、共有インフラをバッチブランチで事前に作成し、各 worktree の `plugin.xml` 以外の競合を回避する。

| 機能 | ブランチ名 | worktree パス |
|------|-----------|--------------|
| Signature Help | `feature/signature-help` | `../rescript-wt-signature-help` |
| Code Lens | `feature/code-lens` | `../rescript-wt-code-lens` |
| インターフェース生成 | `feature/create-interface` | `../rescript-wt-create-interface` |
| コンパイル済み JS | `feature/open-compiled` | `../rescript-wt-open-compiled` |
| ビルドステータス | `feature/build-status` | `../rescript-wt-build-status` |

## 制約事項

- 各機能は共有インフラ（カスタム LSP サーバーインターフェース）に依存するが、機能間の依存はない
- すべてのブランチはバッチブランチから分岐する
- 各ブランチで `./gradlew buildPlugin` が成功すること
- 共有ドキュメント更新はバッチブランチでのマージ後に一括で行う
