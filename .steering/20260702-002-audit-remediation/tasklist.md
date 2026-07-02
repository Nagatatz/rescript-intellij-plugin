# 監査是正実装 — tasklist

各セクション = 1 マージ可能チェックポイント。着手時に即 `[x]` 更新（DoD Phase 2）。

## CP1 — LSP position/edit 堅牢化（rank 1-3, S）
- [ ] `util/RescriptOffsetUtils.positionToOffset` に character の lineEnd/textLength クランプ
- [ ] `refactor/RescriptRenameHandler:262` に start<=end / start<=textLength ガード
- [ ] `refactor/RescriptRenameHandler:226` に documentChanges 分岐
- [ ] テスト: `RescriptOffsetUtilsTest`（範囲外 position）、`RescriptRenameHandlerTest`（両 WorkspaceEdit 形式・範囲外レンジ）
- [ ] ktlint/build/test 緑 → コミット → main マージ

## CP2 — inlay hint LSP 予算化（rank 4,10, S/A）
- [ ] `lsp/RescriptPipeChainTypeHintsProvider:80` に request-cap + per-pass cache + cancel
- [ ] `narrowing/RescriptNarrowingHintProvider:134` に同一ポリシー
- [ ] テスト: counting stub で request 数 assert
- [ ] 緑 → マージ

## CP3 — JFlex lexer 状態修正（rank 8,9,17, A）
- [ ] `Rescript.flex:15` カスタムフィールドを getState() 符号化
- [ ] `Rescript.flex:304` 奇数 quote latch 修正
- [ ] `Rescript.flex:283` IN_TEMPLATE EOL ギャップ処理
- [ ] テスト: state-restart + コメント/テンプレート回帰
- [ ] 緑 → マージ

## CP4 — switch-arm tokenizer 抽出 + intention 修正（rank 5-7, A）
- [ ] 共有 tokenizer（tokenize/isIgnorable/LexedToken）抽出 + 単体テスト
- [ ] `intention/RescriptMergeSwitchCasesIntention:74` を token 認識分割へ
- [ ] `intention/RescriptCaseSplitIntention:69` を full-arm extent へ
- [ ] テスト: `|>`/`||`/nested/multi-line 回帰
- [ ] 緑 → マージ

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
