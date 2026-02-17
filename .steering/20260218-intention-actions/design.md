# Design: ReScript Intention Actions

## アーキテクチャ

### 新規ファイル

- `src/main/kotlin/com/rescript/plugin/intention/RescriptWrapWithIntention.kt`
  - 共通基底クラス + 3 つのサブクラス（Some, Ok, Error）
- `src/main/kotlin/com/rescript/plugin/intention/RescriptAddGenTypeIntention.kt`
  - @genType アノテーション追加

### 変更ファイル

- `src/main/resources/META-INF/plugin.xml` — `<intentionAction>` 4 つ登録

## 詳細設計

### RescriptWrapWithIntention（基底クラス）

```kotlin
abstract class RescriptWrapWithIntention(
    private val wrapper: String  // "Some", "Ok", "Error"
) : PsiElementBaseIntentionAction()
```

- `getText()`: "Wrap with $wrapper(...)"
- `getFamilyName()`: "Wrap with $wrapper(...)"
- `isAvailable()`:
  1. ReScript ファイルか確認
  2. エディタに選択範囲があるか確認
- `invoke()`:
  1. 選択テキストを取得
  2. `$wrapper($selectedText)` で置換

### サブクラス

- `RescriptWrapWithSomeIntention` : `RescriptWrapWithIntention("Some")`
- `RescriptWrapWithOkIntention` : `RescriptWrapWithIntention("Ok")`
- `RescriptWrapWithErrorIntention` : `RescriptWrapWithIntention("Error")`

### RescriptAddGenTypeIntention

- `getText()`: "Add @genType annotation"
- `getFamilyName()`: "Add @genType annotation"
- `isAvailable()`:
  1. ReScript ファイルか確認
  2. カーソル位置の要素が `LET_DECLARATION`, `TYPE_DECLARATION`, `MODULE_DECLARATION` 内か確認
  3. 宣言の直前に `@genType` が既に存在しないか確認
- `invoke()`:
  1. 宣言ノードを特定
  2. 宣言の直前に `@genType\n` を挿入

### plugin.xml 登録

```xml
<intentionAction>
    <language>ReScript</language>
    <category>ReScript</category>
    <className>com.rescript.plugin.intention.RescriptWrapWithSomeIntention</className>
    <skipBeforeAfter>true</skipBeforeAfter>
</intentionAction>
<!-- Ok, Error, AddGenType も同様 -->
```

## テスト

- `src/test/kotlin/com/rescript/plugin/intention/RescriptWrapWithIntentionTest.kt`
  - `getText()` / `getFamilyName()` のテスト
- `src/test/kotlin/com/rescript/plugin/intention/RescriptAddGenTypeIntentionTest.kt`
  - `getText()` / `getFamilyName()` のテスト

テスト省略事項: `isAvailable()` と `invoke()` は PSI コンテキストが必要なため、IntelliJ テストフレームワーク（`BasePlatformTestCase`）が必要。現時点では基本プロパティのテストに留める。
