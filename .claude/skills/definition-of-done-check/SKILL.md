---
description: 現在の作業中変更を Definition of Done 索引と照合して検証する。コミット前・PR 作成前、またはユーザーが「DoD check」「完了？」「コミットしていい？」「マージ可能？」と問うた場合に起動する。`.claude/rules/definition-of-done.md` を読み込み、各フェーズのリンクをたどって staged/working-tree の変更に対し機械チェックを実行する。
model: sonnet
allowed-tools: Read, Glob, Grep, Bash
---

# Definition of Done Check

DoD の各フェーズとリンク先ルールをたどり、staged / working-tree の変更に対して機械的に検証可能なチェックを実行する。非機械的なチェック（KDoc の責務文の質など）は `MANUAL` としてフラグし、`Pass` を主張しない。

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
