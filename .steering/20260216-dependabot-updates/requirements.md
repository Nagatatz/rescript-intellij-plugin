# Requirements: Dependabot 依存関係アップデート

## 概要

Dependabot が提案した 7 つの依存関係アップデートを main ブランチに統合する。

## 対象ブランチと変更内容

### GitHub Actions アップデート（低リスク）

| ブランチ | 変更 | 影響ファイル |
|---------|------|------------|
| `dependabot/github_actions/actions/checkout-6` | actions/checkout v4 → v6 | ci.yml, qodana_code_quality.yml, release.yml |
| `dependabot/github_actions/actions/setup-java-5` | actions/setup-java v4 → v5 | ci.yml, release.yml |
| `dependabot/github_actions/actions/upload-artifact-6` | actions/upload-artifact v4 → v6 | ci.yml |
| `dependabot/github_actions/gradle/actions-5` | gradle/actions v4 → v5 | ci.yml, release.yml |

### Gradle 関連アップデート

| ブランチ | 変更 | 影響ファイル | リスク |
|---------|------|------------|--------|
| `dependabot/gradle/gradle-wrapper-9.3.1` | Gradle Wrapper 8.14 → 9.3.1 | gradle-wrapper.properties | **高** |
| `dependabot/gradle/org.jetbrains.grammarkit-2023.3.0.2` | grammarkit 2023.3.0.1 → 2023.3.0.2 | build.gradle.kts | 低 |
| `dependabot/gradle/org.jlleitschuh.gradle.ktlint-14.0.1` | ktlint plugin 12.3.0 → 14.0.1 | build.gradle.kts | **高** |

## リスク分析

### Gradle 8.14 → 9.3.1（メジャーバージョンアップ）
- Gradle 9 では非推奨 API の削除や設定方法の変更がある可能性
- IntelliJ Platform Gradle Plugin との互換性を要確認
- ビルド成功を確認してから適用

### ktlint plugin 12.3.0 → 14.0.1（2メジャーバージョンジャンプ）
- 新しい lint ルールの追加や既存ルールの変更がありうる
- コードのフォーマット違反が新たに検出される可能性
- ビルド＋ktlintCheck 成功を確認してから適用

## 受け入れ条件

- [ ] 全 7 アップデートが main ブランチに適用されている
- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] 高リスクアップデートでビルドが失敗した場合はスキップし、ユーザーに報告する
- [ ] 適切な粒度でコミットされている
