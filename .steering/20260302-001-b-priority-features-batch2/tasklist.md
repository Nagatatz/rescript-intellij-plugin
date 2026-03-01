# B 優先度機能一括実装 (バッチ2) — タスクリスト

## 機能実装

### #56 Framework Detector
- [x] `RescriptFrameworkDetector.kt` 実装
- [x] `RescriptFrameworkDetectorTest.kt` テスト作成
- [x] `rescript-json.xml` に登録
- [x] コミット

### #52 Code Rearranger
- [x] `RescriptRearranger.kt` 実装
- [x] `RescriptRearrangerTest.kt` テスト作成
- [x] `plugin.xml` に登録
- [x] コミット

### #103 変更可能性の診断
- [x] `RescriptMutabilityInspection.kt` 実装
- [x] `RescriptMutabilityInspectionTest.kt` テスト作成
- [x] `plugin.xml` に登録
- [x] コミット

### #102 スタイルリンティング
- [x] `RescriptStyleLintInspection.kt` 実装
- [x] `RescriptStyleLintInspectionTest.kt` テスト作成
- [x] `plugin.xml` に登録
- [x] コミット

### #97 filter+map チェーン変換
- [x] `RescriptFilterMapChainIntention.kt` 実装
- [x] `RescriptFilterMapChainIntentionTest.kt` テスト作成
- [x] `plugin.xml` に登録
- [x] コミット

### #85 型注釈追加
- [x] `RescriptAddTypeAnnotationIntention.kt` 実装
- [x] `RescriptAddTypeAnnotationIntentionTest.kt` テスト作成 (LSP 部分は免除)
- [x] `plugin.xml` に登録
- [x] コミット

### #109 PPX 可視化
- [x] `RescriptPpxVisualizationProvider.kt` 実装
- [x] `RescriptPpxVisualizationProviderTest.kt` テスト作成
- [x] `plugin.xml` に登録
- [x] コミット

### #99 型ミスマッチ差分表示
- [x] `RescriptTypeDiffComputer.kt` 実装
- [x] `RescriptTypeDiffComputerTest.kt` テスト作成
- [x] `RescriptErrorLensRenderer.kt` 修正
- [x] `RescriptTypeMismatchParser.kt` 修正不要（既存 API で十分）
- [x] コミット

## ドキュメント更新
- [ ] CLAUDE.md — レイヤー 3 に 8 機能追加
- [ ] README.md — Features セクションに 8 機能追加
- [ ] docs/product-requirements.md — 8 件を「実装済み」に移動
- [ ] コミット

## 検証
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] 全テストパス
- [ ] KDoc 全クラスに付与確認

## マージ
- [ ] ユーザーにマージ可否確認
- [ ] main にマージ・ブランチ削除
