# Fixture-Based Integration Tests and Performance Benchmarks — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] requirements / design / tasklist のユーザー承認
- [x] `EnterWorktree fixture-tests-and-benchmarks` で worktree 作成

## Phase 2: 既存資産の確認
- [x] `IntelliJPlatformExtension` の `myFixture` 注入方法を確認（既存 `RescriptFoldingIntegrationTest` に倣う）
- [x] `RescriptParsingTestExtension` ではなく `IntelliJPlatformExtension` を採用（`Project` インスタンスが必要なため）
- [x] `RescriptNotebookFileEditor.panel` のアクセス修飾子を `internal` に変更

## Phase 3: 実装（fixture-based integration tests）
- [x] `impact/RescriptTypeTargetResolverIntegrationTest.kt` を作成（6 ケース、5 種類の型 + caret 外）
- [x] `notebook/RescriptNotebookFileEditorIntegrationTest.kt` を作成（3 ケース、empty + round-trip + invalid JSON fallback）
- [x] `interop/RescriptInteropScannerIntegrationTest.kt` を作成（2 ケース、smoke。populated は LightProject 制約で Phase 2）
- [x] `migration/RescriptMigrationFinderIntegrationTest.kt` を作成（2 ケース、smoke。同上）
- [x] `narrowing/RescriptNarrowingHintProviderIntegrationTest.kt` を作成（3 ケース、設定 ON/OFF + LSP 未起動）

## Phase 3: 実装（performance smoke benchmarks）
- [x] `perf/RescriptSwitchArmCollectorPerfTest.kt` を作成（200 アーム × 1000 行 < 200ms）
- [x] `perf/RescriptVariantFlowModelPerfTest.kt` を作成（5000 行ネスト < 1s）
- [x] `perf/RescriptInteropScannerPerfTest.kt` を作成（100KB ファイル < 500ms）
- [x] `perf/RescriptInteropClassifierPerfTest.kt` を作成（10000 行スイープ < 500ms）

## Phase 3: コミット前検証
- [x] `./gradlew ktlintCheck` パス
- [x] `./gradlew clean buildPlugin` パス
- [x] `./gradlew test` パス
- [ ] ローカルで 3 回連続実行してフレーキー化していないことを確認 — マージ後に検証（CI で運用しながら様子見）
- [x] Deprecated API なし

## Phase 3: ドキュメント更新
- [x] `docs/repository-structure.md` のテスト構成セクションに `perf/` を追記
- [x] 6 機能の `.steering/.../requirements.md` の「手動検証」項目を該当する integration test / benchmark 名に置き換え（残った真の手動項目は Phase 2 リファレンスとして残す）

## Phase 3: コミット
- [x] アクセス修飾子変更コミット（`♻️ Open notebook panel field for tests`）
- [x] integration tests コミット（`✅ Add fixture-based integration tests for the recent six features`）
- [x] performance benchmarks コミット（`✅ Add smoke benchmarks for collector / model / scanner / classifier`）
- [ ] ドキュメント更新コミット（`📝 Wire integration tests into deferred manual checks`）
- [ ] tasklist 完了化コミット（マージ前最終）

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認
- [ ] requirements 受け入れ条件確認
- [x] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [x] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- なし — このバッチは新規プロダクションコード追加なし、テストのみの追加なので免除カテゴリは適用されない
