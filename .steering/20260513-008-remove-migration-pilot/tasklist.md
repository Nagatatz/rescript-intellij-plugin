# タスクリスト: Migration Pilot 機能の削除

単一の `🗑️` コミットで完結させる前提。各セクションは検証の単位として並んでいる。

## セクション 1: コード削除

- [x] `src/main/kotlin/com/rescript/plugin/migration/` 配下のすべての `.kt` を削除
- [x] `src/main/resources/icons/rescript-migration.svg` を削除
- [x] `src/test/kotlin/com/rescript/plugin/migration/` 配下のすべての `.kt` を削除
- [x] `src/test/kotlin/com/rescript/plugin/cli/RescriptMigrationConverterCliTest.kt` を削除
- [x] `src/test/kotlin/com/rescript/plugin/cli/ExternalCliAvailability.kt` から `isRescriptCliAvailable` を削除

## セクション 2: Extension Point 登録解除

- [x] `src/main/resources/META-INF/plugin.xml` の `<toolWindow id="ReScript Migration Pilot" ...>` 行を削除（前後のコメントも含む）
- [x] `src/main/resources/META-INF/plugin.xml` の `<action id="ReScript.ShowMigrationPilot" ...>` ブロックを削除

## セクション 3: ドキュメント更新（EN）

- [x] `CLAUDE.md` のレイヤー 3 段落から Migration Pilot を削除
- [x] `README.md` Features セクションから Migration Pilot 行を削除
- [x] `docs/repository-structure.md` の `migration/` 行と `RescriptMigrationConverterE2eTest` 言及を削除
- [x] `docs/functional-design.md` の `RescriptMigrationToolWindowFactory` / `RescriptMigrationAction` 行を削除
- [x] `docs/lsp-fallback-matrix.md` の Migration Pilot 行を削除
- [x] `sphinx-docs/user/features/advanced.md` の `## Reason → ReScript Migration Pilot` セクションを削除

## セクション 4: ドキュメント更新（JA）

- [x] `cd sphinx-docs && make gettext && make update-po` で `advanced.po` を再生成
- [x] 既存翻訳の漂遊した msgstr が出ないかを確認（必要なら追記）
- [x] `make build-ja` が通ることを確認

## セクション 5: 検証

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功
- [x] `./gradlew koverVerify` が `minBound` を割っていないか確認

## セクション 6: コミット & マージ

- [x] このタスクリストのすべての `[ ]` を `[x]` に更新
- [x] 単一の `🗑️ Remove Reason → ReScript Migration Pilot feature` コミットを作成
- [x] worktree から `main` に merge して branch を削除
