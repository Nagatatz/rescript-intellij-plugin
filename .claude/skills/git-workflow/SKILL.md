---
description: Git操作を自動化する（ブランチ作成・絵文字付きコミット・PR作成・状態確認）
allowed-tools: Read, Glob, Grep, Bash
disable-model-invocation: true
---

CLAUDE.md の Git コミット規約に準拠したブランチ作成、コミット、PR 作成を支援する。

## 使い方
- `/git-workflow branch <名前>` — ブランチ作成
- `/git-workflow commit` — 絵文字付きコミット
- `/git-workflow pr` — PR 作成
- `/git-workflow status` — 状態確認（デフォルト）

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
