# Documentation Audit — 2026-04-27

## 概要

`docs/` / `sphinx-docs/user/` / `sphinx-docs/dev/` / `CLAUDE.md` / `README.md` / `.claude/rules/` / `docs/archive/` を対象に、stale な主張・重複・不整合を Explore エージェントで横断調査。エージェントが報告した 14 件の指摘を一次ソース（`grep -c`、`ls`、PSI コード確認）で検証した結果、**実際に対処すべきは 3 件**、残り 11 件はエージェントの誤計数・既存記法の許容範囲・主観的提案。

## 検証済みの実態

| 指標 | エージェント主張 | 実測 | 検証コマンド |
|---|---|---|---|
| Live Templates | 22 | **21** | `grep -c '<template ' src/main/resources/liveTemplates/ReScript.xml` |
| 抽象 Intention 含めた `*Intention.kt` ファイル数 | 23 | **23** | `ls src/main/kotlin/com/rescript/plugin/intention/*.kt \| wc -l`（`RescriptBaseIntention.kt` 含む） |
| 具象 `class Rescript*Intention` 数 | — | **22** | `grep -lE '^class Rescript[A-Z][a-zA-Z]+Intention\b' src/main/kotlin/com/rescript/plugin/intention/*.kt`（`RescriptBaseIntention` も `class` だが abstract） |
| `LocalInspectionTool` 派生 Inspection 数 | 8 | **7** | `grep -E 'class Rescript[A-Z][a-zA-Z]*Inspection\b' src/main/kotlin/com/rescript/plugin/inspection/*.kt`（`RescriptInspectionSuppressor` は除外） |
| Project Wizard テンプレート数 | 16 | **16** | `ls src/main/kotlin/com/rescript/plugin/wizard/templates/` |
| Postfix templates | 9 | **9** | `RescriptPostfixTemplateProvider.kt` の `simplePostfix(...)` 列挙 |

エージェントが「Live Templates 22」と報告した根拠は、`<template>` タグの行ベース粗カウントで囲み行を含めた可能性。一次ソースの `grep -c '<template '` (空白含み) が 21 のため、README の「21 code snippets」は **正しい**。

## 対処対象（=> 同セッションで実施）

| # | ファイル | 現状 | 修正後 | 理由 |
|---|---|---|---|---|
| 1 | `docs/repository-structure.md:107` | `15 種類の Live Template スニペット` | `21 種類の Live Template スニペット` | ReScript.xml の実数 |
| 2 | `docs/functional-design.md:431` | `Live Templates (15スニペット)` | `Live Templates (21 スニペット)` | ReScript.xml の実数 |
| 3 | `README.md` の Project Wizard 行（~137） | `16 production-shaped templates that scaffold builds, dependencies, and starter code` | 同上 + Validation ライブラリ選択（zod / sury）と Package Manager 選択 UI への言及を追加 | CLAUDE.md には記載済みだが README の Features 説明から欠落 |

`docs/archive/implemented-features.md:31`（`15種のスニペット`）と `:59`（`15 種類のプロジェクトテンプレート`）も古い数値だが、ファイル冒頭が「**履歴記録**」と宣言しているため意図的に保持し、修正しない。

## 未対処（次回以降の検討）

| 項目 | 性質 | 次のアクション |
|---|---|---|
| 同種の機能リストが README / CLAUDE.md / sphinx-docs / docs/functional-design.md の 4 箇所に分散しドリフト管理が困難 | アーキテクチャ | `.claude/rules/documentation.md` の同期表で既にカバーされており、今のところ実害なし |
| Setup 手順が README / CLAUDE.md / sphinx-docs/dev/setup.md の 3 箇所に分散 | 表記方針 | CLAUDE.md を master とし、他は短く参照する案。利用者向け（README）と開発者向け（CLAUDE.md）の分離方針が機能しているうちは触らない |
| Architecture 説明が CLAUDE.md / docs/architecture.md / sphinx-docs/dev/architecture.md に重複 | 表記方針 | docs/architecture.md を master、他は要約＋参照、の整理は将来の大型リファクタとして温存 |
| ~~`plugin.xml` の `<version>` を直接編集される事故防止策が `.claude/rules/release.md` に明示なし~~ | プロセス | **クローズ**: 2026-04-27 検証で `src/main/resources/META-INF/plugin.xml` に `<version>` タグが存在しないことを確認 (`grep -n '<version>' src/main/resources/META-INF/plugin.xml` が空)。`build.gradle.kts:101` の `intellijPlatform.pluginConfiguration.version = providers.gradleProperty("pluginVersion")` が build 時に injection するため、ソース側に編集対象が無い。新ルール追加は不要 |
| ~~LSP fallback matrix への CLAUDE.md / docs/architecture.md からの参照リンクなし~~ | クロスリンク | **対応済み (2026-04-27)**: CLAUDE.md「レイヤー 2: LSP 統合」末尾に `docs/lsp-fallback-matrix.md` への参照を 1 行追加 |
| ~~README.md `Postfix Completion` 行が 5 つ列挙して "and more"~~ | 表記方針 | **対応済み (2026-04-27)**: README の同行を 9 件全列挙 (`switch`/`pipe`/`log`/`some`/`ok`/`error`/`ignore`/`promise`/`await`) に書き換え |

## 結論

- **本セッションで適用する変更**: 3 ファイル、合計 4 行の数値・補記更新。`.claude/rules/steering-workflow.md` の「軽微な修正」例外条項（変更ファイル ≤3 / 行数 ≤50 / 新規クラスなし / EP 登録なし / public API シグネチャ非変更）を満たすため、worktree なしで `main` に直接コミット可
- **重複に関する 4 件の指摘**: いずれも既存ルール（`.claude/rules/documentation.md` の同期表）でカバー済みか、将来の大型整理として温存。今のドリフト実害は確認できなかった
- **`docs/archive/`**: 履歴記録のため意図的にスナップショットを保持
