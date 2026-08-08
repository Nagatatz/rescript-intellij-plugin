# 監査是正実装 — tasklist

各セクション = 1 マージ可能チェックポイント。着手時に即 `[x]` 更新（DoD Phase 2）。

## CP1 — LSP position/edit 堅牢化（rank 1-3, S）
- [x] `util/RescriptOffsetUtils.positionToOffset` に character の lineEnd/textLength クランプ
- [x] `refactor/RescriptRenameHandler:262` に start<=end / start<=textLength ガード
- [x] `refactor/RescriptRenameHandler:226` に documentChanges 分岐（`collectEdits` 抽出）
- [x] テスト: `RescriptOffsetUtilsTest`（範囲外/負 character・clamp）、`RescriptRenameHandlerTest`（changes/documentChanges 両形式・空）
- [x] ktlint/build/test 緑 → コミット → main マージ

## CP2 — inlay hint LSP 予算化（rank 4,10, S/A）
- [x] `lsp/RescriptPipeChainTypeHintsProvider`: トークン `ARROW`→`RIGHT_ARROW` 修正（pipe 誤マッチ是正）+ MAX_PIPE_CHAIN_HINTS cap + PresentationFactory ループ外へ
- [x] `narrowing/RescriptNarrowingHintProvider`: MAX_HOVER_REQUESTS_PER_FILE 予算で総 hover 呼出を上限化
- [x] テスト: `findPipePositions`（`->` のみ・cap）、narrowing counting resolver で 80 上限 assert
- [x] 緑 → マージ

## CP3 — JFlex lexer 状態修正（rank 9,17, A）※ :15 は分離
- [x] `Rescript.flex:304` 奇数 quote latch 修正（`inCommentString` 除去、C/JS 系コメント意味論に統一）
- [x] `Rescript.flex:283` IN_TEMPLATE EOL ギャップ処理（`{EOL}` を STRING_VALUE 化）
- [x] テスト: 奇数 quote 回帰 + テンプレ全文字カバレッジ回帰
- [x] 緑 → マージ
- [~] **分離**: `Rescript.flex:15` getState() 符号化（ネストコメント深度が有限状態化不可 → RestartableLexer 再設計。ハイライト回帰リスク大のため専用ステアリングに残置。ユーザー承認済み分離）

## CP4 — switch-arm tokenizer 抽出 + intention 修正（rank 5-7, A）
- [x] 共有 `lang/RescriptTokenScanner`（tokenize/isIgnorable/LexedToken）抽出、Collector/Flattener を rebase + 単体テスト
- [x] `intention/RescriptMergeSwitchCasesIntention` を token 認識分割へ（`|>`/`||`/nested/or-pattern を区別）
- [x] `intention/RescriptCaseSplitIntention` を full-arm extent（`findSplitTarget`）へ
- [x] テスト: `|>`/`||`/nested/multi-line/**単一行 or-pattern** 回帰（or-pattern 脱落回帰はレビューで検出し修正）
- [x] 緑 → マージ

## CP5 — EDT 応答性（rank 11-12, A/B）
- [x] `navigation/RescriptCreateInterfaceAction` + `RescriptOpenCompiledJsAction` を Task.Backgroundable 化（progress+cancel、opus 委譲）
- [x] `ppx/RescriptPpxViewPanel` caret debounce（RescriptCoroutineDebouncer）+ `find→findAll`
- [x] テスト: 単一行複数アノテーション（`Rescrypt` typo は監査 finder の引用ミスで実ファイルは正しかった → rename 不要）
- [x] 緑 → マージ

## CP6 — エラー表示サニタイズ（rank 13-14, B, security）
- [x] `util/RescriptMessageSanitizer` 共有サニタイザ + 単体テスト7件（opus 委譲、home→`~` / base→`<project>` / 残余絶対パス→basename）
- [x] `binding/DtsGenerateBindingAction:66/128/137` を経由
- [x] `lsp/RescriptLspInstaller:98` を経由
- [x] 緑 → マージ（security-relevant を明示）

## CP7 — docs/挙動 truth-sync（rank 15-16, B）
- [x] `typeinfo/RescriptTypeInfoPanel` に LSP 未接続専用メッセージ（`selectMessage` 抽出 + typeinfo 初テスト）
- [x] `docs/product-requirements.md` US-12 の `.resw` 実行を roadmap 状態へ差戻し（コメント評価/REPL は動作するため維持）
- [x] `docs/lsp-fallback-matrix.md` PPX 行（regex+辞書・bsc 非呼出）+ type-info 行 + worksheet 行を実装に合わせ書換え
- [x] `worksheet/RescriptWorksheetFileType` KDoc の過大主張（「評価しインライン表示」）を修正
- [x] docs は英語必須対象だが既存内容が日本語のため grandfather 原則で日本語同期（sphinx `.po` 対象外の `docs/` 直下）
- [x] 緑 → マージ

## CP8 — 統合クリーンアップ（rank 18-20, C）
- [x] DOT escape 統一（`diagram/DotLabelEscaping`）+ テーブル駆動テスト（sonnet 委譲）
- [x] paintNode を `ui/GraphViewPaintHelpers` へ集約（maxLines パラメータ化）
- [x] `quickfix/RescriptTypeHoleQuickFix` を O(n) 化（lineStartOffsets 事前計算）
- [x] 緑 → マージ

## 分離タスク（ユーザー承認済み）
- [~] **CP3 :15** `Rescript.flex` カスタムフィールドの getState() 符号化 / RestartableLexer 化。ネストコメント深度が有限状態化不可でハイライト再設計を伴うため専用ステアリングに残置。

## 完了
- [x] 全 CP マージ済み（CP1-CP8、:15 のみ分離）
- [x] main 上で全 build+test+verifyPlugin 再検証（エージェント産物の独立検証）
- [x] tasklist 全項目 `[x]`（本項目含む、最終コミットに含める）
