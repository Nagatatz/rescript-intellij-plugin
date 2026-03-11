# 設計: JetBrains プラグインベストプラクティス改善

## 1. getActionUpdateThread() 追加

`ActionUpdateThread.BGT` を返す `getActionUpdateThread()` を追加。すべて PSI/Editor の読み取りのみで BGT で安全。

対象: 9クラス（generate/ 7件、refactor/ 1件、preview/ 1件）

## 2. CopyOnWriteArrayList

`mutableListOf()` → `CopyOnWriteArrayList()` に変更。LSP スレッドと EDT の同時アクセスに対応。

## 3. ModalityState 明示

`invokeLater {}` → `invokeLater(ModalityState.any()) {}` に変更。ダイアログ表示中でも正しく動作。
