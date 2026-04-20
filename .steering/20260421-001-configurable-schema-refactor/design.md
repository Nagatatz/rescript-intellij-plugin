# Design — RescriptConfigurable スキーマ駆動リファクタリング

## 全体構造

```
settings/
├── RescriptConfigurable.kt           (389行 → 約250行: UI 組み立てのみ)
├── RescriptProjectSettings.kt        (無変更)
├── RescriptSettingDescriptor.kt      (新規: sealed class 階層)
├── RescriptSettingsSchema.kt         (新規: descriptor リスト)
└── RescriptSettingsValidator.kt      (新規: apply() バリデーション)
```

## データモデル

### `RescriptSettingDescriptor`

各設定項目を「コンポーネント生成・値の read/write」インターフェースとして表現する。

```kotlin
sealed class RescriptSettingDescriptor<T> {
    abstract val id: String            // デバッグ・テスト識別子
    abstract fun currentValue(settings: RescriptProjectSettings): T
    abstract fun applyValue(settings: RescriptProjectSettings, value: T)
    abstract fun createComponent(project: Project): SettingComponent<T>
}

interface SettingComponent<T> {
    val component: JComponent          // FormBuilder に追加する Swing コンポーネント
    fun getValue(): T
    fun setValue(value: T)
}
```

**具象 descriptor:**

| クラス | 対象フィールド | 値の型 |
|--------|----------------|--------|
| `BoolDescriptor` | 12 個の `JCheckBox` | `Boolean` |
| `PathDescriptor` | 5 個の `TextFieldWithBrowseButton`（lsp/node/binary/platform/runtime） | `String` |
| `ComboDescriptor<E>` | severity / logLevel の `ComboBox` | `String` |
| `IntSpinnerDescriptor` | inlayHintsMaxLength の `JSpinner` | `Int` |

### `RescriptSettingsSchema`

Descriptor を「表示順」に並べた `List<SchemaEntry>` を持つ object。区切り線・グループ見出しは `SchemaEntry.Separator` で表現。

```kotlin
sealed class SchemaEntry {
    data class Field<T>(
        val descriptor: RescriptSettingDescriptor<T>,
        val label: String? = null,       // nullなら addComponent, 非nullなら addLabeledComponent
        val tooltip: String? = null,
    ) : SchemaEntry()
    object Separator : SchemaEntry()
}

object RescriptSettingsSchema {
    val entries: List<SchemaEntry> = listOf( ... )  // 現行順序を厳密に再現
}
```

### `RescriptSettingsValidator`

`RescriptConfigurable.apply()` 冒頭のパス検証ロジックを分離。依存は `File` / `RescriptSecurityUtils` のみで、UI 非依存。

```kotlin
object RescriptSettingsValidator {
    /**
     * @throws ConfigurationException if any path is non-existent or not executable.
     */
    fun validateLspPath(path: String)
    fun validateNodePath(path: String)
    fun validateRescriptBinaryPath(path: String)
    fun validatePlatformPath(path: String)
    fun validateRuntimePath(path: String)
}
```

単体テストは tmpdir に 0-byte ファイル（実行可 / 非実行可）を作って各 validator を呼び出す。

## ワークフロー

### `createComponent()` 新実装

```kotlin
override fun createComponent(): JComponent {
    val components: Map<String, SettingComponent<*>> = RescriptSettingsSchema.entries
        .filterIsInstance<SchemaEntry.Field<*>>()
        .associate { it.descriptor.id to it.descriptor.createComponent(project) }

    val builder = FormBuilder.createFormBuilder()
    for (entry in RescriptSettingsSchema.entries) {
        when (entry) {
            is SchemaEntry.Separator -> builder.addSeparator()
            is SchemaEntry.Field<*> -> {
                val swing = components[entry.descriptor.id]!!.component
                if (entry.label != null) builder.addLabeledComponent(entry.label, swing)
                else builder.addComponent(swing)
                entry.tooltip?.let { builder.addTooltip(it) }
            }
        }
    }
    this.componentMap = components
    return builder.addComponentFillVertically(JPanel(), 0).panel
}
```

### `isModified()` 新実装

```kotlin
override fun isModified(): Boolean {
    val settings = RescriptProjectSettings.getInstance(project)
    return RescriptSettingsSchema.entries
        .filterIsInstance<SchemaEntry.Field<*>>()
        .any { entry -> entry.descriptor.isModified(settings, componentMap[entry.descriptor.id]!!) }
}

// on descriptor:
fun <T> RescriptSettingDescriptor<T>.isModified(
    settings: RescriptProjectSettings,
    component: SettingComponent<*>,
): Boolean {
    @Suppress("UNCHECKED_CAST")
    val typed = component as SettingComponent<T>
    return typed.getValue() != currentValue(settings)
}
```

### `apply()` 新実装

```kotlin
override fun apply() {
    val lsp = pathValue("lspServerPath")
    val node = pathValue("nodePath")
    val bin = pathValue("rescriptBinaryPath")
    val plat = pathValue("platformPath")
    val rt = pathValue("runtimePath")

    RescriptSettingsValidator.validateLspPath(lsp)
    RescriptSettingsValidator.validateNodePath(node)
    RescriptSettingsValidator.validateRescriptBinaryPath(bin)
    RescriptSettingsValidator.validatePlatformPath(plat)
    RescriptSettingsValidator.validateRuntimePath(rt)

    val settings = RescriptProjectSettings.getInstance(project)
    RescriptSettingsSchema.entries
        .filterIsInstance<SchemaEntry.Field<*>>()
        .forEach { entry -> applyEntry(entry, settings) }

    // 副作用: LSP サーバー再起動等（既存コードをそのまま）
}
```

### `reset()` 新実装

```kotlin
override fun reset() {
    val settings = RescriptProjectSettings.getInstance(project)
    RescriptSettingsSchema.entries
        .filterIsInstance<SchemaEntry.Field<*>>()
        .forEach { entry -> resetEntry(entry, settings) }
}
```

## Descriptor 定義の具体例

```kotlin
class BoolDescriptor(
    override val id: String,
    private val title: String,
    private val default: Boolean,
    private val getter: (RescriptProjectSettings) -> Boolean,
    private val setter: (RescriptProjectSettings, Boolean) -> Unit,
) : RescriptSettingDescriptor<Boolean>() {
    override fun currentValue(s: RescriptProjectSettings) = getter(s)
    override fun applyValue(s: RescriptProjectSettings, v: Boolean) = setter(s, v)
    override fun createComponent(project: Project): SettingComponent<Boolean> {
        val cb = JCheckBox(title, default)
        return object : SettingComponent<Boolean> {
            override val component = cb
            override fun getValue() = cb.isSelected
            override fun setValue(value: Boolean) { cb.isSelected = value }
        }
    }
}
```

`PathDescriptor` は同様に `TextFieldWithBrowseButton` + `addBrowseFolderListener` を内包。
Path / Folder の差異は `FileChooserDescriptorFactory` の選択で表現する sub-flag で制御。

## Schema エントリの並び（現行再現）

現行の `createComponent()` と 1:1 対応。参考箇所は `RescriptConfigurable.kt:164-214`。

1. `lspServerPath` (label + tooltip)
2. `nodePath` (label + tooltip)
3. Separator
4. `incrementalTypechecking` (+ tooltip)
5. `incrementalAcrossFiles` (+ tooltip)
6. Separator
7. `errorLensEnabled` (+ tooltip)
8. `errorLensMinSeverity` (label + tooltip)
9. Separator
10. `removeUnusedOpens` (+ tooltip)
11. `formatCheck` (+ tooltip)
12. Separator
13. `signatureHelp` (+ tooltip)
14. `signatureHelpConstructor` (+ tooltip)
15. `cacheProjectConfig` (+ tooltip)
16. `inlayHints` (+ tooltip)
17. `inlayHintsMaxLength` (label + tooltip)
18. `compileStatus` (+ tooltip)
19. `reanalyzeServer` (+ tooltip)
20. Separator
21. `rescriptBinaryPath` (label + tooltip)
22. `platformPath` (label + tooltip)
23. `runtimePath` (label + tooltip)
24. `logLevel` (label + tooltip)

## テスト戦略

### `RescriptSettingsValidatorTest`

- 空文字列 → 例外を投げない
- 存在しないパス → `ConfigurationException` with 既存文言
- 存在する `.js` ファイル → LSP validator のみ pass（実行可判定をスキップ）
- 存在するが非実行可ファイル → Node/Binary validator で例外
- 実行可フラグを立てたファイル → pass
- ディレクトリ（platform/runtime）→ 存在すれば pass、不在なら例外

### 既存テスト

`RescriptConfigurable` 自体は Configurable（Swing UI）の testing.md 免除対象として維持。
ただし `isModified` / `apply` の走査ロジックは validator テストで間接的に保護される。

## 実装順序

各コミットが独立してビルド可能・テスト pass する単位で分割する。

1. **Validator 抽出** — `RescriptSettingsValidator` + テストを新規追加し、`apply()` から
   呼び出しへ置換（UI は無変更）。
2. **Descriptor 基盤** — sealed class と 4 つの具象 descriptor を追加（まだ未使用）。
3. **Schema 定義** — `RescriptSettingsSchema.entries` を定義し、旧 `createComponent` を
   schema 走査に差し替え。`isModified` / `apply` / `reset` も schema 駆動化。
4. **デッドコード削除** — 旧フィールド宣言・個別 `var` を除去。

## 非互換リスクチェック

- `@JvmField` 的な互換性は不要（Kotlin 内部のみ）。
- `RescriptProjectSettings` のフィールド名・`@State` 属性は変更しない → 永続化互換を維持。
- `DialogTitleCapitalization` 警告は `PathDescriptor.createComponent` 内で
  `@Suppress` を付け替える。
