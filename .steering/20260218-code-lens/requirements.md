# Requirements: Code Lens（CodeVision API による型注釈表示）

## 概要

IntelliJ の CodeVision API を使用し、ReScript の関数定義上に推論された型シグネチャを表示する Code Lens 機能を実装する。

## 背景

- rescript-language-server は `textDocument/codeLens` をサポートしている（条件付き: 初期化オプション `codeLens: true` が必要）
- IntelliJ Platform の LSP API は `textDocument/codeLens` を自動サポートしない
- IntelliJ の CodeVision API（`DaemonBoundCodeVisionProvider`）を使用して実装する

## 機能要件

1. 関数定義の上に推論された型シグネチャを表示する
2. `command.title` に型情報が含まれ、`command.command` は空文字列（情報表示のみ、クリックアクションなし）
3. `.resi` ファイルでは表示しない（インターフェースファイルには明示的な型定義があるため不要）
4. Settings > Editor > Inlay Hints > Code Vision で ON/OFF 切り替え可能

## 非機能要件

- LSP サーバーが利用不可の場合は何も表示しない（グレースフルデグレード）
- `DaemonCodeAnalyzer` のバックグラウンドスレッドで実行されるため、ブロッキング呼び出しは最小限に

## 制約事項

- rescript-language-server で codeLens を有効化するには初期化オプション `codeLens: true` が必要
- `RescriptLspServerDescriptor` で `createInitializationOptions()` をオーバーライドする必要がある
