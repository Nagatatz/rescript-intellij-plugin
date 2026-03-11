# タスクリスト: JetBrains プラグインベストプラクティス改善

## コミット 1: getActionUpdateThread() 追加

- [x] `RescriptGenerateGroup.kt` に `getActionUpdateThread()` 追加
- [x] `RescriptGenerateSwitchAction.kt` に追加
- [x] `RescriptGenerateModuleTypeAction.kt` に追加
- [x] `RescriptGenerateModuleImplAction.kt` に追加
- [x] `RescriptGenerateMakeAction.kt` に追加
- [x] `RescriptGenerateRecordValueAction.kt` に追加
- [x] `RescriptGenerateJsonCodecAction.kt` に追加
- [x] `RescriptChangeSignatureAction` (内部クラス) に追加
- [x] `RescriptCompiledJsPreviewPanel` の `OpenInEditorAction` に追加
- [x] 既存テストに `getActionUpdateThread()` 検証を追加
- [x] コミット

## コミット 2: CopyOnWriteArrayList 使用

- [x] `RescriptCompilationStatusService.kt` のリスナーリスト変更
- [x] テスト免除理由: IDE ライフサイクル依存の `@Service` クラス
- [x] コミット

## コミット 3: ModalityState 明示

- [ ] `RescriptTypeInfoPanel.kt` の `invokeLater` に `ModalityState.any()` 追加
- [ ] `RescriptErrorLensManager.kt` の `invokeLater` に `ModalityState.any()` 追加
- [ ] テスト免除理由: Swing UI コンポーネント
- [ ] コミット

## 検証

- [ ] `./gradlew clean buildPlugin test` 成功
- [ ] tasklist 全タスク完了確認

## マージ

- [ ] main にマージ
