# Type Narrowing Visualizer — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] requirements / design / tasklist のユーザー承認
- [x] `EnterWorktree` で `narrowing-visualizer` worktree を作成

## Phase 2: パーサー方針確認（PSI 拡張は不要と決定）
- [x] `RescriptParser.kt` の現状確認 — switch 式は素通りされており PSI 表現なし
- [x] 設計判断: PSI 拡張ではなくトークンウォーカー方式を採用（`RescriptLexer` を直接走らせる）
- [x] design.md を方針変更に合わせて更新

## Phase 3: 実装
- [x] `narrowing/RescriptSwitchArmCollector.kt` を実装
- [x] `narrowing/RescriptSwitchArmCollectorTest.kt` を作成（option/result/list/polyvariant/custom variant + ネスト/or-pattern/when ガード/不完全/scrutinee/arrow offset の 12 ケース）
- [x] `narrowing/RescriptHoverTypeResolver.kt` を実装（LSP 結合のためテスト免除）
- [x] `narrowing/RescriptNarrowingPresenter.kt` を実装
- [x] `narrowing/RescriptNarrowingPresenterTest.kt` を作成
- [x] `narrowing/RescriptNarrowingHintProvider.kt` を実装（InlayHintsProvider）
- [x] `narrowing/RescriptNarrowingHintProviderTest.kt` を作成（`buildHints` の純粋ロジックを検証。InlayHintsSink/PresentationFactory 統合は IntelliJ Platform fixture が必要のため免除）
- [x] `settings/RescriptProjectSettings.kt` に `narrowingHintsEnabled` を追加
- [x] `plugin.xml` に `codeInsight.inlayHintsProvider` を登録

## Phase 3: コミット前検証
- [x] `./gradlew ktlintCheck` パス
- [x] `./gradlew clean buildPlugin` パス
- [x] `./gradlew test` パス（新規テスト含む全てグリーン）
- [x] ビルド警告が増加していないことを確認（既存の RescriptLsp4jClient 警告のみ）
- [x] Deprecated API 利用がないことを確認（新規 import を確認）
- [ ] LSP 未起動時にヒントが出ないことを手動確認（`./gradlew runIde`）
- [ ] 1000 行サンプルでの体感遅延がないことを手動確認

## Phase 3: ドキュメント更新
- [ ] `CLAUDE.md` レイヤー 3 に `narrowing/` パッケージを追加
- [ ] `docs/repository-structure.md` パッケージ表に `narrowing/` を追加
- [ ] `docs/functional-design.md` Extension Point マップに `RescriptNarrowingHintProvider` を追加
- [ ] `README.md` Features セクションに「Type Narrowing Visualizer」追加
- [ ] `sphinx-docs/user/features/code-analysis.md` に新セクション追加
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` の `msgstr` を更新（`make update-po && make build-ja`）
- [ ] `docs/lsp-fallback-matrix.md` に本機能の依存度を追加

## Phase 3: コミット
- [ ] Collector のコミット（`✨ Add switch arm token walker for narrowing`）
- [ ] HoverTypeResolver + Presenter のコミット（`✨ Add LSP hover resolver and presenter for narrowing`）
- [ ] HintProvider + 設定 + plugin.xml 登録のコミット（`✨ Add type narrowing inlay hints`）
- [ ] ドキュメント更新コミット（`📝 Document type narrowing visualizer`）
- [ ] tasklist.md 全項目を `[x]` に更新するコミット（マージ前最終）

## Phase 4: マージ前
- [ ] tasklist.md の全タスクが `[x]` になっていることを確認
- [ ] requirements.md の受け入れ条件をすべて満たしていることを確認
- [ ] `AskUserQuestion` でマージ可否を確認

## Phase 5: マージ後
- [ ] `git checkout main && git merge worktree-narrowing-visualizer`
- [ ] `git branch -d worktree-narrowing-visualizer`
- [ ] セッション終了で worktree を自動クリーンアップ

## テスト免除
- `RescriptHoverTypeResolver`: LSP サーバー結合のためテスト免除（`testing.md` の免除カテゴリ「LSP サーバー結合必須」に該当）。中身は `RescriptLspUtils.getHoverType` への薄いラッパーであり、独立してロジックがない。テスト可能なロジックはすべて `RescriptNarrowingHintProvider.buildHints` 経由で `RescriptHoverTypeResolver` のスタブを注入してカバーしている。
- `RescriptNarrowingHintProvider` の `getCollectorFor` / `InlayHintsCollector` 部分: IntelliJ Platform fixture が必要なためテスト免除。コア処理は `buildHints` に切り出してユニットテスト済み。
