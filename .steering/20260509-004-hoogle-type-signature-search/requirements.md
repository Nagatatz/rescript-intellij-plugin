# Hoogle-style Type Signature Search — Requirements

## 背景

`navigation/RescriptTypeSignatureSearchContributor` は既存の Search Everywhere タブとして "ReScript Types" を提供しているが、現状は単純なトークンの substring 包含で照合しており、型の構造 (arrow / tuple / type application) を理解しない。そのため:

- "functions returning `result<int, string>`" のような **位置を意識した検索** ができない
- `option<int>` で検索して `let map: option<'a> => option<'b>` がヒットしてしまう (位置構造を見ないため)
- 検索結果に signature が表示されない (`GotoFileCellRenderer` が file 名のみ出すため、何が hit したか分かりづらい)

本機能は Haskell の Hoogle に着想を得た **構造ベースの型シグネチャ検索** を提供する。クエリと候補の双方を ReScript 型式として簡易パーサで AST 化し、構造的に照合してスコアリングする。

既存資産:

- `RescriptTypeSignatureSearchContributor` (Search Everywhere エントリポイント)
- `RescriptPsiUtils.NAVIGABLE_TYPES` (let / external / type 宣言の PSI element type)
- 各種 lexer / token type (`UIDENT` / `LIDENT` / `LPAREN` / `LT` / `GT` / `ARROW` / `COMMA` / `QUOTE` / `COLON`)

## ユーザーストーリー

### US-01: 構造ベースの正確な型検索

**ReScript 開発者として**、`(int, string) => result<int, string>` のような型シグネチャを Search Everywhere に入力すると、その構造 (arrow / tuple / applied ctor) を保ったまま実装が一致する関数を見つけたい。

**受け入れ条件:**

- [ ] クエリ `int => int` は `let f: int => int = …` を **EXACT** スコアでヒット
- [ ] クエリ `int => int` は `let g: int => string = …` を **MISMATCH** で除外
- [ ] クエリ `'a => 'a` は `let id: 'a => 'a` を EXACT、`let f: int => int` を **TVAR_MATCH** (より低い weight) でヒット
- [ ] クエリ `option<int>` は `let h: option<int> => option<int>` を **PARTIAL** (右側の return position が一致) でヒット
- [ ] クエリ `option<'a>` は `let map: option<'a> => option<'b>` を TVAR_MATCH でヒット
- [ ] クエリ `=> result<int, string>` (先頭 `=>`) は **「右辺のみ一致」** モードで `let foo: int => result<int, string>` をヒット (関数の戻り値型で検索)

### US-02: 検索結果に型シグネチャを表示

**ユーザーとして**、Search Everywhere のリスト項目で各候補のシグネチャ全体を一目で確認したい。現状の `GotoFileCellRenderer` では file 名しか出ず、何にヒットしたのか分からない。

**受け入れ条件:**

- [ ] Search Everywhere 結果セルに `name: signature  (relative/path:line)` が表示される
- [ ] signature が長い場合は折り返さず末尾を `…` で省略
- [ ] selection 時のハイライトは標準と同じ挙動

### US-03: パーサと unifier の単体テスト性

**保守者として**, 型パーサと unifier ロジックは IDE fixture 無しで完全に単体テスト可能であってほしい。

**受け入れ条件:**

- [ ] `RescriptTypeAst` (sealed class) と `RescriptTypeParser.parse(text): TypeAst?` を pure object として切り出す
- [ ] `RescriptTypeUnifier.match(query: TypeAst, candidate: TypeAst): MatchScore` を pure object として切り出す
- [ ] 50+ パーサテスト (各構文ノード × 失敗ケース) と 30+ unifier テスト (EXACT / TVAR_MATCH / PARTIAL / MISMATCH の各分岐)

## スコープ外

- レコード型 `{name: string}` のパース・照合 (v2)
- ポリモーフィックバリアント `[#Foo | #Bar]` のパース・照合 (v2)
- ラベル引数 `(~name: string, …)` のパース・照合 (v2)
- 推論型 (annotation を持たない `let x = 5` の `x: int`) — LSP `documentSymbol` 取得は将来検討
- Hoogle 風のエイリアス (例: `Int -> Int` を Haskell 風に書く) — ReScript 構文のみ受け付ける
- `RescriptTypeSignatureSearchContributor` のエントリポイントクラス自体の改名・分割 — 既存クラスを差し替える

## 機能カテゴリ

- ナビゲーション (Search Everywhere の改善)
- 静的解析 (型パーサ / unifier)
