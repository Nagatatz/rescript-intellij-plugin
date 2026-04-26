---
myst:
  html_meta:
    "keywords": "cloudflare workers, wrangler, hono, kv, workers kv, edge, v8 isolate, rescript template, validation, zod, sury"
---

# Cloudflare Workers

{bdg-info}`Cloudflare Workers` {bdg-primary}`Hono` {bdg-success}`Validation`

A **Cloudflare Workers** service powered by **Hono** with a working **Workers KV** example baked in. The shipped app goes beyond "hello world": it stores submitted greetings in a KV namespace via `POST /greetings` and reads them back via `GET /greetings`, so the moment you run `wrangler dev` you have a real binding to exercise — not a placeholder.

The runtime is V8 isolates, not Node. The template uses Hono's native `fetch` export rather than `@hono/node-server`, and `wrangler.jsonc` (note: `.jsonc`, not `.json`) pre-declares the KV binding so the dev session boots with a real local store the first time you `wrangler dev`.

## What You Get

```
my-project/
├── rescript.json
├── package.json
├── wrangler.jsonc               # Workers config: name, main, compatibility_date, kv_namespaces
├── src/
│   ├── Server.res               # Hono app + POST/GET /greetings; `export default app`
│   ├── Kv.res                   # Workers KV bindings (get / put / list)
│   ├── Hono.res                 # Hono bindings
│   ├── Validation.res           # zod or sury — selected in the wizard
│   └── __tests__/Server.test.mjs # vitest smoke import
├── README.md                    # API / KV Setup / Deploy
├── LICENSE                      # MIT, holder = project name
├── .nvmrc                       # Node 22 (for tooling — runtime is V8 isolates)
├── .gitignore                   # node_modules, .wrangler/, dist/, .dev.vars
├── .editorconfig                # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml           # weekly npm updates
    └── workflows/ci.yml         # install + rescript build + vitest
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, license holder, and the `name` field interpolated into `wrangler.jsonc` (the deployed Worker name) |
| **Package manager** | npm / pnpm / yarn / bun. Affects `packageManager` field, README install/run commands, and CI cache key |
| **Validation library** | `zod` ↔ `sury`. Picks `src/Validation.res` and adds the matching dependency. Both expose `parseGreetingPayload` so route handlers don't branch |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs the compiled `.res.mjs` imports | `TemplateVersions.RESCRIPT_RUNTIME` |
| `hono` | HTTP router with native Workers fetch handler | `TemplateVersions.HONO` |
| `zod` *or* `sury` | Request body validation (chosen in the wizard) | `TemplateVersions.ZOD` / `SURY` |
| `wrangler` *(dev)* | Cloudflare CLI: dev server, KV, deploy | `TemplateVersions.WRANGLER` |
| `vitest` *(dev)* | Smoke test runner | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |

`@hono/node-server` is **not** included — the Workers runtime takes Hono's native `fetch` export directly (see `Server.res`).

## Key Files

### `wrangler.jsonc`

The Workers config. Comments are preserved (hence `.jsonc`), so the KV setup workflow lives next to the binding it documents:

```jsonc
{
  "name": "my-project",
  "main": "src/Server.res.mjs",
  "compatibility_date": "2024-01-01",
  "kv_namespaces": [
    {
      "binding": "GREETINGS",
      "id": "REPLACE_WITH_PRODUCTION_KV_NAMESPACE_ID",
      "preview_id": "REPLACE_WITH_PREVIEW_KV_NAMESPACE_ID"
    }
  ]
}
```

`id` is what `wrangler deploy` uses (production); `preview_id` is what `wrangler dev` uses (local). Keeping them split means local experimentation never mutates production data — see the README's *KV Setup* section for the full create-and-wire workflow.

### `src/Server.res`

The Hono app. `env` is narrowed to the bindings this Worker actually uses (`GREETINGS`), and the file ends with `%%raw("export default app")` because the Workers runtime invokes the default export's `fetch(request, env, ctx)`:

```rescript
type env = {"GREETINGS": Kv.namespace}

let app = Hono.createApp()

app->Hono.get("/", ctx => ctx->Hono.text("Workers + Hono + ReScript"))

app->Hono.post("/greetings", async ctx => {
  let env: env = %raw("ctx.env")
  let raw = await ctx->Hono.req->Hono.jsonBody
  switch Validation.parseGreetingPayload(raw) {
  | Error(msg) => ctx->Hono.status(400)->Hono.json({"error": msg})
  | Ok(payload) =>
    await env["GREETINGS"]->Kv.put(payload.name, Date.now()->Float.toString)
    ctx->Hono.status(201)->Hono.json({"ok": true, "name": payload.name})
  }
})

app->Hono.get("/greetings", async ctx => {
  let env: env = %raw("ctx.env")
  let result = await env["GREETINGS"]->Kv.list
  ctx->Hono.json({"names": result.keys->Array.map(k => k["name"])})
})

%%raw("export default app")
```

### `src/Kv.res`

Bindings over the Workers KV API. The shipped subset (`get` / `put` / `list`) is enough to run the example; the file is a drop-in seed for further bindings — Durable Objects, R2, queues, D1 — when you need them:

```rescript
type namespace
type listResult = {keys: array<{"name": string}>}

@send external get: (namespace, string) => promise<Nullable.t<string>> = "get"
@send external put: (namespace, string, string) => promise<unit> = "put"
@send external list: namespace => promise<listResult> = "list"
```

The `namespace` type is opaque on purpose — Cloudflare injects the real KV instance at runtime via `env.GREETINGS`, and we read it through `%raw("ctx.env")` in the route handlers.

### `src/Validation.res`

Both variants expose `parseGreetingPayload: JSON.t => result<greetingPayload, string>`. The route handler returns 400 on `Error` and proceeds on `Ok` — no exception propagation, no extra middleware. Choose **zod** if you want the broader ecosystem (zod-to-openapi, hono/zod-openapi, drizzle-zod), or **sury** if you prefer ReScript-native ergonomics and a smaller runtime footprint.

### `src/__tests__/Server.test.mjs`

A one-line smoke test that asserts the module loads:

```js
await expect(import("../Server.res.mjs")).resolves.toBeDefined();
```

It exists so CI catches obvious link-time regressions (missing imports, broken `%raw` blocks) without requiring you to wire up Miniflare or `unstable_dev`. Add KV-bound integration tests once your routes have logic worth covering — `wrangler` ships an `unstable_dev` API that boots a real Workers runtime in-process for vitest.

### Endpoints

| Endpoint | Method | Description |
| --- | --- | --- |
| `/` | GET | Health check (`text/plain`) |
| `/greetings` | POST | Validate `{ name }`, store in KV, return 201 |
| `/greetings` | GET | List stored names |

### Request and response shapes

```http
POST /greetings
Content-Type: application/json

{ "name": "Ada" }
```

```http
HTTP/1.1 201 Created
Content-Type: application/json

{ "ok": true, "name": "Ada" }
```

```http
GET /greetings
```

```http
HTTP/1.1 200 OK
Content-Type: application/json

{ "names": ["Ada", "Grace", "Linus"] }
```

A missing or invalid body produces a 400:

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{ "error": "Validation failed" }
```

## npm Scripts

| Script | Description |
| --- | --- |
| `dev` | `wrangler dev` — local Workers runtime against the preview KV namespace |
| `deploy` | `wrangler deploy` — push to Cloudflare |
| `test` | `vitest run` — execute the smoke suite |
| `test:coverage` | `vitest run --coverage` — same, with v8 coverage report |
| `res:build` | `rescript` — one-shot compile |
| `res:dev` | `rescript -w` — recompile on save |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` |

## KV Setup Walkthrough

The shipped `wrangler.jsonc` has placeholder IDs that wrangler will refuse to use until you replace them. The README's *KV Setup* section walks through the workflow; the gist is:

```bash
# Production namespace (used by wrangler deploy)
npx wrangler kv namespace create GREETINGS

# Preview namespace (used by wrangler dev)
npx wrangler kv namespace create GREETINGS --preview
```

Each command prints a JSON snippet with an `id`. Copy them into `wrangler.jsonc` under `kv_namespaces[0].id` and `.preview_id` respectively. Verify the wiring with:

```bash
npx wrangler kv namespace list
```

Local reads and writes go through the **preview** namespace by default:

```bash
# Writes against preview_id (what wrangler dev sees)
npx wrangler kv key put --binding=GREETINGS hello world

# Writes against the production id explicitly
npx wrangler kv key put --binding=GREETINGS --remote hello world
```

If you omit `preview_id`, wrangler falls back to a one-off in-memory store wiped between sessions. That is fine for throwaway demos but brittle for anything you want to inspect later.

## Extending the Bindings

The shipped `Kv.res` covers the routes in the example, not the full Workers KV surface. When you need more, add the binding directly — most KV operations are a single `@send` line:

```rescript
// Conditional writes (skip if the key already exists):
@send external putWithMeta:
  (namespace, string, string, {"metadata": 'meta}) => promise<unit> = "put"

// Bulk delete:
@send external deleteKey: (namespace, string) => promise<unit> = "delete"

// Pagination — `list` accepts `{prefix, limit, cursor}`:
type listOpts = {prefix?: string, limit?: int, cursor?: string}
@send external listWithOpts:
  (namespace, listOpts) => promise<listResult> = "list"
```

Other Workers primitives follow the same shape. Each gets its own thin module:

| Binding | Module | Wrangler config key | Typical methods |
| --- | --- | --- | --- |
| KV | `Kv.res` (shipped) | `kv_namespaces` | `get` / `put` / `list` / `delete` |
| R2 | add `R2.res` | `r2_buckets` | `put` / `get` / `list` / `head` |
| Durable Objects | add `Do.res` | `durable_objects.bindings` | `idFromName` / `get` / `fetch` |
| D1 | add `D1.res` | `d1_databases` | `prepare` / `bind` / `all` / `first` |
| Queues | add `Queue.res` | `queues.producers` / `consumers` | `send` / `sendBatch` |

For each, declare the binding in `wrangler.jsonc`, add the field to `type env` in `Server.res`, and read it via `%raw("ctx.env")` in the route handler. The pattern is uniform so muscle memory transfers across services.

## Try It

Once `wrangler dev` is running and the KV IDs are wired (see *KV Setup Walkthrough* above), exercise the example end-to-end:

```bash
# Store a greeting
curl -X POST http://localhost:8787/greetings \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ada"}'
# => {"ok":true,"name":"Ada"}

# List the names you have stored
curl http://localhost:8787/greetings
# => {"names":["Ada"]}

# Send something the validator rejects
curl -X POST http://localhost:8787/greetings \
  -H 'Content-Type: application/json' \
  -d '{}'
# => 400 {"error":"Validation failed"}
```

When you're ready to push to Cloudflare, `npx wrangler login` once and then `pnpm deploy` (or your PM equivalent). The first deploy provisions the route at `<worker-name>.<account>.workers.dev`; subsequent deploys are atomic and roll back automatically if the upload fails health-check.

## Day-Two Recipes

- {doc}`../recipes/add-hono-endpoint` — add another route alongside `/greetings`
- {doc}`../recipes/convert-from-typescript` — port an existing Worker to ReScript module-by-module
- {doc}`../recipes/optimize-imports` — cut bundle size (every byte counts at the edge)

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- **No Node runtime.** Cloudflare Workers run on V8 isolates, not Node. That's why the template ships only `hono` (no `@hono/node-server`) and the entry file ends with `%%raw("export default app")` — Workers invoke the default export's `fetch(request, env, ctx)`. `.nvmrc` still says `22` because **tooling** (wrangler, esbuild under the hood, vitest) runs on Node locally.
- **`wrangler.jsonc`, not `wrangler.toml`.** Cloudflare supports both. We chose JSONC because it lets us keep the KV setup walkthrough next to the binding it documents — no separate file to skim.
- **Two KV namespaces, on purpose.** `id` (production) and `preview_id` (local) are kept distinct so `wrangler dev` never writes to production. If you omit `preview_id`, wrangler falls back to a one-off in-memory store that is wiped between sessions — fine for throwaway demos, brittle for anything you want to inspect later. Run `npx wrangler kv namespace create GREETINGS` and `... --preview` once and paste the IDs in.
- **Bindings are typed by hand, not generated.** The template does not depend on `@cloudflare/workers-types`; the `env` shape lives in `Server.res` as `type env = {"GREETINGS": Kv.namespace}`. If you add bindings (Durable Objects, R2, queues), add a field to that type and a binding entry in `wrangler.jsonc`.
- **`.dev.vars` is gitignored.** Use it for local-only secrets that wrangler reads automatically. For production, run `npx wrangler secret put NAME` — secrets never live in `wrangler.jsonc`.
- **`.wrangler/` and `dist/` are gitignored.** wrangler caches local state in the former; the latter is reserved for any bundling step you add later.
- **Smoke test only verifies module load.** `src/__tests__/Server.test.mjs` does `await import("../Server.res.mjs")` and asserts it resolves. Add KV-bound integration tests with `unstable_dev` from `wrangler` once your routes have logic worth covering.
- **Deploy needs auth.** `npx wrangler login` once per machine, then `pnpm deploy` (or your PM equivalent) pushes to your account. The README's *Deploy* section interpolates the right command for the package manager you chose.
- **No `compatibility_flags`, conservative `compatibility_date`.** The shipped `wrangler.jsonc` uses `2024-01-01` and no flags. Bump the date when you need newer Workers runtime behaviour (Node.js compat shims, `nodejs_compat`, `streams_enable_constructors`, etc) — the field is opt-in by design so older deployments don't break when Cloudflare ships a runtime update.
- **Bindings beyond KV.** Add Durable Objects, R2 buckets, queues, D1 databases, or service bindings by editing `wrangler.jsonc` and extending `type env` in `Server.res`. The pattern stays the same: declare the binding in JSONC, name its type in the `env` record, read it via `%raw("ctx.env")`. Each new binding gets its own thin `.res` module mirroring `Kv.res`.
- **Free-plan limits matter.** A Worker has a 50 ms CPU time budget per request on the free plan (10 ms before May 2024) — validation, KV reads, and JSON serialisation all count. The shipped routes stay well within that envelope, but the moment you reach for heavy crypto or large JSON, run `wrangler dev --inspect` and profile.
