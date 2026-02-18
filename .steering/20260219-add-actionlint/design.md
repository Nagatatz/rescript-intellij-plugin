# Design: actionlint 導入

## 方針

`ci.yml` に actionlint 用の独立ジョブを追加する。JDK や Gradle が不要なため、`build` ジョブと並列実行させる。

## 変更対象

| ファイル | 変更内容 |
|---------|---------|
| `.github/workflows/ci.yml` | `actionlint` ジョブを追加 |

## 実装詳細

### `ci.yml` への追加ジョブ

```yaml
  actionlint:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v6

      - name: Run actionlint
        uses: reviewdog/action-actionlint@v1
        with:
          reporter: github-pr-review
          fail_level: error
```

- **reviewdog/action-actionlint@v1**: actionlint の公式推奨 Action。reviewdog 経由で PR にインラインコメントも付与できる
- **reporter: github-pr-review**: PR 時はレビューコメント、push 時は標準出力にフォールバック
- **fail_level: error**: エラーがあれば CI を失敗させる
- `build` / `verify` ジョブとは依存関係なし（並列実行）

## 影響範囲

- CI の実行時間: actionlint ジョブは数秒で完了するため、全体への影響なし
- 既存ジョブ: 変更なし
- `release.yml` / `qodana_code_quality.yml`: 変更なし（actionlint は ci.yml のジョブとして全ワークフローファイルをチェックする）
