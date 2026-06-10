# タスクリスト: リポジトリ衛生 (Phase 0)

セクション間依存: セクション 1 (マージ) → セクション 2 (削除)。マージ前に worktree を削除すると未マージコミットが孤立するため順序厳守。

## セクション 1: v1-followups マージ

- [x] `git fetch origin` + main の ahead/behind 確認 (0/0 を確認)
- [x] `git merge worktree-v1-followups-20260514` を main 上で実行 (コンフリクトなし、merge commit 41f1390)
- [x] `./gradlew test` が green であることを確認 (BUILD SUCCESSFUL in 1m 50s)
- [x] `git push origin main` (99bddc2..41f1390)

## セクション 2: worktree クリーンアップ

- [x] 7 worktree を `git worktree remove` で削除
- [x] `worktree-*` ブランチ 7 本を `git branch -d` で削除 (全件 `-d` で成功 = マージ済み確認)
- [x] 空ディレクトリ 2 個 (`fix-tuple-pattern-summary`, `20260513-008-remove-migration-pilot`) を削除 (中身は空の `.gradle` のみ、`rmdir` で階層削除)
- [x] `git worktree prune`
- [x] 検証: `git worktree list` が main のみ / `git branch` に worktree-* なし / `.claude/worktrees` 0B

## セクション 3: 記録

- [x] tasklist を `[x]` に更新
- [x] steering ドキュメントを main にコミット (`📝`、ドキュメントのみのため直接コミット可) + push

## テスト省略の理由

コード変更なし (worktree/ブランチ操作 + steering ドキュメントのみ) のため、新規テストは対象外。マージ内容の検証は既存テストスイートの全実行で行った。

## 結果

- 回収ディスク: ~2.6 GB (`.claude/worktrees` 2.6 GB → 0 B)
- main に取り込んだ未マージコミット: 5 件 (perf テストのラチェット化、`-Pscope` テストフィルタ、CONTRIBUTING.md、steering 文書)
