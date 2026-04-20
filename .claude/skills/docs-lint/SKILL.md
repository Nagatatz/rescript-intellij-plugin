---
description: staged されたドキュメント変更に対し、sphinx `.md`↔`.po` の同期崩れと 4-target Features ミラーの欠落を Lint する。「lint docs」「check docs」、ドキュメントコミット前、または「docs が同期しているか」と聞かれた場合に起動する。`sphinx-po-ja-sync`（書き込み側）を補完し、チェック側を提供する。
model: sonnet
allowed-tools: Read, Glob, Grep, Bash
---

# Docs Lint

staged ドキュメント差分に対して、`documentation.md` の 4-target sync matrix と sphinx `.md`↔`.po` 同期を機械的にチェックする。機能→ドキュメントのマッチングはヒューリスティックのため、advisory な lint として扱う（ブロッキングゲートではない）。

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
