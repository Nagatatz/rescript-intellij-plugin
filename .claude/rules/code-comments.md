---
paths:
  - "src/main/**/*.kt"
---

# コードコメント規約

すべての Kotlin ソースコードに英語で KDoc コメントを記述すること。

## 必須コメント

- **クラスレベル:** すべての `class`、`object`、`enum class`、`sealed class` に KDoc (`/** ... */`) を付与する
  - クラスの責務と役割を 1〜3 文で記述する
  - 関連クラスへの `@see` 参照を適宜追加する
  - IntelliJ Platform の Extension Point を実装している場合、対応するインターフェースに言及する

- **メソッドレベル:** 以下の条件に該当するメソッドに KDoc を付与する
  - `public` / `internal` メソッドで、名前だけでは目的が明確でないもの
  - パラメータが 2 つ以上あるメソッド（`@param` / `@return` を記述）
  - 複雑なロジック（分岐が多い、外部プロセス呼び出し、正規表現処理など）

- **インラインコメント:** 以下の箇所に `//` コメントを追加する
  - 非自明なアルゴリズムやワークアラウンド
  - 正規表現パターンの説明
  - マジックナンバーや定数の意味

## 記述スタイル

```kotlin
/**
 * Brief one-line summary of the class.
 *
 * Optional detailed description explaining the design rationale,
 * lifecycle, or important implementation notes.
 *
 * @see RelatedClass for additional context
 */
class ExampleClass {
    /**
     * Performs X by doing Y.
     *
     * @param input the source data to process
     * @return the transformed result, or null if input is invalid
     */
    fun process(input: String): String? { ... }
}
```

## 省略可能なケース

- `override` メソッドで、親インターフェースの KDoc が十分な場合
- Getter / Setter のみのプロパティ
- `data class` のプロパティ（名前が自明な場合）
- テストクラス・テストメソッド（テスト名が説明的であれば不要）

## コミット前の検証

**以下は強制的な行動指示であり、例外なく従うこと。**

コミット前に、新規作成・変更したすべての `.kt` ファイルについて以下を確認すること:

1. ファイル内のすべての `class` / `object` / `enum class` / `sealed class` / `interface` 定義の直前に `/** ... */` KDoc ブロックがあるか
2. KDoc がクラスの責務を英語で 1〜3 文で説明しているか（クラス名の繰り返しだけでは不十分）
3. IntelliJ Extension Point を実装する場合、対応するインターフェース名に言及しているか

**KDoc が欠けている状態でコミットしてはならない。**
