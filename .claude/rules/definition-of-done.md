# Definition of Done (DoD)

**以下は強制的な行動指示であり、例外なく従うこと。**

コードの変更を伴う作業を「完了」とみなすには、以下の 5 フェーズすべてを順に通過する必要がある。各フェーズのチェック項目の詳細は、リンク先の正本ルール（canonical rule）を参照すること。DoD は各ルールへの索引であり、ルール本文を重複させない。

---

## Phase 1: 計画

コードを書く前に、Issue ワークフローに従って計画を記録する。

→ `.claude/rules/issue-workflow.md` の全手順（Issue の確認・起票、DoD チェックリストの記載、`EnterWorktree`）

---

## Phase 2: 実装

Issue に記載した DoD チェックリストに従い、機能を実装する。

- コード品質（KDoc） → `.claude/rules/code-comments.md`
- テスト配置・命名・免除基準 → `.claude/rules/testing.md`
- Deprecated API 回避 → `.claude/rules/deprecated-api.md`
- JFlex レクサー編集時の制約 → `.claude/rules/flex-rules.md`
- Extension Point 登録 → `.claude/rules/plugin-xml-rules.md`

### Issue DoD のリアルタイム更新（DoD-owned）

- [ ] 項目が完了したら即座に Issue 上の DoD チェックボックスを `[ ]` → `[x]` に更新している
- [ ] DoD を記載した場所（Issue 本文、または DoD コメント）を編集して更新し、進捗コメントを積み増していない

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

→ `.claude/rules/git-conventions.md`（絵文字プレフィックス / 機能単位の粒度 / ブランチ運用）

### セキュリティ

→ `CLAUDE.md` のセキュリティセクション（LSP レスポンスのバリデーション / `ProcessBuilder` + 明示的引数 / 絶対パスの露出禁止）

---

## Phase 4: マージ前

すべてのタスクが完了し、ブランチを `main` にマージする前に確認する。

### Issue DoD 完了（DoD-owned）

- [ ] Issue の DoD のすべての項目（マージ確認項目を除く）が `[x]` になっている
- [ ] DoD の「受け入れ条件」をすべて満たしている

### マージ確認（DoD-owned）

- [ ] `AskUserQuestion` でユーザーにマージ可否を確認した
- [ ] セキュリティに影響する変更がある場合、その旨をマージ確認時に明示した

マージ手順自体は → `.claude/rules/issue-workflow.md` の「worktree マージ・クリーンアップ手順」

---

## Phase 5: マージ後

`main` へのマージが完了したら、worktree のクリーンアップを実行する。

→ `.claude/rules/issue-workflow.md` の「worktree マージ・クリーンアップ手順」および「残存 worktree の手動クリーンアップ」

---

## 禁止事項（DoD-owned）

以下の行為は明示的に禁止する:

- `git add .` / `git add -A` による一括ステージング（個別ファイル指定を使うこと）
- `--no-verify` によるフック回避
- worktree 内での `git worktree remove` 実行（CWD が壊れる）
- Issue の DoD を更新せずに完了を宣言すること
- KDoc が欠けた状態でのコミット

---

## 例外（DoD-owned — フェーズ横断の免除マトリクス）

以下の変更は DoD の一部を免除してよい。Issue 起票要否・`main` 直接コミット可否・DoD 免除の 3 軸で整理する。Issue 省略の詳細定量基準は下表の「軽微な修正」行を参照。

| 変更種別 | Issue 省略 | main 直接コミット可 | 免除される DoD フェーズ/項目 |
|---------|:--------------:|:-----------------:|--------------------------|
| **軽微な修正** — 以下の定量基準を **すべて** 満たすこと: ① 変更ファイル数 ≤ 3 ② 変更行数（追加+削除）≤ 50 ③ 新規クラス・ファイルなし ④ 新規 EP なし ⑤ public API 不変。典型例: タイポ修正・1行の設定変更・コメント追加・定数値修正 | ○ | ○ | Phase 1（Issue 起票）、Phase 2 テスト、Phase 3 ドキュメント同期 |
| **ドキュメントのみの変更**（CLAUDE.md / `docs/` / `.claude/` 配下、Kotlin ソースなし） | △（定量基準を満たす場合のみ） | ○ | Phase 2（コード品質/テスト）、Phase 3（EP 登録/セキュリティ） |
