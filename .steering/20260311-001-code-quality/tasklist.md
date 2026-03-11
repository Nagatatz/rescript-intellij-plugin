# タスクリスト: KDoc コメント欠落補完 & ドキュメント品質改善

## KDoc コメント補完

- [x] `binding/DtsJsonModel.kt` — 20 箇所の内部データクラスに KDoc 追加
- [x] `wizard/templates/*.kt` — 10 ファイルの `internal object` に KDoc 追加
- [x] `generate/RescriptTypeDeclarationParser.kt` — `TypeShape` サブクラス 3 箇所に KDoc 追加
- [x] `analysis/RescriptReanalyzeAnnotator.kt` — 内部データクラス 3 箇所に KDoc 追加
- [x] `lsp/RescriptLsp4jClient.kt` — パラメータデータクラス 2 箇所に KDoc 追加
- [x] `lsp/RescriptCompilationStatusService.kt` — `CompilationStatus` に KDoc 追加
- [x] `preview/RescriptCompiledJsPreviewPanel.kt` — private inner `AnAction` 2 箇所に KDoc 追加
- [x] `settings/RescriptProjectSettings.kt` — `State` クラスに KDoc 追加
- [x] `editor/RescriptSmartEnterProcessor.kt` — `LineAnalysis` に KDoc 追加

## ドキュメント改善

- [x] `sphinx-docs/user/features/index.md` — 全カテゴリへの言及を追加
- [x] `sphinx-docs/user/features/code-editing.md` — "Paste as ReScript" アクセス方法を追記
- [x] `sphinx-docs/user/features/advanced.md` — "Predefined Code Style" と "Color Preview" を改善
- [x] `docs/product-requirements.md` — Section 4 ステータス更新、Section 6 チェックボックス更新

## コミット・マージ

- [x] `./gradlew clean buildPlugin` 成功確認
- [x] KDoc コメント補完コミット
- [x] ドキュメント改善コミット
- [ ] `main` にマージ
