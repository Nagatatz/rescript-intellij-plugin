# タスクリスト: A 優先度機能実装

## 準備

- [x] feature/a-priority-features ブランチ作成

## 共通ユーティリティ

- [x] RescriptLspUtils.kt 作成（LSP hover 取得、シグネチャパース）
- [x] RescriptLspUtilsTest.kt 作成
- [x] コミット: `✨ Add LSP utility helpers for hover and signature parsing`

## #46 Search Everywhere

- [x] RescriptSearchEverywhereContributor.kt 実装
- [x] RescriptSearchEverywhereContributorTest.kt 作成
- [x] plugin.xml に登録
- [x] コミット: `✨ Add Search Everywhere contributor for ReScript symbols`

## #49 Unresolved Reference Quick Fix

- [x] RescriptAddOpenQuickFix.kt 実装
- [x] RescriptQualifyReferenceQuickFix.kt 実装
- [x] テスト作成
- [x] plugin.xml に登録
- [x] コミット: `✨ Add unresolved reference quick fix with open/qualify suggestions`

## #50 Completion Weigher

- [x] RescriptCompletionWeigher.kt 実装
- [x] RescriptCompletionWeigherTest.kt 作成
- [x] plugin.xml に登録
- [x] コミット: `✨ Add completion weigher for context-based candidate ordering`

## #74 パイプチェーン中間型ヒント

- [x] RescriptPipeChainTypeHintsProvider.kt 実装
- [x] RescriptPipeChainTypeHintsProviderTest.kt 作成
- [x] plugin.xml に登録
- [x] コミット: `✨ Add pipe chain intermediate type inlay hints`

## #75 ラベル付き引数の一括挿入

- [x] RescriptInsertLabeledArgsIntention.kt 実装
- [x] RescriptInsertLabeledArgsIntentionTest.kt 作成
- [x] plugin.xml に登録
- [x] コミット: `✨ Add intention to insert labeled arguments from function signature`

## #77 Make 関数生成

- [x] RescriptTypeDeclarationParser.kt 拡張（レコードフィールドパース）
- [x] RescriptGenerateMakeAction.kt 実装
- [x] テスト作成
- [x] コミット: `✨ Add generate make function for record types`

## #78 Switch ケース統合

- [x] RescriptMergeSwitchCasesIntention.kt 実装
- [x] RescriptMergeSwitchCasesIntentionTest.kt 作成
- [x] plugin.xml に登録
- [x] コミット: `✨ Add intention to merge switch cases with identical bodies`

## #89 使用箇所からの関数生成

- [x] RescriptGenerateFunctionQuickFix.kt 実装
- [x] RescriptGenerateFunctionQuickFixTest.kt 作成
- [x] plugin.xml に登録
- [x] コミット: `✨ Add quick fix to generate function from usage`

## #94 .resi シグネチャ同期

- [x] RescriptSignatureSyncInspection.kt 実装
- [x] RescriptSignatureSyncInspectionTest.kt 作成
- [x] plugin.xml に登録
- [x] コミット: `✨ Add inspection for .res/.resi signature sync`

## #95 ケースの変数分割

- [x] RescriptCaseSplitIntention.kt 実装
- [x] RescriptCaseSplitIntentionTest.kt 作成
- [x] plugin.xml に登録
- [x] コミット: `✨ Add case split intention for pattern variable expansion`

## #98 位置引数→ラベル付き引数

- [x] RescriptConvertToLabeledArgsIntention.kt 実装
- [x] RescriptConvertToLabeledArgsIntentionTest.kt 作成
- [x] plugin.xml に登録
- [x] コミット: `✨ Add intention to convert positional to labeled arguments`

## #100 不要な括弧の削除

- [x] RescriptRemoveParenthesesIntention.kt 実装
- [x] RescriptRemoveParenthesesIntentionTest.kt 作成
- [x] plugin.xml に登録
- [x] コミット: `✨ Add intention to remove unnecessary parentheses`

## #101 不要な修飾子の削除

- [x] RescriptRemoveQualifierIntention.kt 実装
- [x] RescriptRemoveQualifierIntentionTest.kt 作成
- [x] plugin.xml に登録
- [x] コミット: `✨ Add intention to remove redundant module qualifiers`

## ドキュメント更新

- [x] CLAUDE.md のアーキテクチャセクション更新
- [x] docs/product-requirements.md の実装済み移動
- [x] コミット: `📝 Update docs for A-priority features`

## マージ

- [x] ビルド確認 (`./gradlew buildPlugin`)
- [x] tasklist.md 全タスク完了確認
- [x] main にマージ
