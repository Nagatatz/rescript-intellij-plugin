# Hoogle-style Type Signature Search — Design

## アーキテクチャ概要

既存の `RescriptTypeSignatureSearchContributor` を、新規 `RescriptTypeAst` / `RescriptTypeParser` / `RescriptTypeUnifier` をデリゲートで使うように差し替える。Search Everywhere の renderer も `name: signature` を表示するカスタムレンダラに替える。LSP は使わず、PSI 上に文字として現れる型注釈のみを対象にする。

```
Search Everywhere query
  ↓ RescriptTypeParser.parse(query)
TypeAst (query)
  ↓ FileTypeIndex(.res, .resi) → 各 declaration の `: T` を抽出 → parser
TypeAst (candidate)
  ↓ RescriptTypeUnifier.match(query, candidate)
MatchScore (EXACT | TVAR_MATCH | PARTIAL | MISMATCH)
  ↓ filter(MatchScore.weight > 0)
sorted weighted results
  ↓ RescriptTypeSignatureCellRenderer
"name: signature  (path:line)"
```

## TypeAst (sealed class)

```kotlin
sealed class RescriptTypeAst {
    object Unit : RescriptTypeAst()
    data class Ctor(val name: String) : RescriptTypeAst()                  // "int", "string"
    data class TypeVar(val name: String) : RescriptTypeAst()              // "'a"
    data class App(val ctor: String, val args: List<RescriptTypeAst>) : RescriptTypeAst()  // option<int>
    data class Tuple(val elements: List<RescriptTypeAst>) : RescriptTypeAst()              // (int, string)
    data class Arrow(val from: RescriptTypeAst, val to: RescriptTypeAst) : RescriptTypeAst() // a => b
    data class ReturnQuery(val target: RescriptTypeAst) : RescriptTypeAst()                // クエリ専用 "=> T"
}
```

`ReturnQuery` は **クエリ側でのみ** 構築される。「`=> result<int>` のように `=>` 始まりの入力 → return position 一致モード」。

## RescriptTypeParser

LL パーサ。ReScript の型構文の minimal subset:

```
type    := arrowType
arrowType := primary ("=>" arrowType)?         // right-assoc
primary := "'" IDENT                            // type variable
        | IDENT ("<" type ("," type)* ">")?    // ctor / type application
        | "(" ")"                              // unit
        | "(" type ")"                         // paren
        | "(" type ("," type)+ ")"             // tuple

returnQuery := "=>" type                       // query-only entry point
```

`parse(text)` は失敗時 `null` を返す。Tokenizer は手書き (lexer reuse は同期 IElementType の都合で簡素化が難しいため、ここでは目的特化の小さい char-by-char tokenizer を書く)。

## RescriptTypeUnifier

```kotlin
enum class MatchScore(val weight: Int) {
    EXACT(100),
    TVAR_MATCH(60),    // 'a が具体型に対応した一致
    PARTIAL(30),       // return position など部分一致
    MISMATCH(0),
}

fun match(query: TypeAst, candidate: TypeAst): MatchScore
```

Unification rules:

1. `query` が `ReturnQuery(target)` のとき:
   - candidate が `Arrow(_, returnType)` であれば `match(target, returnType)` を返す。`PARTIAL` 以上は `PARTIAL` に切り下げる
   - candidate が `Arrow` でなければ `MISMATCH`
2. `query == candidate` (構造完全一致) なら `EXACT`
3. query 側に `TypeVar` が現れたら、対応位置の candidate が何であってもよく、`TVAR_MATCH`
4. candidate 側にだけ TypeVar が現れる場合は `MISMATCH` (ユーザーが具体型で問い合わせているのに candidate が多相 → 別の判定にしてもよいが MVP では mismatch)
5. `App(name1, args1)` vs `App(name2, args2)`: `name1 == name2 && args1.zip(args2) all match` で再帰的に算出。args 長さが異なれば `MISMATCH`
6. `Tuple(args1)` vs `Tuple(args2)`: 長さ一致 + 各要素一致
7. `Arrow(a1, b1)` vs `Arrow(a2, b2)`: from 同士・to 同士で一致

スコアは複数要素の **最低スコア** を採用 (チェイン中の 1 つでも mismatch なら全体 mismatch)。

## Contributor の差し替え

`RescriptTypeSignatureSearchContributor.fetchWeightedElements` を:

1. `RescriptTypeParser.parse(pattern)` で query AST を作る (失敗なら no-op)
2. 各 `.res` / `.resi` ファイルの top-level 宣言を走査
3. `: signature =` 部分を抽出 → `parse(signature)` で candidate AST
4. `match(queryAst, candidateAst)` の `MatchScore.weight` を `FoundItemDescriptor` の weight に
5. weight > 0 のみを consumer に流す

既存 `looksLikeTypeQuery` / `tokenizeSignature` / `matchSignature` は新パーサ実装に置き換えられて削除可能。

## CellRenderer

```kotlin
class RescriptTypeSignatureCellRenderer : ColoredListCellRenderer<RescriptTypeSignatureSearchHit>() {
    override fun customizeCellRenderer(...) {
        append("${hit.name}: ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        append(hit.signatureDisplay, SimpleTextAttributes.GRAY_ATTRIBUTES)
        append("  (${hit.relativePath}:${hit.line})", SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }
}
```

これに合わせて `RescriptTypeSignatureSearchHit` データクラスを追加し、`fetchWeightedElements` で `FoundItemDescriptor<RescriptTypeSignatureSearchHit>(hit, weight)` を作る。`processSelectedItem` で `OpenFileDescriptor(project, hit.file, hit.declarationOffset).navigate(true)`。

## クラス構成

| パス | 内容 |
|------|------|
| `navigation/RescriptTypeAst.kt` | sealed class hierarchy + companion `unit` |
| `navigation/RescriptTypeParser.kt` | pure object |
| `navigation/RescriptTypeUnifier.kt` | pure object + `MatchScore` enum |
| `navigation/RescriptTypeSignatureSearchHit.kt` | データクラス |
| `navigation/RescriptTypeSignatureCellRenderer.kt` | カスタムレンダラ |
| `navigation/RescriptTypeSignatureSearchContributor.kt` | **差し替え** |

## kover 影響

- AST / Parser / Unifier / Hit はテスト必須 (純関数 / データ)
- CellRenderer / Contributor は IDE-coupled → kover excludes に追加。現状 contributor は `excludes/packages` の `com.rescript.plugin.navigation.*` に既に含まれているため、追加変更不要

## テスト方針

- `RescriptTypeParserTest`: 50+ ケース (Unit / Ctor / TypeVar / App / Tuple / Arrow / 失敗ケース / nested generics / right-assoc arrows / `=>` 始まりの ReturnQuery)
- `RescriptTypeUnifierTest`: 30+ ケース (EXACT / TVAR_MATCH / PARTIAL via ReturnQuery / MISMATCH / 異なる長さの App / arrow chain)
- Contributor / Renderer: テスト免除 (Search Everywhere は IDE 依存)

## 既存テストへの影響

`RescriptTypeSignatureSearchContributorTest` (もし存在すれば) — 内部 helper の名前が変わるので、テストもそれに追従する必要がある。

## ロールアウト

- 既存 contributor の signature(query, candidate) のセマンティクスは substring → 構造一致に厳格化される
- 短すぎるクエリ (例: `int`) は型変数に対しても具体型に対しても EXACT を出さない可能性が出てくるが、`Ctor("int")` vs `Ctor("int")` は EXACT、`Ctor("int")` vs `App("option", [Ctor("int")])` は MISMATCH なので、ユーザは「自分が探したい構造そのもの」を入れる必要が出てくる
- 既存ユーザのクエリで「token-substring に依存していたもの」は機能しなくなる可能性あり — README / sphinx-docs に明記
