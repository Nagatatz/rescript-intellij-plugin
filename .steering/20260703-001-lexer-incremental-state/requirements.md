# Lexer incremental state 修正（CP3 :15 分離分）— requirements

## 背景

`20260702-001-fable5-verification-audit` の所見テーマ②（JFlex lexer の隠れ状態）のうち、CP3 で実装した `:283`/`:304` とは別に分離した **`:15`** を扱う。監査当初は「ネストコメント深度が有限状態化不可」を主眼としていたが、CP3 実装中の精査で**スコープを訂正**した（下記）。

## 問題の正確なスコープ（訂正済み）

`RescriptLexer` は素の `com.intellij.lexer.FlexAdapter`。IntelliJ のインクリメンタル・ハイライタ（`LexerEditorHighlighter`）は**トークン境界**で `lexer.getState()`（= JFlex の `yystate()`）をスナップショットし、編集時にその状態から `lexer.start(...)` で再字句解析する。したがって getState() に含まれない字句状態は restart で失われる。

- **コメント深度（`commentDepth`）は安全**: ブロックコメントは `tokenStart/tokenEnd` トリックで **1 トークン**（`MULTI_COMMENT`）として emit される。restart はコメント境界（`/*` の前）で起こり、`/*` ルールが `commentDepth = 1` を毎回再初期化する。ネストしていてもコメント全体が 1 トークンなので、深度を getState() に符号化する必要はない。→ **当初の懸念は誤り。修正不要。**
- **JSX open タグの状態が真の問題**: `<div ...attrs... >` の属性領域は **`INITIAL` 字句状態のまま**複数トークンにわたって処理され、その間 `inJsxOpenTag`（boolean）と `jsxAttrBraceDepth`（int）がフィールドとして状態を持ち越す（`Rescript.flex:15-16, 214-215, 227, 247`）。これらは getState()（= `INITIAL`）に含まれないため、**属性トークン境界で restart すると失われ**、閉じ `>` が `TAG_GT` でなく `GT` に、`{`/`}` のブレース深度判定も誤る。

## 再現条件

JSX open タグの属性リスト内（例: `<div className={x} onClick={f}>` の `onClick` 付近）を編集すると、ハイライタが直前の属性トークン境界（字句状態 `INITIAL`、ただし `inJsxOpenTag=true` は非復元）から再字句解析し、閉じ `>` を比較演算子 `GT` として着色する。多くの操作（全再解析トリガ）で自己回復するため **B 優先度**。

## 受け入れ条件

- [ ] JSX open タグの属性領域を getState() で表現し、属性トークン境界からの restart 後も閉じ `>`（`TAG_GT`）・`{`/`}`（属性式のブレース）・`/>`（`TAG_AUTO_CLOSE`）が正しくトークン化される
- [ ] **characterization テスト**で incremental restart を再現し、修正前は失敗・修正後は成功することを示す（フル字句解析で各トークン境界の getState() を採取 → タグ内トークンから `start(text, offset, len, state)` で restart → 閉じ `>` が `TAG_GT` になることを assert）
- [ ] 既存 `RescriptLexerTest`（90+ ケース）が全通過（JSX・コメント・テンプレート回帰なし）
- [ ] `./gradlew ktlintCheck clean buildPlugin test --rerun` 緑
- [ ] `commentDepth` は 1 トークン境界で安全という判断をテスト（コメント内編集 restart で化けない）で裏付ける

## 非機能・制約

- flex-rules.md 準拠: `Rescript.flex` のみ編集（生成 `RescriptFlexLexer.java` は直接編集禁止）。`%state` 追加時は対応する状態遷移テスト必須。
- ハイライトは看板機能。**回帰ゼロ**を最優先。実装は worktree 隔離、緑チェックポイントでのみマージ。
- 実装方針（有限状態化 / RestartableLexer / 現状維持+記録）は design.md で決定し、ユーザー承認を得てから着手する。
