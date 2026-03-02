# Tasklist: C 優先度機能 残り 16 件

## Batch 2: 中〜高難易度

### #57 Scratch File
- [x] `RescriptScratchRootType.kt` + `RescriptScratchCreationHelper.kt` を実装
- [x] テスト作成
- [x] `plugin.xml` に登録
- [x] コミット

### #58 REPL
- [x] `RescriptReplToolWindowFactory.kt` + `RescriptReplPanel.kt` + `RescriptReplExecutor.kt` を実装
- [x] テスト作成（`RescriptReplExecutor` の companion object）
- [x] `plugin.xml` に登録
- [x] コミット

### #66 Suggested Refactoring
- [x] `RescriptSuggestedRefactoringInspection.kt` を実装
- [x] テスト作成
- [x] `plugin.xml` に登録
- [x] コミット

### #104 JS→ReScript 変換
- [x] `RescriptPasteAsRescriptProcessor.kt` を実装
- [x] テスト作成
- [x] `plugin.xml` に登録
- [x] コミット

## Batch 3: 高難易度

### #63 Inline Variable/Function
- [x] `RescriptInlineHandler.kt` を実装
- [x] テスト作成
- [x] `plugin.xml` に登録
- [x] コミット

### #65 Introduce Constant
- [x] `RescriptIntroduceConstantHandler.kt` を実装
- [x] テスト作成
- [x] `RefactoringSupportProvider` に登録
- [x] コミット

### #67 Dependency Diagram
- [x] `RescriptDependencyDiagramProvider.kt` + `RescriptDependencyDiagramModel.kt` を実装
- [x] テスト作成
- [x] 登録不要（ダイアグラムプラグイン非依存の自立実装）
- [x] コミット

### #86 React コンポーネント抽出
- [x] `RescriptExtractComponentHandler.kt` を実装
- [x] テスト作成
- [x] 登録不要（リファクタリングメニューから直接呼び出し）
- [x] コミット

### #87 PPX 展開ビュー
- [x] `RescriptPpxViewToolWindowFactory.kt` + `RescriptPpxViewPanel.kt` を実装
- [x] テスト作成（`RescriptPpxViewPanel` の companion object）
- [x] `plugin.xml` に登録
- [x] コミット

### #88 モジュールタイプ実装生成
- [ ] `RescriptGenerateModuleImplAction.kt` を実装
- [ ] テスト作成
- [ ] `plugin.xml` に登録
- [ ] コミット

### #105 型ホール支援
- [ ] `RescriptTypeHoleQuickFix.kt` を実装
- [ ] テスト作成
- [ ] `plugin.xml` に登録
- [ ] コミット

### #106 コメント内コード評価
- [ ] `RescriptCommentEvalProvider.kt` を実装
- [ ] テスト作成
- [ ] `plugin.xml` に登録
- [ ] コミット

### #107 Worksheet モード
- [ ] `RescriptWorksheetFileType.kt` + `RescriptWorksheetRunner.kt` を実装
- [ ] テスト作成（`RescriptWorksheetRunner` の companion object）
- [ ] `plugin.xml` に登録
- [ ] コミット

## Batch 4: 非常に高難易度

### #62 Extract Function
- [ ] `RescriptExtractFunctionHandler.kt` を実装
- [ ] テスト作成
- [ ] `plugin.xml` に登録
- [ ] コミット

### #64 Change Signature
- [ ] `RescriptChangeSignatureHandler.kt` + `RescriptChangeSignatureDialog.kt` を実装
- [ ] テスト作成（`RescriptChangeSignatureHandler` の companion object）
- [ ] `plugin.xml` に登録
- [ ] コミット

### #108 型シグネチャ検索
- [ ] `RescriptTypeSignatureSearchContributor.kt` を実装
- [ ] テスト作成
- [ ] `plugin.xml` に登録
- [ ] コミット

## ドキュメント更新

- [ ] `CLAUDE.md` — レイヤー 3 に 16 機能追加
- [ ] `README.md` — Features セクションに 16 機能追加
- [ ] `sphinx-docs/user/features/` — 該当ページに説明・使用例を追加
- [ ] `docs/product-requirements.md` — 16 件を「実装済み」に移動、残り 0 件に更新
- [ ] ドキュメントコミット

## 検証・マージ

- [ ] `./gradlew clean buildPlugin` 成功
- [ ] 全テストパス
- [ ] `main` にマージ
