# ドキュメント改善 タスクリスト

## 項目 1: product-requirements.md 整理
- [x] `docs/product-requirements.md` の「実装済み機能」テーブル (L66-232) を削除
- [x] 「将来機能（ロードマップ）」セクションをシンプル化
- [x] `.claude/rules/documentation.md` の更新対象表から該当行を削除
- [x] `.claude/rules/documentation.md` のコミット前検証項目4を削除
- [x] コミット: `📝 Remove implemented features table from product-requirements.md`

## 項目 2: CLAUDE.md 機能列挙の要約化
- [x] レイヤー 3 の 109 行を機能カテゴリ別の短い段落に圧縮
- [x] `docs/functional-design.md` と `README.md` にリンクを追加
- [x] コミット: `📝 Summarize CLAUDE.md feature list under layer 3`

## 項目 3: sphinx-docs .po 翻訳 (98 件)
- [x] `user/features/code-analysis.po` (21 件) 翻訳
- [x] `user/features/code-completion.po` (20 件) 翻訳
- [x] `user/features/advanced.po` (20 件) 翻訳
- [x] `user/features/testing.po` (14 件) 翻訳
- [x] `user/features/syntax-highlighting.po` (8 件) 翻訳
- [x] `user/features/run-build.po` (8 件) 翻訳
- [x] `user/features/index.po` (3 件) 翻訳
- [x] `user/configuration.po` (2 件) 翻訳
- [x] `user/keyboard-shortcuts.po` (1 件) 翻訳
- [x] `dev/architecture.po` (1 件) 翻訳
- [x] `cd sphinx-docs && make build-ja` 成功を確認
- [x] Python スクリプトで未翻訳 msgstr が 0 件であることを確認
- [x] コミット: `📝 Translate remaining sphinx-docs .po entries to Japanese` (大きい場合は 2-3 分割)

## 項目 4: README.md 読者分離
- [x] Architecture/Development セクションを要約
- [x] CLAUDE.md / docs/architecture.md への誘導リンクを追加
- [x] コミット: `📝 Separate user/developer content in README.md`

## 項目 5: .claude/rules/README.md 新規作成
- [x] `.claude/rules/README.md` を目的別グルーピングで作成
- [x] コミット: `📝 Add .claude/rules/README.md index`

## 項目 6: Project Wizard テンプレート情報集約
- [x] `docs/templates.md` を新規作成（15 テンプレートのテーブル）
- [x] `CLAUDE.md` の該当段落を要約 + リンクに変更
- [x] `README.md` の該当段落を要約 + リンクに変更
- [x] `docs/product-requirements.md` の該当行を要約 + リンクに変更
- [x] コミット: `📝 Consolidate project wizard templates to docs/templates.md`

## 項目 7: glossary.md ソート
- [x] `docs/glossary.md` の各セクション内をアルファベット順にソート
- [x] コミット: `📝 Sort glossary.md entries within each section`

## 項目 8: troubleshooting.md 拡充
- [x] `sphinx-docs/user/troubleshooting.md` に LSP 診断手順を追記
- [x] キャッシュ関連の問題セクション追記
- [x] ログ確認方法セクション追記
- [x] `cd sphinx-docs && make gettext && make update-po` で .po を再生成
- [x] `sphinx-docs/locale/ja/LC_MESSAGES/user/troubleshooting.po` の新規エントリを翻訳
- [x] `make build-ja` 成功を確認
- [x] コミット: `📝 Expand troubleshooting.md with diagnostic procedures`

## 項目 9: architecture.md に pluginUntilBuild 背景追記
- [x] `docs/architecture.md` に「pluginUntilBuild を設定しない理由」セクションを追加
- [x] コミット: `📝 Document pluginUntilBuild rationale in architecture.md`

## 項目 10: docs/versions.md 新規作成
- [x] `docs/versions.md` を新規作成（バージョン情報・互換性マトリックス要約）
- [x] `README.md` のバージョンバッジ部分にリンク追加
- [x] `docs/product-requirements.md` の「v0.1.7 公開済み」箇所を versions.md リンクに変更
- [x] `sphinx-docs/user/version-matrix.md` から相互リンク追加
- [x] コミット: `📝 Add docs/versions.md as version source of truth`

## コミット前検証（definition-of-done.md Phase 3 準拠）

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew buildPlugin` 成功（ドキュメント変更のため影響なしを確認）
- [x] `cd sphinx-docs && make build-ja` 成功
- [x] 未翻訳 msgstr が 0 件であることを確認
- [x] 主要ドキュメントの相互リンクが生きていることを確認
- [x] ktlintCheck・testing は免除（ドキュメントのみの変更）

## 最終タスク

- [x] tasklist.md の全タスクを `[x]` に更新してコミット（最終コミット）
- [ ] ユーザーにマージ可否を確認
- [ ] 承認後 `main` へマージ（本作業は worktree 上で実施、マージ後 worktree は自動クリーンアップ）

## 備考

- テスト免除: `definition-of-done.md` の「ドキュメントのみの変更」例外に該当
- Extension Point 登録: 該当なし
- セキュリティレビュー: 該当なし
- 本作業は worktree `documentation-improvements` 上で実施。完了後 `main` にマージしセッション終了でクリーンアップ
