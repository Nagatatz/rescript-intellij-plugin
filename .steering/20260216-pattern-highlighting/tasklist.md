# Tasklist: パターンマッチングハイライト

## Phase 1: ハイライター実装

- [x] `RescriptSyntaxHighlighter.kt` に `PATTERN_PIPE` と `WILDCARD` の TextAttributesKey を追加
- [x] `RescriptSyntaxHighlighter.kt` の ATTR_MAP に `PIPE`, `UNDERSCORE`, `DOTDOTDOT`, `SHORTCUT` の 4 エントリを追加

## Phase 2: カラースキーム

- [x] `RescriptDarcula.xml` に `RESCRIPT_PATTERN_PIPE` と `RESCRIPT_WILDCARD` のカラー定義を追加
- [x] `RescriptDefault.xml` に `RESCRIPT_PATTERN_PIPE` と `RESCRIPT_WILDCARD` のカラー定義を追加

## Phase 3: カラー設定ページ

- [x] `RescriptColorSettingsPage.kt` の LEXER_DESCRIPTORS に Pattern//Pipe と Pattern//Wildcard を追加
- [x] `RescriptColorSettingsPage.kt` のデモテキストにパターンマッチングの例を追加

## Phase 4: 検証・コミット

- [x] `./gradlew clean buildPlugin` でビルド成功を確認
- [x] `./gradlew test` で全テスト通過を確認
- [x] コミット: `✨ Add pattern matching syntax highlighting for pipe, wildcard, spread, and cons`
