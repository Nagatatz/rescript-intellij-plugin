# 設計: サンプルプロジェクト品質改善

## 全体方針

既存の `wizard/` パッケージの構造（`ProjectTemplate` enum + `templates/*TemplateFiles.kt`）を維持しつつ、横断的な責務（バージョン管理、README 生成、共通ファイル生成）を共通化する。統合テストは Kotlin テストとして別ソースセットに分離し、Gradle の `integrationTest` タスクで起動する。

## 1. パッケージ構成の変更

```
wizard/
├── ProjectTemplate.kt              # 既存 (変更なし)
├── RescriptModuleBuilder.kt        # packageManager をテンプレ生成に渡すよう修正
├── RescriptProjectGenerator.kt     # PackageManager enum (既存) に displayName 追加
├── RescriptProjectWizardStep.kt    # デフォルト選択を PNPM に変更
└── templates/
    ├── TemplateVersions.kt         # NEW: 依存バージョン集約
    ├── TemplateContext.kt          # NEW: projectName + packageManager のコンテキスト
    ├── CommonFiles.kt              # NEW: README/.gitignore/.editorconfig/ci.yml 生成
    ├── BasicTemplateFiles.kt       # 修正: TemplateContext 受け取り、共通ファイル追加
    ├── ViteReactTemplateFiles.kt   # 修正: Vite → Vite+、JSX v4 automatic、@rescript/core
    ├── NextjsTemplateFiles.kt      # 修正: @rescript/core、テスト雛形
    ├── ElectronTemplateFiles.kt    # 修正: Vite → Vite+
    ├── HonoTemplateFiles.kt        # 修正: @rescript/core、Vitest
    ├── CloudflareWorkersTemplateFiles.kt  # 修正
    ├── AwsLambdaTemplateFiles.kt   # 修正
    ├── GoogleCloudRunTemplateFiles.kt     # 修正
    ├── ReactNativeTemplateFiles.kt # 修正: @rescript/core
    ├── NpmLibraryTemplateFiles.kt  # 修正: genType、Vitest
    ├── CliToolTemplateFiles.kt     # 修正: Vitest
    └── MonorepoTemplateFiles.kt    # 修正: Vite+、pnpm-workspace.yaml
```

## 2. TemplateVersions.kt

```kotlin
package com.rescript.plugin.wizard.templates

/**
 * Centralized version constants for dependencies used in project templates.
 *
 * Having a single source of truth simplifies coordinated version bumps and
 * prevents version drift across templates.
 */
object TemplateVersions {
    // ReScript core
    const val RESCRIPT = "^12.0.0"
    const val RESCRIPT_CORE = "^1.6.0"
    const val RESCRIPT_REACT = "^0.13.0"

    // Build tools
    const val VITE_PLUS = "^0.1.0" // Pinned to latest pre-1.0; upgrade when stable
    const val VITE_PLUS_CORE = "^0.1.0"
    const val VITEST = "^2.1.0"

    // React
    const val REACT = "^18.3.0"
    const val REACT_DOM = "^18.3.0"
    const val REACT_TYPES = "^18.3.0"

    // Backend
    const val HONO = "^4.6.0"
    const val NODE_TYPES = "^22.0.0"

    // Next.js
    const val NEXTJS = "^15.0.0"

    // Electron
    const val ELECTRON = "^33.0.0"

    // Expo / React Native
    const val EXPO = "^52.0.0"
    const val REACT_NATIVE = "^0.76.0"

    // Cloudflare Workers
    const val WRANGLER = "^3.80.0"
    const val CF_WORKERS_TYPES = "^4.20241022.0"

    // AWS Lambda
    const val AWS_LAMBDA_TYPES = "^8.10.0"

    // Package manager defaults
    const val PNPM = "9.12.0"
    const val NPM = "10.9.0"
    const val YARN = "4.5.0"

    // Node.js engine
    const val NODE_ENGINE = ">=20"
}
```

## 3. TemplateContext.kt

テンプレート生成関数に `projectName` と `packageManager` を 1 オブジェクトで渡す。

```kotlin
package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager

/**
 * Context passed to each template file generator, bundling the project name
 * and the selected package manager so templates can render PM-specific
 * commands and metadata (e.g. `packageManager` field in package.json).
 */
data class TemplateContext(
    val projectName: String,
    val packageManager: PackageManager,
) {
    /** Package manager version for the `packageManager` field in `package.json`. */
    fun packageManagerSpec(): String = when (packageManager) {
        PackageManager.NPM -> "npm@${TemplateVersions.NPM}"
        PackageManager.PNPM -> "pnpm@${TemplateVersions.PNPM}"
        PackageManager.YARN -> "yarn@${TemplateVersions.YARN}"
    }

    /** Install command displayed in README. */
    fun installCmd(): String = when (packageManager) {
        PackageManager.NPM -> "npm install"
        PackageManager.PNPM -> "pnpm install"
        PackageManager.YARN -> "yarn"
    }

    /** Script invocation command (e.g. "pnpm dev"). */
    fun runCmd(script: String): String = when (packageManager) {
        PackageManager.NPM -> "npm run $script"
        PackageManager.PNPM -> "pnpm $script"
        PackageManager.YARN -> "yarn $script"
    }
}
```

`ProjectTemplate.generateFiles(projectName: String)` は後方互換を保ったまま、内部で `TemplateContext` を作るオーバーロードを追加する:

```kotlin
fun generateFiles(ctx: TemplateContext): Map<String, String> = when (this) { ... }

// 既存 API は薄いデリゲートとして残す（ユニットテスト互換性のため）
fun generateFiles(projectName: String): Map<String, String> =
    generateFiles(TemplateContext(projectName, PackageManager.PNPM))
```

## 4. CommonFiles.kt

```kotlin
object CommonFiles {
    fun gitignore(extra: List<String> = emptyList()): String = buildString {
        appendLine("# Dependencies")
        appendLine("node_modules/")
        appendLine()
        appendLine("# ReScript build artifacts")
        appendLine("lib/")
        appendLine("*.res.js")
        appendLine("*.res.mjs")
        appendLine()
        appendLine("# OS")
        appendLine(".DS_Store")
        appendLine("Thumbs.db")
        appendLine()
        appendLine("# Logs")
        appendLine("*.log")
        appendLine("npm-debug.log*")
        appendLine("pnpm-debug.log*")
        appendLine("yarn-debug.log*")
        appendLine()
        extra.forEach { appendLine(it) }
    }

    fun editorconfig(): String = """
        root = true

        [*]
        indent_style = space
        indent_size = 2
        end_of_line = lf
        charset = utf-8
        trim_trailing_whitespace = true
        insert_final_newline = true
    """.trimIndent()

    fun ciWorkflow(ctx: TemplateContext, buildScript: String? = "build"): String = ...

    fun readme(
        ctx: TemplateContext,
        description: String,
        scripts: List<Pair<String, String>>,
    ): String = ...
}
```

## 5. PackageManager 選択値の反映

### Wizard 側
- `RescriptProjectWizardStep.kt` の `packageManagerCombo` 初期選択を `PackageManager.PNPM` に変更。
- `RescriptModuleBuilder.kt` が `selectedTemplate.generateFiles(TemplateContext(projectName, packageManager))` を呼ぶよう修正。

### `PackageManager` enum
既存の `enum class PackageManager(val command: String)` に `val displayName: String` は不要（`name.lowercase()` で十分）。既存 `command` プロパティを活用。

### 生成物への反映
- `package.json` に `"packageManager": "pnpm@9.12.0"` 等を出力（Corepack 対応）
- `README.md` のコマンド表記を `ctx.installCmd()` / `ctx.runCmd("dev")` で動的生成
- Monorepo:
  - pnpm 選択時: `pnpm-workspace.yaml` 生成 + root `package.json` は `workspaces` フィールドなし
  - npm/yarn 選択時: root `package.json` に `"workspaces": ["packages/*"]`

## 6. Vite+ への置換

### rescript.json (Vite+React, Electron, Monorepo/client)

```json
{
  "name": "$PROJECT_NAME",
  "sources": [{ "dir": "src", "subdirs": true }],
  "package-specs": [{ "module": "esmodule", "in-source": true }],
  "suffix": ".res.js",
  "bs-dependencies": ["@rescript/core", "@rescript/react"],
  "bsc-flags": ["-open RescriptCore"],
  "jsx": { "version": 4, "mode": "automatic" }
}
```

### package.json (Vite+React)

```json
{
  "name": "$PROJECT_NAME",
  "version": "0.1.0",
  "private": true,
  "type": "module",
  "packageManager": "pnpm@9.12.0",
  "engines": { "node": ">=20" },
  "scripts": {
    "res:dev": "rescript -w",
    "res:build": "rescript",
    "res:clean": "rescript clean",
    "dev": "vp dev",
    "build": "vp build",
    "preview": "vp preview",
    "test": "vp test"
  },
  "dependencies": {
    "@rescript/core": "^1.6.0",
    "@rescript/react": "^0.13.0",
    "react": "^18.3.0",
    "react-dom": "^18.3.0"
  },
  "devDependencies": {
    "@types/react": "^18.3.0",
    "@types/react-dom": "^18.3.0",
    "@vitejs/plugin-react": "^4.3.0",
    "rescript": "^12.0.0",
    "vite-plus": "^0.1.0",
    "@voidzero-dev/vite-plus-core": "^0.1.0"
  }
}
```

### vite.config.ts

```ts
import { defineConfig } from 'vite-plus'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
})
```

README には以下の注意書きを含める:
> This template uses Vite+ (`vite-plus`), an early-access toolchain that bundles Vite, Vitest, and more. Vite+ is pre-1.0 — if you prefer classic Vite, replace `vite-plus` with `vite` and update imports in `vite.config.ts`.

## 7. 統合テスト

### Gradle 設定 (`build.gradle.kts`)

```kotlin
sourceSets {
    create("integrationTest") {
        kotlin.srcDir("src/integrationTest/kotlin")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += output + compileClasspath
    }
}

configurations["integrationTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

tasks.register<Test>("integrationTest") {
    description = "Runs template generation integration tests (requires pnpm and node)."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
    // Isolate: run sequentially to avoid concurrent pnpm cache issues
    maxParallelForks = 1
    // Pass pnpm path if provided via env
    systemProperty("template.test.pnpm", System.getenv("PNPM_BIN") ?: "pnpm")
    systemProperty("template.test.node", System.getenv("NODE_BIN") ?: "node")
}
```

### テストクラス例

```kotlin
// src/integrationTest/kotlin/com/rescript/plugin/wizard/TemplateIntegrationTest.kt
class TemplateIntegrationTest {
    @TempDir lateinit var tempDir: Path

    @ParameterizedTest
    @EnumSource(ProjectTemplate::class)
    fun `template generates a project that installs and builds`(template: ProjectTemplate) {
        val ctx = TemplateContext("demo", PackageManager.PNPM)
        val files = template.generateFiles(ctx)
        val projectDir = tempDir.resolve(template.name)
        writeFiles(projectDir, files)

        exec(projectDir, "pnpm", "install", "--prefer-offline", "--ignore-scripts")
            .assertSuccess("pnpm install for ${template.displayName}")

        if (files.keys.any { it == "rescript.json" }) {
            exec(projectDir, "npx", "rescript")
                .assertSuccess("rescript build for ${template.displayName}")
        }

        // React/Electron/Monorepo-client: also run build
        if (template in listOf(ProjectTemplate.VITE_REACT, ProjectTemplate.ELECTRON)) {
            exec(projectDir, "pnpm", "build")
                .assertSuccess("pnpm build for ${template.displayName}")
        }
    }
}
```

### GitHub Actions (`.github/workflows/integration-tests.yml`)

```yaml
name: Integration Tests

on:
  workflow_dispatch:
  schedule:
    - cron: '0 3 * * *' # nightly 03:00 UTC

jobs:
  integration-test:
    runs-on: ubuntu-latest
    timeout-minutes: 30
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21
      - uses: actions/setup-node@v4
        with:
          node-version: 20
      - uses: pnpm/action-setup@v4
        with:
          version: 9
      - name: Run integration tests
        run: ./gradlew integrationTest --info
```

## 8. ドキュメント更新

| ドキュメント | 更新内容 |
|-------------|---------|
| `CLAUDE.md` | レイヤー 3「プロジェクトウィザード」の説明を更新 (Vite+, @rescript/core, PM 選択反映) |
| `README.md` | Features セクション — サンプル品質向上に言及 |
| `sphinx-docs/user/features/advanced.md` | Project Wizard セクションに Vite+ / PM 選択の説明を追加 |
| `docs/product-requirements.md` | Project Wizard 機能の説明に Vite+/PM 反映を追記 |

## 9. 実装順序（コミット単位）

1. **Phase 3 先行**: `TemplateVersions.kt` + `TemplateContext.kt` + `CommonFiles.kt` を追加（空のインターフェース整備）
2. **追加: PackageManager 反映**: Wizard 側でデフォルト `PNPM` + `TemplateContext` を全テンプレに渡す導線を通す
3. **Phase 2 + 4 (Basic)**: `BasicTemplateFiles` に共通ファイル + @rescript/core を追加
4. **Phase 4 (Vite+React)**: Vite → Vite+ 切替 + JSX v4 + テスト雛形
5. **Phase 4 (Electron, Monorepo)**: Vite → Vite+ + pnpm workspaces
6. **Phase 4 (残りの React 系)**: Next.js / React Native
7. **Phase 2 + 4 (Backend 系)**: Hono / CF Workers / AWS Lambda / Cloud Run に @rescript/core + Vitest
8. **Phase 2 + 4 (Library/CLI)**: genType + Vitest
9. **Phase 1**: 統合テスト基盤 + `integration-tests.yml`
10. **ドキュメント更新**

## 10. リスクと緩和策

| リスク | 緩和策 |
|--------|--------|
| Vite+ の pre-1.0 で仕様変更 | バージョンを範囲指定にせず固定 (`^0.1.0`)。README に注意書き。 |
| 統合テストでの pnpm キャッシュ肥大 | `--prefer-offline` + CI で `pnpm/action-setup@v4` のキャッシュ機能を利用 |
| バージョン集約による意図せぬバージョンロールバック | PR 単位で diff レビュー。Dependabot 連携は将来検討。 |
| 既存ユニットテストの破壊 | `generateFiles(projectName)` の互換レイヤーを残す |
