# ドキュメント改善 タスクリスト

## 項目 1: product-requirements.md 整理
- [ ] `docs/product-requirements.md` の「実装済み機能」テーブル (L66-232) を削除
- [ ] 「将来機能（ロードマップ）」セクションをシンプル化
- [ ] `.claude/rules/documentation.md` の更新対象表から該当行を削除
- [ ] `.claude/rules/documentation.md` のコミット前検証項目4を削除
- [ ] コミット: `📝 Remove implemented features table from product-requirements.md`

## 項目 2: CLAUDE.md 機能列挙の要約化
- [ ] レイヤー 3 の 109 行を機能カテゴリ別の短い段落に圧縮
- [ ] `docs/functional-design.md` と `README.md` にリンクを追加
- [ ] コミット: `📝 Summarize CLAUDE.md feature list under layer 3`

## 項目 3: sphinx-docs .po 翻訳 (98 件)
- [ ] `user/features/code-analysis.po` (21 件) 翻訳
- [ ] `user/features/code-completion.po` (20 件) 翻訳
- [ ] `user/features/advanced.po` (20 件) 翻訳
- [ ] `user/features/testing.po` (14 件) 翻訳
- [ ] `user/features/syntax-highlighting.po` (8 件) 翻訳
- [ ] `user/features/run-build.po` (8 件) 翻訳
- [ ] `user/features/index.po` (3 件) 翻訳
- [ ] `user/configuration.po` (2 件) 翻訳
- [ ] `user/keyboard-shortcuts.po` (1 件) 翻訳
- [ ] `dev/architecture.po` (1 件) 翻訳
- [ ] `cd sphinx-docs && make build-ja` 成功を確認
- [ ] Python スクリプトで未翻訳 msgstr が 0 件であることを確認
- [ ] コミット: `📝 Translate remaining sphinx-docs .po entries to Japanese` (大きい場合は 2-3 分割)

## 項目 4: README.md 読者分離
- [ ] Architecture/Development セクションを要約
- [ ] CLAUDE.md / docs/architecture.md への誘導リンクを追加
- [ ] コミット: `📝 Separate user/developer content in README.md`

## 項目 5: .claude/rules/README.md 新規作成
- [ ] `.claude/rules/README.md` を目的別グルーピングで作成
- [ ] コミット: `📝 Add .claude/rules/README.md index`

## 項目 6: Project Wizard テンプレート情報集約
- [ ] `docs/templates.md` を新規作成（15 テンプレートのテーブル）
- [ ] `CLAUDE.md` の該当段落を要約 + リンクに変更
- [ ] `README.md` の該当段落を要約 + リンクに変更
- [ ] `docs/product-requirements.md` の該当行を要約 + リンクに変更（項目 1 と統合可能）
- [ ] コミット: `📝 Consolidate project wizard templates to docs/templates.md`

## 項目 7: glossary.md ソート
- [ ] `docs/glossary.md` の各セクション内をアルファベット順にソート
- [ ] コミット: `📝 Sort glossary.md entries within each section`

## 項目 8: troubleshooting.md 拡充
- [ ] `sphinx-docs/user/troubleshooting.md` に LSP 診断手順を追記
- [ ] キャッシュ関連の問題セクション追記
- [ ] ログ確認方法セクション追記
- [ ] `cd sphinx-docs && make gettext && make update-po` で .po を再生成
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/user/troubleshooting.po` の新規エントリを翻訳
- [ ] `make build-ja` 成功を確認
- [ ] コミット: `📝 Expand troubleshooting.md with diagnostic procedures`

## 項目 9: architecture.md に pluginUntilBuild 背景追記
- [ ] `docs/architecture.md` に「pluginUntilBuild を設定しない理由」セクションを追加
- [ ] コミット: `📝 Document pluginUntilBuild rationale in architecture.md`

## 項目 10: docs/versions.md 新規作成
- [ ] `docs/versions.md` を新規作成（バージョン情報・互換性マトリックス要約）
- [ ] `README.md` のバージョンバッジ部分にリンク追加
- [ ] `docs/product-requirements.md` の「v0.1.7 公開済み」箇所を versions.md リンクに変更
- [ ] `sphinx-docs/user/version-matrix.md` から相互リンク追加
- [ ] コミット: `📝 Add docs/versions.md as version source of truth`

## コミット前検証（definition-of-done.md Phase 3 準拠）

- [ ] `./gradlew ktlintCheck` 成功
- [ ] `./gradlew buildPlugin` 成功（ドキュメント変更のため影響なしを確認）
- [ ] `cd sphinx-docs && make build-ja` 成功
- [ ] 未翻訳 msgstr が 0 件であることを確認
- [ ] 主要ドキュメントの相互リンクが生きていることを確認
- [ ] ktlintCheck・testing は免除（ドキュメントのみの変更）

## 最終タスク

- [ ] tasklist.md の全タスクを `[x]` に更新してコミット（最終コミット）
- [ ] ユーザーにマージ可否を確認
- [ ] 承認後 `main` へマージ（本作業は main 上で実施中のため worktree マージ不要）

## 備考

- テスト免除: `definition-of-done.md` の「ドキュメントのみの変更」例外に該当
- Extension Point 登録: 該当なし
- セキュリティレビュー: 該当なし
- 本作業は `main` ブランチ上で直接実施（steering-workflow.md の「ドキュメントのみの変更」例外に該当）。worktree は使用しない
