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
- [ ] `navigation/RescriptCreateInterfaceAction:60` を Task.Backgroundable 化（progress+cancel）
- [ ] `ppx/RescriptPpxViewPanel:57` caret debounce + find→findAll
- [ ] テスト: 単一行複数アノテーション（+ ファイル名 typo `Rescrypt` 修正）
- [ ] 緑 → マージ

## CP6 — エラー表示サニタイズ（rank 13-14, B, security）
- [ ] `util/` に共有メッセージサニタイザ + 単体テスト
- [ ] `binding/DtsGenerateBindingAction:66/128/137` を経由
- [ ] `lsp/RescriptLspInstaller:98` を経由
- [ ] 緑 → マージ（マージ確認時に security-relevant を明示）

## CP7 — docs/挙動 truth-sync（rank 15-16, B）
- [ ] `typeinfo/RescriptTypeInfoPanel:154` に LSP 未接続専用メッセージ + テスト
- [ ] `docs/product-requirements.md` US-12 を roadmap 状態へ差戻し
- [ ] `docs/lsp-fallback-matrix.md` PPX 行 + type-info 行を実装に合わせ書換え
- [ ] EN docs + JA `.po` 同一コミット（documentation.md）
- [ ] 緑 → マージ

## CP8 — 統合クリーンアップ（rank 18-20, C）
- [ ] DOT escape 統一（flow/diagram）+ テーブル駆動テスト
- [ ] paintNode を `ui/GraphViewPaintHelpers` へ集約
- [ ] `quickfix/RescriptTypeHoleQuickFix:122` を O(n) 化
- [ ] 緑 → マージ

## 完了
- [ ] 全 CP マージ済み
- [ ] tasklist 全項目 `[x]`（本項目含む、最終コミットに含める）
