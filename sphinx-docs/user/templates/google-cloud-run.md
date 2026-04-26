---
myst:
  html_meta:
    "keywords": "google cloud run, gcp, hono, rescript, docker, multi-stage, dockerfile, cloud sql, container, validation, zod, sury"
---

# Google Cloud Run

{bdg-info}`GCP` {bdg-info}`Hono` {bdg-success}`Validation` {bdg-warning}`Container`

A container-first ReScript service built around [Hono](https://hono.dev) and ready to deploy to [Google Cloud Run](https://cloud.google.com/run). Unlike the Hono (Node.js) starter, this template ships an opinionated multi-stage `Dockerfile`, a `.dockerignore`, an `.env.example`, and README sections that walk you through `gcloud run deploy` and a Cloud SQL recipe.

The runtime image is intentionally small: a builder stage installs every dependency and compiles ReScript, then the runtime stage reinstalls **production-only** dependencies, copies the generated `.res.mjs` files, drops to a non-root user, and exposes port 8080. The result is a deterministic image that runs the same way locally, inside a CI registry, and on Cloud Run.

## What You Get

```
my-service/
├── rescript.json
├── package.json                    # ESM, "type":"module", engines.node = >=24
├── Dockerfile                      # multi-stage: builder → runtime
├── .dockerignore                   # excludes node_modules, lib/, coverage, .env
├── .env.example                    # PORT=8080 (Cloud Run sets it for you)
├── src/
│   ├── ServerMain.res              # entry — calls Server.start()
│   ├── Server.res                  # Hono app: GET /, POST /echo, reads PORT
│   ├── Hono.res                    # bindings shared with other Hono templates
│   ├── HonoNodeServer.res          # @hono/node-server bindings
│   ├── Validation.res              # zod or sury — selected in the wizard
│   └── __tests__/Server.test.mjs   # vitest smoke test that imports Server.res.mjs
├── README.md                       # script docs + API + Environment + Deploy + Cloud SQL
├── LICENSE                         # MIT, holder = project name
├── .nvmrc                          # Node 24
├── .gitignore                      # node_modules + dist/ + .env + ReScript artifacts
├── .editorconfig                   # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml              # weekly npm updates
    └── workflows/ci.yml            # install + rescript build + vitest
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, the LICENSE holder, and substitutes into the README's `gcloud run deploy <projectName>` examples |
| **Package manager** | npm / pnpm / yarn / bun. Drives the `packageManager` Corepack field, the README install/run commands, the CI cache key, **and the Dockerfile's install commands and base image** |
| **Validation library** | `zod` ↔ `sury`. Chooses which `src/Validation.res` variant ships and adds the matching runtime dependency |

The Dockerfile branch is the meaningful difference from other Hono templates. Selecting Bun swaps the base image to `oven/bun:1-slim`, replaces every `npm install` / `pnpm install` line with `bun install`, and changes the runtime user from `node` to `bun` (both uid 1000). Pinning the Node major to `nodeMajor` (defaults to `TemplateVersions.NODE_MAJOR`) means `gcloud run deploy` and your local `node --watch` always see the same runtime — no `package.json` engines-vs-image drift.

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs the compiled `.res.mjs` imports | `TemplateVersions.RESCRIPT_RUNTIME` |
| `hono` | HTTP framework — small, fast, web-standard `Request`/`Response` | `TemplateVersions.HONO` |
| `@hono/node-server` | Adapter that mounts a Hono app on Node's HTTP server | `TemplateVersions.HONO_NODE_SERVER` |
| `zod` *or* `sury` | Validation backend (chosen in the wizard) | `TemplateVersions.ZOD` / `TemplateVersions.SURY` |
| `vitest` *(dev)* | Smoke test runner | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |

## Key Files

### `src/Server.res`

The Hono app. It reads `PORT` from `process.env` (Cloud Run sets it automatically) and falls back to `8080` for local development:

```rescript
@val external processEnv: Dict.t<string> = "process.env"

let port =
  processEnv
  ->Dict.get("PORT")
  ->Option.flatMap(Int.fromString(_))
  ->Option.getOr(8080)

let app = Hono.createApp()

app->Hono.get("/", ctx => ctx->Hono.text("Cloud Run + Hono + ReScript"))

app->Hono.post("/echo", async ctx => {
  let raw = await ctx->Hono.req->Hono.jsonBody
  switch Validation.parseEchoPayload(raw) {
  | Error(msg) => ctx->Hono.status(400)->Hono.json({"error": msg})
  | Ok(payload) =>
    ctx->Hono.json({"echo": payload.message, "receivedAt": Date.now()->Float.toString})
  }
})

let start = () => {
  HonoNodeServer.serve({fetch: app->HonoNodeServer.honoFetch, port})
  Console.log(`Server running on http://localhost:${port->Int.toString}`)
}
```

The CORS hook is left commented near the top — uncomment it when the service is called from a browser on another origin and adjust the allowlist before deploying.

### `src/ServerMain.res`

A single line: `Server.start()`. The split exists so vitest can `import("../Server.res.mjs")` to verify the app compiles without binding to a port.

### `src/Validation.res`

`parseEchoPayload: JSON.t => result<echoPayload, string>` — the runtime contract for the `/echo` route's body. The signature is identical between the zod and sury variants so callers do not branch on which library shipped.

### `Dockerfile`

A two-stage image generated from the wizard inputs. The exact contents depend on the selected package manager and `nodeMajor`, but the shape is:

```dockerfile
# syntax=docker/dockerfile:1.7

# --- Builder stage: install all deps + compile ReScript ---
FROM node:22-slim AS builder
WORKDIR /app
COPY package*.json ./
RUN <pm-install>          # e.g. corepack enable && pnpm install --frozen-lockfile=false
COPY . .
RUN <pm-exec> rescript    # e.g. pnpm exec rescript / npx rescript / bunx rescript

# --- Runtime stage: prod deps + compiled output only ---
FROM node:22-slim AS runtime
ENV NODE_ENV=production
WORKDIR /app
COPY package*.json ./
RUN <pm-install-prod>     # e.g. pnpm install --prod --frozen-lockfile=false
COPY --from=builder /app/src ./src
USER node
EXPOSE 8080
CMD ["node", "src/ServerMain.res.mjs"]
```

Three properties are intentional and load-bearing:

- **`node_modules` is never copied across stages** — it is reinstalled with `--prod` flags so the runtime layer is reproducible from the lockfile.
- **The runtime stage runs as a non-root user** (`node` for Node images, `bun` for `oven/bun`). Cloud Run's hardened policies expect this.
- **The base image's Node major matches `package.json`'s `engines.node` and `.nvmrc`.** Bumping `nodeMajor` updates all three in lockstep.

### `.dockerignore`

Keeps the build context lean by excluding VCS metadata, IDE state, package-manager debug logs, the `lib/` ReScript build artifact, `coverage/`, and any `.env*` files. The `!.env.example` line preserves the sample env file so the README's deploy instructions still find it.

## npm Scripts

| Script | Description |
| --- | --- |
| `start` | `node src/ServerMain.res.mjs` — run the compiled server |
| `dev` | `node --watch src/ServerMain.res.mjs` — restart on rebuilt output |
| `test` | `vitest run` — execute the smoke suite |
| `test:coverage` | `vitest run --coverage` — same, with v8 coverage |
| `res:build` | `rescript` — one-shot compile |
| `res:dev` | `rescript -w` — recompile on save |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` |

For local development, run `res:dev` and `dev` in two terminals (or chain them through your own `concurrently` invocation if you prefer a single-window workflow).

## Deploying to Cloud Run

The README ships a copy-paste deploy block that uses Cloud Build to push the image and `gcloud run deploy` to roll it out:

```bash
gcloud builds submit --tag gcr.io/PROJECT-ID/<projectName>
gcloud run deploy <projectName> \
  --image gcr.io/PROJECT-ID/<projectName> \
  --port 8080 \
  --allow-unauthenticated
```

Cloud Run injects `PORT` at runtime (the value is not always 8080), which is why `Server.res` reads it from `process.env`. The `--port 8080` flag tells Cloud Run which port the container listens on, not which port to publish — they are independent settings.

The README also covers two operational follow-ups: pinning a deployed revision's image digest for immutable rollouts and tailing logs with `gcloud run services logs tail`.

## Cloud SQL recipe

The bundled README includes a short recipe for connecting to a Cloud SQL Postgres instance via `@google-cloud/cloud-sql-connector` (or the Cloud SQL Auth Proxy with a vanilla `pg` driver). The pattern in summary:

```bash
pnpm add pg @google-cloud/cloud-sql-connector
```

```rescript
@module("pg") @new external makePool: 'opts => 'pool = "Pool"
@send external queryAsync: ('pool, string, array<'a>) => promise<'rows> = "query"
```

Pass `DATABASE_URL` (or the Cloud SQL connection name plus credentials) as an environment variable via `--set-env-vars` on `gcloud run deploy`. For Drizzle ORM on top of Postgres, see the {doc}`../recipes/setup-drizzle` recipe — the same shape applies once you swap the libsql client for `pg`.

## Day-Two Recipes

- {doc}`../recipes/add-hono-endpoint` — add a typed POST/GET route alongside `/echo`
- {doc}`../recipes/setup-drizzle` — add a Drizzle ORM schema (translate the libsql examples to `pg` for Cloud SQL)
- {doc}`../recipes/add-openapi-docs` — generate Scalar API docs from `@hono/zod-openapi` schemas

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- **`PORT` is not configurable in the manifest.** Cloud Run sets it on every invocation; `Server.res` honours it. Locally, `PORT=3000 pnpm dev` works because the same env-reading logic falls back to user input before defaulting to 8080.
- **The Dockerfile re-runs `bun install --production` even when there is no lockfile.** Bun creates `bun.lock` on first install. CI environments without a committed lockfile will still produce a working image, but production deployments should always commit the lockfile to guarantee reproducibility.
- **`COPY --from=builder /app/src ./src`** is the only artifact carried across stages. If you generate additional output (`dist/`, `public/`), add explicit `COPY --from=builder` lines for each.
- **The `node` engine is pinned to `>=24`** and `.nvmrc` says `24`. The Dockerfile's base image follows `nodeMajor` (default `TemplateVersions.NODE_MAJOR`); changing one in the wizard updates all of them.
- **The vitest suite is intentionally tiny** — it asserts that `import("../Server.res.mjs")` resolves. Add a `app.request("/")` style integration test (see the Monorepo and Full-Stack templates for examples) before relying on tests for regression coverage.
- **The `.gitignore` excludes `.env`** but keeps `.env.example`. Cloud Run secrets should travel through `--set-env-vars` or Secret Manager, never through a committed `.env`.
- **CORS is off by default.** Uncomment the `Hono.cors(...)` block at the top of `Server.res` and pin an origin allowlist before exposing the service to a browser-side client on another origin.
