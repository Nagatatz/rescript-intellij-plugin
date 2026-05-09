# Definition of Done (DoD)

**以下は強制的な行動指示であり、例外なく従うこと。**

コードの変更を伴う作業を「完了」とみなすには、以下の 5 フェーズすべてを順に通過する必要がある。各フェーズのチェック項目の詳細は、リンク先の正本ルール（canonical rule）を参照すること。DoD は各ルールへの索引であり、ルール本文を重複させない。

---

## Phase 1: 計画

コードを書く前に、ステアリングワークフローに従って計画を完了させる。

→ `.claude/rules/steering-workflow.md` の全手順（`.steering/[YYYYMMDD]-[NNN]-[開発タイトル]/` 作成、requirements / design / tasklist の承認、`EnterWorktree`）

---

## Phase 2: 実装

承認された `tasklist.md` に従い、機能を実装する。

- コード品質（KDoc） → `.claude/rules/code-comments.md`
- テスト配置・命名・免除基準 → `.claude/rules/testing.md`
- Deprecated API 回避 → `.claude/rules/deprecated-api.md`
- JFlex レクサー編集時の制約 → `.claude/rules/flex-rules.md`
- Extension Point 登録 → `.claude/rules/plugin-xml-rules.md`

### tasklist.md リアルタイム更新（DoD-owned）

- [ ] タスク着手時に即座に `[ ]` → `[x]` に更新している
- [ ] コミットタスクは `[x]` 更新後にコミットしている（更新がコミットに含まれるように）

---

## Phase 3: コミット前

`git commit` を実行する **前に** 以下をすべて検証する。1 つでも不合格ならコミットせず、先に修正すること。

### リポジトリ状態の事前検証（DoD-owned）

リポジトリの状態をユーザーに報告する前、または状態に基づいて意思決定する前に、必ず実コマンドで確認すること。**推測で発言してはならない**。

- [ ] 「commit が pushed/unpushed」「branch が ahead/behind」を述べる前に `git status` と `git log --oneline origin/<branch>..HEAD` を実行し、その出力を返答内に引用する
- [ ] 依存パッケージのバージョン・ロックファイル状態に言及する前に該当ファイル（`gradle.properties`, `package.json`, ロックファイル等）を `Read` で確認する
- [ ] 「このコミットは既にある／ない」を述べる前に `git log --grep` または `git log --oneline -- <path>` で実証する

**理由:** 過去に「unpushed と誤って報告」「`@types/node` の bump を未確定と誤認」など、未検証の状態主張による無駄な往復が発生している。**主張する前に検証する**。

### 自己検証（DoD-owned — CI ゲートの正本）

- [ ] `./gradlew ktlintCheck` が成功する
- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] `./gradlew test` が成功する
- [ ] ビルド警告が新たに増加していない（既存警告は許容）
- [ ] Deprecated API 利用がある場合、`@Suppress` と `plugin-verifier-ignored-problems.txt` の両方が揃っている → `.claude/rules/deprecated-api.md`

### ドキュメント同期

→ `.claude/rules/documentation.md` の「機能実装時のドキュメント更新」と「日本語訳の同時更新」

### Git コミット

→ `.claude/rules/git-conventions.md`（絵文字プレフィックス / 機能単位の粒度 / 個別ファイル指定 / ステアリング同梱）

### セキュリティ

→ `CLAUDE.md` のセキュリティセクション（LSP レスポンスのバリデーション / `ProcessBuilder` + 明示的引数 / 絶対パスの露出禁止）

---

## Phase 4: マージ前

すべてのタスクが完了し、ブランチを `main` にマージする前に確認する。

### ステアリング完了（DoD-owned）

- [ ] tasklist.md のすべてのタスクが `[x]` になっている
- [ ] requirements.md の受け入れ条件をすべて満たしている

### マージ確認（DoD-owned）

- [ ] `AskUserQuestion` でユーザーにマージ可否を確認した
- [ ] セキュリティに影響する変更がある場合、その旨をマージ確認時に明示した

マージ手順自体は → `.claude/rules/steering-workflow.md` の「worktree マージ・クリーンアップ手順」

---

## Phase 5: マージ後

`main` へのマージが完了したら、worktree のクリーンアップを実行する。

→ `.claude/rules/steering-workflow.md` の「worktree マージ・クリーンアップ手順」および「残存 worktree の手動クリーンアップ」

---

## 禁止事項（DoD-owned）

以下の行為は明示的に禁止する:

- `git add .` / `git add -A` による一括ステージング（個別ファイル指定を使うこと）
- `--no-verify` によるフック回避
- worktree 内での `git worktree remove` 実行（CWD が壊れる）
- tasklist.md を `[x]` に更新せずにコミットすること
- KDoc が欠けた状態でのコミット

---

## 例外（DoD-owned — フェーズ横断の免除マトリクス）

以下の変更は DoD の一部を免除してよい:

| 変更種別 | 免除フェーズ/項目 |
|---------|-----------------|
| タイポ修正・1 行の設定変更 | Phase 1 (ステアリング), Phase 2 テスト, Phase 3 ドキュメント同期 |
| ドキュメントのみの変更 | Phase 2 (コード品質/テスト), Phase 3 (EP 登録/セキュリティ) |
| ステアリングドキュメントのみ | Phase 2〜3 の全項目, Phase 4〜5 |
