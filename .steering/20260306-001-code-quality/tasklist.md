# Tasklist: Code Quality Quick Wins

## 1. 空の catch ブロックにコメント追加

- [x] `RescriptFormattingService.kt` の 2 箇所に broken pipe コメント追加
- [x] `RescriptFormatCheckAnnotator.kt` の 2 箇所に broken pipe コメント追加

## 2. Regex パターン集約

- [x] `RescriptRegexPatterns.kt` に CONSTRUCTOR_WITH_PAYLOAD, LABELED_PARAM_NAME, INCLUDE_MODULE_CAPTURE を追加
- [x] `RescriptTypeDeclarationParser.kt` のローカル CONSTRUCTOR_PATTERN を共通パターンに置換
- [x] `RescriptLspSignatureParser.kt` のローカル CONSTRUCTOR_PATTERN を共通パターンに置換
- [x] `RescriptGenerateDocCommentIntention.kt` のローカル LABELED_PARAM_REGEX を共通パターンに置換
- [x] `RescriptDependencyDiagramProvider.kt` のローカル INCLUDE_PATTERN を共通パターンに置換

## 3. テスト

- [x] `RescriptRegexPatternsTest.kt` に新パターンのテストケース追加
- [x] 既存テスト全通過確認 (`./gradlew test`)

## 4. ビルド・コミット

- [x] `./gradlew clean buildPlugin` 成功確認
- [x] コミット（♻️ Centralize regex patterns and document empty catch blocks）
- [x] main にマージ
