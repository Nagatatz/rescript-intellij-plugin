# Tasklist: TODO Indexer

## タスク

- [x] `RescriptTodoIndexer.kt` を `src/main/kotlin/com/rescript/plugin/indexing/` に作成
  - `LexerBasedTodoIndexer` を継承
  - `createLexer()` で `IdAndTodoScannerBasedOnFilterLexer(RescriptLexer(), consumer)` を返す
- [x] `plugin.xml` に `todoIndexer` を登録
  - `filetype="ReScript"` 用
  - `filetype="ReScript Interface"` 用
- [x] ビルド確認（`./gradlew clean buildPlugin`）
- [x] `CLAUDE.md` のプロジェクト構成にパッケージを追記
- [x] コミット（`✨ Add TODO indexer for ReScript files`）
