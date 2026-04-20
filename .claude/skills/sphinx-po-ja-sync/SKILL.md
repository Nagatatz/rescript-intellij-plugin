---
description: Keep sphinx-docs English `.md` and Japanese `.po` translations in sync within a single commit. Trigger when creating or editing any file under `sphinx-docs/` (excluding `locale/`), or when the user asks to regenerate translations, fill missing `msgstr`, or verify `make build-ja`.
model: sonnet
allowed-tools: Read, Edit, Write, Glob, Grep, Bash
---

# Sphinx `.po` Japanese Translation Sync

`sphinx-docs/**/*.md`（`locale/` 配下を除く）を追加・編集した場合、同一コミットで対応する `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` の `msgstr` を埋める。`.claude/rules/documentation.md` の「日本語訳の同時更新」ルールを実装する。

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
