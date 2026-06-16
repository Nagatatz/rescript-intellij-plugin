# tasklist — Test Code Lens (#111) + open qualifier 展開 intention (#112)

各セクション = 1 機能 + テスト + plugin.xml 登録 + EN/JA docs = 1 コミット（独立にマージ可能）。
**依存**: #111 と #112 は独立。#111 → #112 の順で着手する。

## セクション 0: worktree 準備

- [x] `git fetch origin` で同期確認（6 ahead / 0 behind を確認）
- [x] worktree 内に feature ブランチ `worktree-test-codelens-open-expand` を作成（HEAD=9d206d72、JSX 取込済み）
- [x] steering ディレクトリ `20260616-004-test-codelens-open-expand` を worktree 内に作成し steering 3 ファイルを書き出す
- [x] steering ディレクトリをコミット（f1ee290f）

## セクション 1: #111 Test Code Lens

> **設計変更（design.md:54-58 のフォールバックを発動）:** CodeVision (`DaemonBoundCodeVisionProvider`) ではなく
> `RunLineMarkerContributor`（ガター実行アイコン）で実装する。`javap` 調査で `TextCodeVisionEntry` の
> 第 6 引数は context-menu の extra action であり click handler ではないこと、`ClickableTextCodeVisionEntry` は
> Kotlin `Function2` onClick が必要で Java/Kotlin から実行構成へ橋渡しする platform 前例が無いことを確認した。
> ガターマーカーは DataContext を自動供給するため `ExecutorAction.getActions(0)` → `ConfigurationContext` →
> producer に綺麗に橋渡しでき、実 Run/Debug を提供できる（既存 `RescriptRunLineMarkerContributor` が前例）。

- [x] `test/RescriptTestCallDetector.kt` を新設（`TestCall` data class + `detect`、純ロジック、英語 KDoc）
- [x] `test/RescriptTestRunLineMarkerContributor.kt` を新設（`RunLineMarkerContributor`、テストファイル + 検出済み test-call の関数名 LIDENT leaf にのみ Run/Debug ガター、`ExecutorAction.getActions(0)` 配線、英語 KDoc）
- [x] `test/RescriptTestConfigurationProducer.kt` を拡張（context offset が TestCall に合致したら `-t <name>` フィルタ設定、無ければファイル単位フォールバック。`configurationName` を internal 化して純ロジックテスト可能に）
- [x] plugin.xml に `<runLineMarkerContributor language="ReScript">` を登録
- [x] `test/RescriptTestCallDetectorTest.kt` を新設（describe/it/test 検出 / ネスト / 補間テンプレートスキップ / 非対象呼び出し / 複数引数）
- [x] producer は実行構成 UI 結合のため本体は免除（testing.md 免除区分）。`configurationName`（file-scoped / test-scoped 命名）の純ロジックを `RescriptTestConfigurationProducerTest` で 4 本テスト。offset→TestCall マッチは detector 側で全網羅
- [x] docs: README(その他) / sphinx `testing.md`(EN) + JA .po（advanced.md ではなく専用ページ testing.md が正） / functional-design.md(EP マップ) / repository-structure.md（CLAUDE.md layer3 は前例に倣い functional-design.md に委譲）
- [x] `./gradlew ktlintCheck test` 緑を確認し `✨ Add test run/debug gutter markers` でコミット

## セクション 2: #112 open qualifier 展開 intention

- [x] `imports/RescriptModuleMemberExtractor.kt` を新設（`extractTopLevelNames`、depth-0 let/type/module/external/exception、純ロジック、英語 KDoc）
- [x] `imports/RescriptOpenExpansionPlanner.kt` を新設（`ExpansionPlan` + `plan`、修飾済みスキップ / 非メンバスキップ / 保守的シャドウ除外、純ロジック、英語 KDoc）
- [x] `intention/RescriptExpandOpenQualifierIntention.kt` を新設（`RescriptBaseIntention` 継承、FilenameIndex で M.res/M.resi 確認、Messages 件数確認、単一 WriteCommandAction reverse-offset 挿入 + open 削除、英語 KDoc）
- [x] plugin.xml に `<intentionAction>` を登録
- [x] `src/main/resources/intentionDescriptions/RescriptExpandOpenQualifierIntention/description.html` を新設
- [x] `imports/RescriptModuleMemberExtractorTest.kt`（let/let rec/type/module/external/exception 抽出 / ネスト内除外 / 空）
- [x] `imports/RescriptOpenExpansionPlannerTest.kt`（基本展開 / 修飾済みスキップ / 非メンバスキップ / シャドウ除外 / 二重修飾防止 / open 削除範囲）
- [x] `intention/RescriptExpandOpenQualifierIntentionTest.kt`（light fixture: availability on/off / ライブラリモジュール非表示 / 展開結果）
- [x] docs: README(Code Editing) / sphinx code-editing.md(EN) + JA .po / functional-design.md(intentionAction EP マップ) / repository-structure.md
- [x] `./gradlew ktlintCheck test` 緑を確認し `✨ Add expand-open-qualifier intention` でコミット

## セクション 3: ロードマップ更新

- [x] `docs/product-requirements.md` の「新機能候補」テーブルから #111 / #112 の行を削除
- [x] `📝 Remove implemented #111/#112 from roadmap` でコミット

## セクション Z: マージ

- [ ] 全セクション緑、`./gradlew clean buildPlugin test` 成功を確認
- [ ] requirements.md の受け入れ条件をすべて満たしていることを確認
- [ ] このファイルの全タスクを `[x]` 更新（マージ前最終コミットに同梱）
- [ ] `AskUserQuestion` でマージ可否を確認
- [ ] 承認後 worktree 内で `main` にマージ → 作業ブランチ削除 → セッション終了（worktree 自動クリーンアップ）

## テスト免除メモ

- `RescriptTestCodeVisionProvider`: editor / platform 結合（DaemonBoundCodeVisionProvider）のため本体は免除。検出ロジックは `RescriptTestCallDetector` で全網羅テスト。
- `RescriptTestConfigurationProducer`: 実行構成 UI 結合（testing.md 免除区分）。offset→TestCall マッチは detector 側でテスト。light fixture で駆動可能なら testName 反映を 1 本だけ確認。
