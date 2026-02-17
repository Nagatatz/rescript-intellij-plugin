# Requirements: Go to Related

## 機能概要

`Navigate > Related Symbol` (Ctrl+Alt+Home / Cmd+Alt+Home) で `.res` ↔ `.resi` ↔ `.js` 間の関連ファイルジャンプを提供する。

## 受け入れ条件

- `.res` ファイルから: 対応する `.resi` と生成された `.js` ファイルが候補に表示される
- `.resi` ファイルから: 対応する `.res` ファイルが候補に表示される
- 候補を選択すると該当ファイルが開く
- 対応ファイルが存在しない場合は候補に表示されない

## 制約事項

- IntelliJ Platform の `GotoRelatedProvider` API を使用
- 既存の `RescriptSwitchFileAction` (Alt+O) とは別機能（こちらはナビゲーションメニュー経由）
