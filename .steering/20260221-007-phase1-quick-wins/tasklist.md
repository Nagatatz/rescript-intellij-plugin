# Tasklist: Phase 1 — Quick Wins (7 features)

**注:** #79 (MultiLang Commenter) は ReScript/JS のコメント構文が同一のため実装不要。7件を実装。

## 実装

- [x] #71: `RescriptAddIgnoreIntention.kt` — 未使用結果に `->ignore` 追加
- [x] #71: `RescriptAddIgnoreIntentionTest.kt` — テスト作成
- [x] #91: `RescriptAddUnderscorePrefixIntention.kt` — 未使用変数に `_` プレフィックス追加
- [x] #91: `RescriptAddUnderscorePrefixIntentionTest.kt` — テスト作成
- [x] #72: `RescriptRemoveRedundantBracesIntention.kt` — 冗長ブロック削除
- [x] #72: `RescriptRemoveRedundantBracesIntentionTest.kt` — テスト作成
- [x] #90: `RescriptDecoratorCompletionContributor.kt` — デコレータ補完
- [x] #90: `RescriptDecoratorCompletionContributorTest.kt` — テスト作成
- [x] #92: `RescriptDocumentationProvider.kt` 拡張 — 演算子優先順位ホバー
- [x] #92: `RescriptDocumentationProviderTest.kt` 拡張 — テスト追加
- [x] #80: `RescriptInspectionSuppressor.kt` 拡張 — Long Line 抑制
- [x] #80: `RescriptInspectionSuppressorTest.kt` 拡張 — テスト追加
- [x] #73: `RescriptFixIdentifierCaseIntention.kt` — 識別子ケース修正
- [x] #73: `RescriptFixIdentifierCaseIntentionTest.kt` — テスト作成
- [x] plugin.xml に全 extension point を登録

## ドキュメント・コミット

- [x] CLAUDE.md 更新（intention/ に新ファイル追記）
- [x] コミット: `✨ Add Phase 1 quick-win features (7 intentions/completions)`
- [x] main にマージして worktree を削除
