# Design — TemplateContext への year / Node メタデータ集約

## 方針

`TemplateContext` を「全テンプレートで共有される可変メタデータの真実の源」に格上げする。

### TemplateContext の新フィールド

```kotlin
data class TemplateContext(
    val projectName: String,
    val packageManager: PackageManager,
    val validationLibrary: ValidationLibrary = ValidationLibrary.ZOD,
    val apiStrategy: ApiStrategy = ApiStrategy.REST,
    val year: Int = Year.now().value,
    val nodeMajor: String = TemplateVersions.NODE_MAJOR,
    val nodeEngine: String = TemplateVersions.NODE_ENGINE,
)
```

- `year`: LICENSE の copyright 年。デフォルトは `java.time.Year.now().value`。テストでは明示注入して決定的にする
- `nodeMajor`: `.nvmrc` / CI `setup-node` 用。デフォルトは `TemplateVersions.NODE_MAJOR`（= "22"）
- `nodeEngine`: `package.json` の `engines.node` 用。デフォルトは `TemplateVersions.NODE_ENGINE`（= ">=22"）

デフォルト値を持たせることで、既存コードは何も変更せずに通り続ける（段階的な移行が可能）。

## CommonFiles の signature 変更

### 変更前
```kotlin
fun mitLicense(holder: String, year: Int = 2026): String
fun nvmrc(): String
fun ciWorkflow(ctx: TemplateContext, hasBuild: Boolean = false, hasTest: Boolean = false): String
```

### 変更後
```kotlin
fun mitLicense(ctx: TemplateContext, holder: String): String
fun nvmrc(ctx: TemplateContext): String
// ciWorkflow は既に ctx を受け取っている — 内部の `node-version: 20` を `${ctx.nodeMajor}` に置換
```

## 呼び出しサイト更新パターン

### 変更前
```kotlin
".nvmrc" to CommonFiles.nvmrc(),
"LICENSE" to CommonFiles.mitLicense(holder = ctx.projectName),
"package.json" to ProjectFileBuilders.packageJson(
    ...
    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
    ...
),
```

### 変更後
```kotlin
".nvmrc" to CommonFiles.nvmrc(ctx),
"LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
"package.json" to ProjectFileBuilders.packageJson(
    ...
    engines = mapOf("node" to ctx.nodeEngine),
    ...
),
```

これを全 16 テンプレートで機械的に適用する。

## CI workflow の node-version バグ修正

現状:
```yaml
- uses: actions/setup-node@v4
  with:
    node-version: 20
```

`TemplateVersions.NODE_MAJOR = "22"` と矛盾しているため、生成されたプロジェクトの CI は Node 20 で走り、ローカル開発は Node 22+ で走るという不整合がある（偶然 22 と 20 の両方で動くコードなら顕在化しないが、22 専用 API を使った瞬間に CI が壊れる）。

`ctx.nodeMajor` に置き換えることで自動的に整合する。

## 決定的テスト

LICENSE に実行時の年が入るため、テストは以下のいずれかで決定性を確保する:

### 案 A: 明示注入
```kotlin
val ctx = TemplateContext("demo", PackageManager.PNPM, year = 2099)
val license = BasicTemplateFiles.generate(ctx)["LICENSE"]!!
assertTrue(license.contains("Copyright (c) 2099"))
```

### 案 B: 現在年を許容（both work for current and future years）
```kotlin
val license = BasicTemplateFiles.generate(ctx)["LICENSE"]!!
val currentYear = Year.now().value.toString()
assertTrue(license.contains("Copyright (c) $currentYear"))
```

**案 A を採用**。テストが時間依存にならず、将来「2027 年になったら通らなくなる」ようなバグを防ぐ。既存の `CommonFilesTest` の LICENSE 検証も明示注入に書き換える。

## 新規テスト

### 1. `CiWorkflowNodeVersionTest`（新規 or CommonFilesTest に追加）

```kotlin
@Test
fun `ciWorkflow uses nodeMajor from the context, not a hardcoded 20`() {
    val ctx = TemplateContext("demo", PackageManager.PNPM, nodeMajor = "24")
    val ci = CommonFiles.ciWorkflow(ctx, hasTest = true)
    assertTrue(ci.contains("node-version: 24"))
    assertFalse(ci.contains("node-version: 20"))
}
```

### 2. LICENSE 年の注入テスト

```kotlin
@Test
fun `mitLicense uses year from the context`() {
    val ctx = TemplateContext("demo", PackageManager.PNPM, year = 2099)
    val license = CommonFiles.mitLicense(ctx, holder = "demo")
    assertTrue(license.contains("Copyright (c) 2099 demo"))
}

@Test
fun `TemplateContext defaults year to current year`() {
    val ctx = TemplateContext("demo", PackageManager.PNPM)
    assertEquals(Year.now().value, ctx.year)
}
```

## ロールアウト順序

1. **Commit 1**: `TemplateContext` フィールド追加 + `CommonFiles` signature 変更 + 16 テンプレート呼び出し更新 + CI バグ修正。既存テストが通ることを確認
2. **Commit 2**: 新規テスト追加（CI node-version / LICENSE 年注入）

分割する理由: refactor と新規テストを分けることで、refactor 単体で既存テストが壊れていないことをレビューで確認しやすくする。

## リスク

| リスク | 緩和策 |
|---|---|
| `Year.now()` はタイムゾーン依存 | `Year.now()` はシステムデフォルト TZ だが LICENSE 年で使うだけなので許容範囲 |
| `TemplateContext` のデフォルト引数が 4 つ → 5 つ → ... と膨らむと可読性低下 | 今回は 3 つ追加に留まる。5 つ超えたら `TemplateMetadata` のような sub-struct に分離を検討 |
| CI node-version 変更で CI 挙動が変わる | `NODE_MAJOR = 22` は既にローカル開発の標準。むしろ乖離解消 |
