# Security Fixes — Design

## 1. chain.crt / private.pem 削除

- `chain.crt` と `private.pem` をワーキングディレクトリから削除
- `.gitignore` に `*.crt` を追加

## 2. REPL JS アーティファクト削除

`RescriptReplExecutor.runWithNode()` の `finally` ブロックで `jsFile` も削除する。

## 3. REPL プロセスリソースリーク修正

- `compileProcess` / `runProcess` のストリームを `.use{}` で閉じる
- `waitFor()` が `false` を返した場合 `destroyForcibly()` を呼ぶ
- タイムアウトエラーメッセージを返す

## 4. REPL 実行をバックグラウンドスレッドに移動

`RescriptReplPanel.executeInput()` で `ApplicationManager.getApplication().executeOnPooledThread` を使用し、UI 更新は `invokeLater` で EDT に戻す。

## 5. projectPath バリデーション

- `execute()` メソッドで `projectPath` が有効なディレクトリか検証
- 一時ファイルは `FileUtil.createTempDirectory()` でシステム一時ディレクトリに作成
- `IntelliJ FileUtil` を活用

## 6. DOT フォーマットインジェクション防止

`toDot()` でモジュール名の `"` と `\` をエスケープするヘルパー関数を追加。
