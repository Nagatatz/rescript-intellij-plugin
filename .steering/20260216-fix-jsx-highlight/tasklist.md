# Tasklist: JSX ハイライト修正

## タスク

- [x] 1. plugin.xml に Islands テーマ (Dark/Light) の additionalTextAttributes を追加
- [x] 2. Rescript.flex に `inJsxOpenTag` フラグと `jsxAttrBraceDepth` カウンターを追加し、JSX 属性後の `>` を `TAG_GT` にする
- [x] 3. RescriptParser.kt で `skipJsxAttributes` と `parseJsxTagOrSelfClosing` に `GT` フォールバックを追加
- [x] 4. RescriptLexerTest.kt にテストケースを追加
- [x] 5. ビルド・テスト確認 (`./gradlew clean buildPlugin` + `./gradlew test`)
- [x] 6. コミット
