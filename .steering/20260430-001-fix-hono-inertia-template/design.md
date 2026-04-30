# 設計

## 修正対象ファイル

| 種別 | パス | 変更内容 |
|------|------|---------|
| Kotlin | `src/main/kotlin/com/rescript/plugin/wizard/templates/HonoInertiaTemplateFiles.kt` | `index.html` エントリ削除、files マップから `inertiaPages` 関連参照は無いので Kotlin 側はファイルマップ更新のみ |
| Resource | `src/main/resources/templates/hono-inertia/index.html` | **削除** |
| Resource | `src/main/resources/templates/hono-inertia/vite.config.mjs` | `@hono/inertia/vite` import と `inertiaPages()` plugin 登録を削除 |
| Resource | `src/main/resources/templates/hono-inertia/src/HonoInertia.res` | `render` 戻り値の `promise` ラップ撤去、`rootView` 型・`serializePage` バインディング・`InertiaOptions` 構造体追加、`inertia()` 引数を `options` 受取に変更 |
| Resource | `src/main/resources/templates/hono-inertia/src/Server.res` | `HonoInertia.inertia({rootView: ...})` でミドルウェアを登録、ReScript 内に `rootView` 関数を実装（HTML shell を返す） |
| Resource | `src/main/resources/templates/hono-inertia/src/Routes.res` | 各 GET handler の `async` / `await` を削除、render を直接 return |
| Resource | `src/main/resources/templates/hono-inertia/src/client/Pages/Home.res` | デッド `type props` と `let _ = ...` を削除 |
| Resource | `src/main/resources/templates/hono-inertia/src/client/pages.js` | `mod.make` のみ参照、欠如時は throw |
| Test | `src/test/kotlin/com/rescript/plugin/wizard/templates/HonoInertiaTemplateFilesTest.kt` | 新仕様に合わせてアサーションを書き換え |

## API 設計

### `src/HonoInertia.res` 改訂版

```rescript
// Inertia.js page envelope sent to the client. Mirrors the @hono/inertia
// `PageObject` shape; `version` is nullable per the JSON wire format.
type pageObject = {
  component: string,
  props: Js.Json.t,
  url: string,
  version: Nullable.t<string>,
}

// Renders the initial HTML host page for non-Inertia visits.
type rootView = (pageObject, Hono.context) => string

type options = {
  version?: Nullable.t<string>,
  rootView?: rootView,
}

// `inertia(options)` returns a Hono middleware that overrides `c.render`.
@module("@hono/inertia")
external inertia: options => Hono.middleware = "inertia"

// Inertia-flavoured `c.render(component, props)`. Returns the same
// Response shape as Hono's text/json helpers; the middleware decides
// HTML vs JSON based on the `X-Inertia` request header.
@send
external render: (Hono.context, string, 'props) => 'response = "render"

// Escapes a page object for embedding inside <script type="application/json">.
@module("@hono/inertia")
external serializePage: pageObject => string = "serializePage"
```

設計上の注意:
- `options` は ReScript 12 の optional record fields（`?:`）で表現する。`@hono/inertia` の `InertiaOptions` は両方とも optional なので合致する。
- 既存テンプレ（`Server.res`）は `inertia()` を引数なしで呼んでいたが、`inertia({})` または `inertia({rootView})` の形に変える。

### `src/Server.res` 改訂版（rootView を含む）

```rescript
// HTML shell returned by Inertia for non-Inertia visits.
let rootView: HonoInertia.rootView = (page, _ctx) => {
  let pageJson = HonoInertia.serializePage(page)
  `<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Hono Inertia</title>
  </head>
  <body>
    <div id="app" data-page='${pageJson}'></div>
    <script type="module" src="/src/client/Main.res.mjs"></script>
  </body>
</html>`
}

let app = Hono.createApp()
app->Hono.use(Logger.logger())
app->Hono.use(HonoInertia.inertia({rootView: rootView}))

app->Hono.onError((err, ctx) => {
  Console.error(err)
  ctx->Hono.status(500)->Hono.json({"error": "Internal Server Error"})
})

app->Hono.get("/health", ctx => ctx->Hono.json({"status": "ok"}))
Routes.Pages.register(app)

let start = () => {
  HonoNodeServer.serve({fetch: app->HonoNodeServer.honoFetch, port: 3000})
  Console.log("Server on http://localhost:3000 — Inertia pages on /, /about")
}
```

注意点:
- `serializePage` は `'/' → '\/'` のみエスケープするため、HTML 属性内に直接埋めると `'`（シングルクオート）や `<` がそのまま入ると壊れる可能性がある。**Hono Inertia の標準パターンに従い `<script data-page="app" type="application/json">${pageJson}</script>` 形式に切り替えるべき**だが、これは Inertia client が `data-page="app"` から JSON を読みに行く必要があり、`createInertiaApp({id: 'app'})` 設定（既定）と合致する。
- 修正方針: `<div id="app"></div>` + `<script id="page" type="application/json">${pageJson}</script>` の二段構成にし、`InertiaBindings.appOptions` には `id: "app"` 等は不要（既定）。`<script>` の `textContent` は HTML パーサーを通らないので serializePage で十分安全。
- 最終的な HTML 形:

```html
<div id="app"></div>
<script id="inertia-page" type="application/json">${pageJson}</script>
```

ただし既定の Inertia client が `data-page` 属性から読む点に注意。最も安全なのは `<div id="app" data-page='${pageJson}'></div>` で属性の囲みをシングルクオートにして `'` のエスケープを serializePage に頼らず別途行う。

→ **採用案**: README の React + Vite 例に倣い `<div id="app"></div>` の隣に `<script data-page="app" type="application/json" textContent="...">` ではなく、`createInertiaApp` のデフォルトに合わせて以下に統一:

```html
<div id="app" data-page='${escapedPageJson}'></div>
```

ここで `escapedPageJson` は `serializePage(page)` の結果に対して `'` (アポストロフィ) のみ追加で `&#39;` にエスケープした文字列を使う。

→ **再採用案（より安全）**: `<script>` の `textContent` 経由で受け渡し、`createInertiaApp({page: ...})` で明示的に渡す方法もあるが、デフォルト動作に倣う方が標準的。

最終決定: **属性経由（`<div id="app" data-page='...'>`）+ 単一引用符 + 追加エスケープ `' → &#39;`** を採用。理由は (1) `createInertiaApp` のデフォルト挙動 (2) JSON は `'` を含まないので追加エスケープ自体ほぼ no-op、(3) 万一 props に `'` が含まれても安全。

実装は `Server.res` 内でローカル関数 `escapeApostrophes` を定義し、`serializePage` の戻り値に適用する。

### `src/Routes.res` 改訂版

```rescript
module Pages = {
  let register = (app: Hono.app) => {
    app->Hono.get("/", ctx =>
      ctx->HonoInertia.render(
        "Home",
        {
          "title": "Home",
          "message": "Hono renders React pages directly through Inertia.",
        },
      )
    )

    app->Hono.get("/about", ctx =>
      ctx->HonoInertia.render(
        "About",
        {
          "title": "About",
          "stack": ["Hono", "Inertia.js", "@rescript/react", "Vite+"],
        },
      )
    )

    // POST handler retains async because Hono.jsonBody returns a promise.
    app->Hono.post("/greet", async ctx => {
      let raw = await ctx->Hono.req->Hono.jsonBody
      switch Validation.parseGreetForm(raw) {
      | Error(message) => ctx->Hono.status(422)->Hono.json({"error": message})
      | Ok({name}) =>
        ctx->HonoInertia.render(
          "Home",
          {
            "title": "Home",
            "message": `Hello, ${name}!`,
          },
        )
      }
    })
  }
}
```

### `src/client/Pages/Home.res` 改訂版

```rescript
@react.component
let make = (~title, ~message) =>
  <MainLayout>
    <h1> {React.string(title)} </h1>
    <p> {React.string(message)} </p>
    <p>
      <InertiaBindings.Link href="/about">
        {React.string("Read about the stack →")}
      </InertiaBindings.Link>
    </p>
  </MainLayout>
```

### `src/client/pages.js` 改訂版

```javascript
const pages = import.meta.glob("./Pages/**/*.res.mjs");

export async function resolvePage(name) {
  const path = `./Pages/${name}.res.mjs`;
  const loader = pages[path];
  if (!loader) {
    throw new Error(`Inertia page not found: ${name}`);
  }
  const mod = await loader();
  if (!mod.make) {
    throw new Error(`Inertia page module ${name} does not export 'make'`);
  }
  return { default: mod.make };
}
```

### `vite.config.mjs` 改訂版

```javascript
import { defineConfig } from "vite-plus";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "^/(?!@vite|src|node_modules|@id|@fs).*": {
        target: "http://localhost:3000",
        changeOrigin: true,
      },
    },
  },
});
```

### `HonoInertiaTemplateFiles.kt`

`files["index.html"]` のエントリを削除する以外は変更なし。

## テスト更新

`HonoInertiaTemplateFilesTest.kt` の以下のアサーションを更新:

| テストメソッド | 変更内容 |
|--------------|---------|
| `template ships rescript, vite, drizzle, README, and CI scaffolding` | `assertTrue(files.containsKey("index.html"))` を **削除** |
| `vite config uses Vite+ defineConfig and registers the inertiaPages plugin` | `inertiaPages` を含まないことを確認するアサーションに変更（テストメソッド名も更新） |
| `server wires the Inertia middleware before defining routes` | `HonoInertia.inertia({rootView` を含むことを確認 |
| `routes call HonoInertia render with named pages and structured props` | `await ctx->HonoInertia.render` でなく `ctx->HonoInertia.render` を含むことを確認 |
| 新規: `HonoInertia bindings declare a non-promise render and serializePage` | `render` の戻り値が `promise<` でないこと、`serializePage` が定義されていることを検証 |
| 新規: `Server.res defines a rootView that embeds Main res mjs and the page JSON` | `let rootView` と `Main.res.mjs` 文字列、`serializePage` 呼び出しを検証 |
| 新規: `Home page omits the redundant props type ascription` | `Home.res` に `type props` と `let _ = ` が含まれないことを確認 |
| 新規: `pages js shim throws when make export is missing` | `pages.js` に `if (!mod.make)` パターンを含むことを確認 |

## ドキュメント影響

- `README.md`: 影響なし（テンプレ名は同じ、API 機能セットも同等）
- `sphinx-docs/user/features/`: 影響なし（テンプレ詳細はリストアップのみ）
- `docs/product-requirements.md`: 影響なし（US-11 の内容は変わらず）
- `CLAUDE.md`: 影響なし（テンプレ説明文の Hono + Inertia 記述は維持）

## リスク

- `serializePage` のエスケープが `'` をカバーしないため、属性埋め込み形式は `<div id="app" data-page='${pageJson}'>` で `'` を `&#39;` に置換するワークアラウンドが必要。テンプレでこれを文字列置換で実装する。
- ReScript の optional record field `?:` を external 引数で使う場合、JS 側に `undefined` として渡る。`@hono/inertia` の `InertiaOptions` 型は両方 optional なので問題なし。
- `inertia({})` のように空オプションを渡す呼び出しは `Server.res` で発生しない（rootView を必ず指定するため）。
