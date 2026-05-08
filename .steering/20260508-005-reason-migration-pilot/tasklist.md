# Reason → ReScript Migration Pilot — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [ ] requirements / design / tasklist のユーザー承認
- [ ] `EnterWorktree reason-migration-pilot` で worktree 作成

## Phase 2: 既存資産の確認
- [ ] `FilenameIndex.getAllFilesByExt` の使い方を確認
- [ ] `RescriptProjectSettings.rescriptBinaryPath` の読み出しパターンを確認
- [ ] `RescriptSecurityUtils` のパス検証関数を確認

## Phase 3: 実装（コアロジック）
- [ ] `migration/RescriptMigrationModel.kt` を実装（MigrationCandidate / ConversionStatus / ConversionResult）
- [ ] `migration/RescriptMigrationFinder.kt` を実装（findCandidates + toCandidates pure helper）
- [ ] `migration/RescriptMigrationFinderTest.kt` を作成（toCandidates の 4-5 ケース）
- [ ] `migration/RescriptMigrationConverter.kt` を実装（convert + buildCommand pure helper）
- [ ] `migration/RescriptMigrationConverterTest.kt` を作成（buildCommand の 3-4 ケース）

## Phase 3: 実装（IDE 統合）
- [ ] `migration/RescriptMigrationPanel.kt` を実装（チェックボックス付き JBList + Toolbar + 結果領域）
- [ ] `migration/RescriptMigrationToolWindowFactory.kt` を実装
- [ ] `migration/RescriptMigrationAction.kt` を実装
- [ ] `plugin.xml` に ToolWindow と action を登録

## Phase 3: コミット前検証
- [ ] `./gradlew ktlintCheck` パス
- [ ] `./gradlew clean buildPlugin` パス
- [ ] `./gradlew test` パス

## Phase 3: ドキュメント更新
- [ ] `CLAUDE.md` レイヤー 3 に `migration/` パッケージを追記
- [ ] `docs/repository-structure.md` パッケージ表に `migration/` を追加
- [ ] `docs/functional-design.md` Extension Point マップに ToolWindow + Action を追加
- [ ] `README.md` Features セクションに「Reason → ReScript migration pilot」追加
- [ ] `sphinx-docs/user/features/advanced.md` に新セクション
- [ ] 日本語 `.po` 同時更新（`make build-ja` パス確認）
- [ ] `docs/lsp-fallback-matrix.md` に行を追加

## Phase 3: コミット
- [ ] Model + Finder + Converter コミット（`✨ Add Reason migration finder and converter`）
- [ ] ToolWindow/Panel/Action コミット（`✨ Add Reason migration pilot tool window`）
- [ ] ドキュメント更新コミット（`📝 Document Reason migration pilot`）
- [ ] tasklist 完了化コミット

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認
- [ ] requirements 受け入れ条件確認
- [ ] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [ ] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- `RescriptMigrationPanel`: Swing UI
- `RescriptMigrationToolWindowFactory` / `RescriptMigrationAction`: IDE ライフサイクル
- `RescriptMigrationFinder.findCandidates` / `RescriptMigrationConverter.convert`: FilenameIndex / ProcessBuilder のため fixture 要、pure helper のみテスト
