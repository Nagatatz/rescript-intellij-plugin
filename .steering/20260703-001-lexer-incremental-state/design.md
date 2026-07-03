# Lexer incremental state 修正 — design

## 実装フェーズ（Phase 1 は低リスク・先行実施推奨）

### Phase 1: characterization テスト（バグの実証）— 低リスク
IntelliJ ハイライタの restart を単体で再現し、バグを**実証**する。lexer 本体は触らないため回帰リスクなし。

手順（`RescriptLexerTest` または新規 `RescriptLexerRestartTest`）:
1. `<div className={x} onClick={f}>` をフル字句解析し、各トークンの `(tokenStart, getState())` を記録（`RescriptLexer` は `FlexAdapter` なので `lexer.state` で取得可）。
2. タグ内の属性トークン（例: `onClick`）の位置で `lexer.start(text, tokenStart, text.length, recordedState)` し restart。
3. restart 後に閉じ `>` が `TAG_GT` になることを assert → **現状は `GT` になり失敗する見込み**。
4. 併せてコメント内編集 restart で `MULTI_COMMENT` が保たれることを assert（commentDepth が安全＝1トークンである根拠をテスト化）。

このテストが失敗すれば `:15` は実バグと確定。成功すれば `:15` は非問題であり Phase 2 不要（requirements の判断を更新して close）。

### Phase 2: 修正（Phase 1 で実バグ確定時のみ・ユーザー承認必須）

## 実装方針の比較（investigation で訂正）

| 方針 | 内容 | 評価 |
|------|------|------|
| **A. 有限状態化（lexical state）** | `inJsxOpenTag` を `%state IN_JSX_ATTRS` に置換 | **不適**。JSX 属性値の `{式}` は任意の ReScript 式で、INITIAL の全表現ルールを要する。INITIAL 内の `{`/`}` は record/block 等でも使われ、「どの `}` が JSX 属性に戻るか」を状態だけで表現できない。ルール重複か複雑な深度ステートラダーが必要で、現実的でない |
| **B. RestartableLexer / state パック** | カスタム `RescriptLexer` で `inJsxOpenTag` + 上限付き `jsxAttrBraceDepth` を getState() の整数に符号化し、start() で復元 | **唯一のクリーンな修正**。フィールドベースの均一 INITIAL 字句解析を保ちつつ、状態を restart に耐えさせる。中〜大の作業、要注意実装 |
| **C. 現状維持 + 記録** | Phase 1 の char テストで現挙動を固定し、docs に既知制限として記録 | 自己回復する B 優先グリッチに対する最小工数。B のコスト/リスクが見合わないと判断した場合の妥当なフォールバック |

## 推奨: Phase 1 → 結果次第で B か C

1. **まず Phase 1**（char テスト）を実施しバグを実証・定量化する（低リスク、価値大）。
2. 実バグ確定かつ修正価値ありと判断 → **B** を採用。
3. 看板ハイライトへの回帰リスク／B 優先度の自己回復性を勘案し見合わないと判断 → **C**（char テストを既知制限の記録として残す）。

## B を採る場合の実装スケッチ

1. `Rescript.flex` の `%{ %}` に `inJsxOpenTag`/`jsxAttrBraceDepth` の public getter/setter を追加（生成クラス経由でアクセス可能に）。
2. `RescriptLexer`（`FlexAdapter` 継承）で:
   - `getState()`: `super.getState()`（= yystate）に、`inJsxOpenTag`（1 bit）+ `jsxAttrBraceDepth`（上限 cap、例 3 bit）を上位ビットで合成。
   - `start(...)`: `initialState` の上位ビットを分解してフィールドへ設定し、下位ビット（yystate）で `super.start`。
   - `jsxAttrBraceDepth` は cap を超える極端なネスト（現実の JSX では稀）を上限クランプで許容（既知の縮退）。
3. flex-rules.md 準拠で state 遷移テスト + Phase 1 の char テストを緑化。

## リスクと緩和

- getState() の符号化は yystate との bit 衝突に注意（`%state` 数 < cap 用ビットの下限を確保）。
- 既存 90+ lexer テスト全通過を必須ゲートにする。
- worktree 隔離、緑チェックポイントでのみ main マージ。
