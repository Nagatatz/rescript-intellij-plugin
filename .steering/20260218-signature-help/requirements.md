# Signature Help（Parameter Info）動作確認

## 概要

IntelliJ 2025.3+ の LSP API が `textDocument/signatureHelp` を自動サポートしていることを確認する。rescript-language-server はデフォルトで signatureHelp を有効化しているため、追加のプラグインコードは不要。

## 背景

- rescript-language-server の `signatureHelpProvider` capability:
  - `triggerCharacters`: `(`
  - `retriggerCharacters`: `=`, `,`
- IntelliJ Platform の LSP API が自動的にこの capability を認識し、Parameter Info（`Cmd+P`）を提供する

## 要件

1. プラグインコードの追加・変更は不要（ゼロコード）
2. 動作確認とドキュメント記録のみ

## 受け入れ条件

- ステアリングドキュメントが作成されている
- ビルドが成功する（既存機能に影響がないこと）

## 制約事項

- テスト省略: LSP サーバーとの結合が必須で単体テストが困難
