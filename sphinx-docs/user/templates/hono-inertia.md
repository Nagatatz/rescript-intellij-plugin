---
myst:
  html_meta:
    "keywords": "hono, inertia, inertiajs, react, rescript, vite-plus, viteplus, server-driven, spa, drizzle, libsql, sqlite, validation, zod, sury"
---

# Hono + Inertia (React)

{bdg-info}`Node.js / Bun` {bdg-primary}`Inertia.js` {bdg-success}`Server-driven` {bdg-warning}`Vite+ alpha`

A server-driven SPA in which Hono routes call `c.render(component, props)` through the **`@hono/inertia`** middleware, and **`@inertiajs/react`** v3 mounts the matching React page on the client. There is no separate REST/GraphQL layer — the controller hands props straight to the page component.

The template uses **Vite+ (`vite-plus`)**, VoidZero's unified toolchain that collapses Vite, Vitest, Oxlint, Oxfmt, and Rolldown behind a single `vp` CLI. **SSR is on by default**: `src/Ssr.res` server-renders each page through `react-dom/server`'s `renderToString`, the rendered HTML is embedded in `<div id="app">…</div>`, and the browser entry hydrates with `hydrateRoot`. Subsequent Inertia visits (`X-Inertia: true`) return JSON only and bypass SSR.

## What You Get

```
my-project/
├── rescript.json
├── package.json                # vp dev / vp build / vp test / vp check
├── vite.config.mjs             # vite-plus + react + inertiaPages plugins
├── drizzle.config.ts           # drizzle-kit config (reads Schema.res.mjs)
├── index.html                  # Inertia HTML host page (#app + data-page)
├── src/
│   ├── Server.res              # Hono + HonoInertia.inertia() + Routes.Pages.register
│   ├── ServerMain.res          # Production entry — calls Server.start()
│   ├── Routes.res              # GET / (Home), GET /about, POST /greet
│   ├── HonoInertia.res         # @hono/inertia middleware bindings
│   ├── InertiaBindings.res     # @inertiajs/react bindings (Link / usePage / createInertiaApp)
│   ├── Schema.res              # Drizzle SQLite schema (posts table)
│   ├── Db.res                  # libsql + Drizzle helpers
│   ├── Validation.res          # zod or sury — selected in the wizard
│   ├── Logger.res              # hono/logger
│   ├── Hono.res                # Hono bindings
│   ├── HonoNodeServer.res      # @hono/node-server bindings
│   ├── client/
│   │   ├── Main.res            # createInertiaApp entry
│   │   ├── pages.js            # JS shim — import.meta.glob page resolver
│   │   ├── MainLayout.res      # Shared chrome (uses InertiaBindings.usePage)
│   │   └── Pages/
│   │       ├── Home.res        # @react.component Home page
│   │       └── About.res       # @react.component About page
│   └── __tests__/
│       └── Server.test.mjs     # vitest hits app.request — both HTML + Inertia JSON paths
├── README.md                   # API / Frontend / Project Layout / About Vite+
├── LICENSE                     # MIT, holder = project name
├── .env.example                # documents DATABASE_URL
├── .nvmrc                      # Node 24
├── .gitignore                  # node_modules, dist/, data/, drizzle/, .vite/, .env
├── .editorconfig
└── .github/
    ├── dependabot.yml
    └── workflows/ci.yml
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, license holder, and `<title>` in `index.html` |
| **Package manager** | npm / pnpm / yarn / bun. Affects `packageManager` field, README install/run commands, and CI cache key |
| **Validation library** | `zod` ↔ `sury`. Picks `src/Validation.res` and adds the matching dependency. Both expose the same `parseGreetForm` signature |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs the compiled `.res.mjs` imports | `TemplateVersions.RESCRIPT_RUNTIME` |
| `@rescript/react` | React JSX bindings | `TemplateVersions.RESCRIPT_REACT` |
| `react` / `react-dom` | React runtime (peer of `@inertiajs/react` v3) | `TemplateVersions.REACT` |
| `hono` | HTTP router | `TemplateVersions.HONO` |
| `@hono/node-server` | Node adapter for Hono | `TemplateVersions.HONO_NODE_SERVER` |
| `@hono/inertia` | Inertia middleware that overrides `c.render` | `TemplateVersions.HONO_INERTIA` |
| `@inertiajs/react` | Inertia client + `<Link>` / `usePage` / `createInertiaApp` | `TemplateVersions.INERTIA_REACT` |
| `@libsql/client` | SQLite driver (also speaks Turso `libsql://`) | `TemplateVersions.LIBSQL_CLIENT` |
| `drizzle-orm` | Type-safe SQL builder | `TemplateVersions.DRIZZLE_ORM` |
| `zod` *or* `sury` | Form input validation (chosen in the wizard) | `TemplateVersions.ZOD` / `SURY` |
| `vite` *(dev)* | Underlying bundler — kept as a direct dep for the Vite+ → Vite fallback | `TemplateVersions.VITE` |
| `vite-plus` *(dev)* | Vite+ unified CLI (`vp`) | `TemplateVersions.VITE_PLUS` |
| `@voidzero-dev/vite-plus-core` *(dev)* | Vite+ runtime helpers | `TemplateVersions.VITE_PLUS_CORE` |
| `@vitejs/plugin-react` *(dev)* | React fast-refresh + JSX support | `TemplateVersions.VITEJS_PLUGIN_REACT` |
| `drizzle-kit` *(dev)* | Migration generator/runner | `TemplateVersions.DRIZZLE_KIT` |

Note: `vitest`, `eslint`, and `prettier` are **deliberately omitted** — Vite+ ships them under `vp test` / `vp check` already.

## Key Files

### `src/Server.res`

The middleware order is the linchpin: `inertia()` must be registered **before** any route that calls `c->HonoInertia.render(...)`.

```rescript
let app = Hono.createApp()
app->Hono.use(Logger.logger())
app->Hono.use(HonoInertia.inertia())

app->Hono.onError((err, ctx) => {
  Console.error(err)
  ctx->Hono.status(500)->Hono.json({"error": "Internal Server Error"})
})

app->Hono.get("/health", ctx => ctx->Hono.json({"status": "ok"}))
Routes.Pages.register(app)

let start = () => {
  HonoNodeServer.serve({fetch: app->HonoNodeServer.honoFetch, port: 3000})
}
```

`Server.res` never calls `serve()` at module top level — the work lives in `start()` and is invoked from `ServerMain.res`. Tests can therefore `import("../Server.res.mjs")` without binding port 3000.

### `src/Routes.res`

Each Inertia route renders a named React page and hands typed props:

```rescript
app->Hono.get("/", async ctx => {
  await ctx->HonoInertia.render(
    "Home",
    {"title": "Home", "message": "Hono renders React pages directly through Inertia."},
  )
})
```

Inertia sniffs the `X-Inertia` header on the request: present → JSON page envelope; absent → the HTML host page (so deep links and address-bar typing work).

### `src/HonoInertia.res`

```rescript
@module("@hono/inertia")
external inertia: unit => Hono.middleware = "inertia"

@send
external render: (Hono.context, string, 'props) => promise<'response> = "render"
```

The middleware overrides Hono's existing `c.render`, so we model `render` as a `@send` external against the same context type the rest of the template already uses.

### `src/InertiaBindings.res`

Minimal bindings for `@inertiajs/react` v3:

```rescript
@module("@inertiajs/react")
external createInertiaApp: appOptions => unit = "createInertiaApp"

module Link = {
  @module("@inertiajs/react") @react.component
  external make: (~href: string, ~method: string=?, ~children: React.element=?, ...) => React.element = "Link"
}

@module("@inertiajs/react")
external usePage: unit => page<'props> = "usePage"
```

`MainLayout.res` calls `usePage()` to read `page.url`; pages call `<InertiaBindings.Link href="/about">` instead of plain `<a>` tags so Inertia intercepts the click.

### `src/client/pages.js` — the JS shim

ReScript 12's `Js.import` resolves the import path **statically** — `import("./Pages/" ++ name + ".res.mjs")` is rejected by the compiler. `import.meta.glob` is a Vite-only extension that builds a lazy-loader table at build time, so we keep the resolver in plain JS:

```js
const pages = import.meta.glob("./Pages/**/*.res.mjs");

export async function resolvePage(name) {
  const path = `./Pages/${name}.res.mjs`;
  const loader = pages[path];
  if (!loader) throw new Error(`Inertia page not found: ${name}`);
  const mod = await loader();
  return { default: mod.make ?? mod.default ?? mod };
}
```

Inertia reads `.default` off whatever `resolve` returns; ReScript-compiled modules expose their React component as `make`, so the shim re-wraps it under `default`.

### `src/client/Main.res`

```rescript
@module("./pages.js")
external resolvePage: string => promise<{..}> = "resolvePage"

@module("react-dom/client")
external hydrateRoot: (Dom.element, React.element) => unit = "hydrateRoot"

InertiaBindings.createInertiaApp({
  resolve: resolvePage,
  setup: ({el, app, props}) => {
    hydrateRoot(el, React.createElement(app, props))
  },
})
```

### `src/Ssr.res`

```rescript
@module("react-dom/server")
external renderToString: React.element => string = "renderToString"

module InertiaApp = {
  @module("@inertiajs/react") @react.component
  external make: (
    ~initialPage: HonoInertia.pageObject,
    ~initialComponent: 'component,
    ~resolveComponent: string => 'component,
  ) => React.element = "App"
}

external castComponent: 'a => 'b = "%identity"

let resolveComponent = (name: string) =>
  switch name {
  | "Home" => castComponent(Home.make)
  | "About" => castComponent(About.make)
  | other => Js.Exn.raiseError(`Inertia SSR: unknown page "${other}"`)
  }

let renderInertia = (page: HonoInertia.pageObject) => {
  let initialComponent = resolveComponent(page.component)
  let body = renderToString(
    <InertiaApp initialPage=page initialComponent resolveComponent />,
  )
  {head: [], body}
}
```

Pages render through Inertia's React `<App>` so `usePage()` (used by `MainLayout`) finds its provider context. `resolveComponent` runs synchronously, which keeps `renderToString` synchronous and `rootView` itself synchronous. Adding a new page requires updating this switch alongside `client/Pages/` and `Routes.res` — the explicit registry catches missing pages at compile time rather than as runtime 500s.

### `vite.config.mjs`

```js
import { defineConfig } from "vite-plus";
import react from "@vitejs/plugin-react";
import { inertiaPages } from "@hono/inertia/vite";

export default defineConfig({
  plugins: [react(), inertiaPages()],
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

The proxy forwards Inertia GET/POST requests to the Hono server during dev; static assets and HMR continue to be served by Vite+ directly. In production the same Hono server serves `dist/` and the React bundle is just a static asset.

## npm Scripts

| Script | Description |
| --- | --- |
| `dev` | `vp dev` — start the Vite+ dev server (run `node --watch src/ServerMain.res.mjs` in a second shell, or pair with `concurrently` if you prefer one process) |
| `build` | `vp build` — produce a production bundle into `dist/` |
| `preview` | `vp preview` — serve the production bundle locally |
| `test` | `vp test` — run the Vitest suite |
| `test:coverage` | `vp test --coverage` — Vitest + V8 coverage |
| `check` | `vp check` — Oxlint + Oxfmt + typecheck in one pass |
| `db:generate` | `drizzle-kit generate` — emit migration SQL from `Schema.res` |
| `db:migrate` | `drizzle-kit migrate` — apply pending migrations |
| `res:dev` | `rescript -w` — recompile on save |
| `res:build` | `rescript` — one-shot compile |
| `res:clean` | `rescript clean` |

## Adding a New Page

1. Drop a new file under `src/client/Pages/<Name>.res`:

   ```rescript
   @react.component
   let make = (~title) => <MainLayout> <h1> {React.string(title)} </h1> </MainLayout>
   ```

2. Register a route in `src/Routes.res`:

   ```rescript
   app->Hono.get("/the/path", async ctx => {
     await ctx->HonoInertia.render("<Name>", {"title": "Hello"})
   })
   ```

`pages.js` discovers the new file automatically through `import.meta.glob` — no central registry to edit.

## Caveats — Vite+ alpha

Vite+ is currently shipping in alpha (`0.1.x` at the time of writing). The CLI and config surface are still in flux:

- The `test` / `lint` / `fmt` configuration keys are not yet documented in `vite.config.mjs`. Defaults work; configuration may move.
- API changes between 0.1.x patch releases are possible.

If a Vite+ release breaks this template, you can fall back to plain Vite by:

1. Replacing `vite-plus` with `vite` in `vite.config.mjs` (`import { defineConfig } from "vite"`).
2. Replacing the `vp ...` scripts in `package.json` with `vite` / `vitest run` directly.
3. Adding `vitest`, `eslint`, `prettier` (and their plugins) as devDependencies to fill the gaps Vite+ used to cover.

Hono, Inertia, and Drizzle do not depend on Vite+ and stay untouched.

## Day-Two Recipes

- {doc}`../recipes/add-hono-endpoint` — adding a REST route alongside Inertia pages (e.g. webhook intake, health probes)
- {doc}`../recipes/setup-drizzle` — Drizzle schema, migrations, and Turso swap

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- **CSR only.** The template renders pages on the client; server-side rendering is planned as a follow-up. If you need first-paint SEO today, render a static skeleton in `index.html` or pre-render specific routes with `vite build --ssr`.
- **`Server.res` never calls `serve()` at module top level** — that work lives in `start()` and is invoked from `ServerMain.res`. Tests can therefore `import("../Server.res.mjs")` without binding port 3000.
- **Inertia middleware order matters.** `HonoInertia.inertia()` must be registered before any route that calls `c->HonoInertia.render(...)`. The shipped `Server.res` enforces this with a comment.
- **The `pages.js` shim is intentional.** It is the smallest piece of JS we can ship — five lines around `import.meta.glob` — and lets the rest of the template stay pure ReScript.
- **Defaults to a local SQLite file.** `DATABASE_URL=file:./data/app.db`. Swap for a `libsql://` URL (Turso) without touching code.
- **React 19 is required.** `@inertiajs/react` v3 declares `react ^19` as a peer dependency. `@rescript/react` works with React 19.
- **Generated artifacts are gitignored:** `dist/`, `data/`, `drizzle/`, `.vite/`, `.env`.
