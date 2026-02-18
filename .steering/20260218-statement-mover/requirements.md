# Requirements: Statement Up/Down Mover

## 概要
Alt+Shift+Up/Down でトップレベル宣言を上下に移動する。

## 受け入れ条件
- トップレベル宣言（let, type, module, external, open, include, exception）を上下に移動できる
- アノテーション付き宣言は一括移動される
- ファイル先頭/末尾では移動しない
- モジュール内宣言も移動可能
