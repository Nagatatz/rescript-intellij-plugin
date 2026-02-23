---
description: Qodana スキャン結果を取得・分析し、指摘事項の修正計画を生成する
model: sonnet
allowed-tools: Read, Glob, Grep, Write, Edit, Bash
disable-model-invocation: true
---

GitHub Actions の Qodana スキャン結果を取得し、指摘事項を分析して修正計画を生成する。

## 使い方
- `/fix-qodana` — 最新の Qodana 結果を分析
- `/fix-qodana <run_id or URL>` — 指定の実行結果を分析

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
