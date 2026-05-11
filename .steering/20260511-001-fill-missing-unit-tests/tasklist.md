# タスクリスト

## セクション 1: テスト追加（マージ可能単位）

各テストは個別コミットせず、9 件まとめて 1 コミットでよい（純粋追加・互いに独立で、レビュー粒度として一貫している）。

- [x] T1. `interop/RescriptInteropModelTest.kt` を作成
- [x] T2. `impact/RescriptTypeImpactModelTest.kt` を作成
- [x] T3. `migration/RescriptMigrationModelTest.kt` を作成
- [x] T4. `narrowing/RescriptHoverTypeResolverTest.kt` を作成
- [x] T5. `intention/RescriptConstructorOccurrenceTest.kt` を作成
- [x] T6. `navigation/RescriptTypeAstTest.kt` を作成
- [x] T7. `navigation/RescriptTypeSignatureSearchHitTest.kt` を作成
- [x] T8. `RescriptLanguageTest.kt` を作成
- [x] T9. `lsp/RescriptWorkspaceLayoutTest.kt` を作成
- [x] T10. `./gradlew ktlintCheck` グリーン
- [x] T11. `./gradlew test` グリーン
- [x] T12. 1 コミット (`✅ Add unit tests for 9 pure data/utility classes`) — 957fbdb
- [x] T13. main にマージ・worktree クリーンアップ

## DoD チェック

- [x] tasklist.md のすべてが `[x]`
- [x] requirements.md の受け入れ条件をすべて満たした
- [x] ktlint / test グリーン
- [x] 新規 KDoc 規約: テストクラスにクラス KDoc を付与（メソッドは省略可）

## 非ドキュメント更新タスク

本作業はテスト追加のみで機能変更を伴わないため、CLAUDE.md / README.md / sphinx-docs / product-requirements.md の更新は不要（`.claude/rules/documentation.md` の対象外）。
