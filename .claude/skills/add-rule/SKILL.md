---
description: 会話の文脈からルール内容を抽出し、.claude/rules/ に追加する
allowed-tools: Read, Glob, Grep, Write, Edit
---

会話の文脈からルール内容を抽出し、`.claude/rules/` に追加する。

## トリガー
「ルール化してください」「ルールに追加して」「rules に書いて」等の発言で起動。

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
