# Design: CLAUDE.md @import 構文化

## 変更内容

CLAUDE.md の開発規約セクション（247-252行目）を以下のように置き換える。

### Before

```
詳細な規約は `.claude/rules/` を参照:
- `.claude/rules/testing.md` — テスト規約
- `.claude/rules/code-comments.md` — コードコメント規約（KDoc）
- `.claude/rules/git-conventions.md` — Git コミット規約・ブランチ運用
- `.claude/rules/steering-workflow.md` — ステアリングワークフロー・git worktree 運用
- `.claude/rules/documentation.md` — ドキュメント管理・開発プロセス
```

### After

```
詳細な規約:

@.claude/rules/testing.md
@.claude/rules/code-comments.md
@.claude/rules/git-conventions.md
@.claude/rules/steering-workflow.md
@.claude/rules/documentation.md
```

## 影響範囲

- CLAUDE.md のみ変更
- ソースコード変更なし
- テスト不要
