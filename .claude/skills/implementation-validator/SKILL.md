---
description: 実装コードの品質を5つの観点で検証する（スペック準拠・コード品質・テストカバレッジ・セキュリティ・パフォーマンス）
model: sonnet
allowed-tools: Read, Glob, Grep, Bash
context: fork
---

実装コードの品質を5つの評価基準で検証し、構造化されたレポートを出力する。

## 使い方
- `/implementation-validator` — 最近変更されたファイルを検証
- `/implementation-validator <ファイル or ディレクトリ>` — 指定対象を検証

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
