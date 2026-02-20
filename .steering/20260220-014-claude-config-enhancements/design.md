# Design: Claude Code 設定強化バッチ

## トラック A: Hooks + settings.json

### 新規作成: `.claude/settings.json`

```json
{
  "permissions": {
    "deny": [
      "Bash(rm -rf *)",
      "Read(.env)",
      "Read(.env.*)"
    ]
  },
  "hooks": {
    "PreToolUse": [...],
    "PostToolUse": [...],
    "Stop": [...]
  },
  "env": {
    "BASH_DEFAULT_TIMEOUT_MS": "120000"
  }
}
```

### Hook 定義

| フック | トリガー | 種別 | 処理内容 |
|--------|---------|------|---------|
| PreToolUse | Bash マッチャー | command | `git add .`, `git add -A`, `git push --force`, `rm -rf` をブロック |
| PreToolUse | Write/Edit マッチャー | command | `RescriptFlexLexer.java` への直接編集をブロック |
| PostToolUse | Write/Edit マッチャー (`.kt`) | command (async) | 変更ファイルのコンパイルチェック |

### Hook スクリプト

`.claude/hooks/` ディレクトリにシェルスクリプトを配置:
- `validate-bash.sh` — 危険コマンドブロック
- `validate-file-edit.sh` — 自動生成ファイル保護

スクリプトはブロック時に stderr にメッセージを出力し、exit 2 で Claude にフィードバック。

## トラック B: CLAUDE.md @import

CLAUDE.md の開発規約セクションを `@` 構文に置き換え:

```markdown
## 開発規約
...基本ルール...

@.claude/rules/testing.md
@.claude/rules/code-comments.md
@.claude/rules/git-conventions.md
@.claude/rules/steering-workflow.md
@.claude/rules/documentation.md
```

現在のテキスト参照リスト（5行）を `@` 構文（5行）に置き換えるだけ。行数変化なし。

## トラック C: Skills フロントマター改善

対象スキルと追加フィールド:

| スキル | 追加フィールド |
|--------|--------------|
| steering | `allowed-tools`, `disable-model-invocation: true` |
| git-workflow | `allowed-tools`, `disable-model-invocation: true` |
| fix-qodana | `allowed-tools`, `disable-model-invocation: true` |
| review-docs | `allowed-tools` |
| implementation-validator | `allowed-tools`, `context: fork` |
| development-guidelines | `allowed-tools` |
| add-feature | `allowed-tools` |
| prd-writing | `allowed-tools` |

## トラック D: Custom Sub-agents

### `.claude/agents/code-reviewer.md`

IntelliJ Plugin コード品質レビュー専門。ツールは Read, Grep, Glob のみ（変更不可）。モデルは sonnet（コスト効率）。

チェック項目:
- KDoc コメントの有無
- plugin.xml の Extension Point 登録
- テストファイルの存在
- RescriptFlexLexer.java への直接編集がないか
- パッケージ構成の遵守

### `.claude/agents/build-resolver.md`

Gradle ビルドエラー修正専門。ツールは Read, Grep, Glob, Bash。モデルは sonnet。

## 影響範囲

- `.claude/settings.json` — 新規作成
- `.claude/hooks/` — 新規ディレクトリ + スクリプト
- `CLAUDE.md` — 5行の参照形式変更
- `.claude/skills/*/SKILL.md` — フロントマター追加（8ファイル）
- `.claude/agents/` — 新規ディレクトリ + 2ファイル
- プラグインソースコード — 変更なし
