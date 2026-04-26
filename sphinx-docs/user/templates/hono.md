---
myst:
  html_meta:
    "keywords": "hono template, rescript, node.js, rest api, drizzle, libsql, sqlite, openapi, scalar, validation, zod, sury"
---

# Hono (Node.js)

{bdg-info}`Node.js` {bdg-primary}`REST` {bdg-success}`Validation`

A production-shaped Hono REST service running on Node.js. The template is intentionally not a "Hello World" — it wires up the four day-two concerns most teams reach for in week one and want to stop reverse-engineering: persistence (SQLite via libsql + Drizzle ORM), runtime body validation (zod or sury, chosen in the wizard), structured request logging, and OpenAPI 3.1 docs served interactively at `/docs` via Scalar UI.

Pick this template when you want a JSON HTTP API on Node and don't want to spend the first sprint deciding which database client to use, where to put validation, and how to publish docs. The shipped users CRUD demonstrates the full stack so you can see exactly where to plug new endpoints in.

## What You Get

```
my-project/
├── rescript.json                  # depends on @rescript/core + validation lib (no JSX)
├── package.json                   # type: "module", hono + node-server + drizzle + scalar deps
├── drizzle.config.ts              # drizzle-kit config — schema: ./src/Schema.res.mjs, dialect: sqlite
├── vitest.config.mjs              # loads vitest.setup.mjs before any module imports
├── vitest.setup.mjs               # pins DATABASE_URL=:memory: before Db.res top-level runs
├── src/
│   ├── Server.res                 # `let app = ...` + `let start = () => ...` (importable, no port bind)
│   ├── ServerMain.res             # entry point: calls Server.start()
│   ├── Routes.res                 # module Users { register: app => ... } — GET/POST/PUT/DELETE /users
│   ├── Schema.res                 # Drizzle SQLite schema (sqliteTable + intCol/textCol)
│   ├── Db.res                     # libsql client + Drizzle wrapper + query helpers (eq, and, asc, …)
│   ├── Logger.res                 # hono/logger middleware binding
│   ├── Scalar.res                 # @scalar/hono-api-reference binding
│   ├── Validation.res             # zod or sury — parseCreateUserInput for POST /users body
│   ├── ZodOpenapi.res             # zod variant only — @hono/zod-openapi bindings
│   ├── Hono.res                   # auto-generated Hono bindings (createApp/get/post/json/text/...)
│   ├── HonoNodeServer.res         # auto-generated @hono/node-server bindings (serve/honoFetch)
│   └── __tests__/Server.test.mjs  # vitest — imports Server.res.mjs, calls app.request("/health")
├── README.md                      # API + Database + OpenAPI Docs + Project Layout sections
├── LICENSE                        # MIT, holder = project name
├── .nvmrc                         # Node 24
├── .env.example                   # DATABASE_URL=file:./data/app.db (or libsql:// for production)
├── .gitignore                     # node_modules + ReScript output + dist/, data/, drizzle/, .env
├── .editorconfig                  # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml             # weekly npm updates
    └── workflows/ci.yml           # install + rescript build + vitest
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, the LICENSE holder, and is referenced by the README |
| **Package manager** | npm / pnpm / yarn / bun. Sets `packageManager`, README install/run snippets, and the CI cache key. The README's Database section substitutes the chosen `db:generate` / `db:migrate` invocations |
| **Validation library** | `zod` ↔ `sury`. Determines which `src/Validation.res` ships, whether `src/ZodOpenapi.res` is generated, whether `@hono/zod-openapi` is added to `dependencies`, and whether `zod` or `sury` is installed |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs imported by compiled `.res.mjs` | `TemplateVersions.RESCRIPT_RUNTIME` |
| `hono` | HTTP framework (router, middleware, context) | `TemplateVersions.HONO` |
| `@hono/node-server` | Node.js adapter — turns the Hono fetch handler into an HTTP server | `TemplateVersions.HONO_NODE_SERVER` |
| `@hono/zod-openapi` | *zod variant only* — declarative routes that double as the OpenAPI spec source | `TemplateVersions.HONO_ZOD_OPENAPI` |
| `@scalar/hono-api-reference` | Mounts the Scalar UI at `/docs` | `TemplateVersions.SCALAR_HONO_API_REFERENCE` |
| `zod` *or* `sury` | Validation backend (HTTP body parser) | `TemplateVersions.ZOD` / `SURY` |
| `@libsql/client` | libsql / SQLite client (works against local files and Turso) | `TemplateVersions.LIBSQL_CLIENT` |
| `drizzle-orm` | ORM + query builder (read/write) | `TemplateVersions.DRIZZLE_ORM` |
| `drizzle-kit` *(dev)* | Schema diff + migration generator | `TemplateVersions.DRIZZLE_KIT` |
| `vitest` *(dev)* | Test runner (uses `app.request` rather than booting a server) | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |

## Key Files

### `src/Server.res`

The Hono app definition. Two top-level bindings, deliberately separated:

- `let app = Hono.createApp()` — the app is constructed at module load, middleware and routes are attached, and the binding is exported. Importing this module is **side-effect-free** with respect to networking (no port is bound).
- `let start = () => { HonoNodeServer.serve(...); Console.log(...) }` — wraps the actual `serve()` call so that booting only happens when something explicitly calls `Server.start()`.

The split is what makes the test harness work: `Server.test.mjs` can `import { app } from "../Server.res.mjs"` and drive endpoints through `app.request("/health")` without binding port 3000.

The shipped `app` registers:

- `Logger.logger()` middleware
- A global `onError` handler converting uncaught exceptions into JSON 500 responses
- `GET /` (health text), `GET /health` (JSON `{ status: "ok" }`)
- `Routes.Users.register(app)` (the CRUD module)
- `GET /openapi.json` (a stub spec — see Notes)
- `GET /docs` (Scalar UI bound to `/openapi.json`)

```rescript
let app = Hono.createApp()
app->Hono.use(Logger.logger())
app->Hono.onError((err, ctx) => {
  Console.error(err)
  ctx->Hono.status(500)->Hono.json({"error": "Internal Server Error"})
})

let start = () => {
  HonoNodeServer.serve({fetch: app->HonoNodeServer.honoFetch, port: 3000})
  Console.log("Server on http://localhost:3000 — docs at /docs")
}
```

### `src/ServerMain.res`

A two-line entry point:

```rescript
Server.start()
```

`package.json`'s `start` and `dev` scripts run `ServerMain.res.mjs` (not `Server.res.mjs`) — that is what triggers the actual port bind. Tests import `Server.res.mjs` directly and never go through this file.

### `src/Routes.res`

Route registrations grouped by domain. The shipped `module Users` shows the canonical pattern: `let register = (app) => { app->Hono.get(...); app->Hono.post(...) }`. Adding new groups means adding `module Posts = { let register = ... }` and calling `Routes.Posts.register(app)` from `Server.res`.

The POST handler is also the canonical example of where validation lives:

```rescript
app->Hono.post("/users", async ctx => {
  let raw = await ctx->Hono.req->Hono.jsonBody
  switch Validation.parseCreateUserInput(raw) {
  | Error(msg) => ctx->Hono.status(400)->Hono.json({"error": msg})
  | Ok(payload) =>
    let inserted =
      await Db.db
      ->Db.insert(Schema.users)
      ->Db.values({"name": payload.name, "email": payload.email})
      ->Db.returning
    ctx->Hono.status(201)->Hono.json(inserted)
  }
})
```

Failed validation is a 400 with `{"error": msg}` — never reaches the database.

### `src/Schema.res`

The Drizzle schema. Stays focused on persistence (no zod / sury intertwined) so the diff that `db:generate` reads is small and unambiguous.

```rescript
let users = sqliteTable("users", {
  "id": intCol("id", {"primaryKey": true, "autoIncrement": true}),
  "name": textCol("name", {"notNull": true}),
  "email": textCol("email", {"notNull": true}),
})
```

### `src/Db.res`

Bindings for the libsql client + Drizzle query builder. Handles three things:

1. Reads `DATABASE_URL` from `process.env` (default: `file:./data/app.db`)
2. Constructs the client + the `db` Drizzle wrapper at module load
3. Exports the query helpers: `select`, `from`, `insert`, `values`, `update`, `set`, `deleteFrom`, `where`, `orderBy`, `limit`, `offset`, `groupBy`, `allAsync`, `getAsync`, `returning`, plus the comparison operators (`eq`, `gt`, `lt`, `inArray`, `like`, …) and the boolean combinators (`and`, `or`, `not`)

Because the client is constructed at module load, anything that imports `Db.res.mjs` will open the database immediately — see the vitest setup below.

### `src/Validation.res`

`parseCreateUserInput: JSON.t => result<createUserInput, string>`. Same signature regardless of zod vs sury, so `Routes.res` does not branch on which library shipped. Failed parses propagate as `Error(msg)` and become HTTP 400 in the route handler.

The zod variant uses `@module("zod")` externals and `parse(...)`; the sury variant uses `S.object` + `S.parseOrThrow` and traps `S.Error` to turn it back into a `result`.

### `src/ZodOpenapi.res` *(zod variant only)*

Bindings for `@hono/zod-openapi` — `createApp`, `createRoute`, `openapiRoute`, `doc`. The zod variant ships these because `@hono/zod-openapi` lets you describe a route declaratively once and have it serve both as the runtime router and as the source of the OpenAPI spec. The sury variant ships *no* equivalent file; if you need OpenAPI generation under sury, use `S.toJSONSchema` and assemble the document by hand in `Server.res`.

### `src/Logger.res`

Two-line binding for `hono/logger`:

```rescript
@module("hono/logger") external logger: unit => Hono.middleware = "logger"
```

Mounted via `app->Hono.use(Logger.logger())` in `Server.res`.

### `src/Scalar.res`

Binding for `@scalar/hono-api-reference`. Mounted in `Server.res` as `app->Hono.get("/docs", Scalar.apiReference({"spec": {"url": "/openapi.json"}}))`.

### `drizzle.config.ts`

drizzle-kit config — schema location, dialect, output directory, and database URL. Reads `DATABASE_URL` from the environment (default: `file:./data/app.db`).

```ts
export default defineConfig({
  schema: "./src/Schema.res.mjs",
  out: "./drizzle",
  dialect: "sqlite",
  dbCredentials: { url: process.env.DATABASE_URL ?? "file:./data/app.db" },
});
```

### `vitest.config.mjs` + `vitest.setup.mjs`

Two-file pair that solves a subtle problem: `Db.res` constructs the libsql client at module load, so any test that imports `Server.res.mjs` (which transitively imports `Db.res.mjs`) would otherwise try to open `./data/app.db` — a file that does not exist in fresh checkouts or in the integration-test temp dir.

`vitest.setup.mjs` pins `process.env.DATABASE_URL = ":memory:"` before any test file's static imports execute. `vitest.config.mjs` registers it via `setupFiles`. The result: tests run against a per-process in-memory SQLite, never touch disk, and `Server.res` requires no test-only branching.

### `src/__tests__/Server.test.mjs`

Imports `app` from the compiled `Server.res.mjs` and exercises endpoints via Hono's built-in `app.request(...)` — the same fetch-style harness Hono ships for testing without a running HTTP server.

```js
import { app } from "../Server.res.mjs";
const res = await app.request("/health");
expect(res.status).toBe(200);
```

This works precisely because `Server.res` does *not* call `serve()` at module load.

### `.env.example`

Documents the single environment variable the template reads:

```
DATABASE_URL=file:./data/app.db
```

Replace with a Turso `libsql://` URL (or any libsql-compatible endpoint) to scale beyond a local file.

## npm Scripts

| Script | Description |
| --- | --- |
| `start` | `node src/ServerMain.res.mjs` — run the server once |
| `dev` | `node --watch src/ServerMain.res.mjs` — run with file-watching restart |
| `test` | `vitest run` — execute the smoke suite (in-memory DB) |
| `test:coverage` | `vitest run --coverage` — same, with v8 coverage |
| `db:generate` | `drizzle-kit generate` — diff `Schema.res.mjs` against the current DB and emit migration SQL into `./drizzle/` |
| `db:migrate` | `drizzle-kit migrate` — apply pending migrations to the database pointed at by `DATABASE_URL` |
| `res:build` | `rescript` — one-shot ReScript compile |
| `res:dev` | `rescript -w` — recompile on save |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` |

In a normal session you'll keep two terminals open: `npm run res:dev` for the ReScript watcher and `npm run dev` for the auto-restarting server.

## Day-Two Recipes

- {doc}`../recipes/add-hono-endpoint` — end-to-end walkthrough adding a new route + Drizzle table + zod validation + OpenAPI doc
- {doc}`../recipes/setup-drizzle` — schema modeling and migration patterns
- {doc}`../recipes/add-openapi-docs` — going from the stub `/openapi.json` to a real generated spec

For ReScript-side editor workflows once the project is open, see {doc}`../features/index`.

## Notes

- **`/openapi.json` ships as a stub.** The default response is `{"openapi": "3.1.0", "info": {...}, "paths": {}}` — Scalar UI mounts cleanly but shows zero endpoints. Wire the real spec by either (a) declaring routes through `@hono/zod-openapi` (`ZodOpenapi.res` ships those bindings in the zod variant) or (b) generating a JSON Schema from sury (`S.toJSONSchema`) and assembling the OpenAPI document by hand. The {doc}`../recipes/add-openapi-docs` recipe walks both paths.
- **Validation is the wall in front of every external input.** The route handler is the canonical place: `parseCreateUserInput(raw)` — `Error → 400`, `Ok → DB`. Do not destructure raw JSON anywhere upstream of the validator.
- **`Db.res` opens the database at module load.** This is a deliberate trade — every route handler gets a hot client without lazy-init plumbing — but it means `import "../Db.res.mjs"` from anywhere triggers an open. The vitest setup pins `DATABASE_URL=:memory:` before module load so tests never touch disk; preserve that ordering if you customize the test config.
- **`Server.res` must remain side-effect-free.** The split between `let app = ...` and `let start = () => ...` is enforced by tests (the server-test asserts no top-level `HonoNodeServer.serve(...)` call). If you need module-load work, do it in `start()` so importers stay safe.
- **The sury variant has no `ZodOpenapi.res`.** That is intentional, not an oversight — `@hono/zod-openapi` is zod-only. Sury users get a smaller `package.json` (no `@hono/zod-openapi`) and write OpenAPI generation themselves when they need it.
- **CORS is commented out, not enabled.** A one-line `app->Hono.use(Hono.cors({"origin": "..."}))` block is shipped commented in `Server.res`. Uncomment when the API is called from a browser origin.
- **Database URL precedence.** `Db.res`, `drizzle.config.ts`, and the test harness all read `DATABASE_URL` from `process.env` (with `file:./data/app.db` as the fallback). Production deployments should set it to a `libsql://` URL.
- **Migrations live in `./drizzle/`.** That directory is gitignored by default — flip the ignore for your own project if you want migrations under version control (the standard workflow for production teams).
- **The CI workflow runs the full suite.** `npm install + rescript build + vitest`. Vitest will use the in-memory database via `vitest.setup.mjs`, so CI does not need any external service to pass the smoke test.
- **OpenAPI 3.1, not 3.0.** The stub spec declares `"openapi": "3.1.0"`. If you generate clients from the spec, ensure your generator supports 3.1 (most modern ones do; `openapi-generator` may need `--openapi-generator-version` flags to opt in).
