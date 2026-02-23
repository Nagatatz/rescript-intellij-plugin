# Qodana 指摘事項の分析と修正計画

GitHub Actions の Qodana スキャン結果を取得し、指摘事項を分析して修正計画を生成する。

## 使い方

- `/fix-qodana` — 最新の Qodana 実行結果を取得して分析
- `/fix-qodana 22093016636` — 指定の run ID の結果を分析
- `/fix-qodana https://github.com/.../actions/runs/...` — URL 指定

## 分析手順

### ステップ1: Qodana 実行結果の取得

引数がない場合は最新の Qodana ワークフロー実行を取得する:

```bash
gh run list --workflow=qodana_code_quality.yml --limit=5 --json databaseId,status,conclusion,headBranch,createdAt
```

引数が URL の場合はそこから run ID を抽出する。引数が数値の場合は run ID として扱う。

対象の run が completed でない場合はその旨を報告し、最新の completed run にフォールバックする。

### ステップ2: Code Scanning アラートの取得

GitHub Code Scanning API から Qodana が報告したアラートを取得する:

```bash
# リポジトリの code scanning alerts を取得
gh api repos/{owner}/{repo}/code-scanning/alerts --paginate -q '.[] | select(.tool.name == "QDKOTLIN" or .tool.name == "qodana" or (.tool.name | test("Qodana"; "i")))' 2>/dev/null
```

Code Scanning が利用できない場合は、run のログから問題を抽出する:

```bash
gh run view {run_id} --log 2>/dev/null | grep -E "(warning|error|CRITICAL|HIGH|MODERATE)" | head -100
```

さらに、GitHub Actions の check runs から annotations を取得する:

```bash
# run に関連する check runs を取得
gh api repos/{owner}/{repo}/check-runs/{check_run_id}/annotations --paginate 2>/dev/null
```

check_run_id は以下で取得する:

```bash
gh api repos/{owner}/{repo}/commits/{sha}/check-runs --jq '.check_runs[] | select(.name | test("[Qq]odana")) | {id: .id, name: .name, conclusion: .conclusion}'
```

### ステップ3: 指摘事項の解析

取得した結果から以下の情報を抽出する:

- **重要度** (Critical / High / Moderate / Low / Info)
- **種別** (inspection ID / rule name)
- **ファイルパス**と**行番号**
- **メッセージ** (問題の説明)

重要度別に集計し、件数サマリーを作成する。

### ステップ4: 対象ファイルの読み込みと影響分析

指摘されたファイルを Read で読み込み、問題の箇所を確認する。

各指摘について以下を分析する:

1. **問題の本質**: なぜ Qodana が指摘しているのか
2. **修正方針**: コード修正 / `@Suppress` による抑制 / 設定での除外
3. **影響範囲**: 修正が他の箇所に影響するか

#### 修正方針の判断基準

| 方針 | 適用条件 |
|------|---------|
| コード修正 | deprecated API の置換、冗長コードの簡略化、型の修正など、コードを改善すべき場合 |
| `@Suppress` | ツール名・ファイル名の capitalization など、指摘が文脈的に不適切な場合。JFlex レクサー等から間接参照されるシンボルの unused 警告 |
| 設定除外 | プロジェクト全体で一律に除外すべきルールの場合（`qodana.yaml` の `exclude` に追加） |

### ステップ5: 修正計画の生成

後述のレポートフォーマットに従い、修正計画を出力する。

## レポートフォーマット

### 実行サマリー

```
**Qodana Run:** #{run_id}
**ブランチ:** main
**実行日時:** 2026-02-17T12:00:00Z
**結果:** {conclusion}

| 重要度 | 件数 |
|--------|------|
| Critical | 3 |
| High | 10 |
| Moderate | 4 |
| Low | 0 |
| **合計** | **17** |
```

### 指摘事項一覧

重要度の高い順に列挙する。

```
### Critical

#### 1. {inspection名}: {メッセージ概要}

- **ファイル:** `path/to/File.kt` L{行番号}
- **Inspection:** {inspection ID}
- **修正方針:** コード修正 / @Suppress / 設定除外
- **修正内容:**

\```kotlin
// Before
{現在のコード}

// After
{修正後のコード}
\```

### High
...

### Moderate
...
```

### 修正対象ファイル一覧

```
| # | ファイル | 指摘数 | 修正方針 |
|---|---------|--------|---------|
| 1 | `RescriptConfigurable.kt` | 2 | コード修正 + @Suppress |
| 2 | `RescriptTokenTypes.kt` | 5 | @Suppress |
```

### 修正手順

ステアリングワークフローの tasklist.md に直接転記できる形式で出力する:

```
- [ ] 1. `ファイル名` — 修正内容の要約
- [ ] 2. `ファイル名` — 修正内容の要約
...
- [ ] N. ビルド確認 (`./gradlew clean buildPlugin`)
- [ ] N+1. コミット
```

## 注意事項

- Qodana Cloud へのアクセスには `QODANA_TOKEN` が必要。このスキルでは GitHub API 経由のみを使用する
- Code Scanning alerts が利用できない場合（private repo の Free プラン等）は、run のログベースでフォールバックする
- `qodana.yaml` の `exclude` セクションで既に除外されているルールは分析対象外とする
- JFlex レクサー生成ファイル (`RescriptFlexLexer.java`) への指摘は無視する（自動生成コード）
