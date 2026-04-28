# Design — Hono + Inertia (React) テンプレート

## 1. 全体方針

既存の `HONO` / `HONO_GRAPHQL` / `FULL_STACK` テンプレートのパターンを踏襲し、**新規 enum 値 + 新規 TemplateFiles object + 新規リソースツリー** という最小構成で実装する。アーキテクチャ変更（FrontendChoice 等の新フィールド追加）は行わない。

## 2. コンポーネント構成

### 2.1 Kotlin 側

```
src/main/kotlin/com/rescript/plugin/wizard/
├── ProjectTemplate.kt                    [変更] enum 追加 + dispatch 追加
├── TemplateVersions.kt                   [変更] Inertia / Vite+ 関連定数追加
└── templates/
    └── HonoInertiaTemplateFiles.kt       [新規]
```

### 2.2 リソース側

```
src/main/resources/templates/hono-inertia/
├── rescript.json
├── tsconfig.json
├── vite.config.mjs                  # Vite+ 統合設定（test/lint/format/build を内包）
├── drizzle.config.ts
├── .gitignore
├── index.html                       # Inertia ホスト HTML
├── src/
│   ├── Server.res                   # Hono ルーティング + Inertia middleware 配線
│   ├── ServerMain.res               # エントリ（Bun/Node 両対応）
│   ├── Routes.res                   # /, /about のサンプルルート
│   ├── Db.res                       # Drizzle 接続（既存 common/db/Db.res と同等）
│   ├── Schema.res                   # サンプル Drizzle スキーマ
│   ├── Logger.res                   # 既存パターンと同じ
│   ├── HonoInertia.res              # @hono/inertia の薄いバインディング
│   ├── InertiaBindings.res          # @inertiajs/react の薄いバインディング
│   ├── client/
│   │   ├── Main.res                 # createInertiaApp エントリ
│   │   ├── MainLayout.res           # 共通レイアウト
│   │   └── Pages/
│   │       ├── Home.res             # サンプルページ 1
│   │       └── About.res            # サンプルページ 2
│   └── __tests__/
│       └── Server_test.res          # `vite test` で実行されるサンプルテスト
├── readme/
│   ├── api.md
│   ├── frontend.md
│   ├── project-layout.md
│   └── viteplus.md                  # Vite+ サブコマンド一覧
└── variants/
    ├── zod/
    │   └── src/Validation.res
    └── sury/
        └── src/Validation.res
```

## 3. 主要モジュール設計

### 3.1 ProjectTemplate.kt の変更

```kotlin
HONO_INERTIA(
    displayName = "Hono + Inertia",
    description = "Hono backend + Inertia.js + React (CSR), unified by Vite+",
    category = TemplateCategory.BACKEND,
    sourceRoots = listOf("src", "src/client"),
)
```

- enum の挿入位置: `HONO_GRAPHQL` の直後（バックエンド系を連続配置）
- `generateFiles()` の `when` 分岐に `HONO_INERTIA -> HonoInertiaTemplateFiles.generate(ctx)` を追加

### 3.2 HonoInertiaTemplateFiles.kt（新規）

責務: `TemplateContext` を受け取り、生成すべきファイルパス → 内容のマップを返す。

```kotlin
object HonoInertiaTemplateFiles {
    fun generate(ctx: TemplateContext): Map<String, String> = buildMap {
        // 動的生成
        put("package.json", generatePackageJson(ctx))
        put("README.md", generateReadme(ctx))

        // 静的リソース（共通）
        addAll(loadStaticResources("hono-inertia"))

        // variants 適用
        put(
            "src/Validation.res",
            TemplateResourceLoader.load(
                "templates/hono-inertia/variants/${ctx.validationLibrary.variantKey()}/src/Validation.res"
            )
        )
    }

    private fun generatePackageJson(ctx: TemplateContext): String { ... }
    private fun generateReadme(ctx: TemplateContext): String { ... }
}
```

KDoc は英語で、責務・variants の挙動を 2〜3 文で記述する。

### 3.3 package.json 動的生成（核心）

`TemplateVersions` 経由で全バージョンを引く。Vite+ 統合により、`vitest` / `eslint` / `prettier` は **含めない**。

```json
{
  "name": "<project-name>",
  "version": "0.1.0",
  "type": "module",
  "scripts": {
    "dev":    "<pm-run> rescript build -w & <pm-exec> vp dev",
    "build":  "<pm-run> rescript build && <pm-exec> vp build",
    "preview": "<pm-exec> vp preview",
    "test":   "<pm-exec> vp test",
    "test:coverage": "<pm-exec> vp test --coverage",
    "check":  "<pm-exec> vp check",
    "db:generate": "<pm-exec> drizzle-kit generate"
  },
  "dependencies": {
    "@hono/inertia": "<TemplateVersions.HONO_INERTIA>",
    "@inertiajs/react": "<TemplateVersions.INERTIA_REACT>",
    "drizzle-orm":   "<TemplateVersions.DRIZZLE_ORM>",
    "hono":          "<TemplateVersions.HONO>",
    "react":         "<TemplateVersions.REACT>",
    "react-dom":     "<TemplateVersions.REACT_DOM>",
    "<validation-pkg>": "<version>"
  },
  "devDependencies": {
    "@rescript/core":  "<TemplateVersions.RESCRIPT_CORE>",
    "@rescript/react": "<TemplateVersions.RESCRIPT_REACT>",
    "@vitejs/plugin-react": "<TemplateVersions.VITEJS_PLUGIN_REACT>",
    "rescript":        "<TemplateVersions.RESCRIPT>",
    "vite":            "<TemplateVersions.VITE>",
    "vite-plus":       "<TemplateVersions.VITE_PLUS>",
    "@voidzero-dev/vite-plus-core": "<TemplateVersions.VITE_PLUS_CORE>",
    "drizzle-kit":     "<TemplateVersions.DRIZZLE_KIT>",
    "typescript":      "<TemplateVersions.TYPESCRIPT>"
  }
}
```

`<pm-run>` / `<pm-exec>` は `PackageManager` enum に既存のヘルパーで解決する（Bun の場合 `bun run` / `bunx`、npm の場合 `npm run` / `npx`）。

### 3.4 TemplateVersions.kt 追加定数

既存の Hono 関連定数の近くに以下を追加（Vite+ / React / Vite 関連は **既に存在するため再利用**）:

```kotlin
const val HONO_INERTIA = "^0.2.0"           // @hono/inertia (latest as of 2026-04-29)
const val INERTIA_REACT = "^3.0.3"          // @inertiajs/react (v3+ requires React 19 peer)
```

既存の以下を再利用する（追加不要）:

- `VITE = "^8.0.10"`、`VITE_PLUS = "^0.1.19"`、`VITE_PLUS_CORE = "^0.1.19"`
- `VITEJS_PLUGIN_REACT = "^6.0.1"`
- `REACT = "^19.2.5"`、`REACT_DOM = "^19.2.5"`、`REACT_TYPES`、`REACT_DOM_TYPES`
- `HONO = "^4.12.15"`、`HONO_NODE_SERVER = "^2.0.0"`

### 3.5 vite.config.mjs（Vite+ 統合）

Vite+ alpha (0.1.x) の時点では `test` / `lint` / `fmt` の **設定キーは公開されていない** ため、`vite.config.mjs` には plugins と build のみ書く。Vitest / Oxlint / Oxfmt はそれぞれのデフォルト設定で動作する（`vp` CLI が内部で配線する）。

```js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { inertiaPages } from '@hono/inertia/vite'

export default defineConfig({
  plugins: [react(), inertiaPages()],
  build: {
    rollupOptions: {
      input: { main: 'index.html' }
    }
  }
})
```

### 3.6 Server.res 設計

`@hono/inertia` 0.2.0 は `inertia()` ミドルウェアを `app.use()` し、ハンドラから `c.render(component, props)` で Inertia レスポンスを返す（Hono 既存の `c.render` を Inertia 版にオーバーライドする）。

```rescript
// IMPORTANT: register the inertia middleware before routes that call c.render.
let app = Hono.make()

app->Hono.use("*", HonoInertia.inertia())
Routes.register(app)

let default = ServerMain.serve(app)
```

### 3.7 InertiaBindings.res（新規バインディング）

`@inertiajs/react` の最小サブセットのみをバインドする:

```rescript
type pageProps = Js.Dict.t<Js.Json.t>

type page<'props> = {
  component: string,
  props: 'props,
  url: string,
  version: option<string>,
}

@module("@inertiajs/react")
external createInertiaApp: {
  "resolve": string => promise<React.component<{..}>>,
  "setup": {"el": Dom.element, "App": React.component<{..}>, "props": pageProps} => unit,
} => unit = "createInertiaApp"

module Link = {
  @module("@inertiajs/react") @react.component
  external make: (~href: string, ~children: React.element=?) => React.element = "Link"
}

@module("@inertiajs/react")
external usePage: unit => page<'props> = "usePage"
```

### 3.8 HonoInertia.res（新規バインディング）

`@hono/inertia` v0.2.0 は `inertia` ミドルウェアエクスポートと、コンテキストに `render` をオーバーライドする挙動を持つ。

```rescript
@module("@hono/inertia")
external inertia: unit => Hono.middlewareHandler = "inertia"

@send
external render: (Hono.context, string, 'props) => promise<Hono.response> = "render"
```

### 3.9 client/Main.res + pages.js（エントリ）

ReScript 12 の `Js.import` は **静的にしか解決できない**（`import("./Pages/" ++ name)` は不可）。そのため、Inertia の resolver は **JS シム** で `import.meta.glob` を呼び、ReScript からはそれをバインドする。

`src/client/pages.js`（JS で書く必要がある最小ファイル）:

```js
// Vite-only: import.meta.glob is a Vite extension.
// Inertia uses this to lazy-load page modules by name.
const pages = import.meta.glob('./Pages/**/*.res.mjs')

export function resolvePage(name) {
  const path = `./Pages/${name}.res.mjs`
  if (!pages[path]) {
    throw new Error(`Inertia page not found: ${name}`)
  }
  return pages[path]()
}
```

`src/client/Main.res`:

```rescript
@module("./pages.js")
external resolvePage: string => promise<{..}> = "resolvePage"

InertiaBindings.createInertiaApp({
  resolve: resolvePage,
  setup: ({el, App, props}) => {
    let root = ReactDOM.Client.createRoot(el)
    root->ReactDOM.Client.Root.render(React.createElement(App, props))
  },
})
```

## 4. UI 統合

`RescriptProjectWizardStep` 系の UI には **新フィールドを追加しない**。既存の以下フィールドが新テンプレートでもそのまま機能する:

- Template Selection（dropdown）: `HONO_INERTIA` を選択肢として追加（自動）
- Package Manager（dropdown）: 全 4 PM サポート
- Validation Library（dropdown）: zod / sury サポート

## 5. テスト戦略

### 5.1 ユニットテスト（新規）

`src/test/kotlin/com/rescript/plugin/wizard/templates/HonoInertiaTemplateFilesTest.kt`:

- ✅ `generate()` が必須ファイルをすべて返す（package.json, rescript.json, Server.res, vite.config.mjs, index.html, client/Main.res, Pages/Home.res, Pages/About.res, Validation.res）
- ✅ `generate()` が `vitest` / `eslint` / `prettier` を **含まない**
- ✅ Zod variant: `Validation.res` に `Zod` 由来コードが含まれる
- ✅ Sury variant: `Validation.res` に `Sury` 由来コードが含まれる
- ✅ npm / yarn / pnpm / bun それぞれで scripts が正しく組み立てられる
- ✅ package.json 内の `vite` バージョンが `TemplateVersions.VITE_PLUS` と一致する
- ✅ package.json に `@hono/inertia` / `@inertiajs/react` が含まれる

### 5.2 既存テストの更新

- `ProjectTemplateTest.kt`: 全テンプレート数 16 → 17
- `ProjectTemplate.values()` の網羅テストがある場合は HONO_INERTIA を含む

### 5.3 手動検証

- `./gradlew runIde` で IDE 起動
- New Project → ReScript → Hono + Inertia を選択
- 各 PM × 各 Validation Library の組み合わせ生成
- 生成プロジェクトで `<pm> install && <pm> dev` 動作確認

## 6. ドキュメント更新

| ドキュメント | 更新内容 |
|-------------|---------|
| CLAUDE.md | レイヤー 3 の Project Wizard セクションで「全 17 テンプレート」に更新、Hono + Inertia の記述を追加 |
| README.md | Features セクションの Project Wizard 一覧に追加 |
| sphinx-docs/user/features/advanced.md | Project Wizard 節に Hono + Inertia の説明と Vite+ 統合の利点を追記 |
| sphinx-docs/locale/ja/LC_MESSAGES/.../advanced.po | 上記の日本語訳を追加 |
| docs/repository-structure.md | テンプレート数 16 → 17 |
| plugin.xml `<change-notes>` | "Add Hono + Inertia (React) project template with Vite+ unified toolchain" |

`docs/product-requirements.md` のロードマップに該当エントリは存在しないため更新不要。

## 7. セキュリティ考慮

- **prop validation**: Inertia 経由で送るサーバー → クライアントの props は `Validation.res` で必ずバリデーションする雛形コードを含める
- **CSRF**: `@hono/inertia` のデフォルト CSRF middleware を有効化（README に記載）
- **入力検証**: Hono ルートハンドラーでは Zod/Sury による入力バリデーションを行うサンプルを `Routes.res` に含める

## 8. 既知の制約とフォローアップ

- **SSR 非対応**: 初版は CSR 専用。SSR 対応は別 PR で。
- **Vite+ alpha リスク**: 公開 alpha のため、API が破壊的に変更される可能性あり。`readme/viteplus.md` で注意喚起する
- **Inertia ReScript バインディングは最小**: 公式バインディングがないため、薄いラッパーを同梱する。利用者が必要に応じて拡張する想定
- **テンプレート ReScript バージョン**: ReScript 12+ 前提（`Js.import` を使うため）
- **JS シム必須**: ReScript 12 の `Js.import` は静的解決のため、Inertia ページの動的解決には `src/client/pages.js`（`import.meta.glob` を呼ぶ薄い JS）が必要。ReScript-only にはできない

## 9. リリース戦略

- 単一 PR で 17 番目のテンプレートとして追加
- バージョンバンプは MINOR（新機能追加）
- リリースノート: "Add Hono + Inertia (React) project template with Vite+ unified toolchain"
