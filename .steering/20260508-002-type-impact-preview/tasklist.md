# Type Impact Preview — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] requirements / design / tasklist のユーザー承認
- [x] `EnterWorktree type-impact-preview` で worktree 作成

## Phase 2: 既存資産の確認
- [x] `RescriptNameIndex` の使い方を確認（symbol contributor で利用中、本機能では参照検索の用途には合わないため `PsiSearchHelper` の word index へ切替）
- [x] LSP references helper 不要 — Phase 1 では LSP fallback を実装せず、`PsiSearchHelper` の word-index ベース検索のみで完結
- [x] `RescriptDeclarationPsiElement` の TYPE_DECLARATION の構造を確認（`elementType == TYPE_DECLARATION` で判別、`RescriptPsiUtils.extractName` で名前抽出）

## Phase 3: 実装（コアロジック）
- [x] `impact/RescriptTypeImpactModel.kt` を実装（`TypeTarget`, `ReferenceEntry`, `TypeRefKind` データクラス）
- [x] `impact/RescriptTypeTargetResolver.kt` を実装（PSI から型宣言を解決、モジュールパス再構築）
- [x] `impact/RescriptReferenceClassifier.kt` を実装（トークンベースで TypeRefKind を判定）
- [x] `impact/RescriptReferenceClassifierTest.kt` を作成（8 ケース）
- [x] `impact/RescriptTypeReferenceFinder.kt` を実装（PsiSearchHelper word-index、200 件制限）
- [x] `impact/RescriptTypeReferenceFinderTest.kt` を作成（lineAndPreview の 5 ケース）

## Phase 3: 実装（IDE 統合）
- [x] `impact/RescriptTypeImpactPanel.kt` を実装（JBList + CaretListener + 200ms debounce + double-click navigation）
- [x] `impact/RescriptTypeImpactToolWindowFactory.kt` を実装
- [x] `impact/RescriptTypeImpactAction.kt` を実装
- [x] `plugin.xml` に ToolWindow と action を登録

## Phase 3: コミット前検証
- [x] `./gradlew ktlintCheck` パス
- [x] `./gradlew clean buildPlugin` パス
- [x] `./gradlew test` パス
- [x] ビルド警告が増加していない（既存の RescriptLsp4jClient 警告のみ）
- [x] Deprecated API なし

## Phase 3: ドキュメント更新
- [x] `CLAUDE.md` レイヤー 3 に `impact/` パッケージを追記
- [x] `docs/repository-structure.md` パッケージ表に `impact/` を追加
- [x] `docs/functional-design.md` Extension Point マップに ToolWindow + Action を追加
- [x] `README.md` Features セクションに「Type impact preview」追加
- [x] `sphinx-docs/user/features/advanced.md` に Type Impact Preview セクション
- [x] 日本語 `.po` 同時更新（`make build-ja` パス確認）
- [x] `docs/lsp-fallback-matrix.md` に「Type Impact Preview」行を追加

## Phase 3: コミット
- [x] 実装コミット（`✨ Add type impact preview tool window` — model + resolver + classifier + finder + UI を一括）
- [ ] ドキュメント更新コミット（`📝 Document type impact preview`）
- [ ] tasklist 完了化コミット（マージ前最終）

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認
- [ ] requirements 受け入れ条件確認
- [x] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [x] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- `RescriptTypeImpactPanel`: Swing UI のためテスト免除
- `RescriptTypeImpactToolWindowFactory`: IDE ライフサイクル依存のためテスト免除
- `RescriptTypeImpactAction`: AnAction 単発呼び出しのみ（テスト免除）
- `RescriptTypeTargetResolver`: PSI fixture（IntelliJ Platform fixture）が必要なためテスト免除。Panel 経由で end-to-end 動作確認
- `RescriptTypeReferenceFinder.findReferences`: PsiSearchHelper のため fixture が必要、コア helper の `lineAndPreview` のみ pure function としてテスト
