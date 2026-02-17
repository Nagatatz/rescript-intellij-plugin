# Design: Qodana レポート指摘事項の修正

## 修正方針

### 1. Critical: `addBrowseFolderListener` deprecated for removal（3件）

**対象:** `RescriptConfigurable.kt` L26,L37 / `RescriptSettingsEditor.kt` L35

4引数の deprecated メソッドを 2引数の新 API に置き換え。title/description は `FileChooserDescriptor` の `withTitle()` / `withDescription()` で設定。`@Suppress("DEPRECATION")` も除去。

```kotlin
// Before
@Suppress("DEPRECATION")
addBrowseFolderListener("Title", "Desc", project, descriptor)

// After
addBrowseFolderListener(project, descriptor.withTitle("Title").withDescription("Desc"))
```

### 2. High: Unused symbol（5件）

**対象:** `RescriptTokenTypes.kt` — `BACKSLASH`, `SHARP`, `AMPERSAND`, `SHARPSHARP`, `SINGLE_QUOTE`

JFlex レクサー (`Rescript.flex`) が `return RescriptTokenTypes.SHARP;` 等で参照するため削除不可。`@Suppress("unused")` を各プロパティに追加。

### 3. High: Incorrect string capitalization（3件）

**対象:** `RescriptFormattingService.kt` / `RescriptConfigurable.kt` / `RescriptMissingConfigInspection.kt`

`rescript` はツール名/ファイル名であり先頭大文字化は不適切。3件すべて `@Suppress("DialogTitleCapitalization")` で抑制。

### 4. High: Redundant nullable return type（1件）

**対象:** `RescriptLineIndentProvider.kt` L23

`getLineIndent()` は常に `""` か `baseIndent` を返し null を返さない。インターフェース `LineIndentProvider.getLineIndent` の戻り値型は `String?` だが、Kotlin ではオーバーライド時に non-null に narrowing 可能。戻り値型を `String` に変更。

### 5. High: Serializable object must implement 'readResolve'（1件）

**対象:** `RescriptLanguage.kt`

`object` のシングルトン保証のため `readResolve` メソッドを追加:

```kotlin
private fun readResolve(): Any = INSTANCE
```

### 6. Moderate: Multi-dollar interpolation（3件）

**対象:** `RescriptCodeStyleSettingsProvider.kt` L42,57

Kotlin 2.1+ の multi-dollar interpolation を使用。文字列プレフィックスに `$` を付与し、`${'$'}` を `$` に簡略化。文字列内に Kotlin 変数の interpolation は不要なため、`$` プレフィックスで十分。

```kotlin
// Before
"""
|    let message = `Hello, ${'$'}{name}!`
""".trimMargin()

// After
$"""
|    let message = `Hello, ${name}!`
""".trimMargin()
```

### 7. Moderate: If-Null foldable to '?:'（1件）

**対象:** `RescriptRunConfiguration.kt` L53

Elvis 演算子に変換:

```kotlin
// Before
val cliPath = RescriptCliDetector.findCli(effectiveWorkDir, project.basePath)
if (cliPath == null) {
    throw RuntimeConfigurationError(...)
}

// After
val cliPath = RescriptCliDetector.findCli(effectiveWorkDir, project.basePath)
    ?: throw RuntimeConfigurationError(...)
```

## 影響範囲

- 全修正はコードスタイル/警告抑制レベルの変更であり、機能的な挙動変更なし
- `readResolve` 追加はシリアライゼーション時のシングルトン保証を強化（既存動作に影響なし）
- multi-dollar interpolation は Kotlin 2.1+ 必須だが、現プロジェクトは Kotlin 2.3.10 を使用しており問題なし
