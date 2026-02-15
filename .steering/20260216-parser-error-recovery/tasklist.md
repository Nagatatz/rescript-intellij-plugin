# Tasklist: パーサーエラーリカバリ

## 実装タスク

- [x] T1: `parseTopLevel` の `else` ブランチで不明トークンをエラーノードにまとめる
- [x] T2: `parseDeclaration` に識別子欠落時のエラー報告を追加
- [x] T3: `parseModuleDeclaration` にモジュール名欠落時のエラー報告を追加
- [x] T4: `parseModuleDeclaration` に閉じ括弧欠落時のエラー報告を追加

## テストタスク

- [x] T5: 不明トークンのエラーリカバリテスト追加
- [x] T6: 識別子欠落のエラーリカバリテスト追加
- [x] T7: 閉じ括弧欠落のエラーリカバリテスト追加
- [x] T8: 複合エラーケースのテスト追加
- [x] T9: 既存テスト全パス確認

## 検証タスク

- [x] T10: `./gradlew buildPlugin` 成功確認
- [x] T11: tasklist.md 更新 & コミット
