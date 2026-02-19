# Design: Project Wizard + Code Generation

## Feature 1: Project Wizard

### アーキテクチャ

```
wizard/
├── RescriptModuleBuilder.kt         -- ModuleBuilder (ウィザード登録)
├── RescriptProjectWizardStep.kt     -- ModuleWizardStep (Swing UI)
└── RescriptProjectGenerator.kt      -- ファイル生成ロジック (純粋ユーティリティ)
```

### RescriptProjectGenerator (テスト対象)

純粋関数でファイル内容を生成するユーティリティクラス。

```kotlin
object RescriptProjectGenerator {
    fun generateRescriptJson(name: String, includeReact: Boolean): String
    fun generatePackageJson(name: String, includeReact: Boolean): String
    fun generateStarterModule(): String
    fun generateReactComponent(): String
}
```

### RescriptModuleBuilder

- `ModuleBuilder` を継承
- `getModuleType()` → デフォルトモジュールタイプ
- `getNodeIcon()` → `RescriptIcons.FILE`
- `getCustomOptionsStep()` → `RescriptProjectWizardStep` を返す
- `setupRootModel()` → ファイル生成 + src/ ソースルート設定

### PackageManager enum

```kotlin
enum class PackageManager(val command: String) {
    NPM("npm"), PNPM("pnpm"), YARN("yarn")
}
```

### plugin.xml 登録

```xml
<moduleBuilder builderClass="com.rescript.plugin.wizard.RescriptModuleBuilder"
               id="RESCRIPT_MODULE" order="last"/>
```

## Feature 2: Code Generation

### アーキテクチャ

```
generate/
├── RescriptGenerateGroup.kt              -- ActionGroup
├── RescriptTypeDeclarationParser.kt      -- テキストベース型パーサー
├── RescriptGenerateSwitchAction.kt       -- Switch Arms 生成
└── RescriptGenerateModuleTypeAction.kt   -- Module Type 生成
```

### RescriptTypeDeclarationParser (テスト対象)

```kotlin
data class VariantConstructor(val name: String, val payload: String?)
data class RecordField(val name: String, val typeAnnotation: String, val isMutable: Boolean)

sealed class TypeShape {
    data class Variant(val constructors: List<VariantConstructor>) : TypeShape()
    data class Record(val fields: List<RecordField>) : TypeShape()
    data object Unknown : TypeShape()
}

object RescriptTypeDeclarationParser {
    fun parse(declarationText: String): TypeShape
    fun extractTypeName(declarationText: String): String?
}
```

### 解析ルール

1. `type ... =` の後のテキストを取得
2. `|` を含む or 大文字で始まる → Variant
3. `{` で始まる → Record
4. それ以外 → Unknown

### RescriptGenerateSwitchAction

- カーソル位置の TYPE_DECLARATION PSI を検出
- `RescriptTypeDeclarationParser.parse()` で Variant を取得
- switch テンプレートを生成してクリップボードにコピー or エディタに挿入

### RescriptGenerateModuleTypeAction

- カーソル位置の MODULE_DECLARATION PSI を検出
- 子ノードを走査して module type スケルトンを生成

### plugin.xml 登録

```xml
<group id="ReScript.GenerateGroup"
       class="com.rescript.plugin.generate.RescriptGenerateGroup"
       text="ReScript" popup="true">
    <add-to-group group-id="GenerateGroup" anchor="last"/>
</group>
```
