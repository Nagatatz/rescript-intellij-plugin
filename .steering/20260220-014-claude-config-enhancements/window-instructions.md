# Window Instructions: Claude Code 設定強化バッチ

## ウィンドウ A: Hooks + settings.json

```
cd /Users/ngtz/Documents/repos/rescript-wt-hooks-settings

ブランチ `feature/hooks-settings` で Hooks + settings.json を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260220-015-hooks-settings/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

要件概要:
- `.claude/settings.json` を新規作成（チーム共有設定）
  - `permissions.deny`: `.env` ファイル読み取り禁止（`Read(.env)`, `Read(.env.*)`）、`rm -rf *` 禁止
  - `env`: `BASH_DEFAULT_TIMEOUT_MS` を `120000` に設定
  - `hooks`: PreToolUse フック定義（下記スクリプト参照）
- `.claude/hooks/validate-bash.sh` を作成
  - stdin から JSON を読み取り、tool_input.command をチェック
  - ブロック対象: `git add .`, `git add -A`, `git push --force`, `rm -rf`
  - ブロック時: stderr にメッセージ出力、exit 2
  - 許可時: exit 0
- `.claude/hooks/validate-file-edit.sh` を作成
  - stdin から JSON を読み取り、tool_input.file_path をチェック
  - ブロック対象: `RescriptFlexLexer.java` への直接編集
  - ブロック時: stderr にメッセージ出力、exit 2
  - 許可時: exit 0

## ステップ 2: 実装
設計に従い実装。スクリプトには `chmod +x` を付与すること。

## ステップ 3: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。

## ステップ 4: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/claude-config-enhancements` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/claude-config-enhancements
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/hooks-settings
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-hooks-settings
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/hooks-settings

## ステップ 5: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## ウィンドウ B: CLAUDE.md @import

```
cd /Users/ngtz/Documents/repos/rescript-wt-claude-md-import

ブランチ `feature/claude-md-import` で CLAUDE.md の @import 構文化を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260220-016-claude-md-import/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

要件概要:
- CLAUDE.md の開発規約参照セクション（247-252行目付近）を `@` 構文に置き換え
- 現在の形式:

      詳細な規約は `.claude/rules/` を参照:
      - `.claude/rules/testing.md` — テスト規約
      - `.claude/rules/code-comments.md` — コードコメント規約（KDoc）
      - `.claude/rules/git-conventions.md` — Git コミット規約・ブランチ運用
      - `.claude/rules/steering-workflow.md` — ステアリングワークフロー・git worktree 運用
      - `.claude/rules/documentation.md` — ドキュメント管理・開発プロセス

- 変更後の形式:

      詳細な規約:

      @.claude/rules/testing.md
      @.claude/rules/code-comments.md
      @.claude/rules/git-conventions.md
      @.claude/rules/steering-workflow.md
      @.claude/rules/documentation.md

## ステップ 2: 実装
CLAUDE.md のテキスト参照リストを `@` 構文に置き換え。

## ステップ 3: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。

## ステップ 4: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/claude-config-enhancements` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/claude-config-enhancements
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/claude-md-import
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-claude-md-import
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/claude-md-import

## ステップ 5: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## ウィンドウ C: Skills フロントマター改善

```
cd /Users/ngtz/Documents/repos/rescript-wt-skills-frontmatter

ブランチ `feature/skills-frontmatter` で全スキルのフロントマター改善を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260220-017-skills-frontmatter/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

要件概要:
各スキルの SKILL.md フロントマター（YAML）に以下のフィールドを追加:

| スキル | 追加フィールド |
|--------|--------------|
| steering | `allowed-tools: [Read, Glob, Grep, Write, Edit, Bash]`, `disable-model-invocation: true` |
| git-workflow | `allowed-tools: [Read, Glob, Grep, Bash]`, `disable-model-invocation: true` |
| fix-qodana | `allowed-tools: [Read, Glob, Grep, Write, Edit, Bash]`, `disable-model-invocation: true` |
| review-docs | `allowed-tools: [Read, Glob, Grep, WebFetch, WebSearch]` |
| implementation-validator | `allowed-tools: [Read, Glob, Grep, Bash]`, `context: fork` |
| development-guidelines | `allowed-tools: [Read, Glob, Grep, Write, Edit]` |
| add-feature | `allowed-tools: [Read, Glob, Grep, Write, Edit, Bash]` |
| prd-writing | `allowed-tools: [Read, Glob, Grep, Write, Edit]` |

`allowed-tools` はそのスキルが本当に必要とするツールのみに制限する。
`disable-model-invocation: true` は副作用のあるスキル（Git操作、ファイル書き込みを伴うもの）に付与する。
`context: fork` は独立したコンテキストで実行すべきスキルに付与する。

## ステップ 2: 実装
各 SKILL.md のフロントマターを更新。

## ステップ 3: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。

## ステップ 4: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/claude-config-enhancements` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/claude-config-enhancements
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/skills-frontmatter
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-skills-frontmatter
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/skills-frontmatter

## ステップ 5: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## ウィンドウ D: Custom Sub-agents

```
cd /Users/ngtz/Documents/repos/rescript-wt-custom-agents

ブランチ `feature/custom-agents` で カスタムサブエージェントを実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260220-018-custom-agents/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

要件概要:

### `.claude/agents/code-reviewer.md`
IntelliJ Plugin コード品質レビュー専門エージェント。

フロントマター:
- `allowed-tools: [Read, Glob, Grep]`（読み取り専用、変更不可）
- `model: sonnet`（コスト効率）

プロンプト内容:
- KDoc コメントの有無チェック
- plugin.xml の Extension Point 登録確認
- テストファイルの存在確認
- RescriptFlexLexer.java への直接編集がないか確認
- パッケージ構成（`com.rescript.plugin.*`）の遵守確認
- レビュー結果をマークダウン表形式で出力

### `.claude/agents/build-resolver.md`
Gradle ビルドエラー修正専門エージェント。

フロントマター:
- `allowed-tools: [Read, Glob, Grep, Bash]`
- `model: sonnet`（コスト効率）

プロンプト内容:
- `./gradlew buildPlugin` のエラー出力を解析
- Kotlin コンパイルエラー、Gradle 設定エラー、依存関係エラーを分類
- 修正提案を具体的なコード変更として提示
- IntelliJ Platform API の互換性問題を検出

## ステップ 2: 実装
`.claude/agents/` ディレクトリを作成し、2つのエージェント定義ファイルを作成。

## ステップ 3: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。

## ステップ 4: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/claude-config-enhancements` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/claude-config-enhancements
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/custom-agents
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-custom-agents
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/custom-agents

## ステップ 5: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```
