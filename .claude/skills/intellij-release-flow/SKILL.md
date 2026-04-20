---
description: ReScript IntelliJ plugin の JetBrains Marketplace リリースを一気通貫で駆動する — `plugin.xml` の `<change-notes>` 更新、`pluginVersion` バンプ、Kover カバレッジラチェット更新、アノテーション付き Git タグ作成、プッシュ、手書き英語ノートでの GitHub Release ノート差し替え。ユーザーが「release」「ship」「bump version」「cut a new version」と述べた場合、または Marketplace リリース用ノートの準備を依頼した場合に起動する。
model: sonnet
allowed-tools: Read, Edit, Bash, Grep, Glob
---

# IntelliJ Plugin リリースフロー

`.claude/rules/release.md` が定める厳密な順序で Marketplace リリースを駆動するスキル。重要な不変条件: `plugin.xml` の `<change-notes>` と `gradle.properties` の `pluginVersion` はタグ作成**前**にコミットする（どちらも `publishPlugin` のアーティファクトに焼き込まれ、後から修正不能）。

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
