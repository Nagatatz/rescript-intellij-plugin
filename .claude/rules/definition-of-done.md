# Definition of Done (DoD)

**以下は強制的な行動指示であり、例外なく従うこと。**

コードの変更を伴う作業を「完了」とみなすには、以下の 5 フェーズすべてを順に通過する必要がある。各フェーズのチェック項目は参照先のルールファイルで詳細を確認すること。

---

## Phase 1: 計画

ステアリングワークフローに従い、コードを書く前に完了させる。

- [ ] `.steering/[YYYYMMDD]-[NNN]-[開発タイトル]/` を作成した
- [ ] `requirements.md` を作成し、ユーザーの承認を得た
- [ ] `design.md` を作成し、ユーザーの承認を得た
- [ ] `tasklist.md` を作成し、ユーザーの承認を得た
- [ ] `EnterWorktree` で作業用 worktree に入った（軽微な修正を除く）

**詳細:** `.claude/rules/steering-workflow.md`

---

## Phase 2: 実装

承認された `tasklist.md` に従い、機能を実装する。

### コード品質

- [ ] すべての `class` / `object` / `enum class` / `sealed class` / `interface` に英語 KDoc (`/** ... */`) が付与されている
- [ ] KDoc がクラスの責務を 1〜3 文で説明している
- [ ] IntelliJ Extension Point を実装するクラスは、対応するインターフェース名に言及している

**詳細:** `.claude/rules/code-comments.md`

### テスト

- [ ] 新規・変更したすべてのクラスに対応する `<ClassName>Test.kt` が `src/test/` に存在する
- [ ] 免除対象（UI/LSP 結合/IDE ライフサイクル依存等）の場合、tasklist.md に省略理由を明記した
- [ ] すべてのテストがパスする

**詳細:** `.claude/rules/testing.md`

### tasklist.md リアルタイム更新

- [ ] タスク着手時に即座に `[ ]` → `[x]` に更新している
- [ ] コミットタスクは `[x]` 更新後にコミットしている（更新がコミットに含まれるように）

---

## Phase 3: コミット前

`git commit` を実行する **前に** 以下をすべて検証する。1 つでも不合格ならコミットせず、先に修正すること。

### 自己検証

- [ ] `./gradlew ktlintCheck` が成功する
- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] `./gradlew test` が成功する
- [ ] ビルド警告が新たに増加していない（既存警告は許容）

### ドキュメント同期

- [ ] `CLAUDE.md` — アーキテクチャセクション（レイヤー 3）に新機能が反映されている
- [ ] `README.md` — Features セクションに新機能が反映されている
- [ ] `sphinx-docs/user/features/` — 該当する機能ページに説明・使用例が記載されている
- [ ] `docs/product-requirements.md` — ロードマップ記載機能の場合、「実装済み」セクションに移動した

**詳細:** `.claude/rules/documentation.md`

### Extension Point 登録

- [ ] Extension Point を実装するクラスは `plugin.xml`（または `META-INF/rescript-*.xml`）に登録されている

**詳細:** `.claude/rules/plugin-xml-rules.md`

### Git コミット

- [ ] コミットは機能単位で分割されている（独立した機能は個別コミット）
- [ ] コミットメッセージに絵文字プレフィックスが付与されている
- [ ] `git add .` / `git add -A` ではなく、個別ファイル指定で `git add` している
- [ ] ステアリングファイル（tasklist.md の進捗更新等）がコミットに同梱されている

**詳細:** `.claude/rules/git-conventions.md`

### セキュリティ

- [ ] 外部入力（LSP レスポンス、ファイルパス、JSON 設定）はバリデーション済み
- [ ] コマンド実行は `ProcessBuilder` + 明示的引数リスト（文字列連結禁止）
- [ ] 絶対パスが UI やエラーメッセージに露出していない

---

## Phase 4: マージ前

すべてのタスクが完了し、ブランチを `main` にマージする前に確認する。

### ステアリング完了

- [ ] tasklist.md のすべてのタスクが `[x]` になっている
- [ ] requirements.md の受け入れ条件をすべて満たしている

### マージ確認

- [ ] `AskUserQuestion` でユーザーにマージ可否を確認した
- [ ] セキュリティに影響する変更がある場合、その旨をマージ確認時に明示した

---

## Phase 5: マージ後

`main` へのマージが完了したら、以下のクリーンアップを実行する。

- [ ] worktree 内から `main` にマージした（手順は `steering-workflow.md` の「worktree マージ・クリーンアップ手順」を参照）
- [ ] 作業ブランチを削除した
- [ ] セッションを終了した（worktree の自動クリーンアップを発動させる）

**詳細:** `.claude/rules/steering-workflow.md`

---

## 禁止事項

以下の行為は明示的に禁止する:

- `git add .` / `git add -A` による一括ステージング（個別ファイル指定を使うこと）
- `--no-verify` によるフック回避
- worktree 内での `git worktree remove` 実行（CWD が壊れる）
- tasklist.md を `[x]` に更新せずにコミットすること
- KDoc が欠けた状態でのコミット

---

## 例外

以下の変更は DoD の一部を免除してよい:

| 変更種別 | 免除フェーズ/項目 |
|---------|-----------------|
| タイポ修正・1 行の設定変更 | Phase 1 (ステアリング), Phase 2 テスト, Phase 3 ドキュメント同期 |
| ドキュメントのみの変更 | Phase 2 (コード品質/テスト), Phase 3 (EP 登録/セキュリティ) |
| ステアリングドキュメントのみ | Phase 2〜3 の全項目, Phase 4〜5 |
