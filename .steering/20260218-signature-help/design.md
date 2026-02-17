# Signature Help 設計

## 実装アプローチ

コード変更なし。IntelliJ Platform の LSP API が `textDocument/signatureHelp` を自動的にサポートする。

## 動作の仕組み

1. `RescriptLspServerDescriptor` が rescript-language-server を stdio で起動
2. サーバーが `initialize` レスポンスで `signatureHelpProvider` capability を返す
3. IntelliJ の LSP クライアントがこの capability を検出し、Parameter Info を有効化
4. ユーザーが関数呼び出し中に `(` を入力、または `Cmd+P` を押すと、LSP サーバーに `textDocument/signatureHelp` リクエストが送信される
5. サーバーがシグネチャ情報を返し、IntelliJ がポップアップで表示

## 新規ファイル

なし

## 変更ファイル

なし

## テスト

省略（LSP サーバーとの結合が必須で単体テスト困難）
