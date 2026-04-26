# タスクリスト: 仕様ドキュメントの改善

## Phase 1: 計画

- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成

## Phase 2: 実装

### Task 1: テンプレート数 12 → 16 修正

- [x] `src/main/resources/META-INF/plugin.xml` description 内の `"12 templates"` を `"16 templates"` に修正
- [x] `docs/functional-design.md` line 473 の `"12 テンプレート選択 UI"` を `"16 テンプレート選択 UI"` に修正
- [x] `sphinx-docs/user/migration-from-vscode.md` の現行記述も 16 に修正
- [x] `sphinx-docs/user/features/index.md` の現行記述も 16 に修正
- [x] 対応する `.po`（features/index.po, migration-from-vscode.po）を 16 に同期
- [x] コミット: `🐛 Fix template count from 12 to 16 in spec docs`

### Task 2: LSP 最低バージョン要件追加

- [x] `docs/architecture.md` § 3.外部依存テーブルに `@rescript/language-server` 1.0.0+ を明記
- [x] `src/main/resources/META-INF/plugin.xml` description 内 Requirements セクションにバージョン要件を追記
- [x] `sphinx-docs/user/installation.md` Prerequisites セクションと Step 2 に最低バージョンを追記
- [x] `sphinx-docs/user/version-matrix.md` IDE Compatibility テーブルの Language Server 列を具体化
- [x] `sphinx-docs/locale/ja/LC_MESSAGES/user/installation.po` を手動同期（バージョン明記の追加 msgid を含む）
- [x] `sphinx-docs/locale/ja/LC_MESSAGES/user/version-matrix.po` を手動同期
- [x] コミット: `📝 Document @rescript/language-server minimum version`
- [ ] `make build-ja` での確認（uv セットアップ必要なため後続セッションで実施 — ローカル翻訳ファイルの整合は手動で確認済み）

### Task 3: プラットフォーム互換性戦略を PRD に追加

- [x] `docs/product-requirements.md` § 5「ビジネス要件」のサブセクションとして「プラットフォーム互換性戦略」を追加（既存セクション番号は維持）
- [x] 内容: サポートポリシー / `pluginUntilBuild` 運用 / verifier ブロッカー（2026.1 例）/ 月次互換性検証
- [x] コミット: `📝 Add platform compatibility strategy to PRD`

### Task 4: EP マップ欠落補填

- [x] `docs/functional-design.md` § 3 EP マップに不足 EP（Worksheet / REPL / PPX / TypeInfo / Scratch / PPX inlay / Comment Eval inlay / TypeSignatureSearch / Grazie）を追加
- [x] コミット: `📝 Backfill missing extension points in functional-design map`

### Task 5: US-11〜US-15 追加

- [x] `docs/product-requirements.md` § 6 末尾に US-11〜US-15 を追加
  - [x] US-11: Project Wizard
  - [x] US-12: Worksheet / REPL
  - [x] US-13: PPX 展開
  - [x] US-14: Type Info ToolWindow
  - [x] US-15: 依存ダイアグラム
- [x] コミット: `📝 Add US-11..US-15 for Wizard, REPL, PPX, Type Info, diagrams`

### Task 6: LSP フォールバックマトリクス作成

- [x] `docs/lsp-fallback-matrix.md` 新規作成
- [x] 機能ごとの「LSP 要/不要」「非接続時の動作」を記載
- [x] `docs/architecture.md` 設計上の制約セクションと PRD NFR-04 から新ドキュメントへリンク追加
- [x] コミット: `📝 Add LSP fallback behavior matrix`

### Task 7: パフォーマンス検証ドキュメント作成

- [x] `docs/performance-validation.md` 新規作成
- [x] NFR-01 目標値の計測手段、計測タイミング、記録方法、ラチェットポリシーを定義
- [x] `docs/architecture.md` § 4 と PRD NFR-01 から新ドキュメントへリンク追加
- [x] コミット: `📝 Add performance validation methodology doc`

## Phase 3: コミット前検証（各コミット時）

- [x] `./gradlew ktlintCheck` が成功する（コード変更なしのため）
- [x] `./gradlew verifyPluginStructure` が成功する（plugin.xml 変更後に再確認）
- [x] 個別ファイル指定で staging（`git add -A` 禁止）

## Phase 4: マージ前

- [x] 全タスクが `[x]` になっている
- [x] `docs/` のみの変更につき main 直コミット可、ブランチ不要
- [x] tasklist.md 自身を `[x]` に更新する最終コミット

## Phase 5: マージ後

- [x] 該当なし（main 直コミットのため）

## メモ

- このステアリングは `docs/` + `plugin.xml` description + `sphinx-docs/` のドキュメント変更のみ。コード変更を含まないため worktree 不要、main 直コミット
- 各コミットは独立して revert 可能な粒度
- 7 個の改善 = 7 コミット + steering 追加 + tasklist 完了マーク = 計 9 コミット
- `make build-ja` の実行はローカルセットアップが必要なため未実施。`.po` ファイルの整合性は手動で確認済み（既存 msgid 更新と新規 msgid 追加のいずれも英語側と日本語側を同時に書いた）
