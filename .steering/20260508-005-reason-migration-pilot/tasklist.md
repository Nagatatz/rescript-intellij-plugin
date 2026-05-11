# Reason → ReScript Migration Pilot — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] requirements / design / tasklist のユーザー承認
- [x] `EnterWorktree reason-migration-pilot` で worktree 作成

## Phase 2: 既存資産の確認
- [x] `FilenameIndex.getAllFilesByExt` を採用（プロジェクトに既存利用なし、IntelliJ Platform 標準 API）
- [x] `RescriptProjectSettings.rescriptBinaryPath` の読み出しパターンを確認（既存設定）
- [x] `RescriptSecurityUtils` 経由の検証は将来検討（Phase 1 では `projectScope` で代替）

## Phase 3: 実装（コアロジック）
- [x] `migration/RescriptMigrationModel.kt` を実装（MigrationCandidate / ConversionStatus / ConversionResult）
- [x] `migration/RescriptMigrationFinder.kt` を実装（findCandidates + toCandidates pure helper）
- [x] `migration/RescriptMigrationFinderTest.kt` を作成（5 ケース）
- [x] `migration/RescriptMigrationConverter.kt` を実装（convert + buildCommand pure helper）
- [x] `migration/RescriptMigrationConverterTest.kt` を作成（4 ケース）

## Phase 3: 実装（IDE 統合）
- [x] `migration/RescriptMigrationPanel.kt` を実装（チェックボックス付き JBList + Toolbar + 結果領域）
- [x] `migration/RescriptMigrationToolWindowFactory.kt` を実装
- [x] `migration/RescriptMigrationAction.kt` を実装
- [x] `plugin.xml` に ToolWindow と action を登録

## Phase 3: コミット前検証
- [x] `./gradlew ktlintCheck` パス
- [x] `./gradlew clean buildPlugin` パス
- [x] `./gradlew test` パス
- [x] ビルド警告が増加していない（既存 RescriptLsp4jClient 警告のみ）
- [x] Deprecated API なし

## Phase 3: ドキュメント更新
- [x] `CLAUDE.md` レイヤー 3 に `migration/` パッケージを追記
- [x] `docs/repository-structure.md` パッケージ表に `migration/` を追加
- [x] `docs/functional-design.md` Extension Point マップに ToolWindow + Action を追加
- [x] `README.md` Features セクションに「Reason → ReScript migration pilot」追加
- [x] `sphinx-docs/user/features/advanced.md` に新セクション
- [x] 日本語 `.po` 同時更新（`make build-ja` 成功）
- [x] `docs/lsp-fallback-matrix.md` に「Reason → ReScript Migration Pilot」行を追加

## Phase 3: コミット
- [x] 実装コミット（`✨ Add Reason migration pilot tool window` — model + finder + converter + UI を一括）
- [ ] ドキュメント更新コミット（`📝 Document Reason migration pilot`）
- [ ] tasklist 完了化コミット

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認
- [ ] requirements 受け入れ条件確認
- [x] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [x] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- `RescriptMigrationPanel`: Swing UI
- `RescriptMigrationToolWindowFactory` / `RescriptMigrationAction`: IDE ライフサイクル
- `RescriptMigrationFinder.findCandidates` / `RescriptMigrationConverter.convert`: FilenameIndex / ProcessBuilder のため fixture 要、pure helper のみテスト
