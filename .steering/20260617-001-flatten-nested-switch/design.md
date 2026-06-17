# 設計: ネスト switch 平坦化 Intention (#114)

## 全体構成

`intention/` パッケージに 2 クラスを追加する。最近の Intention（Expand Open Qualifier =
planner + intention の分離）と同じく、**純粋ロジック** と **IDE 統合の薄いラッパー** を分ける。

| クラス | 責務 | テスト |
|--------|------|--------|
| `RescriptNestedSwitchFlattener` (object) | `String + caretOffset → FlattenPlan?` の純関数。検出・パターン置換・置換テキスト生成 | 必須（純ロジック） |
| `RescriptFlattenNestedSwitchIntention` (class) | `RescriptBaseIntention` 派生。planner を呼び `isAvailable` / `invoke` を実装 | 純ロジックは flattener 側、intention 自体は editor 依存のため薄く保つ |

`RescriptSwitchArmCollector` は「ファイル全体の全アーム列挙」を返すが、本機能が必要とするのは
「キャレット直上の外側アームと、その本体の内側 switch の構造」という局所情報であり、
さらに外側パターンの **束縛トークン位置** と内側パターンの **トークン列** が要る。collector の
`SwitchArm` はこれらを保持しないため、collector が用いるのと同じ lexer 走査手法
（`RescriptLexer` を 1 回流してトークン列を作り、paren/brace 深度で境界を追う）を踏襲した
専用アナライザを flattener 内に実装する。トークン分類・summarize の発想は collector に倣う。

## データモデル

```kotlin
/** A single inner arm: its pattern token text and body text (verbatim). */
internal data class InnerArm(val patternText: String, val bodyText: String)

/**
 * A computed flatten transformation: the text range of the outer arm to
 * replace (from its leading `|` to the end of its body) and the
 * replacement text containing the flattened arms.
 */
data class FlattenPlan(
    val replaceStart: Int,   // offset of the outer arm's leading `|`
    val replaceEnd: Int,     // exclusive end of the outer arm body
    val replacementText: String,
    val armCount: Int,       // number of generated arms (for the intention text/preview)
)
```

## アルゴリズム

### 1. トークン化

`RescriptLexer` を 1 回流し、空白・改行・コメントを除いた `LexedToken(type, start, end, text)`
列を作る（collector の `tokenize` と同じ）。コメント除去後のトークン位置（start/end）は
元ソースのオフセットを保持するので、置換レンジ算出に使える。

### 2. キャレットを含む外側 switch とアームの特定

- キャレット offset を含む最も内側の `switch ... { ... }` を、トークン列の brace/paren 深度で特定する
- ただし本機能の「外側」は **平坦化の起点となるアーム**。キャレットが内側 switch 上にある場合も、
  その親アームを外側として扱えるよう、キャレットを含むアームを brace 深度 1 で走査して見つける。
  実装方針: キャレットを含む switch を見つけたあと、その switch のアーム列（深度 1 の `|`〜次の `|`/`}`）を
  構築し、キャレットがどのアームの範囲（`|` 〜 body 末尾）に入るかを判定する。
  → 入れ子では「キャレット位置のアーム」が外側か内側かで挙動が変わるが、v1 は
  **キャレットが乗っているアームを外側候補** とし、その本体が内側 switch のみかを検査する
  （内側 switch の中にキャレットがあるケースは、当該 switch のアームには「本体=内側 switch」が
  成立しないので自然に非適用となる。明確な動作にするため、後述の利用ガイドで「外側アームの
  パターン行または `=>` 付近にキャレットを置く」と案内する）

### 3. 適用可能性判定（FlattenPlan を組めるか）

外側候補アームについて以下を順に検査し、1 つでも外れたら `null` を返す（Intention 非表示）:

1. **`when` ガードなし**: アームのパターントークンに `WHEN` を含まない
2. **束縛が 1 つ**: 外側パターンが導入する LIDENT 束縛がちょうど 1 個。
   判定は collector の `collectBindingOffsets` と同じ規則（深度問わず LIDENT を束縛とみなす。
   ただし bare LIDENT 単独 = catch-all も「束縛 1 個」として許可する。`None` のような UIDENT 単独や
   `Loaded(a, b)` は不可）。束縛トークンそのもの（置換対象）も記録する
3. **本体が内側 switch のみ**: アーム本体トークン列が `switch` で始まり、その内側 switch の
   閉じ `}` がアーム本体の最後の非自明トークンであること（前後に他トークンがない。本体を
   囲む冗長な `{ ... }` は 1 重まで許容して剥がす）
4. **内側スクラティニー = 外側束縛**: 内側 `switch` の scrutinee トークンが
   単一の LIDENT で、外側束縛 LIDENT のテキストと一致する
5. **内側アームに or-pattern / `when` なし**: 内側各アームのパターンに深度 1 の `|`（追加の
   or 区切り）や `WHEN` を含まない

### 4. 置換テキスト生成

- 外側パターンのテキストを、束縛トークン位置で「前半 + `<innerPattern>` + 後半」に再構成する。
  例: 外側 `Some(y)`、束縛 `y` → 前半 `Some(`、後半 `)`。内側パターン `Some(z)` を挟んで
  `Some(Some(z))`。bare 束縛（外側パターン = `y`）の場合は前半・後半が空なので結果は内側パターン
  そのもの（`Some(z)`）
- 各内側アーム `InnerArm(patternText, bodyText)` について
  `"${indent}| ${newPattern} => ${bodyText}"` を生成し、改行連結する
- インデントは外側アームの `|` のカラム位置（行頭からのスペース数）を踏襲する
- 本体テキストは内側アーム body を **verbatim** で流用（trim して 1 行/複数行を保つ）。
  複数行 body のインデント正規化は v1 では行わず原文維持（format で吸収）

### 5. 置換適用（intention.invoke）

`FlattenPlan` の `replaceStart..replaceEnd` を `replacementText` で置換する。
`RescriptEditorUtils.replaceInWriteAction(project, start, end, text)`（MergeSwitchCases と同じ）を
使い、単一 `WriteCommandAction` にまとめる。

## 確認ダイアログ

MergeSwitchCases 同様、確認ダイアログは出さず即適用（Undo で戻せる純構文変換のため）。
Intention テキストは固定 `"Flatten nested switch"`。

## エッジケース整理

| 入力 | 挙動 |
|------|------|
| `Some(y) => switch y { Some(z)=>a \| None=>b }` | `Some(Some(z))=>a` / `Some(None)=>b` |
| `Ok(v) => switch v { ... }` | `Ok(<inner>)=>...` |
| `y => switch y { ... }`（bare 束縛） | 各内側パターンがそのまま外側パターンに |
| `None => switch y { ... }`（束縛 0） | 非表示 |
| `Loaded(a,b) => switch a { ... }`（束縛 2） | 非表示 |
| `Some(y) => switch f(y) { ... }`（scrutinee 不一致） | 非表示 |
| 内側に `\| A \| B => ...`（or-pattern） | 非表示 |
| 内側 / 外側に `when` | 非表示 |
| 本体が `{ let r = ...; switch y {...} }` | 非表示（本体が switch のみでない） |

## テスト設計（`RescriptNestedSwitchFlattenerTest`）

純関数 `plan(source, caretOffset): FlattenPlan?` を直接呼ぶ。

- option ネスト → 期待 replacementText を文字列一致で検証
- result ネスト
- bare 束縛
- 束縛 0 / 2 個 → null
- scrutinee 不一致 → null
- 内側 or-pattern → null
- 内側 `when` / 外側 `when` → null
- 本体に他式混在 → null
- インデント踏襲（先頭スペース数）
- replaceStart/replaceEnd が外側アームの `|`〜body 末尾を指すこと

Intention クラスは light fixture (`BasePlatformTestCase` 相当の既存ユーティリティ) で
`isAvailable` true/false と `invoke` 後のドキュメント内容を 1〜2 ケース検証する
（editor 経由の結線確認。ロジック本体は flattener テストで担保）。

## plugin.xml 登録

既存 Intention の並びに従い、`<intentionAction>` を 1 件追加する:

```xml
<!-- Intention: Flatten nested switch (#114) -->
<intentionAction>
    <language>ReScript</language>
    <className>com.rescript.plugin.intention.RescriptFlattenNestedSwitchIntention</className>
    <category>ReScript</category>
</intentionAction>
```

## ドキュメント更新

- `CLAUDE.md` レイヤー 3: Intention 一覧に追記
- `docs/repository-structure.md` の `intention/` 行の代表クラス例に追記
- `README.md` Features（Intention カテゴリ）に追記
- `sphinx-docs/user/features/code-editing.md` に変換例つきで追記 + `locale/ja` の `.po` 同期
- `docs/product-requirements.md` の将来機能テーブルから #114 行を削除
