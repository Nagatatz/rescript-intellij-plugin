---
description: Kotlin コードを過去の Qodana 指摘パターンに基づき校正する。未コミット差分や指定ファイル/コミット範囲を対象に、カテゴリ別チェックリストで静的レビューを行う。
allowed-tools: Read, Glob, Grep, Bash
disable-model-invocation: true
---

過去の Qodana 指摘傾向（Null 安全・非推奨 API・未使用宣言・Kotlin イディオム等 12 カテゴリ）を基に、Kotlin コードをヒューリスティックに校正する。

## 使い方

- `/proofread` — 未コミットの変更（staged + unstaged）を対象
- `/proofread <file>` — 特定ファイルを対象
- `/proofread <commit-range>` — 例: `main...HEAD`, `HEAD~3..HEAD`

まず `CHECKLIST.md` を Read ツールで読み込み、その手順とカテゴリに従って対象ファイルをレビューすること。
