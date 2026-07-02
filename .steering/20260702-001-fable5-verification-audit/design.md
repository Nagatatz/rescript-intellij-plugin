# Fable5 徹底検証監査 — design（優先度と実装チェックポイント）

Fable5 大局統合の成果物。23 確定所見（requirements.md）を優先度付けし、独立マージ可能な 8 チェックポイントに分割する。実装は `20260702-002-audit-remediation` で実施。

## 優先度ランキング（20 集約項目、S→A→B→C）

| rank | 優先 | 項目 | files | effort | risk |
|-----:|:----:|------|-------|:------:|:----:|
| 1 | S | `positionToOffset` の character クランプ | util/RescriptOffsetUtils.kt | low | low |
| 2 | S | rename 編集ガード強化（start<=end, start<=textLength） | refactor/RescriptRenameHandler.kt | low | low |
| 3 | S | `applyWorkspaceEdit` の documentChanges 対応 | refactor/RescriptRenameHandler.kt | med | low |
| 4 | S | pipe-chain inlay hover の上限化・非同期化 | lsp/RescriptPipeChainTypeHintsProvider.kt | med | low |
| 5 | A | CaseSplit intention の複数行アーム対応 | intention/RescriptCaseSplitIntention.kt | med | med |
| 6 | A | MergeSwitchCases の regex を token 認識分割へ | intention/RescriptMergeSwitchCasesIntention.kt | med | med |
| 7 | A | switch-arm tokenizer 共有抽出（重複解消） | narrowing/RescriptSwitchArmCollector.kt, intention/RescriptNestedSwitchFlattener.kt | med | low |
| 8 | A | lexer カスタムフィールドの getState() 符号化 | lang/Rescript.flex | med | med |
| 9 | A | ブロックコメント奇数 quote latch 修正 | lang/Rescript.flex | low | low |
| 10 | A | narrowing hint LSP hover の予算化 | narrowing/RescriptNarrowingHintProvider.kt | med | low |
| 11 | A | create-interface LSP 呼出を EDT 外へ | navigation/RescriptCreateInterfaceAction.kt | low | low |
| 12 | B | PPX パネル caret debounce + findAll 修正 | ppx/RescriptPpxViewPanel.kt, test | low | low |
| 13 | B | Dts ダイアログの絶対パス/stderr サニタイズ | binding/DtsGenerateBindingAction.kt | low | low |
| 14 | B | LSP installer stderr サニタイズ | lsp/RescriptLspInstaller.kt | low | low |
| 15 | B | Type Info パネルの LSP 不在メッセージ区別 | typeinfo/RescriptTypeInfoPanel.kt | low | low |
| 16 | B | US-12 worksheet 記述と PPX matrix 行の truth-sync | worksheet/…, docs/ | low | low |
| 17 | B | IN_TEMPLATE EOL 隣接ギャップ処理 | lang/Rescript.flex | low | low |
| 18 | C | DOT escape 統一 | flow/RescriptVariantFlowDotExporter.kt, diagram/RescriptDependencyDiagramModel.kt | low | low |
| 19 | C | paintNode を GraphViewPaintHelpers へ集約 | flow/, diagram/, ui/GraphViewPaintHelpers.kt | low | low |
| 20 | C | detectTypeHoles の O(n^2) 解消 | quickfix/RescriptTypeHoleQuickFix.kt | low | low |

## 実装チェックポイント（CP1〜CP8、各 mergeable_alone）

steering-workflow.md「1 セクション = 1 マージ可能単位 = 1 コミット/PR」に対応。各 CP は独立ブランチで実装 → ktlint/build/test 緑 → main マージ。

- **CP1 — LSP position/edit 堅牢化（rank 1-3）**: `RescriptOffsetUtils.positionToOffset` に character の lineEnd/textLength クランプ追加、`RescriptRenameHandler:262` に start<=end/<=textLength ガード、`:226` に documentChanges 分岐。回帰テスト（範囲外 position、changes/documentChanges 両形式の fake WorkspaceEdit）。**最初に実施**（最小 diff・最大波及）。
- **CP2 — inlay hint LSP 予算化（rank 4, 10）**: `PipeChainTypeHintsProvider:80` と `NarrowingHintProvider:134` に共有の request-cap + per-pass cache + document 変更時 cancellation。counting fake で cap をテスト。
- **CP3 — JFlex lexer 状態修正（rank 8, 9, 17）**: `Rescript.flex:15` の getState() 符号化、`:304` odd-quote latch、`:283` IN_TEMPLATE EOL。flex-rules.md 準拠（`.flex` のみ編集、state-restart テスト必須）。1 コミット（同一生成物・同一テストハーネス）。
- **CP4 — switch-arm tokenizer 抽出 + intention 修正（rank 5-7）**: まず重複 `tokenize/isIgnorable/LexedToken` を共有 util 抽出（rank 7）、次に `MergeSwitchCases:74`（regex 廃止）と `CaseSplit:69`（full-arm extent）を rebase。回帰テスト（`|>`/`||`/nested/multi-line）。必要なら extraction♻️ → fixes🐛 の 2 コミット。
- **CP5 — EDT 応答性（rank 11-12）**: `CreateInterfaceAction:60` を `Task.Backgroundable`（progress+cancel）へ。`PpxViewPanel:57` に caret debounce（他パネル同様）+ `find→findAll` 修正 + 単一行複数アノテーションテスト追加（ファイル名 typo 修正）。
- **CP6 — エラー表示サニタイズ（rank 13-14）**: `util/` に共有メッセージサニタイザ（home/project-root prefix 除去）+ テスト。`DtsGenerateBindingAction:66/128/137` と `RescriptLspInstaller:98` を経由。DoD Phase 4 で security-relevant として明示。
- **CP7 — docs/挙動 truth-sync（rank 15-16）**: `TypeInfoPanel:154` に「LSP 未接続」専用 placeholder（+テスト）、US-12 を roadmap 状態へ差戻し、`lsp-fallback-matrix.md` の PPX 行を実装（regex+辞書）に合わせて書換え。documentation.md 準拠（EN docs + JA .po 同一コミット）。
- **CP8 — 統合クリーンアップ（rank 18-20）**: DOT escape 統一、paintNode 集約、detectTypeHoles の O(n) 化。3 つの小 ♻️/⚡ コミット。最低優先、時間制約時は延期可。

## 依存関係

- CP4 は rank 7（tokenizer 抽出）を先行させ、その上に rank 5-6 を載せる（CP 内順序）。
- CP6 の共有サニタイザは CP7（TypeInfoPanel）とは独立。
- その他 CP 間依存なし。緑になった CP から順に main へマージ可能。
