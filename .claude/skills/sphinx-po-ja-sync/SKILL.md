---
description: sphinx-docs の英語 `.md` と日本語 `.po` 翻訳を単一コミット内で同期する。`sphinx-docs/` 配下 (`locale/` を除く) のファイル作成・編集時、または翻訳再生成・未翻訳 `msgstr` 充填・`make build-ja` 検証の依頼時に起動する。
model: sonnet
allowed-tools: Read, Edit, Write, Glob, Grep, Bash
---

# Sphinx `.po` 日本語訳同期

`sphinx-docs/**/*.md`（`locale/` 配下を除く）を追加・編集した場合、同一コミットで対応する `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` の `msgstr` を埋める。`.claude/rules/documentation.md` の「日本語訳の同時更新」ルールを実装する。

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
