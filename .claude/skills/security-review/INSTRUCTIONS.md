# Security Review スキル

Red Team（攻撃者視点）と Blue Team（防御者視点）の2つのエージェントを並列実行し、セキュリティレビューの統合レポートを生成します。

## 使い方

- `/security-review` — 未コミットの変更をレビュー
- `/security-review HEAD~3..HEAD` — 指定範囲のコミットをレビュー
- `/security-review src/path/to/File.kt` — 特定ファイルをレビュー

## 手順

### 1. レビュー対象の特定

引数に応じてレビュー対象を決定する:

- **引数なし**: `git diff --name-only` + `git diff --cached --name-only` で変更ファイルを取得
- **コミット範囲** (例: `HEAD~3..HEAD`): `git diff --name-only <範囲>` で変更ファイルを取得
- **ファイルパス**: 指定されたファイルをそのまま対象とする

設定ファイルやドキュメントも含める（設定ファイルのセキュリティも重要なため）。ただし `.md` ファイルは除外する。

対象ファイルが0件の場合は「レビュー対象のファイルがありません」と報告して終了する。

### 2. Red Team / Blue Team エージェントの並列起動

Task ツールで2つのエージェントを**同時に**起動する:

```
Task(subagent_type="red-team", prompt="以下のファイルに対してセキュリティ脅威分析を行ってください: <ファイルリスト>")
Task(subagent_type="blue-team", prompt="以下のファイルに対してセキュリティ防御評価を行ってください: <ファイルリスト>")
```

**重要**: 2つの Task を同じメッセージ内で並列に呼び出すこと（逐次実行にしない）。

### 3. Confrontation Matrix の生成

Red Team と Blue Team の結果を突き合わせ、以下の統合分析を行う:

- Red Team が発見した各脅威に対して、Blue Team の防御評価を照合する
- 防御が存在しない脅威（ギャップ）を特定する
- 各ギャップに対する推奨対策と優先度を決定する

### 4. 統合レポートの出力

以下の形式で統合レポートを出力する:

```markdown
## Security Review Report

### レビュー対象
- ファイル一覧

### Threat Analysis (Red Team)

<Red Team エージェントの出力をそのまま掲載>

### Defense Assessment (Blue Team)

<Blue Team エージェントの出力をそのまま掲載>

### Confrontation Matrix

Red Team の脅威と Blue Team の防御を突き合わせた統合分析:

| # | 脅威 (Red Team) | 深刻度 | 現在の防御 (Blue Team) | ギャップ | 推奨対策 | 対応優先度 |
|---|----------------|--------|----------------------|---------|---------|-----------|
| 1 | SQLi の可能性 | High | パラメータ化クエリ使用 | なし | - | - |
| 2 | Zip Slip | High | パス検証なし | あり | Path.GetFullPath で正規化 | Critical |

### Action Items

ギャップが存在する項目を優先度順にリストアップする:

1. **[Critical]** 項目名 — 対象ファイル:行番号 — 推奨対策
2. **[High]** 項目名 — 対象ファイル:行番号 — 推奨対策
3. **[Medium]** ...
4. **[Low]** ...

### Summary

- 分析した脅威の総数
- 防御が確認された脅威の数
- ギャップ（防御なし）の数
- Critical / High / Medium / Low の内訳
```

## 注意事項

- `run_in_background` は使用しない（結果を待って統合レポートを生成する）
- レビュー結果はあくまで静的分析に基づく指摘であり、すべてが実際に悪用可能とは限らない
- False positive の可能性がある場合は、その旨を明記する
