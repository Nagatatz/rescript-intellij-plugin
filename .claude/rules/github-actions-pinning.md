# GitHub Actions ピン留めポリシー

`.github/workflows/` 配下で参照する `uses:` のバージョン指定方針を定める。サプライチェーン攻撃と再現性の観点で、信頼度に応じて固定方法を変える。

## 信頼度別のピン留め方針

| 配信元 | ピン留め方法 | 例 |
|-------|-------------|-----|
| GitHub 公式 (`actions/*`, `github/*`) | major タグ float (`@v6`) | `actions/checkout@v6` |
| Gradle 公式 (`gradle/*`) | major タグ float | `gradle/actions/setup-gradle@v6` |
| JetBrains 公式 (`JetBrains/*`) | major タグ float | — |
| その他のサードパーティ | **完全な commit SHA** + コメントでバージョン併記 | `aquasecurity/trivy-action@<40 桁 SHA>  # v0.35.0` |

サードパーティを SHA 固定にする理由:

- minor タグ・major タグは作者が後付けで上書き可能（タグの再付け替え）。リリース後に悪意あるコミットへ差し替えられても気付けない
- SHA は不変。Dependabot が自動的にアップグレード PR を出してくれる（`dependabot.yml` の `package-ecosystem: github-actions` で対応済み）

## 例外的に minor / patch 固定を許容するケース

以下を **すべて** 満たす場合は SHA 固定を免除し、`@vX.Y.Z` のセマンティックバージョン固定を許容する:

- 配信元の組織がエコシステム上で主要な信頼を獲得している（例: `astral-sh`, `aquasecurity`）
- 当該アクションが頻繁にメジャーバージョンを切る運用ではない
- アクションが署名付きリリースを行っている、または GitHub Verified Owner である

例外を適用する場合は、対応する `uses:` 行の直前にコメントで理由を明記する:

```yaml
# astral-sh は uv 公式メンテナ。tag re-pointing リスクは限定的と判断し semver 固定を許容。
- uses: astral-sh/setup-uv@v8.1.0
```

## 既存 workflow のマイグレーション方針

- 本ポリシーは **新規追加・編集する `uses:` 行** に対して適用する
- 既存のサードパーティアクション（major タグ float）は段階的に SHA 固定へ移行する
- Dependabot が出すアップグレード PR をマージするタイミングで、その行だけ SHA 固定に切り替える（一斉移行は不要）

## 検証

- `actionlint` は SHA 固定形式を許容する
- レビュー時にコメントの「バージョン併記」が抜けていないか確認する
- Dependabot の PR では SHA とバージョン両方が更新される

## 関連ルール

- アクションの自動更新設定: `.github/dependabot.yml` の `package-ecosystem: github-actions`
- リリース手順での actions 更新タイミング: [release.md](release.md)
