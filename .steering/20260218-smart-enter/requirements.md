# Requirements: Smart Enter

## 概要
Shift+Enter で文を補完して改行する SmartEnterProcessor。

## 受け入れ条件
- 未閉じ `{`, `(`, `[` を自動補完
- `switch expr` 後にブレースと初期パターンを挿入
- `| pattern` 後に `=>` を補完
- 上記以外は通常改行（false を返してデフォルト動作）
