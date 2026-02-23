---
description: 直近の変更に対してコードレビューを実行する。code-reviewer エージェントを使った Writer/Reviewer パターンの定型ワークフロー。
allowed-tools: Read, Glob, Grep, Bash, Task
disable-model-invocation: true
---

直近のコード変更に対してプロジェクト規約準拠のレビューを実行する。

## 使い方
- `/review` — 未コミットの変更をレビュー
- `/review <コミット範囲>` — 指定範囲をレビュー
- `/review <ファイルパス>` — 特定ファイルをレビュー

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
