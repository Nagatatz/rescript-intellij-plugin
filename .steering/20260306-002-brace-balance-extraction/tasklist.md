# Tasklist: Brace Balance Utility Extraction

## 1. ユーティリティ作成

- [x] `RescriptBraceBalanceUtil.kt` にテキストベース関数（`findMatchingBracket`, `findMatchingParen`, `findMatchingBrace`）を追加
- [x] `RescriptBraceBalanceUtil.kt` に PSI ベース関数（`skipWhitespace`, `skipWhitespaceBackward`）を追加

## 2. 重複ロジック置換

- [x] `RescriptUnwrapDescriptor.kt` のローカル関数を `RescriptBraceBalanceUtil` への委譲に置換
- [x] `RescriptInsertLabeledArgsIntention.kt` のローカル `findMatchingParen` を置換
- [x] `RescriptConvertToLabeledArgsIntention.kt` のローカル `findMatchingParen` を置換
- [x] `RescriptHighlightUsagesHandlerFactory.kt` の `skipWhitespace`/`skipWhitespaceBackward` を置換

## 3. テスト

- [x] `RescriptBraceBalanceUtilTest.kt` にテキストベース関数のテストケース追加
- [x] PSI ベース関数のテスト省略理由: IDE テスト環境 + PsiElement モック必要。既存 HighlightUsagesHandlerFactory も IDE テストなし
- [x] 既存テスト全通過確認 (`./gradlew test`)

## 4. ビルド・コミット

- [x] `./gradlew clean buildPlugin` 成功確認
- [x] コミット（♻️ Extract brace balance utilities into RescriptBraceBalanceUtil）
- [ ] main にマージ
