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
- [ ] `narrowing/RescriptSwitchArmCollector.kt` を実装
- [ ] `narrowing/RescriptSwitchArmCollectorTest.kt` を作成（5 種類のパターン: option, result, list, polyvariant, custom variant）
- [ ] `narrowing/RescriptHoverTypeResolver.kt` を実装（LSP モック対応）
- [ ] `narrowing/RescriptHoverTypeResolverTest.kt` を作成
- [ ] `narrowing/RescriptNarrowingPresenter.kt` を実装
- [ ] `narrowing/RescriptNarrowingPresenterTest.kt` を作成
- [ ] `narrowing/RescriptNarrowingHintProvider.kt` を実装（InlayHintsProvider）
- [ ] `narrowing/RescriptNarrowingHintProviderTest.kt` を作成（`testInlays` で表示位置・内容検証）
- [ ] `settings/RescriptProjectSettings.kt` に `narrowingHintsEnabled` を追加
- [ ] `settings/RescriptConfigurable.kt` にチェックボックスを追加（UI のためテスト免除）
- [ ] `plugin.xml` に `codeInsight.declarativeInlayProvider` を登録

## Phase 3: コミット前検証
- [ ] `./gradlew ktlintCheck` パス
- [ ] `./gradlew clean buildPlugin` パス
- [ ] `./gradlew test` パス（新規テスト含む全てグリーン）
- [ ] ビルド警告が増加していないことを確認
- [ ] Deprecated API 利用がないことを確認（新規 import を確認）
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
- `RescriptConfigurable` のチェックボックス追加分: Swing UI のためテスト免除（`testing.md` の免除カテゴリ「Swing UI コンポーネント」に該当）
