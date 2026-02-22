# 設計: JSON エンコーダ/デコーダ生成 (#81)

## アーキテクチャ

既存の Generate アクションフレームワーク（`RescriptGenerateMakeAction` 等）と同一パターンで実装する。

### 新規クラス

1. **`RescriptJsonTypeClassifier`** — 型注釈文字列を sealed class にマッピング
2. **`RescriptJsonCodeGenerator`** — TypeShape + RescriptJsonType から encoder/decoder テキストを生成
3. **`RescriptGenerateJsonCodecAction`** — Generate メニューアクション

### 依存関係

```
RescriptGenerateJsonCodecAction
  → RescriptGenerateActionUtil (PSI ナビゲーション)
  → RescriptTypeDeclarationParser (型宣言パース)
  → RescriptJsonCodeGenerator (コード生成)
    → RescriptJsonTypeClassifier (型分類)
```

## 型分類器 (RescriptJsonTypeClassifier)

```kotlin
sealed class RescriptJsonType {
    data object StringType : RescriptJsonType()
    data object IntType : RescriptJsonType()
    data object FloatType : RescriptJsonType()
    data object BoolType : RescriptJsonType()
    data class OptionType(val inner: RescriptJsonType) : RescriptJsonType()
    data class ArrayType(val inner: RescriptJsonType) : RescriptJsonType()
    data class UnknownType(val raw: String) : RescriptJsonType()
}
```

- `"string"` → `StringType`
- `"option<array<string>>"` → `OptionType(ArrayType(StringType))`
- ネストされたジェネリクスは angle bracket depth で抽出

## コード生成マッピング

### Encode 式

| 型 | encode 式 |
|---|---|
| `string` | `String(expr)` |
| `int` | `Number(expr->Int.toFloat)` |
| `float` | `Number(expr)` |
| `bool` | `Boolean(expr)` |
| `option<T>` | `expr->Option.mapOr(Null, v => encodeT(v))` |
| `array<T>` | `Array(expr->Array.map(v => encodeT(v)))` |
| 不明 | `/* TODO: encode X */ Null` |

### Decode 式

| 型 | decode パターン |
|---|---|
| `string` | `switch v { \| String(v) => Some(v) \| _ => None }` |
| `int` | `switch v { \| Number(v) => Some(v->Int.fromFloat) \| _ => None }` |
| `float` | `switch v { \| Number(v) => Some(v) \| _ => None }` |
| `bool` | `switch v { \| Boolean(v) => Some(v) \| _ => None }` |
| `option<T>` | `Null => Some(None)` + 内部型の decode |
| `array<T>` | `Array(arr) =>` + 各要素の decode |
| 不明 | `/* TODO: decode X */ None` |

## 既存ファイルの変更

- `RescriptGenerateGroup.kt` — actions 配列に `RescriptGenerateJsonCodecAction()` を追加
- `RescriptGenerateGroupTest.kt` — カウント 3→4、新アクションのテスト追加
