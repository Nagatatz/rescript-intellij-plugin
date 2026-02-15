---
description: Git操作を自動化する（ブランチ作成・絵文字付きコミット・PR作成・状態確認）
allowed-tools: Bash, Read, Glob, Grep
---

# Git ワークフロー

CLAUDE.md の Git コミット規約に準拠したブランチ作成、コミット、PR 作成を支援するスキル。

## 使い方

- `/git-workflow branch <名前>` — 命名規則に従うブランチを作成
- `/git-workflow commit` — 変更内容を分析し、絵文字付きコミットメッセージを生成・実行
- `/git-workflow commit -m "メッセージ"` — メッセージ指定 + 絵文字自動付与
- `/git-workflow pr` — `gh pr create` でプルリクエストを作成
- `/git-workflow status` — ブランチ状態 + コミット規約リマインダーを表示

引数なしの `/git-workflow` は `status` として動作する。

---

## モード: branch

### 目的

命名規則に従ったブランチを作成する。

### ブランチ命名規則

| プレフィックス | 用途 | 例 |
|--------------|------|-----|
| `feature/` | 新機能追加 | `feature/user-authentication` |
| `fix/` | バグ修正 | `fix/pdf-parse-error` |
| `refactor/` | リファクタリング | `refactor/repository-pattern` |
| `docs/` | ドキュメント更新 | `docs/update-architecture` |
| `test/` | テスト追加・修正 | `test/profit-loss-edge-cases` |
| `chore/` | 設定・依存関係等 | `chore/update-dependencies` |

### 手順

1. 引数からブランチ名を取得する
2. プレフィックスが含まれていない場合、ユーザーに確認する
3. 現在のブランチ状態を確認する:

```bash
git status
git branch --show-current
```

4. 未コミットの変更がある場合はユーザーに警告する
5. ブランチを作成・チェックアウトする:

```bash
git checkout -b <ブランチ名>
```

---

## モード: commit

### 目的

変更内容を分析し、CLAUDE.md の絵文字規約に従ったコミットメッセージでコミットする。

### 絵文字判定ルール

変更ファイルの差分内容から絵文字を自動判定する:

| 絵文字 | 判定条件 | 用途 |
|-------|---------|------|
| ✨ | 新しいファイル追加、または既存ファイルに新しい関数/コンポーネント/エンドポイントを追加 | 新機能追加 |
| 🐛 | 条件分岐の修正、例外処理の追加、既存ロジックの修正 | バグ修正 |
| ♻️ | 関数の抽出・統合、名前変更、構造変更（機能変更なし） | リファクタリング |
| 📝 | `.md` ファイルのみの変更、またはコメントのみの追加・修正 | ドキュメント更新 |
| 🎨 | `.res` ファイルのスタイル変更、CSS/レイアウト関連の変更 | UIやスタイルの改善 |
| ⚡ | クエリ最適化、キャッシュ追加、アルゴリズム改善 | パフォーマンス改善 |
| 🔧 | `.fsproj`、`rescript.json`、`package.json`、設定ファイルの変更 | 設定ファイルの変更 |
| ✅ | テストファイル（`Tests/` 配下）の追加・修正 | テスト追加・修正 |
| 🗑️ | ファイル削除、不要コードの除去（コード量が純減） | 不要コード削除 |

**判定優先順位**: 複数の絵文字が該当する場合、上の表の優先順位に従う（✨ が最優先）。

### 手順

#### 1. 変更内容の確認

```bash
git status
git diff
git diff --cached
```

#### 2. ステージング

**MUST**: `git add .` や `git add -A` は使用しない。個別ファイル指定で `git add` する。

変更ファイルを分析し、以下を除外する:
- `.env` / `credentials.json` 等の秘密情報ファイル
- `node_modules/` / `bin/` / `obj/` 等のビルド成果物
- `*.res.mjs` 等の ReScript コンパイル出力

```bash
git add src/StockTaxApp.Core/Database/Repository.fs
git add database/schema.sql
```

#### 3. コミットメッセージ生成

`-m` オプションでメッセージが指定されている場合:
- 指定メッセージに絵文字を自動付与する（既に絵文字がある場合は付与しない）
- 例: `-m "Add user authentication"` → `✨ Add user authentication`

メッセージが指定されていない場合:
- 差分内容を分析し、英語でコミットメッセージを生成する
- フォーマット: `<絵文字> <動詞で始まる簡潔な説明>`
- 例: `✨ Add monthly interest management feature`
- 例: `🐛 Fix PDF parsing error for SMBC statements`

#### 4. コミット実行

```bash
git commit -m "$(cat <<'EOF'
✨ Add monthly interest management feature

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

#### 5. push に関するルール

**NEVER**: ユーザーから明示的に指示がない限り、`git push` は実行しない。

コミット完了後、push が必要かどうかをユーザーに確認するメッセージを表示する:

```
コミットが完了しました。push が必要な場合はお知らせください。
```

---

## モード: pr

### 目的

`gh` CLI を使用してプルリクエストを作成する。

### 手順

#### 1. 現在の状態確認

```bash
git branch --show-current
git log --oneline master..HEAD
git diff master...HEAD --stat
```

#### 2. PR タイトルの生成

- コミット履歴を分析し、変更の主目的を特定する
- 70文字以内のタイトルを生成する
- 絵文字は PR タイトルには含めない

#### 3. PR 本文の生成

以下のフォーマットで PR 本文を作成する:

```markdown
## Summary
- 変更の概要（1〜3行）

## Changes
- 変更されたファイルとその内容

## Test plan
- [ ] `dotnet test` が全件パス
- [ ] `pnpm run res:build` がエラーなし
- [ ] 手動テスト: （具体的なテスト項目）

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

#### 4. リモートへの push

PR 作成前にリモートへ push する（ユーザーに確認後）:

```bash
git push -u origin <ブランチ名>
```

#### 5. PR 作成

```bash
gh pr create --title "タイトル" --body "$(cat <<'EOF'
本文
EOF
)"
```

作成された PR の URL を表示する。

---

## モード: status

### 目的

現在のブランチ状態とコミット規約のリマインダーを表示する。

### 手順

#### 1. 状態表示

以下のコマンドを実行し、結果を整形して表示する:

```bash
git branch --show-current
git status --short
git log --oneline -5
git stash list
```

#### 2. レポート

```
## Git 状態

**ブランチ**: feature/add-interest
**未コミット変更**: 3ファイル
**最新コミット**: ✨ Add monthly interest table (2分前)

### 最近のコミット
1. ✨ Add monthly interest table
2. 🐛 Fix PDF parsing error
3. ♻️ Refactor repository pattern

### 未コミット変更
- M  src/StockTaxApp.Core/Database/Repository.fs
- M  database/schema.sql
- ?? src/StockTaxApp.Core/Domain/NewType.fs

### コミット規約リマインダー
| 絵文字 | 用途 |
|-------|------|
| ✨ | 新機能追加 |
| 🐛 | バグ修正 |
| ♻️ | リファクタリング |
| 📝 | ドキュメント更新 |
| 🎨 | UIやスタイルの改善 |
| ⚡ | パフォーマンス改善 |
| 🔧 | 設定ファイルの変更 |
| ✅ | テスト追加・修正 |
| 🗑️ | 不要コード削除 |
```
