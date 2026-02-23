---
description: 作業指示毎の作業計画、タスクリストをドキュメントに記録するためのスキル。ユーザーからの指示をトリガーとした作業計画時、実装時、検証時に読み込む。
allowed-tools: Read, Glob, Grep, Write, Edit, Bash
disable-model-invocation: true
---

ステアリングファイル(`.steering/`)に基づいた実装を支援し、tasklist.mdの進捗管理を確実に行う。

## 使い方
- `/steering plan [機能名]` — ステアリングファイル作成
- `/steering implement [パス]` — tasklist.md に従って実装
- `/steering review [パス]` — 振り返り記録

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
