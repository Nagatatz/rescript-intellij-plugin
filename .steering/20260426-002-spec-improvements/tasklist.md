# タスクリスト: 仕様ドキュメントの改善

## Phase 1: 計画

- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成

## Phase 2: 実装

### Task 1: テンプレート数 12 → 16 修正

- [ ] `src/main/resources/META-INF/plugin.xml` description 内の `"12 templates"` を `"16 templates"` に修正
- [ ] `docs/functional-design.md` line 473 の `"12 テンプレート選択 UI"` を `"16 テンプレート選択 UI"` に修正
- [ ] コミット: `🐛 Fix template count from 12 to 16 in spec docs`

### Task 2: LSP 最低バージョン要件追加

- [ ] `docs/architecture.md` § 3.外部依存テーブルに `@rescript/language-server` 1.0.0+ を明記
- [ ] `src/main/resources/META-INF/plugin.xml` description 内 Requirements セクションにバージョン要件を追記
- [ ] `sphinx-docs/user/installation.md` Prerequisites セクションに最低バージョンを追記
- [ ] `sphinx-docs/user/version-matrix.md` IDE Compatibility テーブルの Language Server 列を具体化
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/user/installation.po` を更新（`make gettext && make update-po` 後に msgstr 埋める）
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/user/version-matrix.po` を更新
- [ ] `make build-ja` が通ることを確認
- [ ] コミット: `📝 Add @rescript/language-server minimum version requirement`

### Task 3: プラットフォーム互換性戦略を PRD に追加

- [ ] `docs/product-requirements.md` § 5 と § 6 の間に新規 § 6「プラットフォーム互換性戦略」を挿入
- [ ] 既存 § 6〜§ 8 を § 7〜§ 9 に番号繰り下げ
- [ ] 内容: 年次更新ポリシー / `pluginUntilBuild` 運用 / verifier ブロッカー（2026.1 例）/ LTS 範囲
- [ ] コミット: `📝 Add platform compatibility strategy to PRD`

### Task 4: EP マップ欠落補填

- [ ] `docs/functional-design.md` § 3 EP マップに不足 EP を追加（scratch / REPL / PPX / TypeInfo / Worksheet / 依存ダイアグラム / Grazie）
- [ ] コミット: `📝 Backfill missing extension points in functional-design map`

### Task 5: US-11〜US-15 追加

- [ ] `docs/product-requirements.md` § 6 末尾に US-11〜US-15 を追加
  - [ ] US-11: Project Wizard
  - [ ] US-12: Worksheet / REPL
  - [ ] US-13: PPX 展開
  - [ ] US-14: Type Info ToolWindow
  - [ ] US-15: 依存ダイアグラム
- [ ] コミット: `📝 Add US-11..US-15 for new feature areas`

### Task 6: LSP フォールバックマトリクス作成

- [ ] `docs/lsp-fallback-matrix.md` 新規作成
- [ ] 機能ごとの「LSP 要/不要」「非接続時の動作」を記載
- [ ] `docs/architecture.md` NFR-04 セクションから新ドキュメントへリンク追加
- [ ] コミット: `📝 Add LSP fallback behavior matrix`

### Task 7: パフォーマンス検証ドキュメント作成

- [ ] `docs/performance-validation.md` 新規作成
- [ ] NFR-01 目標値の計測手段、計測タイミング、記録方法、ラチェットポリシーを定義
- [ ] `docs/architecture.md` § 4 から新ドキュメントへリンク追加
- [ ] コミット: `📝 Add performance validation methodology doc`

## Phase 3: コミット前検証（各コミット時）

- [ ] `./gradlew ktlintCheck` が成功する（コード変更なしのため通る想定）
- [ ] `./gradlew verifyPluginStructure` が成功する
- [ ] 該当する場合、`sphinx-docs && make build-ja` が成功する
- [ ] git status で意図しないファイル変更がないか確認
- [ ] 個別ファイル指定で staging（`git add -A` 禁止）

## Phase 4: マージ前

- [ ] 全タスクが `[x]` になっている
- [ ] AskUserQuestion でマージ可否確認
- [ ] tasklist.md 自身を `[x]` に更新する最終コミット
- [ ] `docs/` のみのため main 直コミット可、ブランチ不要

## Phase 5: マージ後

- [ ] 該当なし（main 直コミットのため）

## メモ

- このステアリングは `docs/` のみの変更のため worktree 不要、main 直コミット可
- 各コミットは独立して revert 可能な粒度
- `sphinx-docs/` を更新する Task 2 のみ `.po` の同時更新が必要
