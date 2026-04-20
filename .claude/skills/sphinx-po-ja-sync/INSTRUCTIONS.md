# Sphinx `.po` 日本語訳同期

`.claude/rules/documentation.md` の「日本語訳の同時更新」ルールを強制するスキル。`sphinx-docs/**/*.md` (locale/ 配下を除く) の追加・変更時は、対応する `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` を **同一コミット内で** 更新し、`msgstr` を埋める。

## 利用タイミング

- `sphinx-docs/user/features/` または `sphinx-docs/dev/` 配下に新規 `.md` を追加した
- 任意の追跡下 `sphinx-docs/**/*.md` で見出し・段落・サンプルを編集した
- `git status` で `.md` がステージされているのに対応する `.po` の変更がない
- ユーザーから「翻訳を更新して」「日本語 docs を同期」「`make build-ja` が失敗する理由は？」と問われた

## ワークフロー

1. **英語側の変更を確認**
   - `git diff sphinx-docs/**/*.md` (locale/ 配下を除く) で追加・変更された `msgid` 候補を特定する

2. **`.pot` 再生成と `.po` 同期**
   ```bash
   cd sphinx-docs
   make gettext      # 現在の .md から .pot テンプレートを再生成
   make update-po    # .pot を ja の .po にマージ（新規 msgid は空 msgstr で追記）
   ```

3. **触れた `.po` の空 `msgstr` をすべて埋める**
   - ReST/Sphinx のロール (`:ref:`, `:doc:`, バッククォート) は `msgid` とバイト単位で一致させる
   - 先頭・末尾の空白を保持する
   - 識別子・コードフェンス・クラス名・`docs/glossary.md` に登場する英語の技術用語は翻訳しない
   - `msgid` が既訳ページの見出しと 1:1 対応する場合は、一貫性のため既存の日本語訳を再利用する

4. **日本語ビルドを検証**
   ```bash
   cd sphinx-docs && make build-ja
   ```
   終了コード 0 必須。`msgstr` の不整合警告が出た場合は（通常は ReST ロールの記述ミス）コミット前に該当エントリを修正する

5. **両側をまとめてステージング**
   - コミット内の `sphinx-docs/**/*.md` には必ず対応する `sphinx-docs/locale/ja/LC_MESSAGES/**` 配下の `.po` を同伴させる
   - `git add <paths>` で明示指定する。`git add .` は禁止（`.claude/rules/definition-of-done.md` Phase 3）

## コミット前の受け入れチェック

- [ ] `git diff --name-only --cached | grep 'sphinx-docs.*\.md$'` が非空 ⇒ 対応 `.po` もステージされている
- [ ] 触れた `.po` に `msgstr ""` が残っていない (`grep -nE '^msgstr ""$' <file>` に想定外エントリが出ない。ヘッダの初期エントリのみ許容)
- [ ] `sphinx-docs/` で `make build-ja` が成功する

## `.po` の許容 diff 形状

新規翻訳を要さない軽微な変動は以下のみ:

- `POT-Creation-Date:` ヘッダの更新
- ソース参照行のシフト (`#: user/features/foo.md:42` → `:45`)

`msgid` の新規追加・変更には必ず日本語 `msgstr` が必要。

## アンチパターン

- 英語 `.md` の変更をコミットし、`.po` 更新を後続コミットに先送りする
- コード識別子・CLI フラグ・ファイルパス・`docs/glossary.md` の技術用語を翻訳する
- 「英語で十分伝わる」と判断して `msgstr ""` を放置する — `build-ja` は通るが、ユーザー向け日本語ページで英語フォールバックが表示される

## 参考

- ルール: `.claude/rules/documentation.md` →「日本語訳の同時更新」
- 用語集（翻訳禁止）: `docs/glossary.md`
- Makefile ターゲット: `sphinx-docs/Makefile` (`gettext`, `update-po`, `build-ja`, `serve`)
