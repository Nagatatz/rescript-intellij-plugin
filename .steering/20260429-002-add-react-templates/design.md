# Design — 4 つの React フレームワークテンプレート追加

## 1. 全体方針

- 既存 `*TemplateFiles` パターン（`object`、`generate(ctx: TemplateContext): Map<String, String>`）を踏襲する
- `CommonFiles.kt` / `ProjectFileBuilders.kt` / `TemplateResourceLoader.kt` / `TemplateContext.kt` を再利用する
- 新テンプレート 4 件は `variants/` を持たず、`Validation.res` も生成しない
- `ProjectTemplate` enum にデフォルト値付きフィールド `supportsValidationSelection: Boolean = true` を追加。新 4 件は `false` を明示

## 2. アーキテクチャ変更

### 2.1 `ProjectTemplate.kt`

```kotlin
enum class ProjectTemplate(
    val displayName: String,
    val description: String,
    val category: TemplateCategory,
    val supportsValidationSelection: Boolean = true,  // ← 新規
    val sourceRoots: List<String> = listOf("src"),
) {
    // 既存 16 件は宣言を変更しない（デフォルト true で吸収）
    BASIC(...),
    VITE_REACT(...),
    // ...

    TANSTACK_START(
        "TanStack Start",
        """...""".trimIndent(),
        TemplateCategory.FRONTEND,
        supportsValidationSelection = false,
    ),
    REMIX_RR_V7(
        "Remix / React Router v7",
        """...""".trimIndent(),
        TemplateCategory.FRONTEND,
        supportsValidationSelection = false,
        sourceRoots = listOf("app"),
    ),
    ASTRO(
        "Astro",
        """...""".trimIndent(),
        TemplateCategory.FRONTEND,
        supportsValidationSelection = false,
    ),
    WAKU(
        "Waku",
        """...""".trimIndent(),
        TemplateCategory.FRONTEND,
        supportsValidationSelection = false,
    ),
    ;

    fun generateFiles(ctx: TemplateContext): Map<String, String> = when (this) {
        // ... 既存
        TANSTACK_START -> TanstackStartTemplateFiles.generate(ctx)
        REMIX_RR_V7 -> RemixV7TemplateFiles.generate(ctx)
        ASTRO -> AstroTemplateFiles.generate(ctx)
        WAKU -> WakuTemplateFiles.generate(ctx)
    }
}
```

### 2.2 `RescriptProjectWizardStep.kt`

`templateList.addListSelectionListener` 内で:

```kotlin
templateList.addListSelectionListener {
    val selected = templateList.selectedValue
    if (selected is ProjectTemplate) {
        descriptionArea.text = selected.description
        val supports = selected.supportsValidationSelection
        validationLibraryLabel.isVisible = supports
        validationLibraryCombo.isVisible = supports
        // 既存 PackageManager / その他 UI は影響なし
    }
}
```

`updateDataModel()` では非表示時にも `validationLibraryCombo.selectedItem` のデフォルト（`ZOD`）を builder に渡す。新テンプレートの `*TemplateFiles.generate()` は `ctx.validationLibrary` を参照しないため、何が渡っても出力に影響しない。

### 2.3 新テンプレート共通の `*TemplateFiles.kt` パターン

```kotlin
/**
 * Generates project files for the <Framework> template.
 *
 * Produces a minimal full-stack/SPA setup using <Framework> with a ReScript
 * React component. Does not use the validation library variants since
 * <Framework> handles validation through its own data layer.
 *
 * @see ProjectTemplate.<KEY> for wizard registration
 */
object FrameworkTemplateFiles {
    private const val RESOURCE_ROOT = "templates/<framework>"

    fun generate(ctx: TemplateContext): Map<String, String> = buildMap {
        putAll(CommonFiles.generate(ctx))
        put("rescript.json", ProjectFileBuilders.rescriptJson(ctx, ...))
        put("package.json", buildPackageJson(ctx))
        put("tsconfig.json", TemplateResourceLoader.load("$RESOURCE_ROOT/tsconfig.json"))
        // フレームワーク固有ファイル
    }

    private fun buildPackageJson(ctx: TemplateContext): String { ... }
}
```

## 3. 各テンプレートの構成

### 3.1 TanStack Start

**目的**: Vite ベース・型志向フルスタック・ReScript 型志向と相性が最良。

**主要 deps**:
- `@tanstack/react-start`, `@tanstack/react-router`, `@tanstack/react-router-with-query` (オプション)
- `vite`, `@vitejs/plugin-react`
- `@rescript/react`, `react`, `react-dom`, `@rescript/core`
- `vitest`, `@vitest/coverage-v8`

**リソースファイル**:
- `vite.config.ts` (TanStack Start プラグイン構成)
- `tsconfig.json`, `rescript-modules.d.ts`
- `app/router.tsx`, `app/routes/__root.tsx`, `app/routes/index.tsx`
- `app/components/Greeting.res` (ReScript コンポーネント)
- `app/server/Greet.res` (Server Function サンプル)
- `app/__tests__/Greeting.test.mjs`
- `readme/server-functions.md`, `readme/file-routing.md`

**`sourceRoots`**: `listOf("app")` (TanStack Start の慣習)

### 3.2 Remix / React Router v7 (Framework Mode)

**目的**: 成熟した loader/action パターン・Web 標準志向。

**主要 deps**:
- `react-router`, `@react-router/dev`, `@react-router/node`, `@react-router/serve`
- `vite`
- `@rescript/react`, `react`, `react-dom`, `@rescript/core`
- `vitest`, `@vitest/coverage-v8`

**リソースファイル**:
- `vite.config.ts` (`@react-router/dev/vite` プラグイン)
- `react-router.config.ts`
- `tsconfig.json`, `rescript-modules.d.ts`
- `app/root.tsx`, `app/routes.ts`, `app/routes/home.tsx`
- `app/components/Greet.res`
- `app/loaders/HomeLoader.res` (loader 関数)
- `app/__tests__/Greet.test.mjs`
- `readme/loaders-actions.md`, `readme/file-routing.md`

**`sourceRoots`**: `listOf("app")`

### 3.3 Astro (React Islands)

**目的**: コンテンツ志向 SSG/SSR、React を Islands として埋め込み。

**主要 deps**:
- `astro`, `@astrojs/react`, `@astrojs/node`
- `@rescript/react`, `react`, `react-dom`, `@rescript/core`
- `vitest`, `@vitest/coverage-v8`

**リソースファイル**:
- `astro.config.mjs` (`@astrojs/react` integration)
- `tsconfig.json`, `rescript-modules.d.ts`
- `src/pages/index.astro` (`client:load` で Counter を hydrate)
- `src/components/Counter.res` (ReScript Islands コンポーネント)
- `src/components/StaticGreeting.res` (Static-rendered)
- `src/__tests__/Counter.test.mjs`
- `readme/islands.md`, `readme/static-vs-ssr.md`

### 3.4 Waku (RSC-first)

**目的**: 最小 RSC フレームワーク、Server/Client 境界の学習に最適。

**主要 deps**:
- `waku`
- `@rescript/react`, `react`, `react-dom`, `@rescript/core`
- `vitest`, `@vitest/coverage-v8`

**リソースファイル**:
- `tsconfig.json`, `rescript-modules.d.ts`
- `src/pages/index.tsx` (Server Component)
- `src/components/Counter.res` + `src/components/CounterClient.tsx` (`"use client"` ラッパー)
- `src/components/Greet.res` (Server Component)
- `src/__tests__/Greet.test.mjs`
- `readme/server-vs-client.md`, `readme/rsc-basics.md`

## 4. テスト戦略

### 4.1 各 `*TemplateFilesTest.kt`

`NextjsTemplateFilesTest.kt` を参考に以下を最低限検証:

- 生成ファイル数（CommonFiles + テンプレート固有ファイル）
- 重要ファイル（`package.json`, `rescript.json`, エントリポイント `.res`）の存在
- `package.json` の必須 deps（フレームワーク本体、@rescript/react、@rescript/core）と `scripts`
- `package.json` の `packageManager` メタデータ
- フレームワーク固有パターンの最低 1 件:
  - TanStack Start: `app/routes/__root.tsx` の `createRootRoute` 呼び出し
  - Remix: `app/routes.ts` の `index` route エクスポート
  - Astro: `index.astro` の `client:load` ディレクティブ
  - Waku: `Counter.res` の `"use client"` ラッパー存在 (`CounterClient.tsx`)
- `.gitignore` のフレームワーク固有エントリ（`dist/`, `.astro/`, `.waku/` 等）

### 4.2 `ProjectTemplateTest.kt` の更新

- 既存 16 件の `supportsValidationSelection` がデフォルト `true`
- 新 4 件が明示的に `false`
- enum エントリ数が 20 になっていること

### 4.3 `RescriptProjectWizardStep` の UI ロジック

UI クラスは `WizardStep` 派生のため `.claude/rules/testing.md` の免除対象。`tasklist.md` に「UI コンポーネントのため単体テスト省略」と明記する。手動検証で `runIde` 確認する。

## 5. ドキュメント変更内容

### 5.1 `CLAUDE.md`

レイヤー 3 解説の Project Wizard 段落:
- 「全 16 テンプレート」→「全 20 テンプレート」
- 「TanStack Start / Remix RR v7 / Astro / Waku では Validation 選択を無効化」を 1 文追記

### 5.2 `docs/repository-structure.md`

`wizard/templates/` の説明を「16 種類」→「20 種類」、新ファイル列挙を追加。

### 5.3 `README.md`

Features の「Project Wizard with 16 templates ...」を 20 + 新フレームワーク名列挙へ更新。

### 5.4 `docs/product-requirements.md`

US-11 の受け入れ条件「16 種類のテンプレート」→「20 種類のテンプレート」、テンプレート名列挙を更新。Acceptance criteria のチェックボックスは追加分のみ `[ ]` で開始（実装完了時に `[x]` へ）。

### 5.5 `docs/templates.md`

テンプレート一覧表に 4 行追加。

### 5.6 `sphinx-docs/user/templates/index.md` + 4 ページ新規 + `.po`

- `index.md` のカード一覧に 4 件追加
- `tanstack-start.md`, `remix-v7.md`, `astro.md`, `waku.md` を新規作成
- `make gettext && make update-po` で `.po` を同期し、`msgstr` を日本語で埋める
- `make build-ja` 成功を確認

### 5.7 `plugin.xml` `<change-notes>`

Unreleased セクションに「Add 4 new React project templates: TanStack Start, Remix / React Router v7, Astro, Waku」を追記。

## 6. 実装フェーズと順序

ブランチ: `feature/add-react-templates`（worktree で作業）

1. **Phase 1: 基盤** — `supportsValidationSelection` フラグ追加 + Wizard Step 条件分岐 + `ProjectTemplateTest` 回帰
2. **Phase 2: TanStack Start** — Kotlin/resources/test/docs 同梱 1 コミット
3. **Phase 3: Remix v7** — 同上
4. **Phase 4: Astro** — 同上
5. **Phase 5: Waku** — 同上
6. **Phase 6: 仕上げ** — docs 取りこぼし確認、`./gradlew test koverHtmlReport`、tasklist 完了マーク、マージ

各フェーズで `./gradlew ktlintCheck buildPlugin test` を必ず通す。

## 7. セキュリティ・互換性

- 新テンプレートでも、`ProjectFileBuilders.packageJson` 経由の `packageManager` メタデータ・`scripts` 構築は既存パターンに従う（コマンド注入リスクなし）
- 生成される `.res` / `.tsx` / `.json` ファイル内容は静的（テンプレート変数のみ置換）
- `TemplateResourceLoader.load()` は既存 16 件と同じ仕組みを再利用
- `pluginSinceBuild` 変更不要、`pluginUntilBuild` も変更不要

## 8. ロールバック計画

- 各テンプレートは独立コミットなので、問題があれば `git revert <sha>` で個別に巻き戻し可能
- フラグ追加コミット (Phase 1) のみは新テンプレートコミット全体の前提なので、巻き戻すなら 4 件すべてを先に revert する
