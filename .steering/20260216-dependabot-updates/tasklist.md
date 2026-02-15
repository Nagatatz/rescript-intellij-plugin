# Tasklist: Dependabot 依存関係アップデート

## タスク

### Phase 1: 低リスクアップデート（GitHub Actions + grammarkit）

- [x] 1. GitHub Actions のバージョンを一括更新
  - actions/checkout v4 → v6（ci.yml, qodana_code_quality.yml, release.yml）
  - actions/setup-java v4 → v5（ci.yml, release.yml）
  - actions/upload-artifact v4 → v6（ci.yml）
  - gradle/actions v4 → v5（ci.yml, release.yml）
- [x] 2. grammarkit を 2023.3.0.1 → 2023.3.0.2 に更新（build.gradle.kts）
- [x] 3. コミット: `🔧 Update GitHub Actions and grammarkit versions`

### Phase 2: 高リスクアップデート（ktlint）

- [x] 4. ktlint plugin を 12.3.0 → 14.0.1 に更新（build.gradle.kts）
- [x] 5. ビルド検証: `./gradlew clean buildPlugin`
  - 失敗した場合: ktlint のフォーマット修正を試みる（`./gradlew ktlintFormat`）
  - それでも失敗した場合: スキップしてユーザーに報告
- [x] 6. コミット: `🔧 Update ktlint plugin to 14.0.1`

### Phase 3: 高リスクアップデート（Gradle Wrapper）

- [x] 7. Gradle Wrapper を 8.14 → 9.3.1 に更新（gradle-wrapper.properties）
- [x] 8. ビルド検証: `./gradlew clean buildPlugin`
  - 失敗した場合: エラー内容を分析し修正を試みる
  - それでも失敗した場合: Gradle 更新をリバートしてユーザーに報告
- [x] 9. コミット: `🔧 Update Gradle Wrapper to 9.3.1`
