# JS Interop Risk Map — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [ ] requirements / design / tasklist のユーザー承認
- [ ] `EnterWorktree js-interop-risk-map` で worktree 作成

## Phase 2: 既存資産の確認
- [ ] `FileTypeIndex.getFiles` の使い方を確認
- [ ] 既存の `%raw` / `external` / `@bs.` 検出パターン（`RescriptInspectionSuppressor` など）を参考にする

## Phase 3: 実装（コアロジック）
- [ ] `interop/RescriptInteropModel.kt` を実装（InteropKind / RiskLevel / InteropEntry）
- [ ] `interop/RescriptInteropClassifier.kt` を実装
- [ ] `interop/RescriptInteropClassifierTest.kt` を作成（5+ ケース）
- [ ] `interop/RescriptInteropScanner.kt` を実装（FileTypeIndex + candidates 抽出）
- [ ] `interop/RescriptInteropScannerTest.kt` を作成（candidates 抽出の pure helper のみ）

## Phase 3: 実装（IDE 統合）
- [ ] `interop/RescriptInteropRiskPanel.kt` を実装（JBList + Refresh + double-click navigation）
- [ ] `interop/RescriptInteropRiskToolWindowFactory.kt` を実装
- [ ] `interop/RescriptInteropRiskAction.kt` を実装
- [ ] `plugin.xml` に ToolWindow と action を登録

## Phase 3: コミット前検証
- [ ] `./gradlew ktlintCheck` パス
- [ ] `./gradlew clean buildPlugin` パス
- [ ] `./gradlew test` パス

## Phase 3: ドキュメント更新
- [ ] `CLAUDE.md` レイヤー 3 に `interop/` パッケージを追記
- [ ] `docs/repository-structure.md` パッケージ表に `interop/` を追加
- [ ] `docs/functional-design.md` Extension Point マップに ToolWindow + Action を追加
- [ ] `README.md` Features セクションに「JS interop risk map」追加
- [ ] `sphinx-docs/user/features/advanced.md` に新セクション
- [ ] 日本語 `.po` 同時更新（`make build-ja` パス確認）
- [ ] `docs/lsp-fallback-matrix.md` に行を追加

## Phase 3: コミット
- [ ] Model + Classifier + Scanner コミット（`✨ Add JS interop scanner and classifier`）
- [ ] ToolWindow/Panel/Action コミット（`✨ Add JS interop risk map tool window`）
- [ ] ドキュメント更新コミット（`📝 Document JS interop risk map`）
- [ ] tasklist 完了化コミット

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認
- [ ] requirements 受け入れ条件確認
- [ ] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [ ] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- `RescriptInteropRiskPanel`: Swing UI
- `RescriptInteropRiskToolWindowFactory` / `Action`: IDE ライフサイクル
- `RescriptInteropScanner.scan`: FileTypeIndex のため fixture 要、`collectCandidatesFromText` のみテスト
