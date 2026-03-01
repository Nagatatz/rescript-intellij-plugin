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
- [ ] `RescriptGrazieTextExtractor.kt` 実装
- [ ] `rescript-grazie.xml` 作成
- [ ] `plugin.xml` に `<depends>` 追加
- [ ] `RescriptGrazieTextExtractorTest.kt` テスト作成
- [ ] コミット

### #60 Element Signature Provider
- [ ] `RescriptElementSignatureProvider.kt` 実装
- [ ] `RescriptElementSignatureProviderTest.kt` テスト作成
- [ ] `plugin.xml` に登録
- [ ] コミット

### #61 Index Pattern Builder
- [ ] `RescriptIndexPatternBuilder.kt` 実装
- [ ] `RescriptIndexPatternBuilderTest.kt` テスト作成
- [ ] `plugin.xml` に登録
- [ ] コミット

### #68 File Include Provider
- [ ] `RescriptFileIncludeProvider.kt` 実装
- [ ] `RescriptFileIncludeProviderTest.kt` テスト作成
- [ ] `plugin.xml` に登録
- [ ] コミット

### #69 Editor Floating Toolbar
- [ ] `RescriptFloatingToolbarProvider.kt` 実装
- [ ] `RescriptFloatingToolbarProviderTest.kt` テスト作成
- [ ] `plugin.xml` に登録
- [ ] コミット

## ドキュメント更新
- [ ] CLAUDE.md — レイヤー 3 に 7 機能追加
- [ ] README.md — Features セクションに 7 機能追加
- [ ] sphinx-docs — 該当ページに説明追加
- [ ] docs/product-requirements.md — 7 件を「実装済み」に移動
- [ ] コミット

## 検証
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] 全テストパス
- [ ] KDoc 全クラスに付与確認

## マージ
- [ ] ユーザーにマージ可否確認
- [ ] main にマージ・ブランチ削除
