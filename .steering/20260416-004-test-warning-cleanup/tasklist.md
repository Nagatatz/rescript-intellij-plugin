# タスクリスト: テストコンパイラ警告クリーンアップ

## Phase 1: 準備

- [x] `EnterWorktree` で作業 worktree `test-warning-cleanup` を作成
- [x] 現在のベースライン警告件数を記録（53 件）

## Phase 2: 実装

### コミット 1 — 壊れたテスト修正 (category 2)

- [x] `src/test/kotlin/com/rescript/plugin/lang/RescriptFindUsagesProviderTest.kt` を書き換え
  - `null as? PsiElement ?: return` を削除し、Proxy スタブで非 null PsiElement を渡すよう修正
- [x] `src/test/kotlin/com/rescript/plugin/navigation/RescriptQualifiedNameProviderTest.kt:178-184` を書き換え
  - `null as? Project ?: return` を Proxy スタブの非 null Project に置き換え
- [x] `./gradlew test --tests "*RescriptFindUsagesProviderTest" --tests "*RescriptQualifiedNameProviderTest"` で成功確認
- [x] tasklist.md 更新 + 個別ファイル指定でコミット
  - `🐛 Fix broken tests with unreachable assertions in FindUsagesProvider and QualifiedNameProvider`

### コミット 2 — Deprecated API 置換 (category 3)

- [x] `src/test/kotlin/com/rescript/plugin/navigation/RescriptOpenCompiledJsActionTest.kt:180` を `createEvent(...)` に置換、`ActionUiKind` import 追加
- [x] `src/test/kotlin/com/rescript/plugin/navigation/RescriptSwitchFileActionTest.kt:122` を同様に置換
- [x] `src/test/kotlin/com/rescript/plugin/generate/RescriptGenerateActionUtilTest.kt:40` を同様に置換
- [x] `./gradlew test --tests "*OpenCompiledJs*" --tests "*SwitchFile*" --tests "*GenerateActionUtil*"` で成功確認
- [x] tasklist.md 更新 + 個別ファイル指定でコミット
  - `♻️ Replace deprecated AnActionEvent.createFromDataContext with createEvent`

### コミット 3 — Deprecated override 警告抑制 (category 4)

- [x] `src/test/kotlin/com/rescript/plugin/hierarchy/call/RescriptCallHierarchyProviderTest.kt:49` の `override fun getData(dataId: String)` に `@Suppress("OVERRIDE_DEPRECATION")` を付与
- [x] `./gradlew test --tests "*CallHierarchyProviderTest"` で成功確認
- [x] tasklist.md 更新 + 個別ファイル指定でコミット
  - `♻️ Suppress deprecated DataContext.getData override warning in CallHierarchy test`

### コミット 4 — Unchecked cast 警告抑制 (category 5)

- [x] `src/test/kotlin/com/rescript/plugin/hierarchy/RescriptDependencyAnalyzerTest.kt:262` の `childArray as Array<PsiElement>` に `@Suppress("UNCHECKED_CAST")` を付与
- [x] `./gradlew test --tests "*DependencyAnalyzerTest"` で成功確認
- [x] tasklist.md 更新 + 個別ファイル指定でコミット
  - `♻️ Suppress unchecked cast in stub element test scaffolding`

### コミット 5 — Windows 不正文字テスト名リネーム (category 6)

- [x] `src/test/kotlin/com/rescript/plugin/lsp/RescriptLspSignatureParserTest.kt:44` のテスト名 `` `parseSignatureLabels parses optional param with =?` `` を `` `parseSignatureLabels parses optional param with trailing question mark` `` に変更
- [x] `./gradlew test --tests "*RescriptLspSignatureParserTest"` で成功確認
- [x] tasklist.md 更新 + 個別ファイル指定でコミット
  - `♻️ Rename test to remove Windows-unsafe question mark character`

### コミット 6 — 常に true の is チェック整理 (category 1a + 1b)

#### 1a: ローカル変数型を `Any` に広げる (25 箇所)

- [ ] `src/test/kotlin/com/rescript/plugin/analysis/RescriptUnusedCodeInspectionTest.kt:131`
- [ ] `src/test/kotlin/com/rescript/plugin/completion/RescriptCompletionConfidenceTest.kt:16, 39`
- [ ] `src/test/kotlin/com/rescript/plugin/completion/RescriptLiveTemplateMacrosTest.kt:36, 42`
- [ ] `src/test/kotlin/com/rescript/plugin/completion/RescriptTemplateContextTypeTest.kt:19`
- [ ] `src/test/kotlin/com/rescript/plugin/config/RescriptFileTypeRecoveryStartupActivityTest.kt:29`
- [ ] `src/test/kotlin/com/rescript/plugin/config/RescriptFrameworkDetectorTest.kt:19`
- [ ] `src/test/kotlin/com/rescript/plugin/documentation/RescriptDocumentationProviderTest.kt:25`
- [ ] `src/test/kotlin/com/rescript/plugin/editor/RescriptEnterHandlerTest.kt:18`
- [ ] `src/test/kotlin/com/rescript/plugin/editor/RescriptJoinLinesHandlerTest.kt:18`
- [ ] `src/test/kotlin/com/rescript/plugin/generate/RescriptBaseGenerateActionTest.kt:34`
- [ ] `src/test/kotlin/com/rescript/plugin/highlight/RescriptHighlightUsagesHandlerFactoryTest.kt:18`
- [ ] `src/test/kotlin/com/rescript/plugin/inspection/RescriptDuplicateOpenInspectionTest.kt:18`
- [ ] `src/test/kotlin/com/rescript/plugin/inspection/RescriptEmptyModuleInspectionTest.kt:18`
- [ ] `src/test/kotlin/com/rescript/plugin/inspection/RescriptMissingConfigInspectionTest.kt:18`
- [ ] `src/test/kotlin/com/rescript/plugin/inspection/RescriptMutabilityInspectionTest.kt:78`
- [ ] `src/test/kotlin/com/rescript/plugin/inspection/RescriptStyleLintInspectionTest.kt:77`
- [ ] `src/test/kotlin/com/rescript/plugin/inspection/RescriptSuggestedRefactoringInspectionTest.kt:89`
- [ ] `src/test/kotlin/com/rescript/plugin/intention/RescriptBaseIntentionTest.kt:85`
- [ ] `src/test/kotlin/com/rescript/plugin/lsp/RescriptExpressionTypeProviderTest.kt:18`
- [ ] `src/test/kotlin/com/rescript/plugin/navigation/RescriptGotoSuperHandlerTest.kt:19`
- [ ] `src/test/kotlin/com/rescript/plugin/navigation/RescriptSymbolContributorTest.kt:27`
- [ ] `src/test/kotlin/com/rescript/plugin/quickfix/RescriptTypeHoleQuickFixTest.kt:85`
- [ ] `src/test/kotlin/com/rescript/plugin/refactor/RescriptExtractVariableHandlerTest.kt:18`
- [ ] `src/test/kotlin/com/rescript/plugin/refactor/RescriptRefactoringSupportProviderTest.kt:23`
- [ ] `src/test/kotlin/com/rescript/plugin/run/RescriptRunAnythingProviderTest.kt:53`

#### 1b: `@Suppress("KotlinConstantConditions")` 付与 (2 ファイル)

- [ ] `src/test/kotlin/com/rescript/plugin/lang/psi/RescriptDeclarationElementTypeTest.kt:39, 44, 53` の各 `@Test` 関数に `@Suppress` 付与
- [ ] `src/test/kotlin/com/rescript/plugin/lang/psi/RescriptPsiTest.kt` の該当 `@Test` 関数に `@Suppress` 付与

#### 検証

- [ ] `./gradlew test` を全体実行し、警告が残っていないことを確認
- [ ] tasklist.md 更新 + 個別ファイル指定でコミット
  - `♻️ Clarify runtime type assertions in 25 test classes to remove always-true warnings`

## Phase 3: 最終検証

- [ ] `./gradlew ktlintCheck` が成功する
- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] `./gradlew test` が成功し、**テストソースからの `w:` 警告が 0 件**
- [ ] テスト通過数が変更前と同等以上（`BUILD SUCCESSFUL` を確認）
- [ ] カバレッジ `./gradlew koverHtmlReport` が `minBound=86` を下回らない

## Phase 4: マージ前確認

- [ ] すべてのタスクが `[x]` になっている
- [ ] `AskUserQuestion` でユーザーに main マージ可否を確認する

## Phase 5: マージ実行・クリーンアップ

- [ ] worktree 内で `git checkout main && git merge worktree-test-warning-cleanup`
- [ ] 作業ブランチを `git branch -d worktree-test-warning-cleanup`
- [ ] セッション終了

## テスト免除クラスに関する備考

このタスクはテストコードのみを編集し、新規プロダクションクラスを作成しない。よってテスト新規作成は不要。既存テストの書き換え・削除・注釈追加のみ。
