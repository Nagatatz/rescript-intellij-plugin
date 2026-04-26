# 設計: CI 周りの改善

## 1. release.yml の再構成

### 現状の問題

```
validate-and-build:
  - build (./gradlew buildPlugin)
  - upload-artifact (plugin-zip)
  - create release

publish:
  - checkout
  - build (./gradlew buildPlugin) ← 2 度目のビルド
  - publishPlugin
```

検証ビルドと公開ビルドが別物。`koverVerify` も `integrationTest` も走らない。

### 改修後

```
validate-and-build:
  - checkout
  - validate version consistency
  - run full quality gate:
      ./gradlew ktlintCheck checkKdoc checkExtensionPointRegistration \
        test koverVerify verifyPluginStructure verifyPlugin
  - run integration test:
      ./gradlew integrationTest --info
  - buildPlugin
  - upload-artifact (plugin-zip with retention 30 days)
  - softprops/action-gh-release with files: build/distributions/*.zip

publish:
  - checkout (workflow files only — for gradle config)
  - download-artifact (plugin-zip)
  - publishPlugin (uses pre-built zip)
```

`publishPlugin` は本来 `build/distributions/*.zip` を入力とするので、`download-artifact` で配置すれば再ビルド不要。`signPlugin` はパブリッシュ直前に必要なので、ここは要検証。検証で再ビルドが必要なら、artifact パスを `build/distributions/` に展開して `publishPlugin` に渡す。

template-integration を release.yml に追加する場合は Node.js + pnpm + bun セットアップも必要なので、validate-and-build ジョブに統合する。

## 2. concurrency 設定

### ci.yml

```yaml
concurrency:
  group: ci-${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: ${{ github.event_name == 'pull_request' }}
```

main への push は cancel しない（履歴整合性のため）。PR は古い run を cancel。

### release.yml

```yaml
concurrency:
  group: release-${{ github.ref }}
  cancel-in-progress: false
```

リリースは絶対に cancel しない。

### integration-tests.yml

```yaml
concurrency:
  group: integration-tests-${{ github.ref }}
  cancel-in-progress: ${{ github.event_name == 'pull_request' }}
```

## 3. timeout-minutes

| job | timeout |
|-----|---------|
| ci.yml `actionlint` | 5 |
| ci.yml `security` | 10 |
| ci.yml `build` | 30 |
| ci.yml `template-integration` | 30 |
| release.yml `validate-and-build` | 45 |
| release.yml `publish` | 15 |
| docs.yml `lint-and-test` | 10 |
| docs.yml `build` | 20 |
| docs.yml `a11y` | 15 |
| docs.yml `deploy` | 10 |
| integration-tests.yml | 30 (既存) |

## 4. Trivy SARIF

```yaml
- name: Run Trivy vulnerability scanner
  uses: aquasecurity/trivy-action@v0.35.0
  with:
    scan-type: fs
    scan-ref: .
    scanners: vuln
    severity: HIGH,CRITICAL
    format: sarif
    output: trivy-results.sarif

- name: Upload Trivy results to Security tab
  uses: github/codeql-action/upload-sarif@v4
  if: always()
  with:
    sarif_file: trivy-results.sarif
    category: trivy
```

`exit-code: 1` を外す（SARIF アップロード後に Security タブで管理）。`security-events: write` permission が必要。

## 5. integration-tests.yml PR トリガー

```yaml
on:
  workflow_dispatch:
  schedule:
    - cron: '0 3 * * *'
  pull_request:
    paths:
      - 'src/main/kotlin/com/rescript/plugin/wizard/**'
      - 'src/main/resources/templates/**'
      - 'src/main/resources/META-INF/plugin.xml'
      - 'build.gradle.kts'
      - 'gradle.properties'
      - '.github/workflows/integration-tests.yml'
```

## 6. 週次マルチ OS マトリクス (`os-matrix.yml`)

新規ファイル `.github/workflows/os-matrix.yml`。

```yaml
name: OS Matrix Verify

on:
  workflow_dispatch:
  schedule:
    # 毎週月曜 02:00 UTC
    - cron: '0 2 * * 1'

permissions:
  contents: read

jobs:
  verify:
    strategy:
      fail-fast: false
      matrix:
        os: [ubuntu-latest, macos-latest, windows-latest]
    runs-on: ${{ matrix.os }}
    timeout-minutes: 45
    steps:
      - uses: actions/checkout@v6
      - uses: gradle/actions/wrapper-validation@v6
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 21
      - uses: gradle/actions/setup-gradle@v6
      - name: Build plugin
        run: ./gradlew buildPlugin
      - name: Verify plugin
        run: ./gradlew verifyPlugin
```

テストは含めない（OS 依存が少ない unit test を 3 OS で回すのは無駄）。プラグインのビルドと verifyPlugin だけ。

## 7. 月次 verifyPlugin (`monthly-verify.yml`)

新規ファイル。新リリースされた IDE バージョンへの互換性追従漏れを早期発見。

```yaml
name: Monthly Plugin Verify

on:
  workflow_dispatch:
  schedule:
    # 毎月 1 日 04:00 UTC
    - cron: '0 4 1 * *'

permissions:
  contents: read

jobs:
  verify:
    runs-on: ubuntu-latest
    timeout-minutes: 60
    steps:
      - uses: actions/checkout@v6
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 21
      - uses: gradle/actions/setup-gradle@v6
      - name: Verify plugin against multiple IDE versions
        run: ./gradlew verifyPlugin --info
      - uses: actions/upload-artifact@v7
        if: always()
        with:
          name: plugin-verifier-report
          path: build/reports/pluginVerifier/
          retention-days: 30
```

## 8. integration-tests.yml の Gradle セットアップ統一

```yaml
- uses: gradle/actions/wrapper-validation@v6
- uses: actions/setup-java@v5
  with:
    distribution: temurin
    java-version: 21
- uses: gradle/actions/setup-gradle@v6
  with:
    cache-read-only: ${{ github.event_name == 'pull_request' }}
# 既存の actions/cache@v5 ブロックは削除
```

## 9. pnpm store / bun キャッシュ

```yaml
- name: Get pnpm store directory
  id: pnpm-store
  run: echo "path=$(pnpm store path --silent)" >> "$GITHUB_OUTPUT"

- uses: actions/cache@v5
  with:
    path: |
      ${{ steps.pnpm-store.outputs.path }}
      ~/.bun/install/cache
    key: ${{ runner.os }}-pnpm-bun-${{ hashFiles('src/main/resources/templates/**/package.json') }}
    restore-keys: ${{ runner.os }}-pnpm-bun-
```

`hashFiles` 対象はテンプレート側の `package.json`（依存定義）。テンプレートは生成のたびに新しいので、キャッシュ key はテンプレ定義の hash で OK。

## 10. アクションピン留めポリシー

新規 `.claude/rules/github-actions-pinning.md`:

- 公式アクション (`actions/*`, `github/*`) は major タグ float (`@v6`) を許可
- JetBrains/Gradle 公式 (`gradle/actions/*`) も major タグ float を許可
- それ以外のサードパーティ (`reviewdog/*`, `aquasecurity/*`, `astral-sh/*`, `softprops/*`, `mi-kas/*`, `pnpm/*`, `oven-sh/*` 等) は **完全な commit SHA でピン留め + コメントでバージョン併記**
- Dependabot がアップグレード PR を自動生成する
- 例外的に minor pin (`@v0.35.0`) を残す場合はコメントで理由を明記

`README.md` にエントリ追加（コミット・リリース時カテゴリ）。

> 既存ワークフローのサードパーティアクションを SHA pin にすべて変換するのは別タスク（粒度が大きすぎるため）。今回はポリシー文書化のみ行い、移行は今後の Dependabot PR でローリングで対応する。

## 11. pa11y-ci のバージョン固定

```yaml
- name: Run pa11y-ci
  run: npx --yes pa11y-ci@4.0.1 --config .pa11yci.json
```

`pa11y-ci@4.0.1` を Dependabot 対象外（`devDependencies` ではなく on-the-fly 起動）なので手動で更新する旨をコメントで記載。

## 12. カバレッジ集計の差し替え

オプション 1: `mi-kas/kover-report@v2` の出力に依存し、独自 grep ロジックを削除。
オプション 2: `xmlstarlet` をインストールしてパースする。

→ オプション 1 を採用。`mi-kas/kover-report` は PR コメントに加えて Job Summary 出力もある（`update-comment: true` の他、サマリは別経路）。Job Summary が欠ける場合は最低限の出力を kover XML から python ワンライナーで抽出する。

実装: 既存の grep スクリプトを Python スクリプトに置き換え:

```yaml
- name: Add coverage to Job Summary
  if: always()
  run: |
    python3 - <<'PY'
    import xml.etree.ElementTree as ET
    import os
    import pathlib

    report = pathlib.Path('build/reports/kover/report.xml')
    if not report.exists():
        raise SystemExit(0)
    tree = ET.parse(report)
    root = tree.getroot()
    counter = root.find("./counter[@type='LINE']")
    if counter is None:
        raise SystemExit(0)
    covered = int(counter.get('covered', 0))
    missed = int(counter.get('missed', 0))
    total = covered + missed
    if total == 0:
        raise SystemExit(0)
    pct = covered * 100 // total
    summary = pathlib.Path(os.environ['GITHUB_STEP_SUMMARY'])
    summary.write_text(
        f"## 📊 Code Coverage\n\n"
        f"| Metric | Value |\n"
        f"|--------|-------|\n"
        f"| Line Coverage | **{pct}%** |\n"
        f"| Lines Covered | {covered} / {total} |\n\n"
        f"📄 Full HTML report available in the **coverage-report** artifact.\n",
        encoding='utf-8',
    )
    PY
```

XML 構造変更にも頑健。ubuntu-latest には Python 3 が入っているので追加セットアップ不要。

## 13. permissions のジョブ単位スコープ

`ci.yml`:

```yaml
permissions:
  contents: read
# pull-requests: write は build ジョブのみで付与
```

各ジョブ:
```yaml
build:
  permissions:
    contents: read
    pull-requests: write  # mi-kas/kover-report が PR コメントを書くため
```

`security` job:
```yaml
security:
  permissions:
    contents: read
    security-events: write  # SARIF アップロード用
```

## 14. CodeQL workflow

新規 `.github/workflows/codeql.yml`:

```yaml
name: CodeQL

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
  schedule:
    - cron: '0 5 * * 1'  # 毎週月曜

permissions:
  contents: read
  security-events: write
  actions: read

jobs:
  analyze:
    runs-on: ubuntu-latest
    timeout-minutes: 30
    strategy:
      fail-fast: false
      matrix:
        language: [java-kotlin]
    steps:
      - uses: actions/checkout@v6
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 21
      - uses: github/codeql-action/init@v4
        with:
          languages: ${{ matrix.language }}
      - name: Build with Gradle
        run: ./gradlew compileKotlin compileJava --no-daemon
      - uses: github/codeql-action/analyze@v4
        with:
          category: "/language:${{ matrix.language }}"
```

CodeQL の Kotlin 解析は build trace を要するため `compileKotlin` が必要。

## 15. Dependabot pip エコシステム

`.github/dependabot.yml`:

```yaml
- package-ecosystem: pip
  directory: /sphinx-docs
  schedule:
    interval: weekly
    day: monday
  open-pull-requests-limit: 5
  labels:
    - dependencies
    - docs
```

注: `sphinx-docs/` は uv による pip-compatible プロジェクト。`pyproject.toml` を Dependabot が認識する。

## アーキテクチャ図

```
.github/workflows/
├── ci.yml              [改修] concurrency, timeouts, SARIF, perms scope, coverage script
├── release.yml         [改修] quality gate, artifact passing, concurrency, timeouts
├── integration-tests.yml [改修] PR trigger, setup-gradle, pnpm/bun cache, concurrency
├── docs.yml            [改修] timeouts, pa11y pin
├── os-matrix.yml       [新規] 週次 multi-OS verify
├── monthly-verify.yml  [新規] 月次 plugin verify
└── codeql.yml          [新規] CodeQL SAST

.github/dependabot.yml  [改修] pip ecosystem 追加

.claude/rules/
├── github-actions-pinning.md [新規]
└── README.md          [改修] 索引追加
```

## 既知のリスク

- `release.yml` でビルドアーティファクト受け渡しに切り替える際、`signPlugin` のタイミングに注意。`buildPlugin` だけで署名済み zip ができるか、`publishPlugin` の中で再署名が走るかは Gradle plugin の挙動による。検証として `validate-and-build` job 内で `./gradlew signPlugin buildPlugin` を実行し、できた zip を `publishPlugin` に渡す形にする
- `integrationTest` をリリースゲートに入れることで、リリース時間が ~2 分増える（許容）
- CodeQL の初回実行は 10〜20 分かかる（Kotlin の build trace 構築）
- マルチ OS マトリクスは Windows/macOS で初回 OS 固有エラーが出る可能性。fail-fast: false で他 OS の結果も得る
- pnpm/bun キャッシュ key は templates の package.json hash 依存。テンプレ追加時は自動的に新キーになる
