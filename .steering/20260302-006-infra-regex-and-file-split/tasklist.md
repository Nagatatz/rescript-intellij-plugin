# タスクリスト: インフラ改善 — Regex キャッシュ・統一・ファイル分割

## Phase 1: 準備

- [x] `feature/regex-and-file-split` ブランチを作成し worktree に入る

## Phase 2: #116 重複 Regex パターン統一

- [x] `RescriptRegexPatterns.kt` を `util/` に新設（LIDENT, UIDENT, WHITESPACE）
- [x] `RescriptRegexPatternsTest.kt` を作成
- [x] 重複パターンの参照元を `RescriptRegexPatterns` に書き換え（RescriptNamesValidator, RescriptExtractVariableUtil, RescriptTypeMismatchParser, RescriptRunUtils）
- [x] ビルド確認 + テスト実行
- [x] コミット: `♻️ Unify duplicated regex patterns into RescriptRegexPatterns`

## Phase 3: #115 Regex インスタンスキャッシュ

- [x] RescriptCommentEvalProvider.kt — 調査の結果、全パターンが既に companion object 定数。変更不要
- [x] RescriptPasteAsRescriptProcessor.kt — 4箇所のインライン Regex を companion object 定数に移動
- [x] RescriptTypeDeclarationParser.kt — 3箇所
- [x] RescriptUnwrapDescriptor.kt — 3箇所
- [x] RescriptExpressionTypeProvider.kt — 2箇所
- [x] RescriptPasteAsJsxProcessor.kt — 2箇所
- [x] RescriptSignatureSyncInspection.kt — 1箇所（動的1箇所は除外）
- [x] RescriptAddTypeAnnotationIntention.kt — 1箇所（動的1箇所は除外）
- [x] 残り8ファイル — 各1箇所（RescriptExtractComponentHandler, RescriptExtractFunctionHandler, RescriptMergeSwitchCasesIntention, RescriptTypeSignatureSearchContributor, RescriptPpxViewPanel, RescriptWorksheetRunner, RescriptSearchEverywhereContributor, RescriptMutabilityInspection）+ RescriptGenerateModuleImplAction 1箇所
- [x] ビルド確認 + テスト実行（BUILD SUCCESSFUL, 2517/2517 tests pass）
- [ ] コミット: `♻️ Move inline Regex instantiations to companion object constants`

## Phase 4: #117 長大ファイル分割

- [ ] RescriptJsonCodeGenerator.kt → RescriptJsonEncoderGenerator.kt + RescriptJsonDecoderGenerator.kt を抽出
- [ ] RescriptParser.kt → RescriptDeclarationParser.kt + RescriptJsxParser.kt を抽出
- [ ] RescriptDocumentationProvider.kt → RescriptOperatorDocumentation.kt + RescriptExternalDocUrls.kt を抽出
- [ ] RescriptUnwrapDescriptor.kt → RescriptUnwrappers.kt + RescriptUnwrapUtils.kt を抽出
- [ ] RescriptLspUtils.kt → RescriptLspSignatureParser.kt + RescriptLspDiagnosticParser.kt を抽出
- [ ] ビルド確認 + テスト実行
- [ ] コミット: `♻️ Split large files into focused modules`

## Phase 5: ドキュメント更新

- [ ] CLAUDE.md 更新（アーキテクチャセクション — util/ パッケージに RescriptRegexPatterns 追記）
- [ ] README.md 更新（該当する場合）
- [ ] sphinx-docs 更新（該当する場合）
- [ ] docs/product-requirements.md 更新（#115, #116, #117 を実装済みセクションに移動）
- [ ] コミット: `📝 Update docs for infrastructure refactoring (#115, #116, #117)`

## Phase 6: 完了

- [ ] `./gradlew clean buildPlugin` 成功確認
- [ ] 全テストパス確認
- [ ] tasklist.md 全タスク `[x]` 確認
- [ ] ユーザーにマージ確認
- [ ] main にマージ + ブランチ削除
