# Tasklist: Phase 1 — Quick Wins (7 features)

**注:** #79 (MultiLang Commenter) は ReScript/JS のコメント構文が同一のため実装不要。7件を実装。

## 実装

- [ ] #71: `RescriptAddIgnoreIntention.kt` — 未使用結果に `->ignore` 追加
- [ ] #71: `RescriptAddIgnoreIntentionTest.kt` — テスト作成
- [ ] #91: `RescriptAddUnderscorePrefixIntention.kt` — 未使用変数に `_` プレフィックス追加
- [ ] #91: `RescriptAddUnderscorePrefixIntentionTest.kt` — テスト作成
- [ ] #72: `RescriptRemoveRedundantBracesIntention.kt` — 冗長ブロック削除
- [ ] #72: `RescriptRemoveRedundantBracesIntentionTest.kt` — テスト作成
- [ ] #90: `RescriptDecoratorCompletionContributor.kt` — デコレータ補完
- [ ] #90: `RescriptDecoratorCompletionContributorTest.kt` — テスト作成
- [ ] #92: `RescriptDocumentationProvider.kt` 拡張 — 演算子優先順位ホバー
- [ ] #92: `RescriptDocumentationProviderTest.kt` 拡張 — テスト追加
- [ ] #80: `RescriptInspectionSuppressor.kt` 拡張 — Long Line 抑制
- [ ] #80: `RescriptInspectionSuppressorTest.kt` 拡張 — テスト追加
- [ ] #73: `RescriptFixIdentifierCaseIntention.kt` — 識別子ケース修正
- [ ] #73: `RescriptFixIdentifierCaseIntentionTest.kt` — テスト作成
- [ ] plugin.xml に全 extension point を登録

## ドキュメント・コミット

- [ ] CLAUDE.md 更新（intention/ に新ファイル追記）
- [ ] コミット: `✨ Add Phase 1 quick-win features (7 intentions/completions)`
- [ ] main にマージして worktree を削除
