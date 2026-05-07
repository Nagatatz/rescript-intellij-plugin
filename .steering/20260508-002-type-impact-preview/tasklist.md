# Type Impact Preview — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [ ] requirements / design / tasklist のユーザー承認
- [ ] `EnterWorktree type-impact-preview` で worktree 作成

## Phase 2: 既存資産の確認
- [ ] `RescriptNameIndex` の使い方を確認
- [ ] LSP `textDocument/references` 呼び出し helper があるか確認、なければ `RescriptLspUtils` に追加
- [ ] `RescriptDeclarationPsiElement` の TYPE_DECLARATION の構造を確認

## Phase 3: 実装（コアロジック）
- [ ] `impact/RescriptTypeImpactModel.kt` を実装（`TypeTarget`, `ReferenceEntry`, `TypeRefKind` データクラス）
- [ ] `impact/RescriptTypeTargetResolver.kt` を実装（PSI から型宣言を解決）
- [ ] `impact/RescriptTypeTargetResolverTest.kt` を作成（5 種類: alias, record, variant, polyvariant, abstract）
- [ ] `impact/RescriptReferenceClassifier.kt` を実装（PSI コンテキストで TypeRefKind を判定）
- [ ] `impact/RescriptReferenceClassifierTest.kt` を作成（4 種類の kind + unknown）
- [ ] `impact/RescriptTypeReferenceFinder.kt` を実装（Stub Index + LSP fallback、200 件制限）
- [ ] `impact/RescriptTypeReferenceFinderTest.kt` を作成（fixture で Stub Index を動作確認、LSP 結合は免除）

## Phase 3: 実装（IDE 統合）
- [ ] `impact/RescriptTypeImpactPanel.kt` を実装（JBList + CaretListener + 200ms debounce）
- [ ] `impact/RescriptTypeImpactToolWindowFactory.kt` を実装
- [ ] `impact/RescriptTypeImpactAction.kt` を実装
- [ ] `plugin.xml` に ToolWindow と action を登録

## Phase 3: コミット前検証
- [ ] `./gradlew ktlintCheck` パス
- [ ] `./gradlew clean buildPlugin` パス
- [ ] `./gradlew test` パス
- [ ] ビルド警告が増加していない
- [ ] Deprecated API なし

## Phase 3: ドキュメント更新
- [ ] `CLAUDE.md` レイヤー 3 に `impact/` パッケージを追記
- [ ] `docs/repository-structure.md` パッケージ表に `impact/` を追加
- [ ] `docs/functional-design.md` Extension Point マップに ToolWindow + Action を追加
- [ ] `README.md` Features セクションに「Type impact preview」追加
- [ ] `sphinx-docs/user/features/advanced.md` に Type Impact Preview セクション
- [ ] 日本語 `.po` 同時更新（`make build-ja` パス確認）
- [ ] `docs/lsp-fallback-matrix.md` に「Type Impact Preview」行を追加

## Phase 3: コミット
- [ ] Model + TargetResolver + Classifier コミット（`✨ Add type impact preview core resolvers`）
- [ ] ReferenceFinder コミット（`✨ Find type references via stub index with LSP fallback`）
- [ ] ToolWindow/Panel/Action コミット（`✨ Add type impact preview tool window`）
- [ ] ドキュメント更新コミット（`📝 Document type impact preview`）
- [ ] tasklist 完了化コミット

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認
- [ ] requirements 受け入れ条件確認
- [ ] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [ ] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- `RescriptTypeImpactPanel`: Swing UI のためテスト免除
- `RescriptTypeImpactToolWindowFactory`: IDE ライフサイクル依存のためテスト免除
- `RescriptTypeImpactAction`: AnAction 単発呼び出しのみ（テスト免除）
- `RescriptTypeReferenceFinder` の LSP 結合パス: LSP server 結合のためテスト免除（Stub Index パスは fixture でカバー）
