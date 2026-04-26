# 要求内容: CI 周りの改善

## 背景

`.github/workflows/` 配下の workflow を精査した結果、信頼性・セキュリティ・保守性の観点で改善余地が見つかった。今回は会話で合意した 1〜15 のうち以下を対象とする。

## スコープ

| # | 項目 | 種別 |
|---|------|------|
| 1 | `release.yml` の品質ゲート強化（koverVerify + integrationTest）+ ビルド成果物のジョブ間受け渡し | 改善 |
| 2 | `ci.yml` / `release.yml` / `integration-tests.yml` への `concurrency` 制御追加 | 改善 |
| 3 | 全ジョブへの `timeout-minutes` 設定 | 改善 |
| 4 | Trivy 結果の SARIF 化 + `actions/upload-sarif` で Security タブへ集約 | 改善 |
| 5 | `integration-tests.yml` に PR トリガー追加（テンプレート関連パスのみ） | 改善 |
| 6 | 週次の OS マトリクス verify ジョブ追加（Linux/Mac/Win での build + verifyPlugin） | 新規 |
| 7 | 月次の verifyPlugin スケジュールジョブ追加 | 新規 |
| 8 | `integration-tests.yml` を `gradle/actions/setup-gradle` ベースに変更 + wrapper validation 追加 | 改善 |
| 9 | template-integration ジョブで pnpm store / bun キャッシュを有効化 | 改善 |
| 10 | アクションのバージョン固定ポリシードキュメント新規追加 (`.claude/rules/`) | 新規 |
| 11 | `npx --yes pa11y-ci` のバージョン固定 | 改善 |
| 12 | `ci.yml` のカバレッジ集計 grep スクリプトを置き換え | 改善 |
| 13 | `pull-requests: write` をジョブ単位に絞り込み | 改善 |
| 14 | CodeQL workflow の追加 | 新規 |
| 15 | Dependabot の pip エコシステム追加 | 改善 |

## 受け入れ条件

- [ ] `release.yml` が `koverVerify` と `integrationTest` をパブリッシュ前に実行する
- [ ] `release.yml` で検証したビルド成果物がそのままパブリッシュされる（再ビルドではない）
- [ ] `ci.yml` / `release.yml` / `integration-tests.yml` に `concurrency` ブロックがあり、PR では古い run が cancel される
- [ ] すべてのジョブに `timeout-minutes` が設定されている
- [ ] Trivy 検出結果が GitHub Security タブに表示される
- [ ] テンプレート関連ファイルへの PR で `integration-tests` workflow が起動する
- [ ] 週次でマルチ OS verify が走る (`os-matrix.yml`)
- [ ] 月次で `verifyPlugin` が走る (`monthly-verify.yml`)
- [ ] `integration-tests.yml` が `setup-gradle` + `wrapper-validation` を使用している
- [ ] template-integration ジョブで pnpm store / bun のキャッシュが効いている
- [ ] `.claude/rules/github-actions-pinning.md` が存在し、`README.md` 索引に追加されている
- [ ] `npx pa11y-ci` がバージョン指定で実行される
- [ ] カバレッジ集計が grep を使わない方式に置き換わっている
- [ ] `pull-requests: write` がワークフロー全体ではなくジョブ単位で付与されている
- [ ] CodeQL workflow が存在し、Java/Kotlin を解析する
- [ ] Dependabot の `pip` エコシステムが `sphinx-docs/` を対象に追加されている
- [ ] `./gradlew ktlintCheck buildPlugin test` が成功する
- [ ] すべての workflow が actionlint を通る
