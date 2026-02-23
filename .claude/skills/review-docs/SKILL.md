---
description: ドキュメントの品質をレビューし、改善提案を行う（完全性・明確性・一貫性・実装可能性・測定可能性の5観点で評価）
model: sonnet
allowed-tools: Read, Glob, Grep, WebFetch, WebSearch
---

プロジェクトドキュメントの品質を5つの評価基準でレビューし、構造化されたレポートを出力する。

## 使い方
- `/review-docs` — docs/ 配下の全 .md をレビュー
- `/review-docs <ファイルパス>` — 指定ファイルのみ

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
