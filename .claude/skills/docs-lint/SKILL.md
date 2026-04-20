---
description: Lint staged documentation changes for broken sphinx `.md`↔`.po` sync and missing 4-target Features mirrors. Trigger on "lint docs", "check docs", before doc commits, or when the user asks "are my docs in sync?". Complements `sphinx-po-ja-sync` (write side) by providing the check side.
model: sonnet
allowed-tools: Read, Glob, Grep, Bash
---

# Docs Lint

staged ドキュメント差分に対して、`documentation.md` の 4-target sync matrix と sphinx `.md`↔`.po` 同期を機械的にチェックする。feature→doc マッチングは heuristic のため、advisory lint として扱う（blocking gate ではない）。

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
