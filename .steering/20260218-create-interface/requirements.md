# Requirements: インターフェースファイル生成

## 概要

`.res` ファイルから `.resi` インターフェースファイルを自動生成するアクションを提供する。

## 機能要件

### FR-1: LSP カスタムリクエストによる生成
- LSP カスタムリクエスト `textDocument/createInterface` を使用
- リクエスト: `TextDocumentIdentifier { uri }` → レスポンス: `TextDocumentIdentifier { uri }`（生成された .resi の URI）
- サーバーが `.cmi` ファイルから .resi を生成してディスクに書き込む

### FR-2: アクション有効条件
- `.res` ファイルでのみアクションが有効
- `.resi` や他のファイルでは無効（グレーアウト）

### FR-3: 上書き確認
- `.resi` が既に存在する場合は確認ダイアログを表示
- ユーザーが拒否した場合は処理を中止

### FR-4: エラー処理
- `.cmi` 未生成時はサーバーが `window/showMessage` でエラー通知（IntelliJ LSP 層が自動表示）
- LSP 未接続時は何もしない

### FR-5: 生成後の動作
- 生成された `.resi` ファイルをエディタで開く

### FR-6: メニュー登録
- Go To メニューにアクション登録

## 制約事項
- 共有インフラ `RescriptLanguageServer.createInterface()` が利用可能
- IntelliJ 2025.3+ の LSP API を使用
