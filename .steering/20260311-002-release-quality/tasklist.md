# タスクリスト: リリース品質向上

## Tier 1: Quick Wins

- [x] 1.1 Qodana Dependabot バイパス除去 (`qodana_code_quality.yml`)
- [x] 1.2 リリースワークフロー整合性チェック (`release.yml`)
- [x] 1.3 Kover カバレッジ閾値ラチェット化 (`build.gradle.kts` 54→85, `release.md` ポリシー追記)
- [x] 1.4 Qodana 失敗閾値設定 (`qodana.yaml`)

## Tier 2: Gradle タスク・CI ステップ追加

- [x] 2.3 Extension Point 登録整合性チェック (`build.gradle.kts` + CI)
- [x] 2.1 KDoc 存在チェック自動化 (`build.gradle.kts` + CI)
- [x] 2.2 テストファイル存在チェック自動化 (`build.gradle.kts`)
- [x] 2.4 Marketplace パブリッシュ承認ゲート (`release.yml`)

## Tier 3: 長期的改善

- [x] 3.1 依存関係脆弱性スキャン (`ci.yml` Trivy 追加)
- [x] 3.2 テストフィクスチャ拡充 (`src/test/testData/`)
- [x] 3.3 パフォーマンスベンチマークテスト
- [x] 3.4 ヘッドレス IDE スモークテスト

## コミット・マージ

- [x] `./gradlew clean buildPlugin` 成功確認
- [x] 新規 Gradle タスクの動作確認
- [x] 機能単位でコミット
- [x] `main` にマージ
