---
description: Red Team / Blue Team 双子エージェントによるセキュリティレビューを実行する。攻撃者視点と防御者視点の対立する分析を統合し、セキュリティギャップを明確化する。
allowed-tools: Read, Glob, Grep, Bash, Task
disable-model-invocation: true
---

Red Team（攻撃者視点）と Blue Team（防御者視点）の2つのエージェントを並列実行し、セキュリティレビューの統合レポートを生成する。

## 使い方
- `/security-review` — 未コミットの変更をレビュー
- `/security-review <コミット範囲>` — 指定範囲をレビュー
- `/security-review <ファイルパス>` — 特定ファイルをレビュー

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
