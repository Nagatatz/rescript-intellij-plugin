# Design — Wizard Template Resource Extraction

## 前提となる既存構造

### 入口

`wizard/ProjectTemplate.kt` の enum が `fun generateFiles(ctx: TemplateContext): Map<String, String>` で各 `*TemplateFiles.generate(ctx)` にディスパッチする。この契約は**不変**。

### 各 `*TemplateFiles` オブジェクトの内部パターン

```kotlin
internal object HonoTemplateFiles {
    fun generate(ctx: TemplateContext): Map<String, String> = mapOf(
        "rescript.json" to ProjectFileBuilders.rescriptJson(name = ctx.projectName),
        "package.json" to ProjectFileBuilders.packageJson(...),
        "src/Logger.res" to loggerRes(),    // ← 静的、抽出対象
        "src/Schema.res" to schemaRes(),    // ← 静的、抽出対象
        ...
        "README.md" to CommonFiles.readme(ctx, description, scripts, extraSections = listOf(
            "Database" to databaseSection(ctx),  // ← ctx.runCmd() を含む、要プレースホルダ化
            ...
        )),
        ".gitignore" to CommonFiles.gitignore(extra = ...),   // ← CommonFiles は触らない
    )
    private fun loggerRes(): String = buildString { appendLine("...") ... }
    private fun databaseSection(ctx: TemplateContext): String = buildString { ... ctx.runCmd("db:generate") ... }
    ...
}
```

### 抽出対象

- `private fun <name>Res(): String` や `private fun <name>Config(): String` の大半 (純静的)
- `apiSection()`, `openapiSection()`, `projectLayoutSection()` のような純静的 README セクション
- ctx 依存セクション (`databaseSection(ctx)`) はプレースホルダ差し込みで抽出可

### 抽出しない

- `generate()` 本体 (map composition)
- `ProjectFileBuilders.packageJson()` 呼び出し (PM 条件分岐・version 差込み)
- `ProjectFileBuilders.rescriptJson()` / `ProjectFileBuilders.honoBindings()` 等の別クラス経由の共通生成
- `CommonFiles.readme / gitignore / editorconfig / ciWorkflow / envExample / mitLicense / dependabotYaml / nvmrc` (共通、ctx 依存が多く本 workstream の範囲外)

## 新規クラス設計

### `TemplateResourceLoader`

```kotlin
// src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateResourceLoader.kt
package com.rescript.plugin.wizard.templates

/**
 * Loads wizard template file content from classpath resources under `templates/`
 * and substitutes `{{key}}` placeholders.
 *
 * Keeps static template content (README sections, sample `.res` sources,
 * `drizzle.config.ts`, etc.) out of Kotlin source by sourcing it from
 * `src/main/resources/templates/<template>/<relative-path>`.
 */
internal object TemplateResourceLoader {
    private val PLACEHOLDER = Regex("""\{\{([a-zA-Z][a-zA-Z0-9_]*)}}""")

    /**
     * Loads the resource at `templates/$path` and substitutes `{{key}}` occurrences
     * using [vars]. Throws if the resource is missing or if any `{{key}}` remains
     * unsubstituted (unless [strict] is false).
     */
    fun load(
        path: String,
        vars: Map<String, String> = emptyMap(),
        strict: Boolean = true,
    ): String {
        val resourcePath = "templates/$path"
        val stream = TemplateResourceLoader::class.java.classLoader
            .getResourceAsStream(resourcePath)
            ?: error("Template resource not found: $resourcePath")
        val raw = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val substituted = PLACEHOLDER.replace(raw) { match ->
            val key = match.groupValues[1]
            vars[key] ?: if (strict) {
                error("Unsubstituted placeholder {{$key}} in $resourcePath")
            } else match.value
        }
        return substituted
    }
}
```

### プレースホルダ方針

- 構文: `{{key}}` (Mustache 風、英数字とアンダースコアのみ)。`${...}` は JS/TS/`.res` template literal と衝突するため不採用。
- 動的要素は**呼び出し側で解決してから**マップで渡す。ローダーに `ctx` を渡さない (関心の分離)。
  ```kotlin
  TemplateResourceLoader.load(
      "hono/readme/database.md",
      mapOf(
          "cmdDbGenerate" to ctx.runCmd("db:generate"),
          "cmdDbMigrate" to ctx.runCmd("db:migrate"),
      ),
  )
  ```
- strict=true がデフォルト。未置換検出はリソースファイルの typo 防止。

### リソース配置規約

```
src/main/resources/templates/
  hono/
    src/
      Logger.res
      Schema.res
      Db.res
      ZodOpenapi.res
      Scalar.res
      Server.res
      Routes/
        Users.res
    drizzle.config.ts
    src/__tests__/
      Server.test.mjs
    readme/
      api.md             ← apiSection() 相当
      database.md        ← databaseSection(ctx) 相当 (プレースホルダ入り)
      openapi.md
      project-layout.md
  hono-graphql/
    ...
  react-native-cli/
    ...
  full-stack/
    ...
  monorepo/
    ...
```

**原則:**
- 抽出先の相対パスは**実プロジェクトでの配置パスと一致**させる (`src/Schema.res` 相当は `templates/hono/src/Schema.res`)。例外は README セクション (`readme/<section>.md` にまとめる)。
- 拡張子はオリジナル保持 (`.res`, `.ts`, `.mjs`, `.md` 等)。IntelliJ の IDE でも適切にシンタックスハイライトされる。
- ファイル末尾の改行は `appendLine` → trailing newline に統一。ローダーは `trimEnd('\n')` しない。

## リファクタ手順 (1 テンプレート分の詳細)

以下は `HonoTemplateFiles` を例とした変更手順。他 4 ファイルも同型。

### Before

```kotlin
private fun loggerRes(): String =
    buildString {
        appendLine("// hono/logger: structured request logging middleware.")
        appendLine("@module(\"hono/logger\") external logger: unit => Hono.middleware = \"logger\"")
    }
```

### After

**削除:** `private fun loggerRes()` (Kotlin)
**新規:** `src/main/resources/templates/hono/src/Logger.res` (生テキスト)
```
// hono/logger: structured request logging middleware.
@module("hono/logger") external logger: unit => Hono.middleware = "logger"
```
**差し替え:** `generate()` 内の `"src/Logger.res" to loggerRes()` → `"src/Logger.res" to TemplateResourceLoader.load("hono/src/Logger.res")`

### ctx 依存セクションの例 (`databaseSection`)

**Before:**
```kotlin
private fun databaseSection(ctx: TemplateContext): String =
    buildString {
        ...
        appendLine("```bash")
        appendLine(ctx.runCmd("db:generate"))
        appendLine(ctx.runCmd("db:migrate"))
        appendLine("```")
        ...
    }
```
**After (`templates/hono/readme/database.md`):**
```
Persistence uses **SQLite** via `@libsql/client` with **Drizzle ORM**. ...

```bash
{{cmdDbGenerate}}
{{cmdDbMigrate}}
```

Schema lives in `src/Schema.res`; update it and re-run `db:generate` to diff.
```
**呼び出し側:**
```kotlin
TemplateResourceLoader.load(
    "hono/readme/database.md",
    mapOf(
        "cmdDbGenerate" to ctx.runCmd("db:generate"),
        "cmdDbMigrate" to ctx.runCmd("db:migrate"),
    ),
)
```

### 末尾改行の扱い

既存の `buildString { ... append("...") }` (末尾 `append`) は末尾改行なしを意図している可能性がある。抽出先リソースファイルは末尾改行の有無を明示するため、既存テストの `assertTrue(contains(...))` では検出できないケースは**スナップショット比較**で確認 (下記「検証戦略」参照)。

## 検証戦略

### 1. 既存ユニットテスト (無修正で通ることを確認)

`HonoTemplateFilesTest.kt`, `HonoGraphqlTemplateFilesTest.kt`, `ReactNativeCliTemplateFilesTest.kt`, `FullStackTemplateFilesTest.kt`, `MonorepoTemplateFilesTest.kt` は `contains(...)` アサーション群。戻り値 Map の内容が維持されれば通る。

### 2. バイト等価性確認 (スナップショット)

各テンプレート refactor 前に実測を保存:
```kotlin
// 仮コード (tasklist では「refactor 前にスナップショット取得」タスクに落とす)
HonoTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
    .forEach { (path, content) ->
        File(".steering/.../snapshot/hono/$path").apply {
            parentFile.mkdirs()
            writeText(content)
        }
    }
```
Refactor 後、同じ処理を実行し `diff -r` で完全一致を確認。PNPM 以外 (NPM/YARN) も少なくとも 1 ケース検証。
検証完了後、スナップショットディレクトリは破棄 (リポジトリには残さない)。

### 3. 新規テスト

**`TemplateResourceLoaderTest.kt`:**
- `load()` で存在するリソースが返る
- `load()` でプレースホルダが置換される
- 未指定プレースホルダが strict=true で例外
- 未指定プレースホルダが strict=false で原文残存
- 存在しないリソースパスで `IllegalStateException`
- ダミーリソースは `src/test/resources/templates/__test__/...` に配置

**`TemplateResourcesSmokeTest.kt`:**
- `src/main/resources/templates/` 配下を再帰走査
- 各ファイルが UTF-8 で読める
- `{{key}}` が含まれる場合、既知プレースホルダ一覧に含まれること (typo 検出)
- 既知プレースホルダ一覧はテスト内のリテラル定数として定義

### 4. ビルド + Kover + plugin zip 検査

```bash
./gradlew ktlintCheck
./gradlew clean buildPlugin
./gradlew test
./gradlew koverHtmlReport
# buildPlugin 後:
unzip -l build/distributions/rescript-intellij-plugin-*.zip | grep 'templates/'
```
plugin zip に `templates/` 配下が含まれていれば resource が正しくパッケージされている。

## 影響範囲と変更ファイル一覧

### 新規

- `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateResourceLoader.kt`
- `src/main/resources/templates/{hono,hono-graphql,react-native-cli,full-stack,monorepo}/**/*` (推定 60〜80 ファイル)
- `src/test/kotlin/com/rescript/plugin/wizard/templates/TemplateResourceLoaderTest.kt`
- `src/test/kotlin/com/rescript/plugin/wizard/templates/TemplateResourcesSmokeTest.kt`
- `src/test/resources/templates/__test__/*` (ローダーテスト用のダミー)

### 修正

- `src/main/kotlin/com/rescript/plugin/wizard/templates/HonoTemplateFiles.kt`
- `src/main/kotlin/com/rescript/plugin/wizard/templates/HonoGraphqlTemplateFiles.kt`
- `src/main/kotlin/com/rescript/plugin/wizard/templates/ReactNativeCliTemplateFiles.kt`
- `src/main/kotlin/com/rescript/plugin/wizard/templates/FullStackTemplateFiles.kt`
- `src/main/kotlin/com/rescript/plugin/wizard/templates/MonorepoTemplateFiles.kt`
- `CLAUDE.md` (「ユーティリティ」項に1行追加)
- `docs/repository-structure.md` (resources/ テーブルに `templates/` 行追加)

### 触らない

- `CommonFiles.kt`, `TemplateContext.kt`, `TemplateVersions.kt`, `ProjectFileBuilders.kt`
- 他 13 Wizard テンプレートファイル
- `plugin.xml` (Extension Point 追加なし)

## 代替案 (不採用)

1. **IntelliJ `FileTemplateManager` API を使う** — プレースホルダ構文が `${NAME}` 固定で、本件の衝突問題が再発。また 1 つのテンプレートが 25+ ファイルを生成するケースに合わない。
2. **共通部分 (CommonFiles) のみリソース化** — 抽出量が少なく肥大改善効果が薄い。ユーザー確認済みの「Top 5」スコープと整合しない。
3. **各 `private fun` をそのまま `@MultilineString` 等で別ファイルの Kotlin 定数に分離** — Kotlin の raw string escape 問題が残り、`.res` シンタックスハイライトも得られない。

## セキュリティ

- `TemplateResourceLoader.load(path, vars)` の `path` は**コード内ハードコード限定**。ユーザー入力やファイルシステムパスを直接渡さない。`ctx.projectName` はプレースホルダ置換 (`vars`) 経由でのみ使われ、`path` に混入しない。
- 置換は単純な正規表現ベースで、コード実行やテンプレートエンジンの評価は行わない。
