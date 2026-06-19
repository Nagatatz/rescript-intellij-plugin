# 要求定義: Type Hole Completion (Wingman 風 type hole 補完)

ロードマップ #115。対象カテゴリ: 補完。難易度: 中〜高。優先度: B。
出典は Haskell の Wingman / HLS の type hole 機能。

## 1. 背景と目的

ReScript では値を書きかけのときにプレースホルダとして `_`（型穴）を置くことがある。
Wingman / HLS は型穴の **期待型** と **スコープ内の束縛** を手がかりに、穴を自動充填したり
`switch`（case split）に展開したりして「次の一手」を提示する。

本プラグインには既に `completion/RescriptPlaceholderCompletionContributor` (#117) があり、
型注釈付き値位置 `let x: T = ` で **期待型からリテラル雛形**（record / variant constructor）を提示する。
本機能 (#115) はその発展として、**型穴 `_` に対して**:

1. **ローカル束縛からの自動充填** — スコープ内に期待型と一致する `let` 束縛があれば、その名前を補完候補に出す
2. **case split サポート** — 期待型が variant のとき、穴を網羅的な `switch _ { | Ctor(_) => _ | ... }` 雛形に展開する補完を出す

を追加する。#117（期待型からのリテラル構築）と相補的に動作し、Wingman 的な「穴埋め体験」を完成させる。

## 2. スコープ

### 2.1 v1 で実装する (LSP 非依存・純構文)

すべて `RescriptLexer` のトークン列に基づく純ロジックとし、LSP 無しで動作・単体テスト可能にする。

- **トリガコンテキスト:** 型注釈付き `let` 束縛の値位置にある `_` 型穴。
  すなわち `let [rec] <name>: <T> = _<caret>` の形。期待型 head `T` は既存
  `RescriptTypeAnnotationContext.detectExpectedType` で取得し、加えてキャレット直前のトークンが
  `_`（UNDERSCORE）であることを要求する（型穴であることの明示シグナル）。
- **(A) ローカル束縛からの自動充填:**
  キャレットより前に出現する brace-depth 0 の `let <bindName>: <U> = ...` 束縛のうち、
  注釈型 head `U` が期待型 head `T` と一致するものを補完候補 `<bindName>` として提示する
  （ラベル右に `local binding`）。挿入は穴 `_` を `<bindName>` で置換する。
  - 同名で後続再束縛された場合は最後の宣言を採用する（単純なシャドウイング近似）。
  - 自分自身（編集中の `let <name>`）は候補から除外する。
- **(B) case split:**
  期待型 `T` が `RescriptPlaceholderTypeResolver.resolve` で `TypeShape.Variant` に解決できるとき、
  単一の補完候補 `case split` を提示する。挿入は穴 `_` を次の網羅 `switch` 雛形で置換する:

  ```
  switch _ {
  | Ctor1(_) => _
  | Ctor2 => _
  }
  ```

  - payload を持つ constructor は `Ctor(_)`、持たないものは `Ctor` で出す
    （`RescriptMissingArmsBuilder.buildInsertion` の payload 判定と同じ規則）。
  - インデントは穴の存在する行の行頭インデントを踏襲する。
  - 挿入後はキャレットを scrutinee の `_`（最初の穴）に移動する。

### 2.2 v1 では実装しない (将来検討)

- LSP `textDocument/hover` による期待型の解決（型注釈が無い任意の `_` への対応）。
  v1 は型注釈付き `let` 値位置の `_` のみを対象とする。`narrowing/RescriptHoverTypeResolver` を
  使った拡張は将来の別ステアリングとする。
- case split の scrutinee に「期待型と一致するローカル束縛」を自動で埋め込むこと
  （v1 は scrutinee を `_` 穴のまま残す）。
- record 期待型に対する case split（record は #117 のリテラル雛形で充足するため対象外）。
- 関数引数位置・タプル要素位置・match arm body など、`let` 値位置以外の穴。
- 入れ子型の再帰展開（穴は常に 1 階層）。

## 3. 受け入れ条件

- [ ] `let x: color = _` で `color` が variant のとき、`case split` 補完を選ぶと
      網羅的な `switch _ { | ... => _ }` 雛形に置換され、キャレットが scrutinee の `_` に移動する
- [ ] スコープ内に `let c: color = ...` があるとき、`let x: color = _` で `c` が
      `local binding` 候補として提示され、選択すると穴が `c` に置換される
- [ ] 期待型と型 head が一致しないローカル束縛は候補に出ない
      （`type colors` と `color` の prefix 衝突を `matchesTypeHead` 相当で排除）
- [ ] 編集中の束縛自身（`let x` の `x`）は local binding 候補に出ない
- [ ] キャレット直前が `_` でない（例えば `let x: color = ` の空値位置や `let x: color = R`）場合、
      本機能の候補は出ない（#117 の挙動には干渉しない）
- [ ] 期待型が `Unknown`（解決不能）で、かつ一致するローカル束縛も無いときは何も出さない
- [ ] すべての純ロジッククラスに単体テストがあり、LSP 無しでグリーンになる
- [ ] `CompletionContributor` が `plugin.xml` に登録され、既存 `RescriptPlaceholderCompletionContributor`
      と共存して両方の候補が出る

## 4. 非機能要件・制約

- **LSP 非依存:** v1 はすべて純構文。LSP 未起動環境でも完全動作する。
- **テスト容易性:** 検出・スキャン・雛形生成を純ロジック（object / data class）に分離し、
  Contributor 本体は薄いアダプタに留める（#117 と同じ設計方針）。
- **セキュリティ:** 外部入力は扱わない（トークン列のみ）。絶対パスの露出なし。
- **既存挙動の非破壊:** #117 placeholder completion・他 Contributor の候補に干渉しない。

## 5. 再利用する既存資産

| 資産 | 用途 |
|------|------|
| `completion/RescriptTypeAnnotationContext` | 型注釈付き `let` 値位置の検出・期待型 head 抽出 |
| `completion/RescriptPlaceholderTypeResolver` + `lang/TypeShape` | 期待型 head → variant/record 解決（built-in + stub index） |
| `lang/VariantConstructor` (payload 情報) | case split のアーム生成 |
| `intention/RescriptMissingArmsBuilder` の arm 規則 | `Ctor(_)` / `Ctor` の payload 判定方針を踏襲 |
| `RescriptLexer` / `RescriptTokenTypes` | 純構文トークン走査 |
| `RescriptPlaceholderCompletionContributor` の insert handler | 穴へキャレットを park する挿入パターン |
