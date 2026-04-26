---
myst:
  html_meta:
    "keywords": "full-stack, single package, hono, react, vite, drizzle, shared types, rescript, validation, rest, graphql, relay"
---

# Full-Stack (single package)

{bdg-info}`Hono` {bdg-info}`Vite+/React` {bdg-success}`Drizzle` {bdg-success}`Validation` {bdg-primary}`Shared Types`

A full-stack ReScript application in **one** `package.json`. The server (Hono + Drizzle on SQLite) and client (Vite+/React) live side by side under `src/server/` and `src/client/`, and a third source root, `src/shared/`, holds types both sides import. Pick this template when you want shared types and a single-process dev loop without paying the workspace tooling tax that the {doc}`monorepo` template charges.

The wizard exposes an additional **API strategy** dimension: REST (handwritten Hono routes plus a fetch client) or GraphQL (graphql-yoga mounted on `/graphql` plus rescript-relay on the client). The shared infrastructure — Drizzle schema, libsql client, vitest setup, Vite+ proxy, `Shared.res` — is identical in both modes.

## What You Get (REST)

```
my-app/
├── rescript.json                       # sources: src/shared, src/server, src/client (subdirs)
├── package.json                        # ESM, bundles server + client deps
├── index.html                          # Vite entry — loads /src/client/ClientMain.res.mjs
├── vite.config.mjs                     # Vite+ + react plugin + /api proxy
├── drizzle.config.ts
├── vitest.config.mjs                   # loads vitest.setup.mjs
├── vitest.setup.mjs                    # pins DATABASE_URL=:memory:
├── .env.example                        # DATABASE_URL=file:./data/app.db
├── src/
│   ├── shared/Shared.res               # nested modules: Shared.Types.* / Shared.Api.*
│   ├── server/
│   │   ├── ServerMain.res              # entry — Server.start()
│   │   ├── Server.res                  # Hono app + GET /api/health + Routes.Users.register
│   │   ├── Routes.res                  # nested: module Users { let register = app => ... }
│   │   ├── Schema.res                  # Drizzle SQLite schema (users)
│   │   ├── Db.res                      # libsql client + helpers
│   │   ├── Validation.res              # zod or sury — selected in the wizard
│   │   ├── Hono.res / HonoNodeServer.res
│   │   └── __tests__/Server.test.mjs   # vitest: app.request("/api/health") returns 200
│   └── client/
│       ├── ClientMain.res              # React root render
│       ├── App.res                     # users form + list (uses Shared.Types.user)
│       ├── ApiClient.res               # fetch wrapper, types pinned to Shared.Api.*
│       └── __tests__/Api.test.mjs
├── README.md                           # Architecture + Shared Types + Database + Layout sections
├── LICENSE                             # MIT, holder = project name
├── .nvmrc                              # Node 24
├── .gitignore                          # adds data/, dist/, .vite/, drizzle/, .env
├── .editorconfig
└── .github/
    ├── dependabot.yml
    └── workflows/ci.yml                # install + vitest
```

The GraphQL variant adds `src/server/Yoga.res`, `src/server/GraphqlSchema.res`, `src/server/Resolvers.res`, `src/server/schema.graphql`, `src/client/RelayEnvironment.res`, `src/client/UsersListQuery.res`, and `relay.config.js`; gitignores `src/client/__generated__/`; declares `graphql`, `graphql-yoga`, `rescript-relay`, and `relay-compiler`; and adds a `relay`/`relay:watch` script pair plus an extra `npm:relay:watch` token in `dev`.

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, the LICENSE holder, and the `<h1>` text in `src/client/App.res` |
| **Package manager** | npm / pnpm / yarn / bun. Drives the `packageManager` Corepack field, the README install/run commands, and the CI cache key |
| **Validation library** | `zod` ↔ `sury`. Chooses which `src/server/Validation.res` variant ships and adds the matching dependency |
| **API strategy** | REST ↔ GraphQL. Selects the server/client shape (handwritten Hono routes + fetch vs. graphql-yoga + rescript-relay) and adjusts dependencies, scripts, and the rescript.json `ppx-flags` accordingly |

## Key Dependencies (REST)

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript`, `@rescript/core`, `@rescript/runtime` | ReScript compiler + runtime | `TemplateVersions.RESCRIPT` / `_CORE` / `_RUNTIME` |
| `@rescript/react` | React bindings | `TemplateVersions.RESCRIPT_REACT` |
| `react`, `react-dom` | React runtime | `TemplateVersions.REACT` / `TemplateVersions.REACT_DOM` |
| `hono` | HTTP framework | `TemplateVersions.HONO` |
| `@hono/node-server` | Node HTTP adapter | `TemplateVersions.HONO_NODE_SERVER` |
| `zod` *or* `sury` | Validation backend | `TemplateVersions.ZOD` / `TemplateVersions.SURY` |
| `@libsql/client` | libsql client (SQLite-compatible, Turso-ready) | `TemplateVersions.LIBSQL_CLIENT` |
| `drizzle-orm` | Type-safe SQL builder | `TemplateVersions.DRIZZLE_ORM` |
| `drizzle-kit` *(dev)* | Migration generator | `TemplateVersions.DRIZZLE_KIT` |
| `vite-plus`, `@voidzero-dev/vite-plus-core`, `vite` | Vite+ build tool + classic-Vite fallback | `TemplateVersions.VITE_PLUS` / `_CORE` / `TemplateVersions.VITE` |
| `@vitejs/plugin-react` | React fast-refresh | `TemplateVersions.VITEJS_PLUGIN_REACT` |
| `vitest`, `@vitest/coverage-v8` *(dev)* | Test runner + coverage | `TemplateVersions.VITEST` / `TemplateVersions.VITEST_COVERAGE_V8` |
| `concurrently` *(dev)* | Runs `rescript -w`, `node --watch`, `vp dev` together | `TemplateVersions.CONCURRENTLY` |

### GraphQL variant adds

| Package | Purpose | Version |
| --- | --- | --- |
| `graphql` | Reference implementation, peer dep of `graphql-yoga` and `rescript-relay` | `TemplateVersions.GRAPHQL` |
| `graphql-yoga` | GraphQL server mounted on Hono via `Yoga.res` | `TemplateVersions.GRAPHQL_YOGA` |
| `rescript-relay` | ReScript bindings + ppx for the Relay runtime | `TemplateVersions.RESCRIPT_RELAY` |
| `relay-compiler` *(dev)* | Generates typed `%relay()` artifacts from `schema.graphql` | `TemplateVersions.RELAY_COMPILER` |

## Key Files

### `src/shared/Shared.res`

```rescript
module Types = {
  type user = {id: int, name: string, email: string}
}

module Api = {
  type createUserReq = {name: string, email: string}
  type createUserRes = {id: int, name: string, email: string}
}
```

The nested module layout means consumers reach types as `Shared.Types.user` and `Shared.Api.createUserReq` — easy to grep, easy to refactor, no import gymnastics. Editing a field forces both `src/server/` and `src/client/` to recompile until they agree.

### `src/server/Server.res` (REST)

Hono app skeleton. Health route lives inline; the per-resource routes live in `src/server/Routes.res` and register themselves through a tiny convention:

```rescript
let app = Hono.createApp()

app->Hono.onError((err, ctx) => {
  Console.error(err)
  ctx->Hono.status(500)->Hono.json({"error": "Internal Server Error"})
})

app->Hono.get("/api/health", ctx => ctx->Hono.json({"status": "ok"}))
Routes.Users.register(app)

let start = () => {
  HonoNodeServer.serve({fetch: app->HonoNodeServer.honoFetch, port: 3000})
  Console.log("Server on http://localhost:3000 — try /api/health")
}
```

Add a new resource by adding a sibling module to `Routes.res` (`module Posts = { let register = app => ... }`) and a single `Routes.Posts.register(app)` call here.

### `src/server/Routes.res`

Holds the actual handler logic. The shipped `Users` group does the full Drizzle round trip (select → return rows; insert → 400 on validation failure or 201 + the inserted row).

### `src/server/Validation.res`

`parseCreateUserReq: JSON.t => result<createUserReq, string>` — the runtime contract for `POST /api/users`. The signature is identical between the zod and sury variants so callers do not branch on which library shipped.

### `src/server/ServerMain.res`

A single line: `Server.start()`. The split lets `vitest.setup.mjs` pin `DATABASE_URL=:memory:` before the test imports `Server.res.mjs` to call `app.request("/api/health")` without binding a port:

```js
// vitest.setup.mjs
process.env.DATABASE_URL = ":memory:";
```

### `src/client/App.res`

A small users form + list. The wire-format types are explicit, so any drift in `src/shared/Shared.res` shows up immediately:

```rescript
let req: Shared.Api.createUserReq = {name, email}
let _ = await ApiClient.createUser(req)
```

### `src/client/ApiClient.res`

A thin `fetch` wrapper. Everything routes under `/api/*` so the Vite+ proxy in `vite.config.mjs` can forward to the Hono server in dev (no CORS needed); in production the same server hosts both.

### `vite.config.mjs`

Generated through `ProjectFileBuilders.viteConfigWithProxy` — Vite+ + React fast-refresh + a `/api/*` proxy targeting the Hono server. Edit the proxy target if you split the deployment across two origins.

### GraphQL variant: `src/server/GraphqlSchema.res`, `Resolvers.res`, `schema.graphql`

The GraphQL schema lives in two places that must stay in sync: SDL in `src/server/schema.graphql` (read by the Relay compiler at build time) and the inlined `typeDefs` string in `src/server/GraphqlSchema.res` (read by graphql-yoga at runtime). Resolvers are organized into nested modules under `src/server/Resolvers.res` (e.g. `module Users`).

### GraphQL variant: `src/client/RelayEnvironment.res`, `UsersListQuery.res`

`RelayEnvironment.res` configures the Relay client; queries live in `%relay()` tags (e.g. `module UsersListQuery = %relay( ... )`). Run `relay-compiler --watch` to regenerate `src/client/__generated__/<Query>_graphql.res` after editing a query — those generated files are gitignored.

## npm Scripts (REST)

| Script | Description |
| --- | --- |
| `dev` | `concurrently "npm:res:dev" "npm:dev:server" "npm:dev:client"` — three watchers in parallel |
| `dev:server` | `node --watch src/server/ServerMain.res.mjs` |
| `dev:client` | `vp dev` — Vite+ dev server (with `/api` proxy to the Hono backend) |
| `build` | `vp build` — bundle the client for production |
| `preview` | `vp preview` — preview the production build |
| `test` | `vitest run` — covers server + client smoke tests |
| `test:coverage` | `vitest run --coverage` |
| `db:generate` | `drizzle-kit generate` — generate migration SQL |
| `db:migrate` | `drizzle-kit migrate` — apply pending migrations |
| `res:build` | `rescript` — one-shot compile across `src/shared`, `src/server`, `src/client` |
| `res:dev` | `rescript -w` — watch mode |
| `res:clean` | `rescript clean` |

## npm Scripts (GraphQL adds)

| Script | Description |
| --- | --- |
| `dev` | adds `"npm:relay:watch"` to the concurrently fan-out |
| `relay` | `relay-compiler` — emit typed `%relay()` artifacts once |
| `relay:watch` | `relay-compiler --watch` — keep regenerating on schema/query edits |

## Day-Two Recipes

- {doc}`../recipes/add-hono-endpoint` — add a typed POST/GET route under `src/server/Routes.res`
- {doc}`../recipes/add-graphql-resolver` — add a resolver and matching `%relay()` query (GraphQL variant)
- {doc}`../recipes/setup-drizzle` — deepen the Drizzle schema (`src/server/Schema.res`)
- {doc}`../recipes/create-react-component` — add a new client-side React component
- {doc}`../recipes/add-openapi-docs` — generate OpenAPI docs from Hono + zod schemas (REST variant)

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- **One `package.json` covers the world.** The server, client, and shared roots compile through a single `rescript.json` with three `sources` entries; running `npm install` once installs all dependencies for everyone.
- **`dev` boots three (or four, with GraphQL) processes via `concurrently`.** They are: `rescript -w`, `node --watch src/server/ServerMain.res.mjs`, `vp dev`, and (GraphQL only) `relay-compiler --watch`. If the server fails to start the first time `dev` runs, that means `rescript -w` has not produced its first `.res.mjs` yet — wait one cycle or rerun `dev`.
- **Vite+ proxies `/api/*` (and `/graphql` in the GraphQL variant) to the Hono server**, keeping browser requests same-origin in dev. CORS is off by default; uncomment the `Hono.cors(...)` block at the top of `src/server/Server.res` and pin an origin allowlist before deploying separately-hosted client and server.
- **`Shared.res` lives in `src/shared/`** because nested modules under `src/shared/Shared.res` keep the dependency direction obvious — both sides depend on `Shared`; `Shared` depends on neither. Renaming a record field forces both sides to recompile.
- **`vitest.setup.mjs` pins `DATABASE_URL=:memory:`** before any test imports `Server.res.mjs`. The committed `.env.example` shows the production-shape default (`file:./data/app.db`).
- **`data/`, `dist/`, `.vite/`, `drizzle/`, `.env`** (and `src/client/__generated__/` in the GraphQL variant) are gitignored.
- **GraphQL ships with a one-time bootstrap step**: `pnpm relay` (or `npm run relay`) must run at least once before the first build to populate `src/client/__generated__/`. The `dev` script keeps `relay:watch` running so this is invisible after the first cycle.
- **Vite+ is pre-1.0.** If a release breaks, swap `vite-plus` for `vite` in `vite.config.mjs` and replace the `vp` scripts with `vite`. The classic Vite dep is already declared as a fallback.
