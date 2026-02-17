# Requirements: Open Compiled JavaScript

## 概要

`.res` / `.resi` ファイルに対応するコンパイル済み JavaScript ファイルを開くアクション。

## 機能要件

1. **LSP カスタムリクエスト `textDocument/openCompiled`** を使用して、コンパイル済み JS ファイルの URI を取得
   - リクエスト: `TextDocumentIdentifier { uri }`
   - レスポンス: `TextDocumentIdentifier { uri }` （コンパイル済み .js の URI）
   - サーバーが `rescript.json` の `package-specs` を読んでパスを解決

2. **エラーハンドリング**
   - コンパイル済みファイルが存在しない場合、サーバーが `window/showMessage` でエラー通知（IntelliJ LSP 層が自動表示）

3. **LSP 未接続時のフォールバック**
   - ファイルパス推測: `lib/js/<path>.bs.js`, `.mjs`, `.js`
   - 既存の `RescriptGotoRelatedProvider` の JS ファイル検索ロジックを参考にする

4. **UI 統合**
   - Go To メニューにアクション登録
   - ショートカット `Alt+Shift+J`
   - `.res` / `.resi` ファイルでのみ有効

## 受け入れ条件

- [ ] `.res` / `.resi` ファイルで Go To > Open Compiled JavaScript が表示される
- [ ] LSP 接続時: カスタムリクエストで JS ファイルが開かれる
- [ ] LSP 未接続時: フォールバックで `lib/js/` 配下を検索
- [ ] ファイルが見つからない場合はバルーン通知「Compile your project first」
- [ ] `Alt+Shift+J` で実行可能
- [ ] ビルドが通る
