---
myst:
  html_meta:
    "keywords": "aws lambda, hono, esbuild, api gateway, dynamodb, serverless, rescript template, validation, zod, sury"
---

# AWS Lambda

{bdg-info}`AWS Lambda` {bdg-primary}`Hono` {bdg-success}`Validation` {bdg-warning}`esbuild`

An **AWS Lambda** function powered by **Hono** and bundled with **esbuild** into a single deploy artifact. The shipped app demonstrates the two patterns you reach for first — a JSON-body POST and a path-parameter GET — both wired through the API Gateway-friendly `hono/aws-lambda` adapter. Pair the function with an HTTP API trigger and you have a working endpoint without leaving the wizard.

The build pipeline is intentionally one-shot: `pnpm build` compiles ReScript and then esbuilds `src/Server.res.mjs` into `dist/index.mjs`, ESM, Node target. Zip and upload, or wire it into SAM / CDK / Terraform — the artifact is the same.

## What You Get

```
my-project/
├── rescript.json
├── package.json
├── src/
│   ├── Server.res               # Hono app, exposes `let handler = HonoLambda.handle(app)`
│   ├── HonoLambda.res           # bindings over hono/aws-lambda's `handle`
│   ├── Hono.res                 # Hono bindings
│   ├── Validation.res           # zod or sury — selected in the wizard
│   └── __tests__/Server.test.mjs # vitest smoke import
├── README.md                    # API / Deploy / Bundling Strategy / DynamoDB Recipe
├── LICENSE                      # MIT, holder = project name
├── .nvmrc                       # Node 22 (matches the Lambda runtime)
├── .gitignore                   # node_modules, dist/, *.zip, .aws-sam/
├── .editorconfig                # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml           # weekly npm updates
    └── workflows/ci.yml         # install + rescript build + bundle + vitest
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, license holder, and the `--function-name` interpolated into the README's deploy snippet (and the `--layer-name` in the bundling section) |
| **Package manager** | npm / pnpm / yarn / bun. Affects `packageManager` field, README install/run commands, and CI cache key |
| **Validation library** | `zod` ↔ `sury`. Picks `src/Validation.res` and adds the matching dependency. Both expose `parseCreateOrderPayload` so route handlers don't branch |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs the compiled `.res.mjs` imports | `TemplateVersions.RESCRIPT_RUNTIME` |
| `hono` | HTTP router with `hono/aws-lambda` adapter | `TemplateVersions.HONO` |
| `zod` *or* `sury` | Request body validation (chosen in the wizard) | `TemplateVersions.ZOD` / `SURY` |
| `esbuild` *(dev)* | Bundles `src/Server.res.mjs` → `dist/index.mjs` | `TemplateVersions.ESBUILD` |
| `vitest` *(dev)* | Smoke test runner | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |

The template does not pre-install `@types/aws-lambda` or `aws-lambda-ric`. Hono's adapter (`hono/aws-lambda`) returns a function whose signature matches API Gateway's expected handler — no separate handler types are needed for the shipped routes. Add `@types/aws-lambda` later if you write hand-rolled handlers around `APIGatewayProxyEventV2` / `Context`.

## Key Files

### `src/Server.res`

The Hono app, plus the named `handler` export that AWS Lambda looks up at invocation time. Top-level `let` bindings become named ESM exports automatically, so `let handler = HonoLambda.handle(app)` is the ReScript equivalent of `export const handler = ...`:

```rescript
type createOrderResponse = {orderId: string, productId: string, quantity: int}

let app = Hono.createApp()

app->Hono.get("/", ctx => ctx->Hono.text("Lambda + Hono + ReScript"))

app->Hono.get("/orders/:id", ctx => {
  let id = ctx->Hono.req->Hono.paramAt("id")
  ctx->Hono.json({"orderId": id, "status": "pending"})
})

app->Hono.post("/orders", async ctx => {
  let raw = await ctx->Hono.req->Hono.jsonBody
  switch Validation.parseCreateOrderPayload(raw) {
  | Error(msg) => ctx->Hono.status(400)->Hono.json({"error": msg})
  | Ok(payload) =>
    let response: createOrderResponse = {
      orderId: "ord_" ++ Date.now()->Float.toString,
      productId: payload.productId,
      quantity: payload.quantity,
    }
    ctx->Hono.status(201)->Hono.json(response)
  }
})

let handler = HonoLambda.handle(app)
```

Doing it via a real `let` binding (rather than `%%raw`) ensures the ReScript compiler emits the `HonoLambda` import — esbuild then bundles the adapter into `dist/index.mjs`.

### `src/HonoLambda.res`

A two-line binding over the Hono adapter — small enough to read at a glance, but it captures the only piece of API Gateway knowledge the template needs:

```rescript
type lambdaEvent
type lambdaResult
type handler = lambdaEvent => promise<lambdaResult>

@module("hono/aws-lambda") external handle: Hono.app => handler = "handle"
```

The adapter accepts both REST API (v1) and HTTP API (v2) events from API Gateway and translates them into `fetch`-compatible requests Hono can route.

### `src/Validation.res`

Both variants expose `parseCreateOrderPayload: JSON.t => result<createOrderPayload, string>`. The route handler returns 400 on `Error` and proceeds on `Ok` — the same pattern used by every server template the wizard ships, so muscle memory transfers. Choose **zod** if you want the broader ecosystem (zod-to-openapi, hono/zod-openapi); choose **sury** if you prefer ReScript-native ergonomics and a smaller runtime footprint.

### `src/__tests__/Server.test.mjs`

A one-line smoke test that asserts the module loads:

```js
await expect(import("../Server.res.mjs")).resolves.toBeDefined();
```

It runs in CI alongside the bundle step so a broken handler fails fast. For real Lambda integration tests, drive the bundled `dist/index.mjs` with synthetic API Gateway events using `aws-lambda-mock-context` or `sam local invoke`.

### Endpoints

| Endpoint | Method | Description |
| --- | --- | --- |
| `/` | GET | Health check (`text/plain`) |
| `/orders/:id` | GET | Look up an order by ID (path parameter) |
| `/orders` | POST | Create an order from `{ productId, quantity }` (JSON body) |

### Request and response shapes

```http
POST /orders
Content-Type: application/json

{ "productId": "sku-42", "quantity": 3 }
```

```http
HTTP/1.1 201 Created
Content-Type: application/json

{ "orderId": "ord_1714142512345", "productId": "sku-42", "quantity": 3 }
```

```http
GET /orders/ord_1714142512345
```

```http
HTTP/1.1 200 OK
Content-Type: application/json

{ "orderId": "ord_1714142512345", "status": "pending" }
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
| `build` | `rescript && pnpm bundle` — compile ReScript then bundle |
| `bundle` | `esbuild src/Server.res.mjs --bundle --platform=node --outfile=dist/index.mjs --format=esm` |
| `test` | `vitest run` — execute the smoke suite |
| `test:coverage` | `vitest run --coverage` — same, with v8 coverage report |
| `res:build` | `rescript` — one-shot compile |
| `res:dev` | `rescript -w` — recompile on save |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` |

The `build` script chains the two steps with `&&` (the package-manager prefix is interpolated from your wizard choice). `dist/index.mjs` is the artifact you upload.

## Deploy Walkthrough

### Build and upload

```bash
pnpm build                        # rescript && esbuild → dist/index.mjs
cd dist && zip lambda.zip index.mjs
aws lambda update-function-code \
  --function-name my-project \
  --zip-file fileb://lambda.zip
```

Set the function's *Handler* to `index.handler` and *Runtime* to `Node.js 22`. Use **API Gateway HTTP API** as the trigger unless you need a feature only REST API offers.

### Bundle size escape hatches

The default bundle inlines every dependency for low cold-start latency. When the artifact approaches the **50 MB direct-upload limit** (or 250 MB unzipped), reach for one of the following:

#### Mark Lambda-provided deps as external

The AWS SDK v3 (`@aws-sdk/*`) is preinstalled on the Node.js Lambda runtime. Excluding it saves several MB per command:

```bash
esbuild src/Server.res.mjs --bundle --platform=node \
  --outfile=dist/index.mjs --format=esm \
  --external:@aws-sdk/* --external:aws-sdk
```

Anything you mark `--external` must already exist at runtime — either because Lambda ships it, or because you publish it via a Lambda Layer.

#### Lambda Layers

For large native dependencies (Sharp, Prisma engines, custom binaries), publish them once as a Layer and exclude them from the bundle:

```bash
aws lambda publish-layer-version \
  --layer-name my-project-deps \
  --compatible-runtimes nodejs22.x \
  --zip-file fileb://layer.zip

aws lambda update-function-configuration \
  --function-name my-project \
  --layers arn:aws:lambda:<region>:<account>:layer:my-project-deps:1
```

Layers are cached per execution environment, so the cold-start cost is paid once per container instead of every invocation.

#### Tree-shaking and source maps

Add `--tree-shaking=true --minify --sourcemap` for production builds. Source maps stay outside the deployment artifact (`dist/index.mjs.map`) — you can upload them to your APM (Datadog, Sentry, CloudWatch RUM) for symbolicated stack traces without inflating the Lambda zip.

## DynamoDB Recipe (Quick Sketch)

The shipped README documents a full DynamoDB integration; the short version:

```bash
pnpm add @aws-sdk/client-dynamodb @aws-sdk/lib-dynamodb
```

```rescript
type docClient
@module("@aws-sdk/lib-dynamodb") @new
external makeClient: 'opts => docClient = "DynamoDBDocumentClient"
@send external send: (docClient, 'command) => promise<'result> = "send"
```

Grant the Lambda IAM role `dynamodb:PutItem` / `dynamodb:GetItem` on the target table. If you stay on the Node runtime that preinstalls `@aws-sdk/*`, mark these as `--external` to keep the artifact slim.

## Try It Locally

The shipped routes are testable without an AWS account by booting Hono on Node and pretending it's a Lambda. Add a one-off entry file:

```rescript
// src/Local.res
HonoNodeServer.serve({fetch: Server.app->HonoNodeServer.honoFetch, port: 3000})
```

Run it with `node src/Local.res.mjs` and curl against `http://localhost:3000`:

```bash
curl -X POST http://localhost:3000/orders \
  -H 'Content-Type: application/json' \
  -d '{"productId":"sku-42","quantity":3}'
# => 201 {"orderId":"ord_…","productId":"sku-42","quantity":3}

curl http://localhost:3000/orders/ord_42
# => 200 {"orderId":"ord_42","status":"pending"}
```

The Hono adapter you ship to Lambda (`hono/aws-lambda`) and the Node server you use locally (`@hono/node-server`) both wrap the same `app` instance, so behaviour is identical bar the runtime adapter at the edges. This is the cheapest way to iterate on routes before paying the deploy round-trip.

For full Lambda fidelity, use AWS SAM:

```bash
sam local invoke MyFunction -e events/post-order.json
```

`sam local invoke` shells the bundled `dist/index.mjs` inside a container that matches the production Lambda runtime, so cold-start behaviour, environment variables, and IAM masquerading all match what the cloud will do.

## Day-Two Recipes

- {doc}`../recipes/add-hono-endpoint` — add another route alongside `/orders`
- {doc}`../recipes/convert-from-typescript` — port an existing Lambda to ReScript module-by-module
- {doc}`../recipes/optimize-imports` — keep the bundle slim (cold-start latency matters)

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- **API Gateway-friendly out of the box.** `hono/aws-lambda` accepts both REST API (v1) and HTTP API (v2) event shapes, so wiring the function to either trigger works without code changes. HTTP API is cheaper and faster — pick that one unless you need a feature only REST API offers (mTLS, request validation at the gateway, etc).
- **Single-artifact bundle.** `pnpm build` produces `dist/index.mjs` — every dependency inlined, ESM, Node target. Zip it (`cd dist && zip lambda.zip index.mjs`) and `aws lambda update-function-code` it. The function's *Handler* setting is `index.handler`; the *Runtime* is Node.js 22.
- **Watch the 50 MB upload limit.** The default bundle inlines every dependency for low cold-start latency, but the artifact grows with each new dep. The README's *Bundling Strategy* section walks through three escape hatches: marking Lambda-provided deps as `--external` (the AWS SDK v3 is preinstalled on the Node runtime — excluding it saves several MB), publishing big native deps as Lambda Layers (Sharp, Prisma engines), and turning on tree-shaking + minify + sourcemaps for production.
- **DynamoDB recipe is in the README.** The shipped *DynamoDB Recipe* section shows the minimal ReScript bindings over `@aws-sdk/lib-dynamodb` (`DynamoDBDocumentClient`, `send`) and reminds you to grant the Lambda IAM role `dynamodb:PutItem` / `dynamodb:GetItem`. Install with `pnpm add @aws-sdk/client-dynamodb @aws-sdk/lib-dynamodb` — and do **not** `--external` those if you are on a runtime where they are not preinstalled.
- **`handler` is a real `let` binding for a reason.** ReScript's compiler only emits the `HonoLambda` import when the module is actually referenced. Defining `let handler = HonoLambda.handle(app)` keeps the import alive; switching to `%%raw("export const handler = ...")` would silently drop it from the bundle.
- **Smoke test only verifies module load.** `src/__tests__/Server.test.mjs` does `await import("../Server.res.mjs")` and asserts it resolves. For real Lambda integration tests, drive the bundled `dist/index.mjs` with synthetic API Gateway events using `aws-lambda-mock-context` or a SAM `sam local invoke`.
- **`dist/`, `*.zip`, and `.aws-sam/` are gitignored.** Build artifacts and SAM CLI working state never go in the repo.
- **Node 22 is mandatory.** `engines.node` is `>=22`, `.nvmrc` says `22`, and the README's *Deploy* section pins the Lambda runtime to `Node.js 22`. The Layer publish snippet interpolates `nodejs22.x` from the same source — bumping `TemplateVersions.NODE_MAJOR` updates all three locations atomically.
- **No reserved concurrency or provisioned concurrency configured.** The shipped template assumes on-demand. If your workload is latency-sensitive enough to need provisioned concurrency, configure it on the Lambda console (or via SAM/CDK/Terraform) — it doesn't affect the bundle.
- **Cold start matters.** ReScript's compiled output and Hono are both small (`hono` is ~14 KB minzipped), so a cold start is dominated by V8 warm-up and any AWS SDK clients you instantiate at module load. Move SDK client construction outside the handler closure to keep it warm across invocations within the same container, but keep request-scoped state inside the handler.
- **Bundling is intentional, not optional.** `node src/Server.res.mjs` directly will not work as a Lambda — Lambda invokes the named ESM export, and ReScript's compiler emits relative imports that break once `index.mjs` ships in isolation. Always upload the esbuild output, never raw `.res.mjs` files.
- **CI runs both build and test.** The shipped `.github/workflows/ci.yml` invokes `pnpm build` (which compiles ReScript and bundles esbuild) followed by `pnpm test`. A bundle failure (e.g. forgotten `@module(...)` binding, missing dependency) blocks the merge — exactly when you want to know.
