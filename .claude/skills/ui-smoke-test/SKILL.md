---
description: サンドボックス IDE (`runIdeForUiTests`) を Remote-Robot 付きで起動し、caret 駆動のツールウィンドウ (ReScript Type / Switch Flow / Type Impact / PPX 等) を JS 実行 API で操作してスモークテストする。ユーザーが「スモークテスト」「runIde で動作確認」「ツールウィンドウを実際に動かして確認」「computer use でテスト」と述べた場合、または coroutine/Alarm デバウンス・caret 追従・LSP 連携・プロジェクト分離の手動検証を依頼した場合に起動する。
model: sonnet
allowed-tools: Read, Write, Bash, Grep, Glob
---

# ツールウィンドウ スモークテスト (Remote-Robot)

サンドボックス IDE を Remote-Robot サーバー付きで起動し、HTTP の `/js/execute`・`/screenshot` API で caret を動かし、ツールウィンドウのパネル内容・スクリーンショット・サンドボックス `idea.log` を読み取って機能を検証する擬似 computer use スキル。マウス座標ではなく **JVM 内 JS 実行**で駆動するため、ヘッドレス寄りでも安定して再現できる。

主な検証対象:

- **caret 追従**: ReScript Type / Switch Flow / Type Impact / PPX が caret 移動に追従して更新されるか
- **デバウンス**: 連打中は更新されず、停止後に 1 回だけ更新されるか (`RescriptCoroutineDebouncer`)
- **ライフサイクル**: プロジェクト close → reopen で例外・leak・"already disposed" が出ないか
- **プロジェクト分離**: 2 プロジェクト同時起動時、片方の操作が他方のパネルを更新しないか

**最重要の罠:** サンドボックス初回起動では「Trust Project」「Meet the Islands Theme」等の **APPLICATION_MODAL ダイアログが EDT を塞ぎ**、LSP が起動せずパネルも更新されない。検証の前に必ずダイアログを解除すること（INSTRUCTIONS.md ステップ 2）。

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。`assets/robot.sh` が curl + JSON→PNG 変換 + JS 実行のボイラープレートをまとめている。
