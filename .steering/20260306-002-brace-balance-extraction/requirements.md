# Requirements: Brace Balance Utility Extraction

## 概要

コードベース内に散在するブレースバランス追跡ロジックを `RescriptBraceBalanceUtil` に集約し、重複を排除する。

## 背景

以下の2種類のブレースバランスロジックが複数ファイルに重複している:

1. **テキストベース**: `String` の文字インデックスで括弧の対応を検出する `findMatchingBracket` パターン（4ファイルに重複）
2. **PSI ベース**: `PsiElement` の兄弟要素を走査して `IElementType` トークンの深度を追跡するパターン（`RescriptHighlightUsagesHandlerFactory` 内の6メソッドで繰り返し）

## 対象ファイル

### テキストベース重複
- `RescriptUnwrapDescriptor.kt` — `findMatchingBracket`, `findMatchingBrace`, `findMatchingParen`
- `RescriptInsertLabeledArgsIntention.kt` — `findMatchingParen` (重複)
- `RescriptConvertToLabeledArgsIntention.kt` — `findMatchingParen` (重複)
- `RescriptWordSelectionHandler.kt` — PSI版 `findMatchingBracket` (テキストベースに統一可能か要検討)

### PSI ベース重複
- `RescriptHighlightUsagesHandlerFactory.kt` — `skipWhitespace`, `skipWhitespaceBackward` を companion object に持つ。6つのスキャンメソッドが braceDepth パターンを繰り返すが、各メソッドのロジックが微妙に異なるため、共通ユーティリティへの抽出は `skipWhitespace` / `skipWhitespaceBackward` に限定する。

## 受け入れ条件

- [ ] `RescriptBraceBalanceUtil.kt` に共通ロジックが集約されている
- [ ] テキストベースの `findMatchingBracket`/`findMatchingBrace`/`findMatchingParen` が共通化されている
- [ ] PSI ベースの `skipWhitespace`/`skipWhitespaceBackward` が共通化されている
- [ ] 元のファイルから重複ロジックが除去されている
- [ ] `RescriptBraceBalanceUtilTest.kt` でユーティリティがテストされている
- [ ] 既存テスト全通過
- [ ] ビルド成功
