# Tasklist — Module Dependency Diagram Visual モード

各セクションは「実装 + テスト + 該当ドキュメント = マージ可能 1 単位」の粒度で並べてある。緑になったセクションから順に main に反映できる構成。

## セクション A: Visual GraphView 実装とテスト

- [x] `RescriptDependencyDiagramGraphView.kt` を新規作成（pure `computeLayout` + Java2D `paintComponent`）
- [x] `RescriptDependencyDiagramGraphViewTest.kt` を新規作成（empty / single / linear / branching / cycle / self-loop / determinism のケース）
- [x] `./gradlew test --tests "com.rescript.plugin.diagram.RescriptDependencyDiagramGraphViewTest"` が緑
- [x] コミット: `✨ Add visual mode graph view for module dependency diagram`

## セクション B: Panel への Visual/Source トグル組み込み

- [x] `RescriptDependencyDiagramPanel.kt` を `CardLayout` 構成に変更し `VisualModeAction` / `SourceModeAction` を追加
- [x] `refresh()` で graphView と textArea の両方を更新
- [x] 既存テストが通る or 必要に応じて更新（既存 `RescriptDependencyDiagramPanelTest` の動作確認）
- [x] `./gradlew ktlintCheck buildPlugin test` が緑
- [x] コミット: `✨ Wire Visual/Source toggle into module dependency diagram tool window`

## セクション C: ドキュメント更新（EN + JA 同時）

- [x] `CLAUDE.md` レイヤー 3 — `diagram/` 段落に Visual / Source トグルの言及を追記
- [x] `README.md` Features の Module Dependency Diagram 行に「Visual / Source toggle (Java2D)」追加
- [x] `docs/repository-structure.md` の `diagram/` 行に `RescriptDependencyDiagramGraphView` を追加
- [x] `sphinx-docs/user/features/advanced.md` の該当セクションに Visual モード説明を追記
- [x] `cd sphinx-docs && make gettext && make update-po && make build-ja` を実行し、新規 / 変更 `msgid` の日本語 `msgstr` を埋める
- [x] コミット: `📝 Document Visual mode for module dependency diagram`

## セクション D: 仕上げとマージ

- [ ] `./gradlew ktlintCheck buildPlugin test koverHtmlReport verifyPluginStructure` が緑
- [ ] DoD Phase 3 のチェック項目を確認（KDoc、deprecated API、セキュリティ）
- [ ] 本ファイルのすべてのチェックボックスを `[x]` に更新してコミット
- [ ] `AskUserQuestion` でマージ可否を確認
- [ ] 承認後: `git checkout main && git merge worktree-20260513-010-module-diagram-visual && git branch -d worktree-20260513-010-module-diagram-visual`
- [ ] セッション終了（worktree 自動クリーンアップ）

## 依存関係

- セクション A → B → C → D の順で進める（B は A の `graphView` API、C は B の動作確認結果に依存）
- A だけ先に push して B 以降を後続セッションで実装することも可能（A 単独でビルドは通るが Visual モードは Panel から呼び出されないため動作はしない）

## テスト省略の理由

- `RescriptDependencyDiagramPanel` の UI 操作（CardLayout の切替）部分はSwing UI に該当するため、`testing.md` の免除カテゴリ「Swing UI コンポーネント」に該当。トグル状態の単体テストは追加しない。pure 関数の `computeLayout` のみテスト対象とする。
