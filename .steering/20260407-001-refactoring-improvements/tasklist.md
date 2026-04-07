# タスクリスト: コードベースリファクタリング

## R1: skipWhitespace 関数の統一

- [x] `RescriptBraceBalanceUtil.kt` に `skipWhitespaceAndEolBackward` メソッドを追加
- [x] `RescriptRawJsInjector.kt` の private `skipWhitespace` を `RescriptBraceBalanceUtil.skipWhitespaceAndEolBackward` 呼び出しに置換
- [x] `RescriptBraceBalanceUtilTest.kt` のコメント更新（PSI ベースメソッドは IDE テスト基盤が必要なため間接テスト）
- [x] コミット: `♻️ Extract skipWhitespaceAndEolBackward to RescriptBraceBalanceUtil`

## R3: RescriptFormatCheckAnnotator の責務分離

- [x] `RescriptProcessUtils.kt` に `StdinProcessResult` データクラスと `executeWithStdin` メソッドを追加
- [x] `RescriptFormatCheckAnnotator.kt` の `runFormatCheck` を `RescriptProcessUtils.executeWithStdin` 使用に置換
- [x] `RescriptProcessUtilsTest.kt` に `executeWithStdin` のテストを追加（5テスト）
- [x] コミット: `♻️ Extract process execution logic to RescriptProcessUtils`

## R4: Paste Processors の共通化

- [x] `RescriptBasePasteProcessor.kt` を新規作成（共通ワークフロー基底クラス）
- [x] `RescriptPasteAsRescriptProcessor.kt` を基底クラス継承に変更
- [x] `RescriptPasteAsJsxProcessor.kt` を基底クラス継承に変更
- [x] 既存テストがパスすることを確認
- [x] コミット: `♻️ Extract common paste processor base class`
- テスト省略理由: `RescriptBasePasteProcessor` は抽象基底クラスであり、具象サブクラスの既存テスト（`RescriptPasteAsRescriptProcessorTest`, `RescriptPasteAsJsxProcessorTest`）で間接的にテスト済み

## R6: JSON Codec Generators の共通型ハンドリング抽出

- [x] `RescriptJsonCodeGenerator.kt` に `mapFieldsWithType`, `classifyPayloadTypes`, `isSimpleEnum` ヘルパーを追加
- [x] `RescriptJsonEncoderGenerator.kt` をヘルパー使用に変更
- [x] `RescriptJsonDecoderGenerator.kt` をヘルパー使用に変更
- [x] 既存テストがパスすることを確認
- [x] コミット: `♻️ Extract common field iteration helpers in JSON codec generators`

## R7: HighlightUsagesHandlerFactory の制御フロー簡素化

- [x] `RescriptHighlightUsagesHandlerFactory.kt` の `when` 式をマップルックアップに置換
- [x] 既存テストがパスすることを確認
- [x] コミット: `♻️ Simplify HighlightUsagesHandlerFactory with keyword mapping`

## 最終検証

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功
- [x] ドキュメント更新不要を確認（リファクタリングのみ、機能変更なし）
- [x] main にマージ
