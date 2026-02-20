# Requirements: Claude Code 設定強化バッチ

## 概要

Claude Code 公式ドキュメントのベストプラクティスに基づき、4つの独立した改善を並列実装する。

## 改善項目

### A: Hooks + settings.json（1+2 統合）

- `.claude/settings.json` を新規作成（チーム共有設定）
- `PreToolUse` フック: `git add .`, `git push --force`, `rm -rf` 等の危険コマンドをブロック
- `PostToolUse` フック: Kotlin ファイル変更時の非同期ビルド検証
- `Stop` フック: tasklist.md 全タスク完了チェック
- `env` 設定: `BASH_DEFAULT_TIMEOUT_MS` を Gradle ビルド向けに拡張
- `deny` ルール: `.env` ファイル読み取り禁止

### B: CLAUDE.md @import

- CLAUDE.md の開発規約セクションで `.claude/rules/` を `@` 構文で参照
- 現在のテキスト参照リストを `@` 構文に置き換え

### C: Skills フロントマター改善

- 全スキルに `allowed-tools` を追加
- 副作用のあるスキル（steering, git-workflow）に `disable-model-invocation: true` を追加
- implementation-validator に `context: fork` を追加

### D: Custom Sub-agents

- `.claude/agents/code-reviewer.md` を作成（KDoc、plugin.xml、テストカバレッジチェック）
- `.claude/agents/build-resolver.md` を作成（Gradle ビルドエラー修正）

## 受け入れ条件

- [ ] `.claude/settings.json` が存在し、hooks と deny ルールが定義されていること
- [ ] CLAUDE.md が `@` 構文で rules を参照していること
- [ ] 全スキルのフロントマターが改善されていること
- [ ] `.claude/agents/` に 2 つのエージェント定義が存在すること
