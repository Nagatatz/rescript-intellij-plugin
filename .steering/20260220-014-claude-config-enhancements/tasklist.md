# Tasklist: Claude Code 設定強化バッチ

## フェーズ 0: バッチブランチ準備

- [x] `feature/claude-config-enhancements` バッチブランチを作成
- [x] ステアリングドキュメントをバッチブランチにコミット
- [x] 4 トラック用の git worktree を作成

## トラック A: Hooks + settings.json（worktree: `rescript-wt-hooks-settings`）

- [x] `.claude/settings.json` を新規作成（permissions.deny + env）
- [x] `.claude/hooks/validate-bash.sh` を作成（危険コマンドブロック）
- [x] `.claude/hooks/validate-file-edit.sh` を作成（自動生成ファイル保護）
- [x] `.claude/settings.json` に hooks 定義を追加
- [x] tasklist.md を更新してコミット

## トラック B: CLAUDE.md @import（worktree: `rescript-wt-claude-md-import`）

- [x] CLAUDE.md のテキスト参照リスト（5行）を `@` 構文（5行）に置き換え
- [x] tasklist.md を更新してコミット

## トラック C: Skills フロントマター改善（worktree: `rescript-wt-skills-frontmatter`）

- [x] `steering/SKILL.md` に `allowed-tools`, `disable-model-invocation: true` を追加
- [x] `git-workflow/SKILL.md` に `allowed-tools`, `disable-model-invocation: true` を追加
- [x] `fix-qodana/SKILL.md` に `allowed-tools`, `disable-model-invocation: true` を追加
- [x] `review-docs/SKILL.md` に `allowed-tools` を追加
- [x] `implementation-validator/SKILL.md` に `allowed-tools`, `context: fork` を追加
- [x] `development-guidelines/SKILL.md` に `allowed-tools` を追加
- [x] `add-feature/SKILL.md` に `allowed-tools` を追加
- [x] `prd-writing/SKILL.md` に `allowed-tools` を追加
- [x] tasklist.md を更新してコミット

## トラック D: Custom Sub-agents（worktree: `rescript-wt-custom-agents`）

- [x] `.claude/agents/code-reviewer.md` を作成
- [x] `.claude/agents/build-resolver.md` を作成
- [x] tasklist.md を更新してコミット

## フェーズ 1: マージ・検証

- [x] 全トラックをバッチブランチにマージ
- [x] 受け入れ条件の検証（settings.json, @import, frontmatter, agents）
- [x] `window-instructions.md` を作成（各ウィンドウへの命令文）
- [x] tasklist.md を更新してバッチブランチにコミット
- [x] main にマージして worktree を削除
