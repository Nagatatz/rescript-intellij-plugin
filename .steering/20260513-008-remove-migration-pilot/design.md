# 設計: Migration Pilot 機能の削除

## 方針

- 関連コード・テスト・登録・アイコン・ドキュメントを **単一の `🗑️` コミット** で削除する
- 機能削除なので機能単位コミットの分割は不要。全削除を 1 コミットで一括し、レビューでも「Migration Pilot を消した」とだけ把握できるようにする
- `ExternalCliAvailability.isRescriptCliAvailable` は Migration Pilot のテストでしか使われていない（他は `mmdc` / `dot` のみ）ので併せて削除する

## 削除対象

### コード

| 種別 | パス |
|------|------|
| Production | `src/main/kotlin/com/rescript/plugin/migration/RescriptMigrationConverter.kt` |
| Production | `src/main/kotlin/com/rescript/plugin/migration/RescriptMigrationFinder.kt` |
| Production | `src/main/kotlin/com/rescript/plugin/migration/RescriptMigrationModel.kt` |
| Production | `src/main/kotlin/com/rescript/plugin/migration/RescriptMigrationAction.kt` |
| Production | `src/main/kotlin/com/rescript/plugin/migration/RescriptMigrationPanel.kt` |
| Production | `src/main/kotlin/com/rescript/plugin/migration/RescriptMigrationToolWindowFactory.kt` |
| Resource | `src/main/resources/icons/rescript-migration.svg` |
| Test | `src/test/kotlin/com/rescript/plugin/migration/RescriptMigrationConverterTest.kt` |
| Test | `src/test/kotlin/com/rescript/plugin/migration/RescriptMigrationConverterE2eTest.kt` |
| Test | `src/test/kotlin/com/rescript/plugin/migration/RescriptMigrationFinderTest.kt` |
| Test | `src/test/kotlin/com/rescript/plugin/migration/RescriptMigrationFinderIntegrationTest.kt` |
| Test | `src/test/kotlin/com/rescript/plugin/migration/RescriptMigrationModelTest.kt` |
| Test | `src/test/kotlin/com/rescript/plugin/cli/RescriptMigrationConverterCliTest.kt` |

### 編集（部分削除）

| パス | 削除箇所 |
|------|---------|
| `src/main/resources/META-INF/plugin.xml` | `<toolWindow id="ReScript Migration Pilot" ...>` と `<action id="ReScript.ShowMigrationPilot" ...>` |
| `src/test/kotlin/com/rescript/plugin/cli/ExternalCliAvailability.kt` | `isRescriptCliAvailable()` メソッド |
| `CLAUDE.md` | レイヤー 3 内の Migration Pilot 段落 |
| `README.md` | Features セクション内の Migration Pilot 行 |
| `docs/repository-structure.md` | `migration/` 行 と `RescriptMigrationConverterE2eTest` 言及 |
| `docs/functional-design.md` | `RescriptMigrationToolWindowFactory` と `RescriptMigrationAction` の 2 行 |
| `docs/lsp-fallback-matrix.md` | `Reason → ReScript Migration Pilot` 行 |
| `sphinx-docs/user/features/advanced.md` | `## Reason → ReScript Migration Pilot` 配下のセクション全体 |
| `sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po` | `make update-po` 経由で再生成して同期 |

## 削除しない箇所

- `manual-test-projects/` 配下に `.re` サンプルがあれば検出して残置（テストの目的が変わる場合は別途判断）
- `docs/product-requirements.md` — Migration Pilot に対応する US は存在しないため変更不要

## 検証

- `./gradlew ktlintCheck` — 失敗する import / 参照が残っていないか
- `./gradlew clean buildPlugin` — `plugin.xml` の dangling reference がないか
- `./gradlew test` — 残ったテストが緑のまま通るか
- `./gradlew koverVerify` — `minBound` を割っていないか（割った場合は別途相談）
- `cd sphinx-docs && make update-po && make build-ja` — 日本語訳ビルドが通るか

## ロールバック計画

単一コミットなので `git revert` で 1 ステップ復元できる。Reason サポートが再度必要になったときの参照点として、削除コミットの SHA を将来 `.steering/` 配下から辿れるようにする。
