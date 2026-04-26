---
myst:
  html_meta:
    "keywords": "monorepo, workspaces, pnpm, npm, yarn, bun, hono, react, vite, drizzle, shared types, rescript, validation"
---

# Monorepo (Hono + React)

{bdg-info}`Workspaces` {bdg-info}`Hono` {bdg-info}`Vite+/React` {bdg-success}`Drizzle` {bdg-success}`Validation`

A three-package workspace that wires a Hono backend, a Vite+/React client, and a shared types package together. Pick this template when you want clear separation between server, client, and the contract that connects them — and when you are willing to pay the workspace-tooling cost to get it. If a single `package.json` is enough, prefer the {doc}`full-stack` template instead.

The shared package exports ReScript types under a `Shared.*` namespace; the server depends on `@<project>/shared` and uses those types in route handlers; the client depends on the same workspace and consumes them inside React components. Editing a wire-format record in `packages/shared/src/Api.res` breaks the build on **both** sides until they agree — the structural typing is the entire point.

## What You Get

```
my-monorepo/
├── package.json                       # root: workspaces + dev/dev:server/dev:client/test fan-out
├── pnpm-workspace.yaml                # only when PM = pnpm; npm/yarn/bun use the workspaces field
├── packages/
│   ├── shared/
│   │   ├── rescript.json              # name = @<project>/shared, namespace = "Shared"
│   │   ├── package.json
│   │   └── src/
│   │       ├── Types.res              # domain records (Shared.Types.user)
│   │       └── Api.res                # wire-format records (Shared.Api.createUserReq)
│   ├── server/
│   │   ├── rescript.json              # bs-deps: @rescript/core, @<project>/shared, validation
│   │   ├── package.json               # Hono + Drizzle + libsql + validation
│   │   ├── drizzle.config.ts
│   │   ├── vitest.config.mjs          # loads vitest.setup.mjs
│   │   ├── vitest.setup.mjs           # pins DATABASE_URL=:memory:
│   │   ├── .env.example               # DATABASE_URL=file:./data/app.db
│   │   └── src/
│   │       ├── ServerMain.res         # entry — calls Server.start()
│   │       ├── Server.res             # Hono app: GET/POST /api/users, GET /api/hello
│   │       ├── Hono.res               # bindings shared with other Hono templates
│   │       ├── HonoNodeServer.res
│   │       ├── Schema.res             # Drizzle SQLite schema (users table)
│   │       ├── Db.res                 # libsql client + query helpers
│   │       ├── Validation.res         # zod or sury — selected in the wizard
│   │       └── __tests__/Server.test.mjs   # vitest: app.request("/api/hello") returns 200
│   └── client/
│       ├── rescript.json              # bs-deps: ..., @rescript/react, @<project>/shared
│       ├── package.json               # Vite+ + React + workspace dep on shared
│       ├── index.html
│       ├── vite.config.mjs            # proxies /api/* to the Hono server
│       └── src/
│           ├── Main.res               # React root render
│           ├── App.res                # users form + list — uses Shared.Types.user
│           ├── ApiClient.res          # fetch wrapper, types pinned to Shared.Api.*
│           └── __tests__/ApiClient.test.mjs
├── README.md                          # workspaces note + Database + Vite+ + Networking sections
├── LICENSE                            # MIT, holder = project name
├── .nvmrc                             # Node 24
├── .gitignore                         # adds dist/, .vite/, packages/*/dist/, packages/*/data/, .env
├── .editorconfig
└── .github/
    ├── dependabot.yml
    └── workflows/ci.yml               # install + test fan-out
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name` for the root and the prefix for every workspace package (`@<project>/shared`, `@<project>/server`, `@<project>/client`) |
| **Package manager** | npm / pnpm / yarn / bun. Selects the workspace declaration shape, the per-workspace command syntax, the `dev`/`test` fan-out script, the workspace-dep version specifier, the README install/run snippets, and the CI cache key |
| **Validation library** | `zod` ↔ `sury`. Chooses which `packages/server/src/Validation.res` variant ships and which dependency is added to the server workspace |

The package-manager choice affects three load-bearing things in addition to the install command:

| Concern | pnpm | yarn | npm | bun |
| --- | --- | --- | --- | --- |
| Workspace declaration | `pnpm-workspace.yaml` | `workspaces` field | `workspaces` field | `workspaces` field |
| Per-workspace command | `pnpm --filter ./packages/<x> <s>` | `yarn workspace ./packages/<x> run <s>` | `npm --workspace packages/<x> run <s>` | `bun --filter ./packages/<x> <s>` |
| Workspace dep version | `workspace:*` | `workspace:*` | `*` (npm rejects `workspace:*`) | `workspace:*` |

## Key Dependencies

### Root

| Package | Purpose | Version |
| --- | --- | --- |
| `concurrently` *(dev)* | Runs server + client dev watchers in parallel | `TemplateVersions.CONCURRENTLY` |

### `packages/shared`

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs the compiled `.res.mjs` imports | `TemplateVersions.RESCRIPT_RUNTIME` |

### `packages/server`

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript`, `@rescript/core`, `@rescript/runtime` | ReScript compiler + runtime | `TemplateVersions.RESCRIPT` / `_CORE` / `_RUNTIME` |
| `@<project>/shared` | Workspace dep on the shared types package | `workspace:*` (or `*` on npm) |
| `hono` | HTTP framework | `TemplateVersions.HONO` |
| `@hono/node-server` | Node HTTP adapter | `TemplateVersions.HONO_NODE_SERVER` |
| `zod` *or* `sury` | Validation backend | `TemplateVersions.ZOD` / `TemplateVersions.SURY` |
| `@libsql/client` | libsql client (SQLite-compatible, Turso-ready) | `TemplateVersions.LIBSQL_CLIENT` |
| `drizzle-orm` | Type-safe SQL builder | `TemplateVersions.DRIZZLE_ORM` |
| `drizzle-kit` *(dev)* | Migration generator | `TemplateVersions.DRIZZLE_KIT` |
| `vitest` *(dev)* | Test runner | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider | `TemplateVersions.VITEST_COVERAGE_V8` |
| `concurrently` *(dev)* | Pairs `rescript -w` with `node --watch` for dev | `TemplateVersions.CONCURRENTLY` |

### `packages/client`

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript`, `@rescript/core`, `@rescript/runtime` | ReScript compiler + runtime | `TemplateVersions.RESCRIPT` / `_CORE` / `_RUNTIME` |
| `@rescript/react` | React bindings | `TemplateVersions.RESCRIPT_REACT` |
| `@<project>/shared` | Workspace dep on the shared types package | `workspace:*` (or `*` on npm) |
| `react` / `react-dom` | React runtime | `TemplateVersions.REACT` / `TemplateVersions.REACT_DOM` |
| `vite-plus` | Vite+ build tool (vite + vitest + opinionated defaults) | `TemplateVersions.VITE_PLUS` |
| `@voidzero-dev/vite-plus-core` | Vite+ runtime core | `TemplateVersions.VITE_PLUS_CORE` |
| `vite` | Underlying Vite (held as a fallback dep) | `TemplateVersions.VITE` |
| `@vitejs/plugin-react` | React fast-refresh plugin | `TemplateVersions.VITEJS_PLUGIN_REACT` |
| `vitest` / `@vitest/coverage-v8` *(dev)* | Test runner + coverage | `TemplateVersions.VITEST` / `TemplateVersions.VITEST_COVERAGE_V8` |
| `concurrently` *(dev)* | Pairs `rescript -w` with `vp dev` | `TemplateVersions.CONCURRENTLY` |

## Key Files

### `packages/shared/src/Types.res` and `Api.res`

Two tiny modules that own the shared contract:

```rescript
// packages/shared/src/Types.res
type user = {id: int, name: string, email: string}

// packages/shared/src/Api.res
type createUserReq = {name: string, email: string}
type createUserRes = {id: int, name: string, email: string}
```

`rescript.json` for the shared package sets `namespace: "Shared"`, so consumers reference `Shared.Types.user` and `Shared.Api.createUserReq` — no flat-module collisions when two packages happen to define `Types`.

### `packages/server/src/Server.res`

Hono app with three routes (`GET /api/hello`, `GET /api/users`, `POST /api/users`). The POST route validates the body with `Validation.parseCreateUserReq`, then writes through Drizzle:

```rescript
app->Hono.post("/api/users", async ctx => {
  let raw = await ctx->Hono.req->Hono.jsonBody
  switch Validation.parseCreateUserReq(raw) {
  | Error(msg) => ctx->Hono.status(400)->Hono.json({"error": msg})
  | Ok(payload) =>
    let inserted =
      await Db.db
      ->Db.insert(Schema.users)
      ->Db.values({"name": payload.name, "email": payload.email})
      ->Db.returning
    ctx->Hono.status(201)->Hono.json(inserted->Array.get(0))
  }
})
```

The CORS hook is left commented; the dev workflow does not need it because Vite+ proxies `/api/*` to the server (same-origin).

### `packages/server/src/ServerMain.res`

A single line: `Server.start()`. The split exists so `vitest.setup.mjs` can pin `DATABASE_URL=:memory:` before tests `import("../Server.res.mjs")` to call `app.request("/api/hello")` without binding a port.

### `packages/server/vitest.setup.mjs`

```js
process.env.DATABASE_URL = ":memory:";
```

Pinning the URL before the test imports `Server.res.mjs` (transitively `Db.res`) ensures the libsql client opens an in-memory database instead of trying to read `./data/app.db`, which does not exist in the test temp directory.

### `packages/client/src/App.res` and `ApiClient.res`

`App.res` is a small users form + list. Its critical detail is the `Shared.Api.createUserReq` annotation: any drift in `packages/shared` produces a compile error in **both** the client and the server, exactly when you want the breakage:

```rescript
let req: Shared.Api.createUserReq = {name, email}
let _ = await ApiClient.createUser(req)
```

`ApiClient.res` wraps `fetch` and returns `Shared.Types.user[]` (or `Shared.Api.createUserRes`), keeping the type round-trip honest end-to-end.

### `packages/client/vite.config.mjs`

Vite+ config with a `/api/*` proxy that forwards to the Hono server. Generated via `ProjectFileBuilders.viteConfigWithProxy` so it stays consistent with the Vite+/Hono contract used by other templates.

## npm Scripts

### Root

| Script | Description |
| --- | --- |
| `dev` | `concurrently "<server-dev>" "<client-dev>"` — runs both watchers in parallel |
| `dev:server` | Per-workspace dispatch to `packages/server`'s `dev` script |
| `dev:client` | Per-workspace dispatch to `packages/client`'s `dev` script |
| `build:client` | Per-workspace dispatch to `packages/client`'s `build` script |
| `test` | Fan-out to every workspace exposing a `test` script (`pnpm -r run test` / `yarn workspaces foreach -A run test` / `npm --workspaces run test --if-present` / `bun --filter '*' run test`) |
| `test:coverage` | Same fan-out, but for `test:coverage` |
| `res:build` | `rescript` — one-shot compile across the monorepo |
| `res:dev` | `rescript -w` — watch mode |
| `res:clean` | `rescript clean` |

### `packages/server`

| Script | Description |
| --- | --- |
| `start` | `node src/ServerMain.res.mjs` |
| `dev` | `concurrently "npm:res:dev" "node --watch src/ServerMain.res.mjs"` |
| `test` | `vitest run` |
| `test:coverage` | `vitest run --coverage` |
| `db:generate` | `drizzle-kit generate` |
| `db:migrate` | `drizzle-kit migrate` |
| `res:build` / `res:dev` / `res:clean` | ReScript compile / watch / clean |

### `packages/client`

| Script | Description |
| --- | --- |
| `dev` | `concurrently "npm:res:dev" "vp dev"` — pairs ReScript watch with Vite+ dev server |
| `build` | `vp build` |
| `preview` | `vp preview` |
| `test` | `vp test` |
| `test:coverage` | `vp test --coverage` |
| `res:build` / `res:dev` / `res:clean` | ReScript compile / watch / clean |

## Day-Two Recipes

- {doc}`../recipes/setup-monorepo` — IDE-side LSP configuration tips for monorepo workspaces
- {doc}`../recipes/add-hono-endpoint` — add a typed POST/GET route on the server side
- {doc}`../recipes/setup-drizzle` — extend the Drizzle schema (the template ships one already)
- {doc}`../recipes/create-react-component` — pattern for adding a new client-side React component
- {doc}`../recipes/add-openapi-docs` — generate OpenAPI docs from the Hono routes

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- **The workspace declaration shape changes with the package manager.** pnpm uses a separate `pnpm-workspace.yaml`; npm, yarn, and bun all use the `workspaces` field in the root `package.json`. The wizard generates the right one automatically.
- **npm rejects `workspace:*`** as a dependency specifier and treats it as a literal version. The template falls back to `*` on npm and uses `workspace:*` for pnpm, yarn, and bun.
- **`concurrently` runs at three levels.** The root `dev` runs the two workspace watchers in parallel; each workspace `dev` runs `rescript -w` alongside its respective node/vite watcher. Without the inner `concurrently`, edits to `.res` files would not recompile while `dev` was running and you would chase stale `.res.mjs` for hours — a footgun previous template versions hit.
- **`packages/shared` does not have its own `rescript -w`** — the root `res:dev` covers it because ReScript walks the workspace from the project root. Each workspace also exposes `res:build`/`res:dev`/`res:clean` for when you want per-package control.
- **The vitest config pins `DATABASE_URL=:memory:`** so the smoke test does not try to open the real SQLite file. The committed `.env.example` shows the production-shape default (`file:./data/app.db`).
- **Vite+ is pre-1.0.** If a future release breaks, swap `vite-plus` for plain `vite` in `packages/client/vite.config.mjs` and replace the `vp` scripts with `vite`. The classic Vite dep is already declared as a fallback.
- **`packages/server/data/`, `packages/*/dist/`, `.vite/`, `.env`** are gitignored; the SQLite file produced by `db:migrate` stays out of version control.
- **CORS is off by default** because the Vite+ proxy keeps requests same-origin in dev. A ready-to-uncomment `Hono.cors(...)` block lives at the top of `packages/server/src/Server.res`. Pin an origin allowlist before deploying separately-hosted client and server.
