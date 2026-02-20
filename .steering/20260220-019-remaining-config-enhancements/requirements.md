# Requirements: 残りの Claude Code 設定強化

## 概要

前バッチで未実装だった 5 つの改善項目を実装する。

## 改善項目

### 1. Path-specific Rules

`.claude/rules/` のルールファイルに YAML フロントマター `paths` フィールドを追加し、特定ファイルにのみ適用されるようスコープを絞る。

対象ルールとパス:
- `testing.md` — `src/**/*.kt`（テスト対象の Kotlin ソースのみ）
- `code-comments.md` — `src/main/**/*.kt`（メインソースの Kotlin のみ）
- `git-conventions.md` — パス制限なし（Git 操作は全ファイル共通）
- `steering-workflow.md` — パス制限なし（ワークフローは全ファイル共通）
- `documentation.md` — `**/*.md`, `docs/**`, `.steering/**`（ドキュメントファイルのみ）

### 2. PostToolUse Hook

Kotlin ファイル (`.kt`) の Write/Edit 後に非同期でビルドチェックを実行する。

- マッチャー: `Write|Edit`
- 条件: ファイルパスが `.kt` で終わる場合のみ
- 処理: `./gradlew compileKotlin` を非同期実行（結果を stdout に出力）
- タイムアウト: 120秒

### 3. Stop Hook

Claude が停止する前に `tasklist.md` の未完了タスクをチェックする。

- マッチャー: `.*`
- 処理: カレントディレクトリの `.steering/` 配下で最新の `tasklist.md` を検索し、`[ ]` が残っていれば警告
- exit 0 で警告のみ（ブロックはしない）

### 4. SessionStart Hook

セッション開始時に開発環境の状態を表示する。

- マッチャー: `.*`
- 処理: JDK バージョン、Node.js バージョン、git ブランチ名を stdout に出力
- exit 0（情報提供のみ）

### 5. MCP サーバー設定

プロジェクトルートに `.mcp.json` を作成し、既存の Context7 MCP サーバー設定をプロジェクトスコープで共有可能にする。

## 受け入れ条件

- [ ] ルールファイルに `paths` フロントマターが追加されていること
- [ ] PostToolUse hook が `.kt` ファイル変更時に動作すること
- [ ] Stop hook が未完了タスクを検出して警告すること
- [ ] SessionStart hook が環境情報を出力すること
- [ ] `.mcp.json` が存在すること
