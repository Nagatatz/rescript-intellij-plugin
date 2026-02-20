# Tasklist: Custom Agents

## タスク

- [x] `.claude/agents/` ディレクトリを作成
- [x] `code-reviewer.md` エージェント定義ファイルを作成
- [x] `build-resolver.md` エージェント定義ファイルを作成
- [x] コミット: `✨ Add custom agents for code review and build resolution`
- [x] バッチブランチ `feature/claude-config-enhancements` にマージして worktree を削除

## テスト省略理由

カスタムエージェント定義ファイル（`.claude/agents/*.md`）は Claude Code の設定ファイルであり、Kotlin ソースコードではないため、ユニットテストの対象外。
