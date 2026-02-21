---
globs: ["**/*.flex"]
---

# JFlex レクサールール

- `RescriptFlexLexer.java` は自動生成ファイル。直接編集せず `Rescript.flex` を編集すること
- トークンを追加・変更する場合は `Rescript.flex` と `RescriptTokenTypes.kt` の両方を更新すること
- JFlex の状態（`%state`）を追加する場合は、対応する状態遷移のテストを必ず作成すること
- レクサーの生成は `generateRescriptLexer` Gradle タスクで自動実行される（手動生成は不要）
