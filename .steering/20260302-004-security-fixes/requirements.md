# Security Fixes — Requirements

## 背景

セキュリティレビューで Critical 2件、High 2件、Medium 1件 + 秘密鍵/証明書漏洩 1件が発見された。

## 修正対象（6件）

### Critical
1. `chain.crt` / `private.pem` 削除 + `.gitignore` に `*.crt` 追加
2. REPL で生成される `.js` ファイルの未削除を修正（`finally` ブロックで削除）
3. REPL プロセスのリソースリーク修正（`.use{}`、タイムアウト時 `destroyForcibly()`）

### High
4. REPL 実行を EDT からバックグラウンドスレッドに移動
5. REPL の `projectPath` バリデーション追加 + システム一時ディレクトリ使用

### Medium
6. DOT フォーマットインジェクション防止（モジュール名エスケープ）

## 受け入れ条件

- 上記6件すべてが修正されていること
- 既存テストが通ること
- 新規修正に対するテストが追加されていること
- `./gradlew clean buildPlugin` が成功すること
