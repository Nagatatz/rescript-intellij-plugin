# Requirements: テストカバレッジの導入と GitHub Actions 表示

## 概要

テストカバレッジを計測し、GitHub Actions の CI パイプラインでカバレッジレポートを確認できるようにする。

## 要求内容

### 1. カバレッジ計測ツールの導入

- **Kover** (JetBrains 公式 Kotlin カバレッジツール) を Gradle プラグインとして導入する
- `./gradlew koverXmlReport` で XML レポートを生成できるようにする
- `./gradlew koverHtmlReport` で HTML レポートを生成できるようにする

### 2. GitHub Actions でのカバレッジ表示

- **PR コメント**: Pull Request にカバレッジレポートをコメントとして投稿する
  - [`mi-kas/kover-report`](https://github.com/mi-kas/kover-report) Action を使用
  - 既存コメントの更新（重複コメント防止）
- **Job Summary**: GitHub Actions の Job Summary にカバレッジ結果を表示する

### 3. ローカル開発でのカバレッジ確認

- `./gradlew koverHtmlReport` でローカルに HTML レポートを確認可能にする
- `./gradlew koverLog` でコンソールにカバレッジサマリーを出力可能にする

## 受け入れ条件

- [ ] `./gradlew koverXmlReport` が成功し、XML レポートが生成される
- [ ] `./gradlew koverHtmlReport` が成功し、HTML レポートが生成される
- [ ] CI の PR でカバレッジコメントが投稿される
- [ ] GitHub Actions の Job Summary にカバレッジが表示される
- [ ] 既存のテストが引き続き正常に動作する

## 制約事項

- カバレッジ閾値（最低カバレッジ率）の強制は今回は設定しない（現状把握が目的）
- 外部サービス（Codecov 等）は使用しない
- JFlex 自動生成コード (`RescriptFlexLexer.java`) はカバレッジ対象から除外する
