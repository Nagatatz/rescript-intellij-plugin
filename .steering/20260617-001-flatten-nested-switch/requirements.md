# 要求定義: ネスト switch 平坦化 Intention (#114)

## 背景

ロードマップ #114（Gleam LS の "flatten nested case" 由来）。`switch` のアームの本体が
「外側アームが束縛した値に対する別の `switch`」だけで構成されているとき、内側パターンを
外側パターンに畳み込んで 1 階層の `switch` に統合する Alt+Enter Intention を提供する。

ネストした `switch` は読みづらく、特に option/result/variant のネストでは
コンストラクタパターンを入れ子にした 1 階層 `switch` のほうが網羅性を把握しやすい。

## スコープ（v1）

### 対象とする変換

外側アームのパターンが **単一の束縛 (LIDENT) を 1 つだけ** 導入し、その本体が
**内側 `switch` 式のみ** であり、かつ内側 `switch` のスクラティニーが
**その束縛と同一の識別子** である場合に限り、内側アームを外側パターンに畳み込む。

変換前:

```rescript
switch x {
| Some(y) =>
  switch y {
  | Some(z) => handleZ(z)
  | None => fallback
  }
| None => none
}
```

変換後:

```rescript
switch x {
| Some(Some(z)) => handleZ(z)
| Some(None) => fallback
| None => none
}
```

外側パターン `Some(y)` の束縛位置 `y` を、各内側アームのパターンで置換して
新しい外側アームを生成する（内側本体はそのまま流用）。

### Intention を表示する条件（すべて満たす場合のみ）

1. キャレットが外側 `switch` のアーム上にある
2. そのアームの本体が **内側 `switch` 式のみ**（前後に他の式・`{}` ブロック・コメント以外の要素がない）
3. 外側アームのパターンが導入する束縛は **ちょうど 1 つの LIDENT**（例: `Some(y)` / `Ok(v)` / 単独 `y`）
4. 内側 `switch` のスクラティニーが **その束縛 LIDENT と完全一致**（`switch y { ... }`）
5. 外側アームに `when` ガードが付いていない
6. 内側アームのいずれにも `when` ガードが付いていない

### v1 では非対応（Intention 非表示にして安全側に倒す）

- 外側パターンの束縛が 0 個（`None` 等）または 2 個以上（`Loaded(a, b)` 等のタプル束縛）
- 内側スクラティニーが外側束縛と異なる式（`switch f(y)` / `switch other` 等）
- 内側アームに or-pattern (`| A | B =>`) が含まれる（外側への分配は複雑なため v1 対象外）
- 外側／内側いずれかに `when` ガードがある
- 本体に内側 `switch` 以外の式が混在する
- 3 階層以上の同時平坦化（1 回の発火で 1 階層のみ畳む。さらに畳みたい場合は再度発火）

## 機能要件

- FR-1: 上記条件を満たすキャレット位置で Alt+Enter に "Flatten nested switch" を表示する
- FR-2: 実行時、外側アームを内側アーム数ぶんの新しいアームに置換する。各新アームの
  パターンは「外側パターンの束縛位置を内側パターンで置換したもの」、本体は内側アーム本体
- FR-3: 置換は単一の `WriteCommandAction` で行い、Undo が 1 ステップで戻ること
- FR-4: 既存のインデント幅を踏襲し、生成結果が `rescript format` で大きく崩れないこと
- FR-5: LSP 非依存（純構文・lexer ベース）で、LSP 未起動環境でも動作すること

## 非機能要件

- 純粋ロジック（検出・平坦化テキスト生成）を副作用のないクラスに分離し、ユニットテストで
  決定的に検証可能にする（light fixture で完結、heavy fixture 不要）
- 既存の `intention/` パッケージ・`RescriptBaseIntention` の規約に従う
- パフォーマンス: ファイル全体を 1 回だけ lexer 走査する

## 受け入れ条件

- [ ] option ネスト（`Some(y) => switch y {...}`）が 1 階層に畳まれる
- [ ] result ネスト（`Ok(v) => switch v {...}`）が畳まれる
- [ ] 単独束縛（`y => switch y {...}`）でも畳まれる（外側パターン = 束縛そのものなので結果は内側パターンに置換）
- [ ] 内側に or-pattern / `when` ガードがある場合は Intention 非表示
- [ ] 外側束縛が 0 / 2 個以上、内側スクラティニーが束縛と不一致の場合は Intention 非表示
- [ ] 本体に内側 switch 以外の式が混在する場合は Intention 非表示
- [ ] 変換が単一 Undo で戻る
- [ ] ユニットテストで上記をカバー
- [ ] ドキュメント（CLAUDE.md / repository-structure.md / README.md / sphinx EN+JA / product-requirements.md の #114 行削除）を更新
