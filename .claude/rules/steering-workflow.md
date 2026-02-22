# ステアリングワークフロー

**以下は強制的な行動指示であり、例外なく従うこと。**

コードの変更を伴う指示を受けた場合、**コードを1行も書く前に**以下を実行すること:

1. `.steering/[YYYYMMDD]-[NNN]-[開発タイトル]/` ディレクトリを作成する
2. `requirements.md` を作成し、ユーザーの承認を得る
3. `design.md` を作成し、ユーザーの承認を得る
4. `tasklist.md` を作成し、ユーザーの承認を得る
5. 承認された `tasklist.md` に従って実装を進める
6. 実装完了後、ビルドが通ることを確認し、適切な粒度でコミットする

## tasklist.md 更新ルール

- タスク着手時に即座に `[ ]` → `[x]` に更新する
- コミットタスクは `[x]` 更新後にコミットする（更新がコミットに含まれるように）
- マージタスクは `[x]` 更新をマージ前の最終コミットに含める
- ドキュメント更新（CLAUDE.md, README.md, docs/, sphinx-docs/）は該当コードのコミットに含める

## コミット前検証チェックリスト

**以下は強制的な行動指示であり、例外なく従うこと。**

`git commit` を実行する前に、以下の5項目をすべて検証すること。1つでも不合格の場合はコミットせず、先に修正すること。

### 1. KDoc コメント

新規作成・変更したすべての `.kt` ファイルについて:

- [ ] すべての `class` / `object` / `enum class` / `sealed class` / `interface` に英語 KDoc (`/** ... */`) があるか
- [ ] KDoc がクラスの責務を 1〜3 文で説明しているか

詳細: `.claude/rules/code-comments.md`

### 2. テスト

新規作成したすべての `.kt` ファイルについて:

- [ ] `src/test/` に対応する `<ClassName>Test.kt` が存在するか
- [ ] 免除対象（UI/LSP結合）の場合、tasklist.md に省略理由を明記したか

詳細: `.claude/rules/testing.md`

### 3. ドキュメント同期

新しい機能・変更が以下のドキュメントに反映されているか:

- [ ] `CLAUDE.md` — アーキテクチャセクション（レイヤー 3）
- [ ] `README.md` — Features セクション
- [ ] `sphinx-docs/user/features/` — 該当する機能ページ
- [ ] `docs/product-requirements.md` — 実装済み機能セクション（ロードマップ記載機能の場合）

詳細: `.claude/rules/documentation.md`

### 4. plugin.xml 登録

Extension Point を実装するクラスを追加した場合:

- [ ] `plugin.xml`（または `META-INF/rescript-*.xml`）に登録されているか

### 5. tasklist.md 進捗

- [ ] 完了したタスクが `[x]` に更新されているか

## tasklist.md の必須セクション

tasklist.md には以下のセクションを必ず含めること:

1. **各機能の実装タスク** — コード + テスト + plugin.xml 登録
2. **ドキュメント更新タスク** — CLAUDE.md, README.md, sphinx-docs, product-requirements.md
3. **コミット前検証タスク** — 「コミット前検証チェックリスト」の5項目を確認
4. **マージタスク** — ビルド確認 + tasklist 完了確認 + main マージ

## 必ず守ること

- コード変更前にステアリングファイルを作成すること
- 新しい作業には新しい `.steering/` ディレクトリを作成すること（既存ドキュメントの使い回しではなく）

**例外:** タイポ修正、1行の設定変更など明らかに軽微な修正はステアリングを省略してよい。

## 調査・リサーチタスク

コード変更を伴わない調査でもステアリングドキュメントを作成しコミットすること。`main` に直接コミット可。コミットメッセージは `📝 Add <調査内容>` とする。

## git worktree 運用

ステアリングを伴うコード実装は **Claude Code のビルトイン worktree 機能**で隔離して行うこと。メインリポジトリではステアリングドキュメントの作成・承認のみ行う。

- **単一機能:** `EnterWorktree` ツール（または `claude --worktree <機能名>`）を使用する。worktree は `.claude/worktrees/<機能名>/` に作成され、ブランチ `worktree-<機能名>` が HEAD から自動生成される。セッション終了時に自動クリーンアップされる（変更ありの場合は確認プロンプト）。
- **並列実装:** 各ウィンドウで `claude --worktree <機能名>` を使用する。バッチブランチ戦略の詳細手順は `/steering` スキルを参照。
- **手動 worktree は使わない:** `git worktree add ../rescript-wt-*` による手動作成は非推奨。ビルトイン機能を使うこと。
- **マージ後のクリーンアップ:** ブランチを `main` にマージした後、以下を必ず実行すること:
  1. `git worktree remove <worktree-path>` で worktree を削除する
  2. `git branch -d <branch-name>` でマージ済みブランチを削除する（リモート未プッシュの場合は `-D` を使用）
  3. `git worktree list` で残存 worktree がないことを確認する
