# Window Instructions: テストカバレッジ 100% 到達

## Worktree 1: test/unit-tests（純粋ユニットテスト）

```
cd /Users/ngtz/Documents/repos/rescript-wt-unit-tests

ブランチ `test/unit-tests` で純粋ユニットテストを実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260219-unit-tests/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

要件概要:
- IDE 依存なしの純粋ユニットテスト 12 ファイルを新規作成
- 既存テストパターン（JUnit 4、スタブベース、RescriptTestUtils）に準拠
- テスト対象:
  1. RescriptNamesValidatorTest.kt — isIdentifier(), isKeyword() テスト（project引数はnull渡し）
  2. RescriptCommandTest.kt — fromId(), enum プロパティ, args 検証
  3. RescriptCliDetectorTest.kt — findCli() テスト（一時ディレクトリにnode_modules/.bin/rescript作成）
  4. RescriptSemanticTokensSupportTest.kt — getTextAttributesKey() 全トークンタイプマッピング
  5. RescriptCompilationStatusServiceTest.kt — updateStatus(), addListener(), 初期状態UNKNOWN（ProjectスタブはstubProxy使用）
  6. RescriptSpellcheckingStrategyTest.kt — getTokenizer() トークンタイプ→トークナイザーマッピング
  7. RescriptPsiUtilsTest.kt — extractName(), getIcon(), getElementDescription()（stubAstNodeWithChildren使用）
  8. RescriptTypeDeclarationParserTest.kt — parse() Variant/Record/Unknown, extractTypeName()
  9. RescriptRunConfigurationTypeTest.kt — ID, displayName 検証
  10. RescriptConfigurationFactoryTest.kt — ID, optionsClass 検証
  11. RescriptTestRunConfigurationTypeTest.kt — ID, displayName 検証
  12. RescriptTestConfigurationFactoryTest.kt — ID, optionsClass 検証

## ステップ 2: 実装
設計に従い12テストファイルを実装。テストパターン参考: src/test/kotlin/com/rescript/plugin/ 配下の既存テスト。

## ステップ 3: ビルド確認
`./gradlew test` を実行し、全テスト通過を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット: `✅ Add 12 pure unit test files for untested classes`
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/test-coverage-100` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/test-coverage-100
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge test/unit-tests
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-unit-tests
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d test/unit-tests

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Worktree 2: test/inspection-analysis（Inspection & Analysis）

```
cd /Users/ngtz/Documents/repos/rescript-wt-inspection-analysis

ブランチ `test/inspection-analysis` で Inspection & Analysis テストを実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260219-inspection-analysis-tests/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

要件概要:
- 新規テスト 3 ファイル + 既存テスト改善 4 ファイル
- 新規:
  1. RescriptEmptyModuleInspectionTest.kt — メタ情報、QuickFix familyName、hasDeclarationChildren ロジック
  2. RescriptDuplicateOpenInspectionTest.kt — メタ情報、QuickFix familyName、重複検出ロジック
  3. RescriptMissingConfigInspectionTest.kt — メタ情報、検証対象ファイル名リスト
- 改善:
  4. RescriptReanalyzeAnnotatorTest.kt — parseJsonOutput 完全パスマッチ/逆方向マッチ、parseAllDiagnostics range.size<4、doAnnotate(null)
  5. RescriptReanalyzeQuickFixTest.kt — isAvailable(), findWordStart空文字列, findWordEnd末尾超過, アポストロフィ含み
  6. RescriptUnusedCodeInspectionTest.kt — isGraphNeeded, getDisplayName, getGroupDisplayName
  7. RescriptDependencyAnalyzerTest.kt — extractModulePath PSIスタブ(OPEN+UIDENT+DOT+UIDENT)、単一モジュール、子要素なし

## ステップ 2: 実装
設計に従い7テストファイルを実装/改善。

## ステップ 3: ビルド確認
`./gradlew test` を実行し、全テスト通過を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット: `✅ Add and improve inspection & analysis tests`
※ 共有ドキュメントはバッチブランチで一括更新するため、このウィンドウでは更新不要。

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/test-coverage-100` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/test-coverage-100
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge test/inspection-analysis
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-inspection-analysis
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d test/inspection-analysis

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Worktree 3: test/editor-intention（Editor & Intention）

```
cd /Users/ngtz/Documents/repos/rescript-wt-editor-intention

ブランチ `test/editor-intention` で Editor & Intention テストを実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260219-editor-intention-tests/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

要件概要:
- 新規テスト 3 ファイル + 既存テスト改善 8 ファイル
- 新規:
  1. RescriptBreadcrumbsProviderTest.kt — getLanguages(), acceptElement(NAVIGABLE_TYPES→true/false), getElementInfo()
  2. RescriptStructureViewElementTest.kt — getAlphaSortKey(), getPresentation(), getChildren()（NavigatablePsiElementスタブ）
  3. RescriptPostfixTemplateProviderTest.kt — テンプレート数=7, 各key検証, isTerminalSymbol
- 改善:
  4. RescriptSmartEnterProcessorTest.kt — analyzeLine 追加エッジケース（空行、コメント行、テンプレートリテラル）
  5. RescriptStatementUpDownMoverTest.kt — findDeclaration/findNextDeclaration/findPreviousDeclaration PSIスタブテスト
  6. RescriptWrapWithIntentionTest.kt — startInWriteAction(), wrapper プロパティ, isAvailableロジック
  7. RescriptAddGenTypeIntentionTest.kt — startInWriteAction(), findParentDeclaration/hasGenTypeAnnotation PSIスタブ
  8. RescriptSurroundDescriptorTest.kt — isApplicable 空配列/非空配列
  9. RescriptFoldingBuilderTest.kt — JSX_ELEMENT/JSX_FRAGMENT プレースホルダー, extractJsxTagName ドット付き
  10. RescriptCustomFoldingProviderTest.kt — isCustomRegionEnd空白付き, getPlaceholderText空名前バリアント
  11. RescriptLineIndentProviderTest.kt — isSuitableFor(RescriptLanguage/null), findLastSignificantToken追加

## ステップ 2: 実装
設計に従い11テストファイルを実装/改善。

## ステップ 3: ビルド確認
`./gradlew test` を実行し、全テスト通過を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット: `✅ Add and improve editor & intention tests`
※ 共有ドキュメントはバッチブランチで一括更新するため、このウィンドウでは更新不要。

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/test-coverage-100` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/test-coverage-100
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge test/editor-intention
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-editor-intention
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d test/editor-intention

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Worktree 4: test/navigation-misc（Navigation & Misc）

```
cd /Users/ngtz/Documents/repos/rescript-wt-navigation-misc

ブランチ `test/navigation-misc` で Navigation & Misc テストを実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260219-navigation-misc-tests/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

要件概要:
- 新規テスト 1 ファイル + 既存テスト改善 7 ファイル
- 新規:
  1. RescriptRawJsInjectorTest.kt — getInjectionRange テスト（"content"→TextRange(1,8), ""→null, テンプレート文字列→フルレンジ）、isInsideRawBlock PSIスタブ
- 改善:
  2. RescriptQualifiedNameProviderTest.kt — findDeclarationElement/buildModulePath PSIスタブ, getQualifiedName
  3. RescriptGotoRelatedProviderTest.kt — .mjs/.js JS出力バリアント, サブディレクトリ
  4. RescriptImportOptimizerTest.kt — extractModulePath child.node==null, supports()追加
  5. RescriptPasteAsJsonActionTest.kt — getActionUpdateThread()==BGT, escapeStringバックスラッシュ
  6. RescriptTestFrameworkDetectorTest.kt — 未カバー分岐追加
  7. RescriptTestLocatorTest.kt — 未カバー分岐追加
  8. RescriptCompilerStatusWidgetFactoryTest.kt — 未カバー分岐追加

## ステップ 2: 実装
設計に従い8テストファイルを実装/改善。

## ステップ 3: ビルド確認
`./gradlew test` を実行し、全テスト通過を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット: `✅ Add and improve navigation & misc tests`
※ 共有ドキュメントはバッチブランチで一括更新するため、このウィンドウでは更新不要。

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/test-coverage-100` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/test-coverage-100
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge test/navigation-misc
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-navigation-misc
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d test/navigation-misc

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```
