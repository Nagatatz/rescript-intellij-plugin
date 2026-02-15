# Tasklist: テストカバレッジ拡充（Phase 2）

## Phase 1: ビルド設定クリーンアップ

- [x] `build.gradle.kts` から `kotlin.srcDir("src/test-local/kotlin")` を削除
- [x] `./gradlew test` で既存テスト全 PASS を確認

## Phase 2: 新規テストクラス作成（単純コンポーネント）

- [x] `RescriptCommenterTest.kt` を作成（5テスト）
- [x] `RescriptBraceMatcherTest.kt` を作成（9テスト）
- [x] `RescriptTokenTypesTest.kt` を作成（10テスト）
- [x] `RescriptParserDefinitionTest.kt` を作成（6テスト）

## Phase 3: 新規テストクラス作成（ハイライト・設定）

- [x] `RescriptSyntaxHighlighterTest.kt` を作成（22テスト）
- [x] `RescriptColorSettingsPageTest.kt` を作成（10テスト）
- [x] `RescriptFoldingBuilderTest.kt` を作成（4テスト）

## Phase 4: 既存テストへのエッジケース追加

- [x] `RescriptLexerTest.kt` にエッジケーステスト追加（10テスト）
- [x] `RescriptLineIndentProviderTest.kt` に追加テスト（4テスト）

## Phase 5: 最終確認・コミット

- [x] `./gradlew test` で全テスト PASS を確認（255テスト、0 failures）
- [x] `./gradlew buildPlugin` でビルド成功を確認
- [x] CLAUDE.md / README.md のドキュメント更新が必要か確認し、必要なら更新（不要）
- [x] コミット: `✅ Extend test coverage with highlighter, brace matcher, commenter, and edge case tests`
