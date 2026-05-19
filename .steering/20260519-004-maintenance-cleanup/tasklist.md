# Maintenance Cleanup — タスクリスト

## セクション A: Gradle wrapper 9.5.1

- [x] `gradle/wrapper/gradle-wrapper.properties` の `distributionUrl` を 9.5.0 → 9.5.1
- [x] `./gradlew --version` で 9.5.1 が起動することを確認
- [x] `./gradlew ktlintCheck buildPlugin` が緑
- [x] コミット: `🔧 Bump Gradle wrapper to 9.5.1`

## セクション B: IntelliJ Platform 2026.1.2

- [x] `gradle.properties` の `platformVersion` を 2026.1.1 → 2026.1.2
- [x] `build.gradle.kts` の `pluginVerification.ides` の pin を 2026.1.2 に
- [x] `./gradlew verifyPlugin` が緑 (1 件の既存 `MarkedString` deprecation のみ、新規なし)
- [x] `docs/product-requirements.md` の Verifier ブロッカー記述を 2026.1.2 に同期
- [x] memory の `project_platform_2026_1_blocked.md` の現バージョン記述を 2026.1.2 に更新
- [x] コミット: `⬆ Bump IntelliJ Platform to 2026.1.2`

## セクション C: reasonml-idea-plugin 記述補正

- [ ] `docs/product-requirements.md:11` の「メンテナンス停止」→「2025-09 を最後に低頻度メンテ」
- [ ] `docs/product-requirements.md:31` の「メンテナンス停止状態」→「活発度が低下」
- [ ] `grep -rn "メンテナンス停止" .` で他に残っていないことを確認
- [ ] コミット: `📝 Correct reasonml-idea-plugin maintenance status in product requirements`

## セクション D: Alarm UnstableApiUsage review date 更新

- [ ] `src/main/kotlin/com/rescript/plugin/typeinfo/RescriptTypeInfoPanel.kt:44` の inline コメントを最新の review date と stable replacement 不在の根拠で更新
- [ ] `./gradlew ktlintCheck buildPlugin` が緑
- [ ] コミット: `🔧 Re-document Alarm UnstableApiUsage suppression for 2026.1.2`

## セクション E: UNCHECKED_CAST helper 集約

- [ ] `RescriptConfigurable` に `componentFor<T>` と `pathComponent` ヘルパーを追加
- [ ] 既存 4 箇所の `@Suppress("UNCHECKED_CAST")` を helper 呼出に差し替え
- [ ] 既存テスト (`RescriptConfigurableTest` または `RescriptProjectSettingsTest`) があれば緑のまま、なければ動作不変の手動検証 (apply/reset/isModified)
- [ ] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.settings.*"` が緑
- [ ] コミット: `♻️ Consolidate UNCHECKED_CAST into componentFor helpers in RescriptConfigurable`

## セクション F: 仕上げとマージ

- [ ] `./gradlew ktlintCheck buildPlugin test koverHtmlReport koverVerify verifyPluginStructure` 全緑
- [ ] DoD Phase 3 自己検証
- [ ] 本ファイルの全チェックボックスを `[x]` に更新してコミット
- [ ] `AskUserQuestion` でマージ可否確認、承認後 main にマージ

## テスト省略の理由

- P1/P2 は設定ファイル変更のみ (`./gradlew --version` と verifyPlugin で実証)
- F21 はドキュメント変更のみ
- D は inline コメント更新のみ (動作変化ゼロ、テスト不要)
- E は型システム上等価な refactor (動作変化ゼロ)。既存 `RescriptConfigurable` 自体は `Configurable` インターフェースで UI 免除カテゴリだが、ヘルパー部分は将来的にユニットテスト対象になり得る
