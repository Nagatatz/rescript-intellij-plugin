# design.md — `PackageManager.BUN` の追加

## 設計方針

- **項目ごとに 1 コミット** に分割し、可読性と bisect 容易性を最優先
- **非破壊拡張**: 既存の 12 テンプレートは `ctx.*` 経由の PM 抽象化を利用しているため、enum 拡張のみで自動的に BUN 対応になる
- **Wizard UI は Swing + `PackageManager.entries` 駆動** のため、enum 追加だけで第 4 選択肢が自動的に出現する
- **integrationTest は PNPM 固定** のまま。BUN 検証は将来的に Bun + rescript-bun の互換性が固まった段階で自動化
- **Bun の最小バージョンは 1.2** — `bun.lock`（テキストロックファイル）と `bun --filter`（monorepo ワークスペース）が必要

## コンポーネント設計

### 1. `PackageManager` enum

```kotlin
enum class PackageManager(val command: String) {
    NPM("npm"),
    PNPM("pnpm"),
    YARN("yarn"),
    BUN("bun"),  // NEW
    ;
    override fun toString(): String = command
}
```

`toString()` の挙動はそのまま — Swing の `DefaultComboBoxModel` が `"bun"` をラベルに使う。

### 2. `TemplateVersions.BUN`

```kotlin
// Bun runtime + package manager. Bun v1.2+ uses the text-based `bun.lock`
// lockfile and provides `bun --filter` workspaces. Earlier versions shipped
// a binary `bun.lockb` which the template does not support.
const val BUN = "1.2.0"
```

### 3. `TemplateContext` の 5 メソッド

| メソッド | 現行 | BUN 追加 |
|---|---|---|
| `packageManagerSpec()` | `"npm@${NPM}"` / `"pnpm@${PNPM}"` / `"yarn@${YARN}"` | `"bun@${BUN}"` |
| `installCmd()` | `"npm install"` / `"pnpm install"` / `"yarn"` | `"bun install"` |
| `runCmd(s)` | `"npm run $s"` / `"pnpm $s"` / `"yarn $s"` | `"bun run $s"` |
| `execCmd(b)` | `"npx $b"` / `"pnpm exec $b"` / `"yarn $b"` | `"bunx $b"` |
| `lockfileName()` | `"package-lock.json"` / `"pnpm-lock.yaml"` / `"yarn.lock"` | `"bun.lock"` |

`runCmd` は `"bun run $s"` を採用（`"bun $s"` との選択肢あり。Bun は両方受け付けるが、`run` 明示で `test` などのサブコマンド衝突を避ける）。

### 4. `CommonFiles.packageManagerName`

```kotlin
private fun packageManagerName(pm: PackageManager): String =
    when (pm) {
        PackageManager.NPM -> "npm"
        PackageManager.PNPM -> "pnpm"
        PackageManager.YARN -> "Yarn"
        PackageManager.BUN -> "Bun"
    }
```

### 5. `CommonFiles.readme` の Prerequisites 表示切り替え

現状は `"- $pmName (managed via Corepack)"` 固定。BUN のときは以下のように分岐する:

```kotlin
if (ctx.packageManager == PackageManager.BUN) {
    appendLine("- Bun ${TemplateVersions.BUN} or later (install from https://bun.sh)")
} else {
    appendLine("- ${packageManagerName(ctx.packageManager)} (managed via Corepack)")
}
```

### 6. `CommonFiles.ciWorkflow` の自動 Bun セットアップ

```kotlin
fun ciWorkflow(
    ctx: TemplateContext,
    hasBuild: Boolean = false,
    hasTest: Boolean = false,
    setupBun: Boolean = false,
): String = buildString {
    val needsBun = setupBun || ctx.packageManager == PackageManager.BUN
    // ...
    if (needsBun) {
        appendLine("      - uses: oven-sh/setup-bun@v2")
        appendLine("        with:")
        appendLine("          bun-version: latest")
    }
    // pnpm/action-setup は既存の if (ctx.packageManager == PNPM) のまま
}
```

BUN が選ばれても pnpm/action-setup は出ない（既存の判定が排他）。

### 7. `MonorepoTemplateFiles` の 4 ヘルパー

| ヘルパー | BUN 返り値 | 備考 |
|---|---|---|
| `workspaceDep()` | `"workspace:*"` | Bun v1.1+ は pnpm と同じ構文をサポート |
| `perWorkspaceCmd(pm, pkg, script)` | `"bun --filter ./packages/$pkg $script"` | Bun v1.1+ の `--filter` は pnpm 系 glob 構文 |
| `allWorkspacesTestCmd()` | `"bun --filter '*' run test"` | 全ワークスペースで `test` を並列実行 |
| `allWorkspacesCoverageCmd()` | `"bun --filter '*' run test:coverage"` | 同上 |
| `workspacesNote()` | `"This project uses Bun workspaces (see the \`workspaces\` field in \`package.json\`)."` | Bun は package.json の `workspaces` 配列を参照（pnpm-workspace.yaml は使わない） |

### 8. `GoogleCloudRunTemplateFiles.dockerfile()`

```kotlin
private fun dockerfile(ctx: TemplateContext): String {
    val baseImage =
        if (ctx.packageManager == PackageManager.BUN) "oven/bun:1-slim" else "node:22-slim"
    val installInPm =
        when (ctx.packageManager) {
            PackageManager.NPM -> "npm install --omit=dev"
            PackageManager.PNPM -> "corepack enable && pnpm install --prod --frozen-lockfile=false"
            PackageManager.YARN -> "corepack enable && yarn install --production"
            PackageManager.BUN -> "bun install --production"
        }
    val runner = if (ctx.packageManager == PackageManager.BUN) "bun" else "node"
    return buildString {
        appendLine("FROM $baseImage")
        appendLine("WORKDIR /app")
        appendLine("COPY package*.json ./")
        appendLine("RUN $installInPm")
        appendLine("COPY . .")
        appendLine("RUN ${ctx.execCmd("rescript")}")
        appendLine("EXPOSE 8080")
        appendLine("CMD [\"$runner\", \"src/App.res.mjs\"]")
    }
}
```

### 9. `TemplateResourcesSmokeTest` の BUN コンテキスト追加

`contexts` リストに 1 エントリ追加:

```kotlin
TemplateContext("demo-bun", PackageManager.BUN, ValidationLibrary.ZOD),
```

### 10. 既存テストへの BUN 拡張

- `TemplateContextTest`: 既存 5 テストに BUN アサーションを足す
- `CommonFilesTest`: `ciWorkflow` の BUN 自動セットアップ / pnpm セットアップ非出力 / `packageManagerName` 「Bun」を検証
- `MonorepoTemplateFilesTest`: BUN で 4 ヘルパーの出力を検証
- `GoogleCloudRunTemplateFilesTest`: BUN Dockerfile の `FROM oven/bun:1-slim` + `bun install --production` を検証

## 既存 API 再利用

| 機能 | 参照先 |
|---|---|
| Swing ComboBox の enum 自動列挙 | `RescriptProjectWizardStep.kt:34-37` |
| TemplateContext メソッドパターン | `TemplateContext.kt:28-78`（既存 3 値の when 分岐に倣う） |
| Corepack spec 文字列 | `TemplateVersions.NPM`/`PNPM`/`YARN`（BUN も同形式） |
| `setupBun` フラグ経路 | `CommonFiles.ciWorkflow` の既存引数（論理和で統合） |
| 既存テスト列挙パターン | 各 `*TemplateFilesTest`、`TemplateContextTest` |

## 前提条件・制約

- **Bun 最小バージョン**: v1.2（`bun.lock` テキスト形式 + `bun --filter`）。README で明記する
- **`packageManager` フィールド**: Corepack は Bun を自動インストールしないが、`"bun@1.2.0"` を書いても害はない（Volta などが参照）
- **integrationTest**: BUN をマトリクスに入れない。PR レビュー時の手動検証に留める
- **Bun Dockerfile (Cloud Run)**: `oven/bun:1-slim` は Debian slim ベースで Cloud Run と互換
