# Tasklist: テストカバレッジ拡充 & 公開ドキュメント更新

## Phase 1: テストインフラ整備

- [ ] 1.1 `RescriptTestUtils.kt` を作成（`stubProxy`, `stubAstNode`, `SimpleStubElement` を `RescriptImportOptimizerTest` から抽出）
- [ ] 1.2 `RescriptImportOptimizerTest.kt` を `RescriptTestUtils` を使うようリファクタリング
- [ ] 1.3 テストが通ることを確認（`./gradlew test`）

## Phase 2: テスト追加（純粋ロジック）

- [ ] 2.1 `RescriptNamesValidatorTest.kt` を作成
- [ ] 2.2 `RescriptCommandTest.kt` を作成
- [ ] 2.3 `RescriptPostfixTemplateProviderTest.kt` を作成

## Phase 3: テスト追加（PSI スタブ利用）

- [ ] 3.1 `RescriptPsiUtilsTest.kt` を作成
- [ ] 3.2 `RescriptBreadcrumbsProviderTest.kt` を作成
- [ ] 3.3 `RescriptStructureViewElementTest.kt` を作成

## Phase 4: テスト追加（Inspection）

- [ ] 4.1 `RescriptDuplicateOpenInspectionTest.kt` を作成
- [ ] 4.2 `RescriptEmptyModuleInspectionTest.kt` を作成

## Phase 5: テスト全体確認

- [ ] 5.1 `./gradlew test` で全テスト通過を確認

## Phase 6: テストコミット

- [ ] 6.1 tasklist.md 更新 & コミット（`✅ Add unit tests for 8 untested source files`）

## Phase 7: ドキュメント更新

- [ ] 7.1 `README.md` の Features セクションを全機能に更新
- [ ] 7.2 `plugin.xml` の `<description>` を主要機能に更新

## Phase 8: ドキュメントコミット

- [ ] 8.1 `./gradlew buildPlugin` で成功確認
- [ ] 8.2 tasklist.md 更新 & コミット（`📝 Update README and plugin.xml feature descriptions`）
