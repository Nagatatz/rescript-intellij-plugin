# JS Interop Risk Map — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] requirements / design / tasklist のユーザー承認
- [x] `EnterWorktree js-interop-risk-map` で worktree 作成

## Phase 2: 既存資産の確認
- [x] `FileTypeIndex.getFiles` の使い方を確認（`RescriptDependencyDiagramProvider` で利用中）
- [x] `%raw` / `external` / `@bs.*` トークンを確認

## Phase 3: 実装（コアロジック）
- [x] `interop/RescriptInteropModel.kt` を実装（InteropKind / RiskLevel / InteropEntry）
- [x] `interop/RescriptInteropClassifier.kt` を実装（行ベース・ヒューリスティック）
- [x] `interop/RescriptInteropClassifierTest.kt` を作成（8 ケース）
- [x] `interop/RescriptInteropScanner.kt` を実装（FileTypeIndex + collectEntriesFromText pure helper）
- [x] `interop/RescriptInteropScannerTest.kt` を作成（pure helper の 5 ケース）

## Phase 3: 実装（IDE 統合）
- [x] `interop/RescriptInteropRiskPanel.kt` を実装（JBList + Refresh + double-click navigation + status breakdown）
- [x] `interop/RescriptInteropRiskToolWindowFactory.kt` を実装
- [x] `interop/RescriptInteropRiskAction.kt` を実装
- [x] `plugin.xml` に ToolWindow と action を登録

## Phase 3: コミット前検証
- [x] `./gradlew ktlintCheck` パス
- [x] `./gradlew clean buildPlugin` パス
- [x] `./gradlew test` パス
- [x] ビルド警告が増加していない（既存 RescriptLsp4jClient 警告のみ）
- [x] Deprecated API なし

## Phase 3: ドキュメント更新
- [x] `CLAUDE.md` レイヤー 3 に `interop/` パッケージを追記
- [x] `docs/repository-structure.md` パッケージ表に `interop/` を追加
- [x] `docs/functional-design.md` Extension Point マップに ToolWindow + Action を追加
- [x] `README.md` Features セクションに「JS interop risk map」追加
- [x] `sphinx-docs/user/features/advanced.md` に新セクション
- [x] 日本語 `.po` 同時更新（`make build-ja` 成功）
- [x] `docs/lsp-fallback-matrix.md` に「JS Interop Risk Map」行を追加

## Phase 3: コミット
- [x] 実装コミット（`✨ Add JS interop risk map tool window` — model + classifier + scanner + UI を一括）
- [ ] ドキュメント更新コミット（`📝 Document JS interop risk map`）
- [ ] tasklist 完了化コミット

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認
- [ ] requirements 受け入れ条件確認
- [x] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [x] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- `RescriptInteropRiskPanel`: Swing UI
- `RescriptInteropRiskToolWindowFactory` / `RescriptInteropRiskAction`: IDE ライフサイクル
- `RescriptInteropScanner.scan`: FileTypeIndex のため fixture 要、`collectEntriesFromText` のみテスト
