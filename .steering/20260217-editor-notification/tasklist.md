# Tasklist: Editor Notification Bar

## 実装タスク

- [x] `RescriptEditorNotificationProvider.kt` を作成
- [x] `plugin.xml` に `editorNotificationProvider` を登録
- [x] ビルド確認 (`./gradlew buildPlugin`)
- [x] ドキュメント更新 (CLAUDE.md, product-requirements.md, functional-design.md)
- [x] コミット

## テスト省略理由

LSP サーバー検出との結合、EditorNotificationPanel の UI 表示テストが単体テスト困難なため、テスト作成を省略する。
