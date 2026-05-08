# Add Missing Switch Arms Intention — Design

## 1. アーキテクチャ概要

```
RescriptAddMissingSwitchArmsIntention (intention/)
   │
   ├─ isAvailableInRescript(): SWITCH トークンの有無 + builder.computeMissing() の null チェック
   │       └─ LSP 呼び出し前のキャッシュ目的で builder を 1 度走らせるが、constructors=emptyList で
   │          「最低 switch がそこにあること」だけ検証する。フル判定は invoke() 側で行う
   │
   └─ invoke(): 
       ├─ scrutineeRange 取得（builder の helper 経由）
       ├─ RescriptLspUtils.getHoverType(project, file, scrutineeOffset)
       ├─ parseVariantConstructors(typeText)
       ├─ builder.computeMissing(source, offset, constructors)
       └─ document.replaceInWriteAction で挿入

RescriptMissingArmsBuilder (intention/, pure object)
   ├─ data class MissingArmsResult(insertOffset, insertText, missingNames)
   ├─ fun findEnclosingSwitchArms(source, offset): List<SwitchArm>?
   │      RescriptSwitchArmCollector.collect() の出力から、最も狭い scrutineeRange を選ぶ
   ├─ fun extractCoveredNames(source, arms): CoveredSet
   │      パターン文字列を再 lex して UIDENT/SOME/NONE/POLY_VARIANT を収集。
   │      _ や LIDENT-only パターンに当たれば WildcardSeen を返す
   ├─ fun computeMissing(source, offset, constructors): MissingArmsResult?
   └─ fun buildInsertion(arms, missingConstructors): String
          既存アームの先頭インデントを真似た改行+インデント連結
```

## 2. パッケージ構成

```
src/main/kotlin/com/rescript/plugin/intention/
├── RescriptAddMissingSwitchArmsIntention.kt   (new)
└── RescriptMissingArmsBuilder.kt              (new)

src/test/kotlin/com/rescript/plugin/intention/
├── RescriptAddMissingSwitchArmsIntentionTest.kt   (new, light fixture)
└── RescriptMissingArmsBuilderTest.kt              (new, pure)

src/main/resources/META-INF/plugin.xml
└── <intentionAction> 1 件追加
```

## 3. 主要データ型

```kotlin
sealed class CoveredSet {
    object WildcardSeen : CoveredSet()
    data class Names(val names: Set<String>) : CoveredSet()
}

data class MissingArmsResult(
    val insertOffset: Int,
    val insertText: String,
    val missingNames: List<String>,
)
```

## 4. 主要アルゴリズム

### 4.1 対象 switch の決定（ネスト対応）

`RescriptSwitchArmCollector.collect(source)` は全 switch のアームを flat に返す。同じ switch のアームは `scrutineeRange` が等しい性質を利用してグループ化する。

```kotlin
fun findEnclosingSwitchArms(source: String, offset: Int): List<SwitchArm>? {
    val all = RescriptSwitchArmCollector.collect(source)
    val candidateGroups = all
        .groupBy { it.scrutineeRange }
        .filter { (range, arms) ->
            // offset が switch 全体（scrutinee start から末尾アームの bodyEnd まで）に含まれる
            val end = arms.maxOfOrNull { it.bodyEndOffset } ?: return@filter false
            offset >= range.startOffset && offset <= end
        }
    if (candidateGroups.isEmpty()) return null
    // 最も狭い range = 最内 switch
    return candidateGroups.minBy { (range, _) -> range.length }.value
}
```

### 4.2 Covered constructor 抽出

各アームの `source.substring(arm.patternOffset, arm.arrowOffset - 2)` を `RescriptLexer` で再トークン化し、

- `UIDENT` / `SOME` / `NONE` / `POLY_VARIANT` → 名前を Names セットに追加（`SOME` → `"Some"` のように正規化）
- `WHEN` トークン以降は無視
- パターン全体が `UNDERSCORE` 単独、または LIDENT 単独（`UIDENT` を 1 つも含まない）→ `WildcardSeen` を即返却（全カバー扱い）

POLY_VARIANT の値は `#Foo` 形式なので、`#` を含めて Names に格納する。`parseVariantConstructors` は通常の Variant のみを返すため POLY_VARIANT 利用時は実質的には constructors が空 → builder は null を返す。

### 4.3 挿入文字列とオフセット

最後のアームの `bodyEndOffset`（次の `|` または閉じ `}` の直前位置）を挿入位置とする。インデントは「先頭アームの patternOffset 行頭からの空白文字列」を真似る。

```kotlin
fun buildInsertion(arms: List<SwitchArm>, missing: List<VariantInfo>, source: String): String {
    val firstArm = arms.first()
    // patternOffset の行頭からインデントを抽出
    val lineStart = source.lastIndexOf('\n', firstArm.patternOffset - 1) + 1
    val indent = source.substring(lineStart, firstArm.patternOffset - 1) // exclude leading "|"
    return missing.joinToString("") { v ->
        val pat = if (v.hasPayload) "${v.name}(_)" else v.name
        "${indent}| $pat => todo\n"
    }
}
```

挿入後に末尾 `\n` で 1 行空けることで、閉じ `}` が新しい行に来る前提のフォーマットを保つ。

### 4.4 isAvailableInRescript

軽量化のため LSP 呼び出しは行わず、

1. 現在オフセット周辺の token が `SWITCH` を含む switch ブロック内か（`findEnclosingSwitchArms` で判定）
2. `WildcardSeen` の場合は false
3. それ以外は true（実際の constructor 計算は invoke 時）

これにより、Alt+Enter メニューを開く度に LSP リクエストが飛ぶことを避ける。`invoke` 時に constructors が空または完全カバーなら no-op で抜ける。

## 5. テスト戦略

### 5.1 RescriptMissingArmsBuilderTest（pure）

| テストケース | 入力 | 期待 |
|---|---|---|
| option / Some のみ | `switch x { \| Some(_) => 1 }` + `[Some, None]` | `None` を提案 |
| Result 完全 | `switch x { \| Ok(_) => ... \| Error(_) => ... }` + `[Ok, Error]` | null |
| 自前 variant 一部 | `switch x { \| Red => 0 \| Green => 1 }` + `[Red, Green, Blue]` | `Blue` を提案 |
| or-pattern | `switch x { \| Red \| Green => 0 }` + `[Red, Green, Blue]` | `Blue` のみ提案 |
| wildcard | `switch x { \| Some(_) => 1 \| _ => 0 }` + `[Some, None]` | null |
| LIDENT bind | `switch x { \| anything => 0 }` + `[Some, None]` | null |
| nested switch | 内側 switch にカーソル | 外側ではなく内側の missing |
| 不完全 (} 欠落) | `switch x { \| Some(_) => 1` | null |

### 5.2 RescriptAddMissingSwitchArmsIntentionTest（light fixture）

`isAvailableInRescript` の分岐のみを fixture でカバー（書き込み動作は builder のテストでカバー済）。LSP hover の実際の呼び出しテストは現行 `RescriptCaseSplitIntentionTest` でも省略されており同方針。

- switch 外のオフセット → false
- カバー済 switch → false
- 未カバー switch → true（constructors を引数取らない簡易判定）

## 6. プラグイン互換性

- 新規 Extension Point は `intentionAction` 1 件のみ。既存 alphabet 順位置に追加
- Deprecated API を新規利用しない
- KDoc は class / object に必須付与

## 7. リスク

| リスク | 影響 | 緩和 |
|---|---|---|
| or-pattern 検出の見落とし | over-suggest | 再 lex で `|` を pattern 内 token として安全に拾う |
| インデント推定ミス | 不格好な挿入結果 | 先頭アームの行頭空白を機械的に再利用 |
| LSP hover 取得失敗 | 機能が動かない | invoke で no-op、isAvailable は LSP に依存しない |
