# Design: Claude Code 設定最適化

## 設計方針

CLAUDE.md を「毎セッション必須の情報」と「必要時に参照するルール」に分離し、トークン消費を削減する。

## 変更内容

### 1. CLAUDE.md の分離

**CLAUDE.md に残す内容（毎セッション必要）:**
- プロジェクト概要（言語、ビルドシステム、JDK）
- ビルド・実行コマンド
- プロジェクト構成（ファイルツリー）
- アーキテクチャ（レイヤー説明）
- 開発規約（基本ルールのみ: パッケージ、plugin.xml、レクサー更新ルール）
- 重要な注意事項（自動生成ファイル、LSP、Gradle）
- エージェント実行ルール

**`.claude/rules/` に分離する内容:**

| ファイル | 内容 | 元の CLAUDE.md セクション |
|---------|------|------------------------|
| `testing.md` | テスト規約 | 「テスト規約」セクション |
| `code-comments.md` | コードコメント規約（KDoc） | 「コードコメント規約」セクション |
| `git-conventions.md` | コミット規約 + ブランチ規則 + 命名規則 | 「Git コミット規約」〜「ブランチ命名規則」 |
| `steering-workflow.md` | ステアリングワークフロー + worktree 運用 | 「実装前の必須プロセス」〜「命令文のテンプレート」 |
| `documentation.md` | ドキュメント分類 + 開発プロセス + 図表ルール | 「ドキュメントの分類」〜「注意事項」 |

### 2. permissions の整理

**現状（160行）→ 統合後（約20行）:**

```json
{
  "permissions": {
    "allow": [
      "Bash(./gradlew:*)",
      "Bash(git:*)",
      "Bash(git -C:*)",
      "Bash(gh:*)",
      "Bash(make:*)",
      "Bash(node:*)",
      "Bash(npx:*)",
      "Bash(uv:*)",
      "Bash(python3:*)",
      "Bash(ls:*)",
      "Bash(open:*)",
      "Bash(chmod:*)",
      "Bash(du:*)",
      "Bash(wc:*)",
      "Bash(test:*)",
      "WebSearch",
      "WebFetch",
      "mcp__context7__resolve-library-id",
      "mcp__context7__query-docs",
      "mcp__ide__getDiagnostics",
      "Skill(steering)",
      "Skill(fix-qodana)"
    ]
  }
}
```

**統合の考え方:**
- `Bash(git:*)` で `git add`, `git commit`, `git -C ...`, `git worktree` 等をすべてカバー
- `Bash(gh:*)` で `gh api`, `gh run`, `gh repo` 等をすべてカバー
- `Bash(./gradlew:*)` で全 Gradle タスクをカバー（worktree の絶対パス指定は都度許可）
- `WebFetch` でドメイン制限を撤廃（全ドメイン許可）
- 個別の `jar`, `for`, `xargs` 等の ad-hoc コマンドは削除（必要時に都度許可）

## 影響範囲

- `CLAUDE.md` — 大幅削減（825行 → 約150行）
- `.claude/rules/` — 5ファイル新規作成
- `.claude/settings.local.json` — permissions セクション書き換え
- プラグインソースコード — 変更なし
