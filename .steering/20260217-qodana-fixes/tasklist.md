# Tasklist: 静的解析指摘事項の修正

## CRITICAL

- [x] 1. `RescriptTodoIndexer` を `BaseFilterLexer` ベースに移行
  - `RescriptFilterLexer` クラスを新規作成
  - `RescriptTodoIndexer.createLexer()` を修正
  - `IdAndTodoScannerBasedOnFilterLexer` の import を削除

## HIGH

- [x] 2. `RescriptParser.isTopLevelStart` に括弧を追加して演算子優先順位を明示化

## MEDIUM

- [x] 3. `it.node.elementType` を `it.node?.elementType` に修正（全5ファイル）
  - `RescriptStructureViewElement.kt`
  - `RescriptDuplicateOpenInspection.kt`
  - `RescriptEmptyModuleInspection.kt`

## LOW (パフォーマンス)

- [x] 4. ホットパスでの不要なオブジェクト生成を定数化
  - `RescriptParser.kt` — `listOf(...)` → companion object 定数
  - `RescriptFoldingBuilder.kt` — `setOf(...)` → companion object 定数
  - `RescriptRunConfiguration.kt` — `"\\s+".toRegex()` → companion object 定数

## LOW (エラーハンドリング)

- [x] 5. 例外処理の改善
  - `RescriptLspServerDescriptor.kt` — `tryExec` でリソースリーク修正 + `InterruptedException` 再スロー
  - `RescriptFormattingService.kt` — `proc.waitFor` にタイムアウト追加
  - `RescriptRenameHandler.kt` — `CancellationException` 再スロー

## LOW (コードスタイル)

- [x] 6. FQN を import に統一（5ファイル）
  - `RescriptDuplicateOpenInspection.kt`
  - `RescriptEmptyModuleInspection.kt`
  - `RescriptMissingConfigInspection.kt`
  - `RescriptRenameHandler.kt`
  - `RescriptRunConfiguration.kt`

- [x] 7. `RESCRIPT_EXTENSIONS` 定数を共通化
  - `RescriptLspServerSupportProvider.kt` — `internal` に公開
  - `RescriptLspServerDescriptor.kt` — 参照に変更
  - `RescriptRenameHandler.kt` — 参照に変更

- [x] 8. 冗長なオーバーライドの削除
  - `RescriptLanguage.kt` — `getDisplayName()` 削除
  - `RescriptFileTypes.kt` — `getDisplayName()` 削除

## LOW (ベストプラクティス)

- [x] 9. `RescriptFile.getFileType()` を `viewProvider.fileType` に修正

- [x] 10. `RescriptMissingConfigInspection` の `VirtualFileManager.findFileByUrl` → `LocalFileSystem.findFileByPath` に修正

- [x] 11. `RescriptRenameHandler.applyWorkspaceEdit` の `saveAllDocuments()` → 個別 `saveDocument()` に修正

## ビルド確認・コミット

- [x] 12. `./gradlew buildPlugin` でビルド成功を確認
- [x] 13. コミット（`♻️ Fix static analysis issues found by code quality review`）
