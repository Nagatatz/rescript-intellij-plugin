# タスクリスト: A 優先度機能実装

## 準備

- [x] feature/a-priority-features ブランチ作成

## 共通ユーティリティ

- [x] RescriptLspUtils.kt 作成（LSP hover 取得、シグネチャパース）
- [x] RescriptLspUtilsTest.kt 作成
- [x] コミット: `✨ Add LSP utility helpers for hover and signature parsing`

## #46 Search Everywhere

- [ ] RescriptSearchEverywhereContributor.kt 実装
- [ ] RescriptSearchEverywhereContributorTest.kt 作成
- [ ] plugin.xml に登録
- [ ] コミット: `✨ Add Search Everywhere contributor for ReScript symbols`

## #49 Unresolved Reference Quick Fix

- [ ] RescriptAddOpenQuickFix.kt 実装
- [ ] RescriptQualifyReferenceQuickFix.kt 実装
- [ ] テスト作成
- [ ] plugin.xml に登録
- [ ] コミット: `✨ Add unresolved reference quick fix with open/qualify suggestions`

## #50 Completion Weigher

- [ ] RescriptCompletionWeigher.kt 実装
- [ ] RescriptCompletionWeigherTest.kt 作成
- [ ] plugin.xml に登録
- [ ] コミット: `✨ Add completion weigher for context-based candidate ordering`

## #74 パイプチェーン中間型ヒント

- [ ] RescriptPipeChainTypeHintsProvider.kt 実装
- [ ] RescriptPipeChainTypeHintsProviderTest.kt 作成
- [ ] plugin.xml に登録
- [ ] コミット: `✨ Add pipe chain intermediate type inlay hints`

## #75 ラベル付き引数の一括挿入

- [ ] RescriptInsertLabeledArgsIntention.kt 実装
- [ ] RescriptInsertLabeledArgsIntentionTest.kt 作成
- [ ] plugin.xml に登録
- [ ] コミット: `✨ Add intention to insert labeled arguments from function signature`

## #77 Make 関数生成

- [ ] RescriptTypeDeclarationParser.kt 拡張（レコードフィールドパース）
- [ ] RescriptGenerateMakeAction.kt 実装
- [ ] テスト作成
- [ ] コミット: `✨ Add generate make function for record types`

## #78 Switch ケース統合

- [ ] RescriptMergeSwitchCasesIntention.kt 実装
- [ ] RescriptMergeSwitchCasesIntentionTest.kt 作成
- [ ] plugin.xml に登録
- [ ] コミット: `✨ Add intention to merge switch cases with identical bodies`

## #89 使用箇所からの関数生成

- [ ] RescriptGenerateFunctionQuickFix.kt 実装
- [ ] RescriptGenerateFunctionQuickFixTest.kt 作成
- [ ] plugin.xml に登録
- [ ] コミット: `✨ Add quick fix to generate function from usage`

## #94 .resi シグネチャ同期

- [ ] RescriptSignatureSyncInspection.kt 実装
- [ ] RescriptSignatureSyncInspectionTest.kt 作成
- [ ] plugin.xml に登録
- [ ] コミット: `✨ Add inspection for .res/.resi signature sync`

## #95 ケースの変数分割

- [ ] RescriptCaseSplitIntention.kt 実装
- [ ] RescriptCaseSplitIntentionTest.kt 作成
- [ ] plugin.xml に登録
- [ ] コミット: `✨ Add case split intention for pattern variable expansion`

## #98 位置引数→ラベル付き引数

- [ ] RescriptConvertToLabeledArgsIntention.kt 実装
- [ ] RescriptConvertToLabeledArgsIntentionTest.kt 作成
- [ ] plugin.xml に登録
- [ ] コミット: `✨ Add intention to convert positional to labeled arguments`

## #100 不要な括弧の削除

- [ ] RescriptRemoveParenthesesIntention.kt 実装
- [ ] RescriptRemoveParenthesesIntentionTest.kt 作成
- [ ] plugin.xml に登録
- [ ] コミット: `✨ Add intention to remove unnecessary parentheses`

## #101 不要な修飾子の削除

- [ ] RescriptRemoveQualifierIntention.kt 実装
- [ ] RescriptRemoveQualifierIntentionTest.kt 作成
- [ ] plugin.xml に登録
- [ ] コミット: `✨ Add intention to remove redundant module qualifiers`

## ドキュメント更新

- [ ] CLAUDE.md のアーキテクチャセクション更新
- [ ] docs/product-requirements.md の実装済み移動
- [ ] コミット: `📝 Update docs for A-priority features`

## マージ

- [ ] ビルド確認 (`./gradlew buildPlugin`)
- [ ] tasklist.md 全タスク完了確認
- [ ] main にマージ
