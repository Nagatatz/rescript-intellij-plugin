# Design: Claude 設定ダイエット

## 方針

「何をすべきか」（原則）は Rules に残し、「どうやるか」（手順・テンプレート）は Skills に移動する。CLAUDE.md は参照ハブとして最小化する。

---

## A1: CLAUDE.md ダイエット（466行 → 200行以下）

### 削除・移動する内容

| セクション | 現在行数 | 対応 |
|-----------|---------|------|
| プロジェクト構成ツリー（`src/main/` 以下の全ファイル一覧） | ~150行 | `@docs/repository-structure.md` への `@` 参照に置換 |
| ロードマップ（Phase 2-4 + C/D 優先度の全テーブル） | ~100行 | `@docs/product-requirements.md` への `@` 参照に置換 |
| Rules 詳細参照（testing, code-comments, git-conventions, steering-workflow, documentation） | ~5行 | 現行維持（`@` 参照で良い） |

### 残す内容の構成（目標180行）

```
# CLAUDE.md                                    (~5行)
## プロジェクト概要                              (~10行) — 言語/ビルド/プラットフォーム
## ビルド・実行コマンド                          (~15行) — 変更なし
## プロジェクト構成                              (~5行)  — @docs/repository-structure.md 参照のみ
## アーキテクチャ                                (~40行) — 3レイヤー概要（現行から変更なし）
## 開発規約                                      (~10行) — Rules 参照（現行から変更なし）
## 重要な注意事項                                (~10行) — 変更なし
## コンテキスト管理 (NEW - A3)                    (~10行)
## セキュリティ (NEW - A4)                        (~10行)
## ロードマップ                                  (~5行)  — @docs/product-requirements.md 参照のみ
```

### 新設セクション

**コンテキスト管理（A3）:**
```markdown
## コンテキスト管理

When compacting, always preserve:
- Current working branch and worktree path
- The active .steering/ directory path and current tasklist.md progress
- List of modified/created files in the current session
- Build errors or test failures encountered
- Task ツール（サブエージェント）を使用する場合、`run_in_background` は明示的に指示された場合のみ使用すること
```

**セキュリティ（A4）:**
```markdown
## セキュリティ

- Validate all external inputs (LSP server responses, file system paths, JSON config parsing)
- Use ProcessBuilder with explicit argument lists for external process execution; never concatenate user input into command strings
- Never expose absolute file system paths in user-facing UI elements or error messages
- Sanitize file paths from LSP responses before using in file operations
```

---

## A2: Rules スリム化

### steering-workflow.md（229行 → 45行）

**残す内容（原則のみ）:**
- 1-14行: 必須プロセスの6ステップ（そのまま）
- 16-21行: tasklist.md 更新ルール（要約）
- 22-31行: ドキュメント更新ルール（要約）
- 33-38行: 禁止事項・例外（そのまま）
- 40-59行: 調査タスクのルール（要約して5行に）
- 61-70行: worktree 運用の原則（要約して5行に）

**Skills に移動する内容:**
- 72-91行: 単一機能実装の手順 → `steering` Skill に移動
- 92-168行: 並列実装の手順・ブランチ戦略 → `steering` Skill に移動
- 149-215行: worktree 命名規則・命令文テンプレート → `steering` Skill に移動
- 217-229行: ステアリングディレクトリ命名規則 → `steering` Skill に既にある（重複）

### documentation.md（237行 → 40行）

**残す内容（原則のみ）:**
- ドキュメント分類の定義（永続 `docs/` vs 作業 `.steering/`）— 10行に要約
- ドキュメント管理の原則 — 5行
- 図表ルール: draw.io MCP 必須の1行ルール — 5行
- 注意事項 — 5行

**Skills に移動する内容:**
- 12-60行: `docs/` 配下の各ファイル説明 → `review-docs` Skill に移動
- 83-167行: 開発プロセス手順（初回セットアップ、機能追加） → `steering` Skill に移動
- 195-225行: draw.io ツール選択表・禁止事項詳細 → 新規 Rule `diagram-rules.md` (globs: `docs/**`, `.steering/**`) に移動

### 移動先の詳細

**`steering` Skill (SKILL.md) への追記:**
- worktree 運用手順（単一・並列）
- 並列実装のブランチ戦略
- 命令文テンプレート
- 開発プロセス手順（初回セットアップ、機能追加フロー）

**`review-docs` Skill (SKILL.md) への追記:**
- `docs/` 配下の各ファイルの役割一覧

**新規 Rule `.claude/rules/diagram-rules.md`:**
```yaml
---
globs: ["docs/**/*.md", ".steering/**/*.md"]
---
```
- draw.io MCP ツール選択表
- 禁止事項の詳細（ASCII アート、Mermaid コードブロック）

---

## 変更の影響

- **ビルドへの影響:** なし（設定ファイルのみの変更）
- **既存の指示内容:** 全て外部ファイルまたは Skills に移動。情報の喪失なし
- **既存 Hooks:** 変更なし
- **既存 Settings:** 変更なし
