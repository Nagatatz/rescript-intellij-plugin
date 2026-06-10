# 設計: リポジトリ衛生 (Phase 0)

## 方針

コード変更ゼロの作業のため、worktree は作らず main 上で直接実施する (steering-workflow.md の例外「ステアリングドキュメントのみの変更」+ worktree 削除自体が作業内容)。

## 手順

### 1. v1-followups のマージ

```bash
git merge worktree-v1-followups-20260514   # main 上で。コンフリクトが出たら停止してユーザーに報告
./gradlew test                              # build.gradle.kts (-Pscope フィルタ) と perf テスト変更が含まれるため必須
git push origin main
```

- 5 コミットの内容はテストインフラ + ドキュメントのみで本体コード変更なし → リスク低
- `-Pscope` 未指定時に従来どおり全テストが走ることをマージ後の `./gradlew test` で確認する

### 2. worktree とブランチの削除

メインリポジトリのセッションから実行する (worktree 内での `git worktree remove` 禁止規約は、メイン repo からの実行には該当しない):

```bash
git worktree remove .claude/worktrees/<name>   # 7 個
git branch -d worktree-<name>                  # マージ済みなので -d で安全に削除できる
rm -rf .claude/worktrees/fix-tuple-pattern-summary .claude/worktrees/20260513-008-remove-migration-pilot  # 空ディレクトリ (git 管理外)
git worktree prune
```

- `-D` (強制) は使わない。`-d` が失敗するブランチがあれば未マージのシグナルとして停止する

### 3. 検証

```bash
git worktree list        # main のみ
git branch | grep worktree-   # ヒットなし
du -sh .claude/worktrees 2>/dev/null   # 存在しないか 0
```

### 4. steering コミット

ドキュメントのみのため main 直接コミット可。`📝 Add repo-hygiene steering for refactoring Phase 0`

## リスク

| リスク | 緩和策 |
|---|---|
| マージで perf テストが flaky 化 | ratchet 化 (baseline + slack) が目的のコミットなので逆に安定化するはず。`./gradlew test` で確認 |
| 削除した worktree に未コミット変更が残っていた | `git worktree remove` は dirty な worktree を拒否する。拒否されたら内容を確認して報告 |
