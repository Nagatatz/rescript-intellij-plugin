# Design: Claude 設定強化（B + C1）

## B2: PreCompact 状態保存 Hook

`.claude/hooks/pre-compact-save.sh` を作成。`settings.json` の `PreCompact` イベントに登録。状態を `.claude/session-state.md` に出力。

## B3: Notification Hook

`settings.json` の `Notification` イベントに `osascript` コマンドを登録（macOS 専用）。スクリプトファイルは不要（インラインコマンド）。

## B4: code-reviewer エージェント強化

`.claude/agents/code-reviewer.md` に以下のチェック項目を追記:
- テスト改ざん: テストのアサーションが実装に合わせて緩められていないか
- デッドコード: 新関数作成後に旧関数が残っていないか
- 80/20 パターン: エッジケース・null/empty 処理・エラーパスの確認

## B5: パス固有 Rules

2つの新規ファイルを作成:
- `.claude/rules/flex-rules.md` — `Rescript.flex` 編集時の注意（`RescriptTokenTypes.kt` との同期、状態管理）
- `.claude/rules/plugin-xml-rules.md` — extension point 登録パターン、オプション依存の分離ルール

## C1: 禁止→肯定の書き換え

対象ファイル:
- `.claude/rules/steering-workflow.md` — 「禁止する」→「〜すること」
- `.claude/rules/git-conventions.md` — 「禁止する」→「〜すること」
- `.claude/rules/testing.md` — 「原則禁止」→ 肯定形
