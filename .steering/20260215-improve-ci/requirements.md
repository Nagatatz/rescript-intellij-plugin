# Requirements: GitHub Actions CI 整備

## 概要

GitHub Actions で CI を構築し、PR / push 時にビルド・テスト・静的解析を自動実行する。既存の `ci.yml` をベースに、実用的な改善を施す。

## 背景

- `.github/workflows/ci.yml` が既に存在するが、未コミット状態
- ローカルでの検証により全ステップが `BUILD SUCCESSFUL` であることを確認済み
- ただし `verifyPlugin`（バイナリ互換性検証）は IDE ダウンロードを伴い CI 上で非常に遅い可能性がある
- `verifyPluginProjectConfiguration` から 2 件の警告が出ている

## 要求事項

### R1: CI ワークフローの整備

- `main` ブランチへの push および PR をトリガーに CI を実行する
- 以下のステップを含む:
  - Gradle Wrapper 検証
  - JDK 21 セットアップ
  - ktlint チェック
  - プラグインビルド
  - テスト実行
  - プラグイン構造検証
- ビルド成果物（plugin zip）とテスト結果をアーティファクトとしてアップロードする

### R2: 重いタスクの分離

- `verifyPlugin`（バイナリ互換性検証）は IDE バイナリのダウンロードを伴い、実行に長時間かかる
- PR の CI を軽量に保つため、`verifyPlugin` を通常の CI ジョブから分離する
- 方針: `main` ブランチへの push 時のみ実行する、または別ジョブとして分離する

### R3: Gradle 設定の警告対応

- `until-build` プロパティ: IntelliJ Platform 2024.3+ では前方互換性のため削除が推奨されている → `gradle.properties` から `pluginUntilBuild` を削除
- Kotlin stdlib 依存関係: Gradle Kotlin プラグインが自動追加する stdlib と IntelliJ Platform バンドル版が競合する可能性 → `gradle.properties` に `kotlin.stdlib.default.dependency=false` を追加

## 受け入れ条件

- [ ] `ci.yml` が PR / push 時に正しく実行される構成になっている
- [ ] `verifyPlugin` が通常の CI フローから分離されている
- [ ] `verifyPluginProjectConfiguration` の警告が解消されている
- [ ] ローカルで `./gradlew buildPlugin test ktlintCheck verifyPluginStructure verifyPluginProjectConfiguration` が全て成功する

## スコープ外

- リリースワークフロー（`release.yml`）の整備は今回のスコープ外
- Dependabot 設定（`dependabot.yml`）の整備は今回のスコープ外
