# Requirements: Call Hierarchy (#44)

## 概要

関数の呼び出し階層（Caller/Callee）をツリー表示する機能。IntelliJ の `callHierarchyProvider` Extension Point を使用。

## ユーザーストーリー

ReScript 開発者として、関数の呼び出し元（Callers）と呼び出し先（Callees）をツリー形式で確認できることで、コードの依存関係を素早く把握したい。

## 受け入れ条件

- [ ] `.res` ファイルの `let` 宣言にカーソルを置き、`Ctrl+Alt+H` で Call Hierarchy ウィンドウが表示される
- [ ] Callers ビュー: 選択した関数を呼び出している他の関数が一覧表示される
- [ ] Callees ビュー: 選択した関数が呼び出している他の関数が一覧表示される
- [ ] Callers/Callees のタブ切り替えが可能
- [ ] ツリーノードに関数名とアイコンが表示される
- [ ] ツリーノードをダブルクリックで該当ソースに遷移できる
- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] ユニットテストがパスする

## 制約

- LSP は `textDocument/prepareCallHierarchy` を未サポートのため、PSI ベースのテキスト検索で実装する
- 完全な型解析は行わず、名前ベースのテキスト検索で呼び出し関係を推定する
- 同名の異なる関数を区別できない（PSI の制約）

## 優先度

B（有用 — あると便利だが緊急性は低い機能）
