# タスクリスト: inferred 型注釈の一括挿入

各セクション = マージ可能な1単位（1コミット目安）。先行コミットで `RescriptBatchAnnotationPlanner` 本体とテストは作成済み。

## セクション1: planner 挿入可能性フィルタ

- [x] `RescriptBatchAnnotationPlanner` に `isInsertableType(type): Boolean` を追加（weak typevar `'_`、バッククォート、markdown 残渣を弾く）
- [x] `buildPlan` で `normalizeType` 通過後に `isInsertableType` を適用し、false は skip 算入
- [x] `RescriptBatchAnnotationPlannerTest` に振り分け・skip 算入のテストを追加
- [x] `./gradlew test` グリーン確認
- [x] コミット `✨ Add insertable-type filter to batch annotation planner`

## セクション2: 実行グルー RescriptBatchAnnotationRunner

- [ ] `RescriptBatchAnnotationRunner` 作成（background hover 解決 → modificationStamp ガード → 単一 WriteCommandAction → 結果 balloon）
- [ ] 通知・ダイアログは `file.name` のみ（絶対パス露出禁止）
- [ ] KDoc 付与（クラス責務 + planner との関係）
- [ ] テスト免除（理由: LSP 結合 + IDE スレッド + write action。light fixture で駆動不可）
- [ ] `./gradlew test` グリーン確認
- [ ] コミット `✨ Add batch annotation runner for inferred types`

## セクション3: エディタ Intention + 登録

- [ ] `RescriptBatchInsertInferredTypesIntention` 作成（`isAvailable` は純判定 + LSP 在席、hover を呼ばない / `invoke` は Runner 委譲 / preview は EMPTY）
- [ ] `plugin.xml` に `<intentionAction>` 登録（既存並び順に従う）
- [ ] `RescriptBatchInsertInferredTypesIntentionTest`（`isAvailable` の純判定をテスト: 候補0件・LSP非在席で false）
- [ ] `./gradlew test` グリーン確認
- [ ] コミット `✨ Add batch insert inferred types intention`

## セクション4: Heat Map 行アクション

- [ ] `RescriptTypeCoveragePanel` に行アクション追加（選択ファイルを開いて Runner 起動）
- [ ] テスト免除（理由: Swing UI パネル）
- [ ] `./gradlew test` グリーン確認
- [ ] コミット `✨ Add inferred-type annotation action to coverage heat map`

## セクション5: ドキュメント同期

- [ ] CLAUDE.md レイヤー3（`intention/`）に本機能を追記
- [ ] README Features に追記
- [ ] sphinx `user/features/code-editing.md`（EN）+ `.po`（JA）に変換例つきで追記、`make build-ja` 確認
- [ ] `docs/product-requirements.md` のロードマップ #117 行を削除
- [ ] コミット `📝 Document batch insert inferred types feature`

## セクション6: マージ

- [ ] `./gradlew ktlintCheck clean buildPlugin test` 全グリーン
- [ ] 全タスク `[x]` 確認
- [ ] ユーザーにマージ可否確認（AskUserQuestion）
- [ ] 承認後 `main` にマージ・ブランチ削除・セッション終了
