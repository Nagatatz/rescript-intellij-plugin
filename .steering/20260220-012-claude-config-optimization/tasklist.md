# Tasklist: Claude Code 設定最適化

## フェーズ 1: ルールファイル作成

- [x] `.claude/rules/testing.md` を作成（CLAUDE.md「テスト規約」セクションから移行）
- [x] `.claude/rules/code-comments.md` を作成（CLAUDE.md「コードコメント規約」セクションから移行）
- [x] `.claude/rules/git-conventions.md` を作成（CLAUDE.md「Git コミット規約」〜「ブランチ命名規則」から移行）
- [x] `.claude/rules/steering-workflow.md` を作成（CLAUDE.md「実装前の必須プロセス」〜「命令文のテンプレート」から移行）
- [x] `.claude/rules/documentation.md` を作成（CLAUDE.md「ドキュメントの分類」〜「注意事項」から移行）

## フェーズ 2: CLAUDE.md 削減

- [x] CLAUDE.md から分離済みセクションを削除し、`.claude/rules/` への参照を追加
- [x] CLAUDE.md の行数確認: 825行 → 260行（68%削減）。150行目標未達はプロジェクト構成ツリー（約180行）が原因。

## フェーズ 3: permissions 整理

- [x] `.claude/settings.local.json` の permissions を汎用パターンに統合（160行 → 32行）

## フェーズ 4: 検証・コミット

- [x] 分離されたルールファイルの内容が元の CLAUDE.md と一致することを確認
- [x] tasklist.md を更新してコミット
- [ ] main にマージして worktree を削除
