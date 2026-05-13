# 20260513-008 タスクリスト

## セクション 1: コア実装 (1 コミット)

- [x] `RescriptVariantFlowGraphView.kt` を編集
  - [x] `armLabel(node)` で `node.children.isNotEmpty()` なら body preview を返さない
  - [x] `computeLayout(...)` を 2 経路に分岐: flat (既存) と tree (新規)
  - [x] `computeFlatLayout(...)`: 既存ロジックを private 関数に切り出し
  - [x] `computeTreeLayout(...)`: 新規ツリーレイアウト本体
  - [x] private data class `ArmSubtree` + `computeArmSubtree(node, fm)` 再帰関数
  - [x] クラス KDoc の「intentionally collapsed」記述を「Visual モードもネストを展開する」に更新
- [x] `RescriptVariantFlowGraphViewTest.kt` にテスト追加
  - [x] `nested arm renders children as separate boxes below parent`
  - [x] `parent arm with children drops body preview from label`
  - [x] `canvas height grows to accommodate nested subtree`
- [x] `./gradlew ktlintCheck` 緑
- [x] `./gradlew clean buildPlugin` 緑
- [x] `./gradlew test --tests "com.rescript.plugin.flow.*"` 緑
- [x] `./gradlew test` 全体緑 (回帰なし)
- [x] コミット: `🐛 Render nested switch arms as a sub-tree in Visual flow view`

## セクション 2: ドキュメント同期 (1 コミット)

- [ ] CLAUDE.md `flow/` 段落を更新 (Visual モードのネスト展開を明記)
- [ ] sphinx-docs/user/features/advanced.md の Switch Flow セクションを更新
- [ ] `cd sphinx-docs && make gettext && make update-po` 実行
- [ ] `locale/ja/LC_MESSAGES/**/*.po` の対応 msgstr を日本語化
- [ ] `cd sphinx-docs && make build-ja` 緑
- [ ] コミット: `📝 Document Visual flow nested rendering`

## セクション 3: マージ前検証 (本セクションは PR 不要、main 直マージ)

- [ ] `git fetch origin && git log --oneline origin/main..HEAD` で push 待ち本数を再確認
- [ ] tasklist の全項目が `[x]` であることを確認
- [ ] AskUserQuestion でマージ可否を確認
- [ ] 承認後、worktree 内で `git checkout main && git merge worktree-fix+switch-flow-recursion`
- [ ] `git branch -d worktree-fix+switch-flow-recursion`
- [ ] セッション終了 (worktree 自動クリーンアップ発動)

## マージ依存

セクション 1 → セクション 2 (実装が緑になってからドキュメントを書く方が安全)
