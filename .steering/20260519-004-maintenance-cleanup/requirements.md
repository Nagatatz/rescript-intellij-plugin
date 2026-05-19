# Maintenance Cleanup — 要求内容

## 背景

3 軸監査 (ドキュメント整合性 / リファクタ / パッケージ更新) の結果として、リファクタ高優先 3 件 (Steering 003) を片付けた後に残る軽微な保守タスクを 1 ステアリングで一括処理する。

## スコープ

5 件の独立した変更 + ドキュメント = 6 コミット。

### P1: Gradle wrapper 9.5.0 → 9.5.1

- `gradle/wrapper/gradle-wrapper.properties` の `distributionUrl` を更新
- パッチ版なので動作影響ほぼゼロ、診断改善のみ

### P2: IntelliJ Platform 2026.1.1 → 2026.1.2

- `gradle.properties` の `platformVersion`
- `build.gradle.kts` の `pluginVerification.ides` で pin している `IntellijIdea` バージョン
- `./gradlew verifyPlugin` を実行し、新規 deprecated 警告がないことを確認
- メモリ (project_platform_2026_1_blocked.md) 記述も同期更新

### F21: reasonml-idea-plugin 記述補正

- `docs/product-requirements.md:11`: 「メンテナンス停止状態」→「2025-09 を最後に低頻度メンテ」
- `docs/product-requirements.md:31`: 「既存プラグインがメンテナンス停止状態」→「既存プラグインの活発度が低下」
- 補正の根拠は `.steering/20260514-001-feature-discovery/requirements.md` で実証済み (`gh api` の検証結果)

### 技術負債 1: `Alarm(POOLED_THREAD)` の `UnstableApiUsage` 再評価

- `src/main/kotlin/com/rescript/plugin/typeinfo/RescriptTypeInfoPanel.kt:44-46`
- 2026.1.2 で stable な代替 API が出ているか確認
- 出ていなければ inline コメントを「Reviewed: 2026-05-19, no stable replacement in 2026.1.x」に更新
- Plugin Verifier ではなく Kotlin compiler の `UnstableApiUsage` 警告なので `plugin-verifier-ignored-problems.txt` への追加は不要

### 技術負債 2: `RescriptConfigurable` の `UNCHECKED_CAST` 4 箇所を集約

- `src/main/kotlin/com/rescript/plugin/settings/RescriptConfigurable.kt:67, 117, 126, 135`
- 4 箇所のうち 117/126/135 は同じパターン (`components[entry.descriptor.id] as SettingComponent<T>`)
- 型付きヘルパー `private fun <T> componentFor(entry: SchemaEntry.Field<T>): SettingComponent<T>` に集約
- 67 はパス指定子用 (`pathSnapshot` 構築) で別パターン、こちらは個別に `componentFor` 風の helper を用意するか、コメントで invariant を明記
- 結果: 4 つの `@Suppress` を 1〜2 に削減

## 受け入れ条件

- `./gradlew ktlintCheck buildPlugin test koverHtmlReport koverVerify verifyPluginStructure` 全緑
- `./gradlew verifyPlugin` で新規 deprecated 警告が出ない (既存 ignored エントリは適用)
- `RescriptConfigurable` の動作は不変 (UI 設定 apply / reset / isModified の挙動が変わらない)
- ドキュメント補正で実装側 (CLAUDE.md) との表記矛盾が発生していないことを `grep` で確認

## 制約

- 既存の API シグネチャ (public/internal) は変えない
- 新規 Extension Point の登録なし
- ステアリングコミット粒度は機能単位 (5 commit + docs/tasklist 仕上げ)
