# Import Optimizer 要求定義

## 概要

Ctrl+Alt+O で重複 `open` 文を削除する Import Optimizer を実装する。

## 機能要件

- IntelliJ の「Optimize Imports」アクション（Ctrl+Alt+O）で重複 `open` 文を自動削除
- 既存の `RescriptDuplicateOpenInspection` と同様のロジックで重複を検出
- 同一モジュールパスの2回目以降の `open` 文を削除対象とする
- 最適化後に通知バルーンで削除数を表示（`getUserNotificationInfo()`）

## スコープ

- 重複 `open` 文の削除に注力
- 未使用 `open` の検出はセマンティック解析が必要なため対象外

## 制約

- LSP 不要（PSI ツリーのみで動作）
- 既存の `RescriptDuplicateOpenInspection` のロジックを参考にする
