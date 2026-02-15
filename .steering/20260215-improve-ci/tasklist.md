# Tasklist: GitHub Actions CI 整備

## タスク一覧

### T1: Gradle 設定の警告対応

- [x] `gradle.properties` から `pluginUntilBuild` を削除
- [x] `gradle.properties` に `kotlin.stdlib.default.dependency=false` を追加
- [x] `build.gradle.kts` から `untilBuild` の参照を削除
- [x] ローカルで `./gradlew buildPlugin` が成功することを確認
- [x] `./gradlew verifyPluginProjectConfiguration` の警告が解消されていることを確認

### T2: CI ワークフローの改善

- [x] `ci.yml` を 2 ジョブ構成（`build` + `verify`）に変更
  - `build` ジョブ: push(main) + PR(main) で実行。ktlint, build, test, verifyPluginStructure, verifyPluginProjectConfiguration
  - `verify` ジョブ: push(main) のみで実行。verifyPlugin（バイナリ互換性検証）
- [x] Gradle キャッシュ設定が適切であることを確認（PR 時は `cache-read-only: true`）

### T3: 動作確認

- [x] ローカルで全 Gradle タスクが成功することを確認
  - `./gradlew ktlintCheck` ✅
  - `./gradlew buildPlugin` ✅
  - `./gradlew test` ✅
  - `./gradlew verifyPluginStructure` ✅
  - `./gradlew verifyPluginProjectConfiguration` ✅ (警告解消)
