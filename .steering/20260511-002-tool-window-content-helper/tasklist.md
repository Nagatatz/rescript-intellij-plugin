# タスクリスト

## セクション 1: helper 導入 + 6 factory 書き換え（マージ可能単位）

helper と 6 ファクトリは互いに依存するため、1 コミットでまとめて提出する。

- [ ] T1. `util/RescriptToolWindowContent.kt` を作成 (KDoc 付き object)
- [ ] T2. `impact/RescriptTypeImpactToolWindowFactory` を helper 経由に書き換え
- [ ] T3. `flow/RescriptVariantFlowToolWindowFactory` を helper 経由に書き換え
- [ ] T4. `interop/RescriptInteropRiskToolWindowFactory` を helper 経由に書き換え
- [ ] T5. `migration/RescriptMigrationToolWindowFactory` を helper 経由に書き換え
- [ ] T6. `diagram/RescriptDependencyDiagramToolWindowFactory` を helper 経由に書き換え
- [ ] T7. `coverage/RescriptTypeCoverageToolWindowFactory` を helper 経由に書き換え
- [ ] T8. `./gradlew ktlintCheck` グリーン
- [ ] T9. `./gradlew clean buildPlugin` グリーン
- [ ] T10. `./gradlew test` グリーン
- [ ] T11. 1 コミット (`♻️ Centralise single-content tool window installation`)
- [ ] T12. main にマージ + worktree クリーンアップ

## DoD チェック

- [ ] tasklist.md のすべてが `[x]`
- [ ] requirements.md の受け入れ条件をすべて満たした
- [ ] ktlint / buildPlugin / test グリーン
- [ ] 新規 KDoc 規約: `RescriptToolWindowContent` にクラス KDoc を付与
- [ ] Pattern B factory（5 個）は触っていない

## テスト省略の理由（DoD-owned)

`RescriptToolWindowContent` 単体テスト免除:
- `ContentFactory.getInstance()` は IntelliJ Application を要求するため、light fixture では駆動できない（testing.md の「IDE ライフサイクル依存」相当）
- 既存の `Rescript*ToolWindowFactory` 群もすべて同じ理由で `*Test.kt` を持たない。本作業はその慣習を踏襲する

## 非ドキュメント更新タスク

helper は internal な実装詳細であり、ユーザー向け機能変更を伴わないため、CLAUDE.md / README.md / sphinx-docs / product-requirements.md の更新は不要。`docs/repository-structure.md` の `util/` パッケージ表に helper を追記するかは optional とし、最終コミット時に判断する。
