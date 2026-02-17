# Requirements: Quick Fix (LSP Code Actions)

## 概要

IntelliJ Platform の LSP API が `textDocument/codeAction` を自動サポートしているため、rescript-language-server が提供する Quick Fix / Code Actions は追加のプラグインコードなしで動作する。

## 背景

- IntelliJ 2024.1+ の LSP API は `textDocument/codeAction` リクエストを自動的に処理する
- rescript-language-server は以下のようなコードアクションを提供:
  - import 追加（未定義シンボルへの自動 import）
  - 型注釈追加
  - その他のリファクタリングアクション
- これらは Alt+Enter メニューに Quick Fix / Intention として自動表示される

## 要件

1. **追加コード不要（ゼロコード）**: LSP API が自動的にコードアクションをサポートするため、プラグイン側の追加実装は不要
2. **動作確認**: LSP 経由でコードアクションが正しく表示・実行されることを確認
3. **ドキュメント整備**: ステアリングドキュメントを作成し、この機能が LSP 経由で提供されることを記録

## 受け入れ条件

- [x] ステアリングドキュメントが作成されている
- [ ] ビルドが成功する（既存コードへの変更がないことの確認）

## 制約事項

- コードアクションの種類と品質は rescript-language-server の実装に依存する
- LSP サーバーが起動していない環境では Quick Fix は利用不可
