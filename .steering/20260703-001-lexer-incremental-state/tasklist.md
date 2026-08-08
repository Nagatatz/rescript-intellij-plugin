# Lexer incremental state 修正 — tasklist

## Phase 1: バグ実証（characterization テスト）
- [x] `RescriptLexerRestartTest` 作成（フル字句解析で各トークンの `(offset, state)` 採取 → restart → トークン型比較）
- [x] JSX restart で `>` が `GT` に化けることを実証（実バグ確定）
- [x] コメント restart が `MULTI_COMMENT` を保つことを実証（1トークンで安全）

## Phase 2: 修正（LexerBase 化）
- [x] `Rescript.flex` に `inJsxOpenTag`/`jsxAttrBraceDepth` の public accessor を追加
- [x] `RescriptLexer` を `FlexAdapter` → `LexerBase` に書換え、`RescriptFlexLexer` を直接駆動
- [x] 各トークンの advance 前に `(yystate + JSX fields)` をスナップショットし getState() で返す（before-token 一貫性）
- [x] state 符号化（下位5bit yystate / bit5 inJsxOpenTag / bit6-8 depth cap7）
- [x] テスト追加: `{`トークン restart / ネストブレース全境界一貫性 / コメント安全性
- [x] `ktlintCheck` + `buildPlugin` + 全 `test --rerun` 緑（全パッケージ回帰なし）
- [x] `verifyPlugin`: 3 IDE (IU-253/261/262) すべて **"Compatible."**、新規 deprecated/internal 利用ゼロ。task exit 1 は verdict 書込み中のディスクスパイク（ENOSPC）で検証内容はパス
- [ ] コミット（ブランチ `fix/lexer-restart-char-test`）→ **ユーザーにマージ承認確認**（看板ハイライトに関わる基盤変更のため）→ main マージ

## 備考
- 当初 design の FlexAdapter パック案は before/after 不整合で不成立 → LexerBase 実装に変更（design.md「実施結果」参照）。
- コメント深度は当初の懸念に反し修正不要（1トークン）。
