# Requirements: Hooks + settings.json

## 概要

Claude Code のチーム共有設定ファイル（`.claude/settings.json`）と PreToolUse フックスクリプトを作成し、危険な操作を事前にブロックする仕組みを導入する。

## 機能要件

### 1. `.claude/settings.json`（チーム共有設定）

- `permissions.deny` で以下の操作を禁止:
  - `.env` ファイルの読み取り（`Read(.env)`, `Read(.env.*)`）
  - `rm -rf *` コマンドの実行
- `env` で環境変数を設定:
  - `BASH_DEFAULT_TIMEOUT_MS`: `"120000"`
- `hooks` で PreToolUse フックを定義:
  - Bash ツール使用時: `validate-bash.sh` を実行
  - Edit/Write ツール使用時: `validate-file-edit.sh` を実行

### 2. `.claude/hooks/validate-bash.sh`

- stdin から JSON を読み取り、`tool_input.command` を検査
- ブロック対象コマンド:
  - `git add .` — ステージング漏れ防止
  - `git add -A` — ステージング漏れ防止
  - `git push --force` — 強制プッシュ防止
  - `rm -rf` — 再帰的削除防止
- ブロック時: stderr にメッセージ出力、exit 2
- 許可時: exit 0

### 3. `.claude/hooks/validate-file-edit.sh`

- stdin から JSON を読み取り、`tool_input.file_path` を検査
- ブロック対象:
  - `RescriptFlexLexer.java` への直接編集（自動生成ファイルのため）
- ブロック時: stderr にメッセージ出力、exit 2
- 許可時: exit 0

## 非機能要件

- スクリプトは POSIX sh 互換で記述する
- スクリプトには実行権限（`chmod +x`）を付与する
- JSON パースには `jq` コマンドを使用する（macOS / Linux で標準的に利用可能）

## 受け入れ条件

- [ ] `.claude/settings.json` が正しい JSON 形式で作成されている
- [ ] `validate-bash.sh` がブロック対象コマンドを正しく検出する
- [ ] `validate-file-edit.sh` がブロック対象ファイルを正しく検出する
- [ ] すべてのスクリプトに実行権限が付与されている
