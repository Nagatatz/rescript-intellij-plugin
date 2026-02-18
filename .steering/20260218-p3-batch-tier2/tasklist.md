# Tasklist: P3 Batch Tier 2

## 準備
- [x] ステアリングドキュメント作成（requirements.md, design.md, tasklist.md, window-instructions.md）
- [x] バッチブランチ `feature/p3-batch-tier2` 作成
- [x] ステアリングドキュメントをバッチブランチにコミット
- [x] worktree 3つ作成

## 並列実装
- [x] Feature 1: Statement Up/Down Mover（worktree: `rescript-wt-statement-mover`）
- [x] Feature 2: Qualified Name Copy（worktree: `rescript-wt-qualified-name`）
- [x] Feature 3: Smart Enter（worktree: `rescript-wt-smart-enter`）

## マージ・仕上げ
- [x] 3ブランチをバッチブランチにマージ（plugin.xml 競合解決）
- [x] `./gradlew buildPlugin` で最終確認
- [x] 共有ドキュメント一括更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] ドキュメント更新コミット
- [x] main にマージ
