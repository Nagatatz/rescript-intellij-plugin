---
description: Drive a JetBrains Marketplace release for the ReScript IntelliJ plugin end-to-end — update `<change-notes>` in `plugin.xml`, bump `pluginVersion`, update Kover coverage ratchet, create an annotated git tag, push, and overwrite GitHub Release notes with manually authored English notes. Trigger when the user says "release", "ship", "bump version", "cut a new version", or asks to prepare notes for a marketplace release.
model: sonnet
allowed-tools: Read, Edit, Bash, Grep, Glob
---

# IntelliJ Plugin Release Flow

`.claude/rules/release.md` が定める厳密な順序で Marketplace リリースを駆動するスキル。Critical invariant: `plugin.xml` `<change-notes>` と `gradle.properties` `pluginVersion` はタグ作成**前**にコミットする（どちらも `publishPlugin` のアーティファクトに焼き込まれ、後から修正不能）。

まず `INSTRUCTIONS.md` を Read ツールで読み込み、その手順に従うこと。
