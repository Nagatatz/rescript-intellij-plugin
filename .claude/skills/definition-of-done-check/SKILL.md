---
description: Verify the current working changes against the Definition of Done index. Trigger before commits, before PR creation, or when the user says "DoD check", "is this done?", "can I commit?", "ready to merge?". Reads `.claude/rules/definition-of-done.md`, walks each phase's links, and runs the mechanical checks against staged/working-tree changes.
model: sonnet
allowed-tools: Read, Glob, Grep, Bash
---

# Definition of Done Check

DoD の各フェーズ・リンク先ルールをたどり、機械的に検証可能なチェックを staged/working-tree の変更に対して実行する。非機械的チェック（KDoc 責務文章の質など）は `MANUAL` としてフラグし、Pass を主張しない。

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
