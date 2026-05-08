# Fixture-Based Integration Tests and Performance Benchmarks — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [ ] requirements / design / tasklist のユーザー承認
- [ ] `EnterWorktree fixture-tests-and-benchmarks` で worktree 作成

## Phase 2: 既存資産の確認
- [ ] `IntelliJPlatformExtension` の `myFixture` 注入方法を確認
- [ ] `RescriptParsingTestExtension` の使い分けを確認
- [ ] `RescriptNotebookFileEditor.panel` のアクセス修飾子を internal に変更（テスト用 seam）

## Phase 3: 実装（fixture-based integration tests）
- [ ] `impact/RescriptTypeTargetResolverIntegrationTest.kt` を作成（5+ ケース）
- [ ] `notebook/RescriptNotebookFileEditorIntegrationTest.kt` を作成（round-trip）
- [ ] `interop/RescriptInteropScannerIntegrationTest.kt` を作成（fixture project scan）
- [ ] `migration/RescriptMigrationFinderIntegrationTest.kt` を作成（fixture project enumeration）
- [ ] `narrowing/RescriptNarrowingHintProviderIntegrationTest.kt` を作成（settings ON/OFF）

## Phase 3: 実装（performance smoke benchmarks）
- [ ] `perf/RescriptSwitchArmCollectorPerfTest.kt` を作成（50 switch × 1000 行 < 200ms）
- [ ] `perf/RescriptVariantFlowModelPerfTest.kt` を作成（5000 行ネスト < 1s）
- [ ] `perf/RescriptInteropScannerPerfTest.kt` を作成（100KB ファイル < 500ms）
- [ ] `perf/RescriptInteropClassifierPerfTest.kt` を作成（10000 行スイープ < 500ms）

## Phase 3: コミット前検証
- [ ] `./gradlew ktlintCheck` パス
- [ ] `./gradlew clean buildPlugin` パス
- [ ] `./gradlew test` パス（既存 + 新規テストすべてグリーン）
- [ ] ローカルで 3 回連続実行してフレーキー化していないことを確認
- [ ] Deprecated API なし

## Phase 3: ドキュメント更新
- [ ] `docs/repository-structure.md` のテスト構成セクションに `perf/` を追記
- [ ] 6 機能の `.steering/.../requirements.md` の「手動検証」項目を該当する integration test 名に置き換え

## Phase 3: コミット
- [ ] アクセス修飾子変更コミット（`♻️ Open notebook panel field for tests`）
- [ ] integration tests コミット（`✅ Add fixture-based integration tests for the recent six features`）
- [ ] performance benchmarks コミット（`✅ Add smoke benchmarks for collector / model / scanner / classifier`）
- [ ] ドキュメント更新コミット（`📝 Wire integration tests into deferred manual checks`）
- [ ] tasklist 完了化コミット

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認
- [ ] requirements 受け入れ条件確認
- [ ] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [ ] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- なし — このバッチは新規プロダクションコード追加なし、テストのみの追加なので免除カテゴリは適用されない
