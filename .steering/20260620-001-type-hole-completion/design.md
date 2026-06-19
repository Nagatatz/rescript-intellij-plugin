# 設計: Type Hole Completion (#115)

requirements.md の受け入れ条件を満たす実装設計。すべて `completion/` パッケージに追加する。
#117 (`RescriptPlaceholderCompletionContributor`) と同じ「純ロジック分離 + 薄い Contributor」方針を踏襲する。

## 1. クラス構成

| クラス | 種別 | 責務 | テスト |
|--------|------|------|--------|
| `RescriptTypeHoleContext` | object | `let [rec] <name>: <T> = _<caret>` の型穴を検出し、期待型 head と束縛名を返す | `RescriptTypeHoleContextTest` |
| `RescriptLocalBindingScanner` | object | brace-depth 0 の `let <name>: <U> = ...` 束縛を走査し `(name, typeHead)` を返す | `RescriptLocalBindingScannerTest` |
| `RescriptCaseSplitBuilder` | object | variant constructor 列 + 行インデントから網羅 `switch _ {…}` 雛形テキストを生成 | `RescriptCaseSplitBuilderTest` |
| `RescriptTypeHoleCompletionContributor` | class (EP) | 上記を束ねて補完候補を提示する薄いアダプタ | `RescriptTypeHoleCompletionContributorTest` (light fixture) |

いずれも LSP 非依存。`RescriptTypeHoleContext` / `RescriptLocalBindingScanner` / `RescriptCaseSplitBuilder` は純関数で
fixture 不要の JUnit テストとする。Contributor のみ `RescriptPlaceholderTypeResolver.resolve` のために project を要し、
light fixture でワイヤリングを検証する。

## 2. 各クラス詳細

### 2.1 RescriptTypeHoleContext

```kotlin
data class TypeHoleDetection(
    val expectedTypeHead: String,  // 期待型の head 名 (option<int> → "option", Belt.Map.t → "Belt")
    val bindingName: String,       // 編集中の束縛名 (self 除外用)
)

object RescriptTypeHoleContext {
    fun detect(textBeforeCaret: String): TypeHoleDetection?
}
```

検出ロジック（`RescriptLexer` でトークン化し WHITE_SPACE / EOL / コメントを除外した列に対して）:

1. 非トリビアルトークン列の **末尾が `_` (UNDERSCORE)** であること。違えば null（→ #117 の空値位置・部分識別子に非干渉）。
2. その `_` の直前トークンが `=` (EQ) であること（値が厳密に `= _` の形）。
3. EQ より前を `let [rec] <name> : <type...>` として検証:
   - 先頭が `let` (LET) キーワード（行頭でなくとも、対象 `=` を支配する直近の `let`）。実装は #117 と同じく
     **末尾の `=` を起点に左方向**へ `let` を探索する（`RescriptTypeAnnotationContext` の `findAnnotationColon`/EQ 起点走査と同じ要領）。
   - `let` の次（`rec` があれば飛ばす）の IDENTIFIER を `bindingName` とする。
   - その直後が `:` (COLON)。`:` から `=` までを型注釈テキストとし、**depth-0** の範囲で切り出す
     （`<...>` / `(...)` / `[...]` / `{...}` のネストを数え、外側の `=` のみを境界とする）。
   - 型注釈テキストの先頭から `LEADING_IDENTIFIER = Regex("^[A-Za-z_][A-Za-z0-9_']*")` で head 名を抽出。
4. 上記すべて満たせば `TypeHoleDetection(head, bindingName)`、さもなくば null。

> 設計判断: #117 の `RescriptTypeAnnotationContext` は head 抽出ロジックを private に持つため、本クラスは
> 同等ロジックを自前で持つ（束縛名抽出という追加責務があるため再利用より複製が明快）。head 抽出の正規表現定数は
> 両者で一致させ、挙動の差異を出さない。

### 2.2 RescriptLocalBindingScanner

```kotlin
data class LocalBinding(val name: String, val typeHead: String)

object RescriptLocalBindingScanner {
    fun scan(source: String): List<LocalBinding>
}
```

- `source`（呼び出し側は caret までの `textBeforeCaret` を渡す）をトークン化し、`{` `}` の **brace-depth** を追跡。
- depth 0 で `let` [`rec`] IDENTIFIER `:` <type…> `=` のパターンを検出するたびに `(name, typeHead)` を収集。
  type head は 2.1 と同じ `LEADING_IDENTIFIER`（`:` 直後から depth-0 で `=` までを切った注釈テキストの先頭識別子）。
- **同名は最後の宣言で上書き**（後勝ち）。出現順を保ったまま重複名を除去して返す。
- 値部分の中身は解釈しない（`=` 検出後はパターンマッチをリセットして次の `let` を待つ）。
- 編集中の束縛 `let x: T = _` も `(x, T)` として含まれる → 除外は Contributor 側で `bindingName` 一致により行う。

型 head の一致判定は **完全一致**（`==`）。head は識別子トークン全体を取るため、`colors` と `color` は等しくならず、
prefix 衝突（`type colors` を `color` 穴に出す誤爆）は自然に排除される（requirements の `matchesTypeHead` 相当）。

### 2.3 RescriptCaseSplitBuilder

```kotlin
object RescriptCaseSplitBuilder {
    /** @return switch skeleton text, or null if [constructors] is empty. */
    fun build(constructors: List<VariantConstructor>, lineIndent: String): String?
}
```

`constructors` が空なら null。さもなくば:

```
switch _ {
<lineIndent>| <arm1>
<lineIndent>| <arm2>
<lineIndent>}
```

- 各 arm = `${name}(_) => _`（`payload != null`）または `${name} => _`（payload なし）。
  payload 判定は `VariantConstructor.payload` の null 性で行い、`RescriptMissingArmsBuilder` の arm 規則と一致させる。
- `lineIndent` は穴のある行の行頭空白（Contributor が算出して渡す）。arm と閉じ `}` をその列に揃える。
- 先頭行 `switch _ {` の `_`（index 7 のスクラティニー穴）が `insertText` 中で最初に出現する `_` になる
  → Contributor の caret parking（最初の `_` へ移動）がそのままスクラティニーを指す。

### 2.4 RescriptTypeHoleCompletionContributor

`CompletionContributor` を継承（EP: `completion.contributor` language=ReScript）。`fillCompletionVariants`:

1. `completionType == BASIC` / `position.language == RescriptLanguage` を確認。
2. `textBeforeCaret = document.getText(TextRange(0, caretOffset))`（`caretOffset > textLength` ガード）。
3. `detection = RescriptTypeHoleContext.detect(textBeforeCaret) ?: return`。
4. **prefix matcher を空に差し替える**: `val out = result.withPrefixMatcher("")`。
   理由: キャレット直前の `_` が補完 prefix になり、`switch`/束縛名のような候補が prefix `_` に一致せず表示されないため。
   `_` の置換レンジ（`context.startOffset..tailOffset`）は CompletionInitializationContext が決めるので、空 matcher にしても
   挿入時に `_` は正しく置換される。
5. **(A) local binding fill**:
   - `RescriptLocalBindingScanner.scan(textBeforeCaret)` の各 `b` について
     `b.typeHead == detection.expectedTypeHead && b.name != detection.bindingName` を満たすものを
     `holeElement(insertText = b.name, typeText = "local binding")` で `out.addElement`。
6. **(B) case split**:
   - `shape = RescriptPlaceholderTypeResolver.resolve(position.project, detection.expectedTypeHead)`。
   - `shape is TypeShape.Variant` のとき `RescriptCaseSplitBuilder.build(shape.constructors, lineIndent)` を作り、
     非 null なら `holeElement(insertText = text, typeText = "case split", lookupString = "switch")` で追加。
   - `lineIndent` は `textBeforeCaret` の最終行（最後の `\n` 以降）の先頭空白。
7. 候補が無ければ何も追加しない（穴に対する他 Contributor の挙動は不変）。

挿入ハンドラは #117 と同一ロジック:

```kotlin
private fun applyInsertion(context: InsertionContext, insertText: String) {
    context.document.replaceString(context.startOffset, context.tailOffset, insertText)
    val holeIndex = insertText.indexOf('_')
    val caret = if (holeIndex >= 0) context.startOffset + holeIndex
                else context.startOffset + insertText.length
    context.editor.caretModel.moveToOffset(caret)
}
```

- local binding fill: `insertText` に `_` は無い → caret は名前末尾へ。
- case split: 最初の `_`（スクラティニー）へ caret。

## 3. plugin.xml 登録

`<extensions defaultExtensionNs="com.intellij">` 内、既存の completion.contributor 群（`RescriptPlaceholderCompletionContributor` の隣）に追加:

```xml
<completion.contributor language="ReScript"
    implementationClass="com.rescript.plugin.completion.RescriptTypeHoleCompletionContributor"/>
```

## 4. テスト方針

| テスト | 検証内容 |
|--------|----------|
| `RescriptTypeHoleContextTest` | `let x: color = _` → `(color, x)`；`let x: color = ` / `let x: color = R` / `let x = _`（注釈なし）→ null；`rec`/ネスト型 `option<color>` の head 抽出；`Belt.Map.t` → `Belt` |
| `RescriptLocalBindingScannerTest` | depth-0 束縛の列挙；`{ }` 内（depth>0）束縛の除外；同名後勝ち；注釈なし `let` の無視；head 抽出（`colors` vs `color` の非一致） |
| `RescriptCaseSplitBuilderTest` | payload あり/なし混在の arm 生成；空 constructor → null；インデント反映；先頭 `_` がスクラティニー |
| `RescriptTypeHoleCompletionContributorTest` | light fixture で `type color = Red \| Green` を置き、`let x: color = _<caret>` 補完に `case split` と（束縛があれば）`local binding` 候補が出ること、選択後のテキスト・caret 位置 |

`RescriptTypeHoleCompletionContributorTest` は stub index 解決のため light fixture（`BasePlatformTestCase` 相当）を使う。
純ロジック 3 クラスは fixture 不要。

## 5. ドキュメント更新（DoD Phase 3）

| ドキュメント | 内容 |
|-------------|------|
| `docs/repository-structure.md` | `completion/` 行の代表クラスに 4 クラスを追記 |
| `docs/functional-design.md` | 補完カテゴリに Type Hole Completion の解説・EP マップ追加 |
| `README.md` | Features の補完カテゴリに 1 項目追加 |
| `sphinx-docs/user/features/code-completion.md` | 機能説明 + 変換例（`let x: color = _` → case split / local binding） |
| `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` | 上記 EN 追記分の `msgstr` を日本語で同コミット更新 |
| `CLAUDE.md` | 補完は既存カテゴリのため **更新不要**（新カテゴリ無し） |
| `docs/product-requirements.md` | ロードマップ #115 行を削除 |

## 6. 既知の制約（v1 / 将来検討）

- 型注釈付き `let` 値位置の `_` のみ対象（LSP hover 連携は将来）。
- local binding は brace-depth 0（モジュール直下）のみ。関数内ローカル束縛は対象外。
- case split のスクラティニーは `_` のまま（一致束縛の自動埋め込みは将来）。
- record 期待型の case split は非対象（#117 のリテラル雛形で充足）。
- 入れ子型の再帰展開なし（穴は 1 階層）。
