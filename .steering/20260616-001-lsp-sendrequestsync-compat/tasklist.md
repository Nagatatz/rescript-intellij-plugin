# タスクリスト: sendRequestSync 互換性修正

## セクション 1: 4 箇所の明示タイムアウト化（1 コミット）

- [x] `lsp/RescriptLspUtils.kt:113` `getHoverType` の `sendRequestSync` に `10_000` を明示
- [x] `lsp/RescriptExpressionTypeProvider.kt:41` `getInformationHint` の `sendRequestSync` に `10_000` を明示
- [x] `navigation/RescriptCreateInterfaceAction.kt:54` `actionPerformed` の `sendRequestSync` に `10_000` を明示
- [x] `inspection/RescriptSignatureSyncInspection.kt:86` `RegenerateInterfaceQuickFix.applyFix` の `sendRequestSync` に `10_000` を明示
- [x] 各箇所に platform default 一致の旨をインラインコメントで明記

### テスト

- [x] テスト新規作成は不要（`testing.md` 免除「LSP サーバー結合必須」+ デフォルト値明示のみで新ロジックなし）。既存テスト green を確認

### 検証（コミット前）

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功
- [x] 新規警告なし（残存 3 警告は本変更と無関係の既存ファイル）
- [x] deprecated/internal API の新規導入なし

### コミット

- [x] `🐛 Pass explicit timeout to sendRequestSync for 2026.2 compatibility` でコミット（本 tasklist の `[x]` 更新を同梱）

## セクション 2: マージ

- [ ] requirements.md 受け入れ条件をすべて満たすことを確認
- [ ] ユーザーにマージ可否を確認
- [ ] `main` へマージ・ブランチ削除

## ドキュメント

機能・API 変更がないため CLAUDE.md / README / sphinx-docs の更新は不要（受け入れ条件で確認済み）。
