# Requirements: Paste as JSON.t

## 概要
クリップボードの JSON テキストを ReScript の `JSON.t` 型に変換してエディタに挿入するアクション。

## 機能要件
- Edit メニューおよびエディタコンテキストメニューから「Paste as JSON.t」アクションを実行可能
- クリップボードの JSON を再帰的に ReScript `JSON.t` コンストラクタに変換
- ReScript ファイルでのみ利用可能

## 変換ルール
- `null` → `JSON.Null`
- `true/false` → `JSON.Boolean(true/false)`
- 数値 → `JSON.Number(n.)`（整数の場合は末尾に `.`）
- 文字列 → `JSON.String("...")`
- 配列 → `JSON.Array([...])`
- オブジェクト → `JSON.Object(dict{...})`

## テスト省略理由
- AnAction の `actionPerformed` は IntelliJ Platform テストフレームワーク必須のため単体テスト困難
- `convertJsonToRescript` および `isLikelyJson` は純粋関数のため単体テスト可能
