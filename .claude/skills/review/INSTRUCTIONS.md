# Review スキル

直近のコード変更に対してプロジェクト規約準拠のレビューを実行します。

## 使い方

- `/review` — 未コミットの変更をレビュー
- `/review HEAD~3..HEAD` — 指定範囲のコミットをレビュー
- `/review src/main/kotlin/.../SomeClass.kt` — 特定ファイルをレビュー

## 手順

### 1. レビュー対象の特定

引数に応じてレビュー対象を決定する:

- **引数なし**: `git diff --name-only` + `git diff --cached --name-only` で変更ファイルを取得
- **コミット範囲** (例: `HEAD~3..HEAD`): `git diff --name-only <範囲>` で変更ファイルを取得
- **ファイルパス**: 指定されたファイルをそのまま対象とする

`.kt` ファイルのみをフィルタする（設定ファイルやドキュメントは除外）。

### 2. code-reviewer エージェントの起動

Task ツールで `.claude/agents/code-reviewer.md` のサブエージェントを起動する:

```
Task(subagent_type="code-reviewer", prompt="Review the following files for project convention compliance: <ファイルリスト>")
```

**注意**: `run_in_background` は使用しない（結果を待って報告する）。

### 3. セルフレビューの実行

code-reviewer エージェントの結果に加え、以下の追加チェックを自分で実行する:

- **変更の意図**: `git log --oneline` で直近のコミットメッセージを確認し、変更が意図通りか検証
- **ビルド確認**: `./gradlew compileKotlin --quiet` でコンパイルが通ることを確認
- **テスト確認**: 変更ファイルに対応するテストファイルが存在するか Glob で確認

### 4. レビュー結果の報告

以下の形式で統合レポートを出力する:

```
## Code Review Report

### Automated Checks (code-reviewer agent)
<エージェントの出力テーブル>

### Additional Checks
| Check | Status | Details |
|-------|--------|---------|
| Build | PASS/FAIL | ... |
| Tests exist | PASS/WARN | ... |
| Commit messages | PASS/WARN | ... |

### Recommendations
- <優先度順の改善提案>
```
