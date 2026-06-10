# 要求内容: リポジトリ衛生 (完全リファクタリング Phase 0)

## 背景

完全リファクタリング計画 (全 6 フェーズ、プランファイル: `~/.claude/plans/virtual-exploring-kernighan.md`) の Phase 0。
一次調査 + 裏取りで以下のリポジトリ衛生上の無駄が確定した:

- `.claude/worktrees/` に worktree が 7 個 + 空ディレクトリ 2 個残存し、合計 ~2.6 GB を占有している
- 7 個のうち 6 個のブランチは `git log main..<branch>` が 0 コミットでマージ済み
- `worktree-v1-followups-20260514` のみ 5 コミット未マージ:
  - `b18de9a` 🔧 Add -Pscope=fast|perf|cli filter to the test task
  - `15fd46c` ♻️ Convert perf tests to baseline + slack ratchet with warmup
  - `73392cc` 📝 Add CONTRIBUTING.md and good-first-issue inventory
  - `16362f3` 📝 Scope deep v1 followups as steering 005
  - `6e9a2ed` 📝 Renumber steering 002→003 / 003→004 after parallel-session rebase

## 要求

1. `worktree-v1-followups-20260514` の 5 コミットを main にマージする (ユーザー承認済み: プラン承認時の AskUserQuestion で「Phase 0 でマージする」を選択)
2. マージ後、全 worktree (7 個) と対応ブランチ、空ディレクトリ 2 個を削除する
3. ディスク ~2.6 GB を回収する

## 受け入れ条件

- [ ] `git log main..worktree-v1-followups-20260514` が 0 コミットになる (マージ完了)
- [ ] マージ後に `./gradlew test` が green
- [ ] `git worktree list` がメインリポジトリのみになる
- [ ] `worktree-*` ブランチがすべて削除されている
- [ ] `.claude/worktrees/` が空になる
- [ ] main を origin に push 済み

## スコープ外

- コード変更 (Phase 1 以降で実施)
- `plugin-verifier-ignored-problems.txt` (裏取りで整理済み・対応不要と確認)
