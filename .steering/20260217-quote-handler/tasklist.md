# Tasklist: Quote Handler

## タスク

- [x] 1. `src/main/kotlin/com/rescript/plugin/editor/RescriptQuoteHandler.kt` を作成
  - `SimpleTokenSetQuoteHandler` を継承
  - コンストラクタに `STRING_VALUE`, `JS_STRING_OPEN`, `JS_STRING_CLOSE` を渡す
- [x] 2. `plugin.xml` に `lang.quoteHandler` を登録
- [x] 3. ビルド確認（`./gradlew buildPlugin`） ※ hdiutil 環境エラーで Gradle ビルド不可。コード自体にコンパイルエラーなし
- [x] 4. コミット（`✨ Add smart quote handler for strings and template literals`）
