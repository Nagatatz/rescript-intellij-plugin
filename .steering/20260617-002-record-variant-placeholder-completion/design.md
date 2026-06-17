# 設計: Record / Variant Placeholder 補完 (#116)

## 全体方針

純ロジック (注釈検出 + 雛形生成) を IDE 非依存クラスに切り出し、
`CompletionContributor` はそれらを組み立てる薄い orchestrator に徹する
(`intention/RescriptMissingArmsBuilder` + thin wrapper パターンを踏襲)。

```
RescriptPlaceholderCompletionContributor (IDE wrapper, completion/)
  ├─ RescriptTypeAnnotationContext.detectExpectedType(textBeforeCaret)  : 純ロジック
  ├─ RescriptPlaceholderTypeResolver.resolve(project, typeName)         : stub index + 既存パーサ
  └─ RescriptPlaceholderBuilder.build*(...)                             : 純ロジック (雛形文字列)
```

## 新規クラス

### 1. `completion/RescriptTypeAnnotationContext` (object, 純ロジック)

キャレット直前テキストから「型注釈付き let 値位置」を検出し型名を返す。

```kotlin
object RescriptTypeAnnotationContext {
    /**
     * Detects whether the caret sits at the value position of a
     * type-annotated let binding and returns the annotated type's head name.
     *
     * @param textBeforeCaret document text from start to the caret offset
     * @return the head type identifier (type args stripped), or null
     */
    fun detectExpectedType(textBeforeCaret: String): Detection?

    data class Detection(
        val typeName: String,      // head identifier, e.g. "person", "option"
        val afterOpenBrace: Boolean // caret is already inside a "{" (record skip flag)
    )
}
```

検出ルール (末尾アンカー):
- 末尾の空白/改行を無視した上で、直近が `=` または `= {` / `={` で終わるか確認。
- そこから手前に向かって `let <name>:` の型注釈節を探し、`:` と `=` の間のテキストを型注釈として取り出す。
- 型注釈から head 識別子を抽出 (型引数 `<...>` を剥がす)。`array<...>` `Belt.Map.t` などドット区切りは末尾セグメントではなく **先頭セグメント** を head とする (`option<int>` → `option`)。
- `=` の後にすでに識別子の一部 (LIDENT/UIDENT 断片) が入力されていてもよい (補完プレフィックスとして扱う)。
- `=` の後に既に `{` がある場合は `afterOpenBrace = true` を返し、呼び出し側が record 候補を抑止する。
- パターンに合致しなければ null。

実装は `RescriptLexer` のトークン走査を基本とし、正規表現は型注釈断片の head 抽出にのみ補助的に使う。
誤爆を避けるため、`let` キーワードトークンを起点に `LIDENT : <type> =` の並びを検証する。

### 2. `completion/RescriptPlaceholderTypeResolver` (object)

型名を `RescriptTypeDeclarationParser.TypeShape` に解決する。

```kotlin
object RescriptPlaceholderTypeResolver {
    fun resolve(project: Project, typeName: String): TypeShape?
}
```

- built-in: `option` / `result` は `TypeShape.Variant` を直接生成
  (`option` → `Some(payload)` / `None`、`result` → `Ok(payload)` / `Error(payload)`)。
- それ以外: `RescriptVariantTypeResolver` 同様に
  `StubIndex.processElements(RescriptNameIndex.KEY, typeName, ...)` で
  `type <typeName> = ...` 宣言の PSI を取得 → 宣言テキストを
  `RescriptTypeDeclarationParser.parse` で `TypeShape` 化して返す。
- 宣言が複数ヒットした場合は最初の Record/Variant を採用、Unknown はスキップ。
- `RescriptVariantTypeResolver.resolve` は variant 専用 (record を扱わない) のため、
  record も返せる本リゾルバを新設する。共通の stub-index lookup ヘルパーがあれば再利用するが、
  v1 では本クラスに閉じて実装し重複は最小に留める。

### 3. `completion/RescriptPlaceholderBuilder` (object, 純ロジック)

`TypeShape` から補完挿入文字列を生成する。

```kotlin
object RescriptPlaceholderBuilder {
    /** "{ name: _, age: _ }" — empty fields → null */
    fun buildRecordPlaceholder(fields: List<RecordField>): String?

    /** Per-constructor insertion strings: "Some(_)", "None", "Rgb(_)" ... */
    fun buildVariantPlaceholders(constructors: List<VariantConstructor>): List<VariantPlaceholder>

    data class VariantPlaceholder(
        val lookupName: String,  // "Some", shown in completion list
        val insertText: String   // "Some(_)" or "None"
    )
}
```

- record: `{ ` + フィールドを `name: _` で `, ` 連結 + ` }`。mutable は無視 (値構築では不要)。フィールド 0 件なら null。
- variant: payload (`VariantConstructor.payload != null`) があれば `Name(_)`、無ければ `Name`。

### 4. `completion/RescriptPlaceholderCompletionContributor` (CompletionContributor, IDE wrapper)

```kotlin
class RescriptPlaceholderCompletionContributor : CompletionContributor {
    override fun fillCompletionVariants(parameters, result) {
        // 1. ReScript ファイルか確認
        // 2. textBeforeCaret = document.text[0, caretOffset]
        // 3. detection = RescriptTypeAnnotationContext.detectExpectedType(textBeforeCaret) ?: return
        // 4. shape = RescriptPlaceholderTypeResolver.resolve(project, detection.typeName) ?: return
        // 5. shape が Record かつ !afterOpenBrace → record 候補 1 件
        //    shape が Variant → constructor 候補 N 件
        // 6. LookupElementBuilder で result.addElement(...)
    }
}
```

- record 候補: `LookupElementBuilder.create(insertText).withPresentableText("{ ... }").withTypeText(typeName)`。
- variant 候補: `LookupElementBuilder.create(insertText).withLookupString(lookupName).withTypeText(typeName)`。
- 補完候補挿入は LookupElement の既定 (キャレット位置のプレフィックス置換) に委ねる。
- `@Deprecated` API は使用しない。`CompletionType.BASIC` のみ対象。

## plugin.xml 登録

`<extensions defaultExtensionNs="com.intellij">` 内、既存 `completion.contributor` (Decorator) の近傍に
アルファベット順を保って追加:

```xml
<completion.contributor language="ReScript"
                        implementationClass="com.rescript.plugin.completion.RescriptPlaceholderCompletionContributor"/>
```

## テスト設計

| クラス | テスト | fixture |
|--------|--------|---------|
| `RescriptTypeAnnotationContext` | 検出 (record/variant/型引数/ドット型/`{` 済み/注釈なし/誤爆ケース) | 不要 (pure) |
| `RescriptPlaceholderBuilder` | record 0/1/N field, variant payload 有無, option/result | 不要 (pure) |
| `RescriptPlaceholderTypeResolver` | built-in option/result の解決 | 純判定部はテスト。stub index 部は heavy fixture が要るため built-in と Unknown 経路を中心に検証 |
| `RescriptPlaceholderCompletionContributor` | — | Swing/IDE 結合のため免除 (testing.md: CompletionContributor の fillCompletionVariants は light fixture 困難)。免除理由を tasklist に明記 |

> 注: Decorator contributor の既存テストは Proxy stub で `fillCompletionVariants` を直接駆動せず補助メソッドのみ検証している。本機能も純ロジックを完全にテストし、contributor 本体は免除扱いとする。

## エッジケース

- 型注釈が `option<result<int, string>>` のようにネスト → head は `option`、payload は `_` 1 個。
- 型名が大文字始まり (モジュール修飾なし variant 型名は通常小文字だが) → head 抽出は識別子をそのまま使う。
- `type` 宣言が `.resi` にしか無い場合も stub index 経由で解決可能。
- 宣言テキストが巨大/壊れている場合 `TypeShape.Unknown` → 候補なし。

## セキュリティ

- stub index から得る宣言テキストは `RescriptTypeDeclarationParser` のパース内で長さ・形を検証する (既存パーサの堅牢性に依存)。
- ファイルパス露出なし、外部プロセス起動なし。
