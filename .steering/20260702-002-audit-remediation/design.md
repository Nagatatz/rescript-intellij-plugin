# 監査是正実装 — design

CP 分割・依存・各 CP の技術方針は `20260702-001-fable5-verification-audit/design.md`「実装チェックポイント」を正本とする。本ファイルは実装上の補足のみ記す。

## ブランチ運用

- 各 CP は `main` から `fix/audit-cpN-<topic>` ブランチを作成して実装。
- 緑になり次第 `main` へマージし、次 CP へ。中断耐性のため CP 単位で確実にマージする。

## テスト方針の補足

- CP1: `RescriptOffsetUtilsTest` に範囲外 position（character > line 長、line 範囲外）の回帰。`RescriptRenameHandlerTest` に changes/documentChanges 両形式 + 範囲外レンジの fake WorkspaceEdit。
- CP2: counting LSP stub で「N arrow → min(N, cap) request」を assert。
- CP3: lexer の state-restart テスト（コメント/JSX/テンプレート途中からの再開）+ 奇数 quote コメント回帰（flex-rules.md 準拠、`.flex` のみ編集）。
- CP4: `|>`/`||`/nested switch/multi-line body の回帰。抽出した共有 tokenizer に単体テスト。
- CP5: PPX の単一行複数アノテーション（`find→findAll`）テスト、debounce は Alarm/coroutine で既存パネルに倣う。
- CP6: サニタイザ util の単体テスト（home/project-root prefix 除去、パスなし文字列の不変性）。
- CP7: TypeInfoPanel の LSP-down 分岐テスト。docs は EN 本体 + JA `.po` 同時更新（sphinx-po-ja-sync）。
- CP8: DOT escape のテーブル駆動テスト、paintNode 集約後の既存テスト通過、detectTypeHoles のベンチ的テスト。

## 免除対象（testing.md）

- UI パネル本体（`RescriptPpxViewPanel` の Swing 部分、`RescriptTypeInfoPanel`）はロジック関数のみテスト。debounce 配線自体は UI 免除。
- `RescriptCreateInterfaceAction` の `Task.Backgroundable` 配線は IDE ライフサイクル免除だが、抽出可能なロジックがあればテストする。
