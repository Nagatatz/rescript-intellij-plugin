# C 優先度機能バッチ1 — タスクリスト

## 機能実装

### #53 Strip Trailing Spaces
- [x] `RescriptStripTrailingSpacesFilterFactory.kt` 実装
- [x] `RescriptStripTrailingSpacesFilterFactoryTest.kt` テスト作成
- [x] `plugin.xml` に登録
- [x] コミット

### #55 Formatting for Injected
- [x] `RescriptInjectedFormattingModelBuilder.kt` 実装
- [x] `RescriptInjectedFormattingModelBuilderTest.kt` テスト作成
- [x] `plugin.xml` に登録（#53 コミットに含む）
- [x] コミット

### #59 Grazie Text Extractor
- [x] `RescriptGrazieTextExtractor.kt` 実装
- [x] `rescript-grazie.xml` 作成
- [x] `plugin.xml` に `<depends>` 追加（#53 コミットに含む）
- [x] `RescriptGrazieTextExtractorTest.kt` テスト作成
- [x] コミット

### #60 Element Signature Provider
- [x] `RescriptElementSignatureProvider.kt` 実装
- [x] `RescriptElementSignatureProviderTest.kt` テスト作成
- [x] `plugin.xml` に登録（#53 コミットに含む）
- [x] コミット

### #61 Index Pattern Builder
- [x] `RescriptIndexPatternBuilder.kt` 実装
- [x] `RescriptIndexPatternBuilderTest.kt` テスト作成
- [x] `plugin.xml` に登録（#53 コミットに含む）
- [x] コミット

### #68 File Include Provider
- [x] `RescriptFileIncludeProvider.kt` 実装
- [x] `RescriptFileIncludeProviderTest.kt` テスト作成
- [x] `plugin.xml` に登録（#53 コミットに含む）
- [x] コミット

### #69 Editor Floating Toolbar
- [x] `RescriptFloatingToolbarProvider.kt` 実装
- [x] `RescriptFloatingToolbarProviderTest.kt` テスト作成
- [x] `plugin.xml` に登録（#53 コミットに含む）
- [x] コミット

## ドキュメント更新
- [x] CLAUDE.md — レイヤー 3 に 7 機能追加
- [x] README.md — Features セクションに 7 機能追加
- [x] sphinx-docs — 該当ページに説明追加
- [x] docs/product-requirements.md — 7 件を「実装済み」に移動
- [x] コミット

## 検証
- [x] `./gradlew clean buildPlugin` 成功
- [x] 全テストパス
- [x] KDoc 全クラスに付与確認

## マージ
- [ ] ユーザーにマージ可否確認
- [ ] main にマージ・ブランチ削除
