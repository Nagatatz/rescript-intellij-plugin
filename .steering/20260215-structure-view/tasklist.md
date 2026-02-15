# Tasklist: Structure View

## パーサー拡張

- [x] `RescriptParser.kt` に `skipToOpenBrace` ヘルパーメソッドを追加
- [x] `parseModuleDeclaration` を修正し、`{}` 内で `parseTopLevel` を再帰呼出しするようにする

## Structure View 実装

- [x] `structure/RescriptStructureViewFactory.kt` を作成（`PsiStructureViewFactory` 実装）
- [x] `structure/RescriptStructureViewModel.kt` を作成（`TextEditorBasedStructureViewModel` 継承）
- [x] `structure/RescriptStructureViewElement.kt` を作成（名前抽出・アイコン・子要素・ナビゲーション）

## 登録・ビルド

- [x] `plugin.xml` に `lang.psiStructureViewFactory` を登録
- [x] `./gradlew buildPlugin` でビルド成功を確認

## コミット

- [x] `tasklist.md` を更新し、全タスク完了を記録してコミット
