# タスクリスト: CI 周りの改善

## 準備

- [ ] worktree (`feature/ci-improvements`) を作成
- [ ] `gradlew clean buildPlugin` のローカル動作確認

## 実装フェーズ

### コミット 1: アクションピン留めポリシー文書追加 (item 10)

- [ ] `.claude/rules/github-actions-pinning.md` を新規作成
- [ ] `.claude/rules/README.md` の索引に追加
- [ ] コミット: `📝 Add GitHub Actions pinning policy rule`

### コミット 2: ci.yml の concurrency / timeouts / permissions スコープ (items 2, 3, 13)

- [ ] `concurrency` ブロック追加
- [ ] 各ジョブに `timeout-minutes` 設定
- [ ] ワークフロー直下の `permissions` を `contents: read` のみに削減し、PR コメント権限は build ジョブに移す
- [ ] コミット: `🔧 Tighten ci.yml with concurrency, timeouts, and per-job permissions`

### コミット 3: Trivy SARIF 化 (item 4)

- [ ] `format: sarif` + `output: trivy-results.sarif` に変更
- [ ] `exit-code: 1` を削除
- [ ] `github/codeql-action/upload-sarif@v4` を追加
- [ ] `security-events: write` を security ジョブに付与
- [ ] コミット: `🔧 Upload Trivy results as SARIF to GitHub Security tab`

### コミット 4: ci.yml カバレッジスクリプトを Python 化 (item 12)

- [ ] grep ベースのスクリプトを Python スクリプトに置き換え
- [ ] コミット: `♻️ Replace grep-based coverage parser with Python XML parser`

### コミット 5: release.yml の品質ゲート強化と artifact 受け渡し (item 1)

- [ ] `validate-and-build` ジョブに以下を追加:
  - Node.js + pnpm + bun セットアップ
  - `./gradlew ktlintCheck checkKdoc checkExtensionPointRegistration test koverVerify verifyPluginStructure verifyPlugin`
  - `./gradlew integrationTest --info`
  - `./gradlew buildPlugin signPlugin`
- [ ] `publish` ジョブを `download-artifact` ベースに変更
- [ ] `concurrency` (cancel-in-progress: false)
- [ ] `timeout-minutes` 追加
- [ ] コミット: `🔧 Add full quality gate and artifact reuse to release workflow`

### コミット 6: integration-tests.yml の PR トリガー / setup-gradle / キャッシュ (items 5, 8, 9)

- [ ] `on:` に `pull_request` トリガーを追加（テンプレ関連 paths のみ）
- [ ] 既存の手書き `actions/cache@v5` を削除
- [ ] `gradle/actions/wrapper-validation@v6` 追加
- [ ] `gradle/actions/setup-gradle@v6` 追加
- [ ] pnpm store + bun キャッシュ追加
- [ ] `concurrency` ブロック追加
- [ ] コミット: `🔧 Add PR trigger and unify Gradle setup for integration-tests`

### コミット 7: docs.yml の timeouts / pa11y 固定 (items 3, 11)

- [ ] 各ジョブに `timeout-minutes`
- [ ] `pa11y-ci` のバージョンピン
- [ ] コミット: `🔧 Add timeouts and pin pa11y-ci version in docs workflow`

### コミット 8: os-matrix.yml 新規追加 (item 6)

- [ ] `.github/workflows/os-matrix.yml` を新規作成
- [ ] コミット: `✨ Add weekly multi-OS verification workflow`

### コミット 9: monthly-verify.yml 新規追加 (item 7)

- [ ] `.github/workflows/monthly-verify.yml` を新規作成
- [ ] コミット: `✨ Add monthly plugin verifier workflow`

### コミット 10: codeql.yml 新規追加 (item 14)

- [ ] `.github/workflows/codeql.yml` を新規作成
- [ ] コミット: `✨ Add CodeQL SAST workflow`

### コミット 11: Dependabot pip エコシステム追加 (item 15)

- [ ] `.github/dependabot.yml` に pip 設定追加
- [ ] コミット: `🔧 Track sphinx-docs Python dependencies with Dependabot`

### コミット 12: tasklist.md 完了マークと最終調整

- [ ] tasklist のすべてのタスクを `[x]` に
- [ ] コミット: `📝 Mark CI improvements tasklist complete`

## 検証フェーズ

- [ ] `./gradlew ktlintCheck` 成功
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] `./gradlew test` 成功
- [ ] `actionlint` をローカルでも実行（可能なら）
- [ ] 各 workflow の YAML 構文確認

## マージフェーズ

- [ ] `AskUserQuestion` でマージ可否確認
- [ ] 承認後 `main` にマージ + ブランチ削除
- [ ] worktree の自動クリーンアップ（セッション終了時）

## テスト免除の理由

CI ワークフローファイル (`.github/workflows/*.yml`) と Dependabot 設定はビルドツール構成であり、Kotlin/Java のテスト対象ではない。Workflow は actionlint で構文検証する（CI 上で reviewdog/action-actionlint が走るため、PR 提出時に自動検証される）。
