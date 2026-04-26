---
myst:
  html_meta:
    "keywords": "res-x, rescript-x, htmx, bun, vite, server-driven, jsx server-side, hjsx, rescript-bun, validation, zod, sury"
---

# res-x (HTMX on Bun)

{bdg-info}`Bun` {bdg-info}`HTMX` {bdg-info}`Vite` {bdg-success}`Validation` {bdg-warning}`Bun-only`

A server-driven web application built with [res-x](https://www.npmjs.com/package/rescript-x) (`rescript-x` on npm), running on [Bun](https://bun.sh) and bundled by Vite. JSX renders HTML on the server; HTMX drives client-side interactivity by swapping HTML fragments returned from typed endpoints. There is no React, no virtual DOM, and no client-side bundle of application logic — the browser just receives HTML and trades it back and forth with the server.

Pick this template when you want fast first-load, no client framework, and the simplest possible state model: every interaction is an HTTP request that returns a JSX fragment. The starter ships two end-to-end examples — a counter and a todo form — that demonstrate the patterns you reach for ninety percent of the time.

## What You Get

```
my-app/
├── rescript.json                       # jsx.module = Hjsx, opens ResX.Globals + RescriptBun.Globals
├── package.json                        # type:module, bun-driven scripts, vite devDep
├── vite.config.js                      # rescript-x/res-x-vite-plugin.mjs, dev port 9000
├── Dockerfile                          # multi-stage oven/bun image (deps → builder → runtime)
├── src/
│   ├── App.res                         # entry — Bun.serve + path-based routing + Layout fallback
│   ├── Handler.res                     # per-request context + ResX.Handlers.make bootstrap
│   ├── Layout.res                      # shared HTML shell, loads HTMX from CDN
│   ├── Counter.res                     # /counter/{increment,decrement} + outerHTML <span> swap
│   ├── TodoForm.res                    # /todos POST: validates input, re-renders list or form+error
│   ├── Validation.res                  # zod or sury — selected in the wizard
│   └── __tests__/App.test.mjs          # bun:test smoke test
├── README.md                           # Application + HTMX + Layout + Deploy + Persistence sections
├── LICENSE                             # MIT, holder = project name
├── .nvmrc                              # Node 22 (used by tooling that looks at .nvmrc)
├── .gitignore                          # adds dist/, build/, .env, .res-x-cache/
├── .editorconfig
└── .github/
    ├── dependabot.yml
    └── workflows/ci.yml                # auto-enables setup-bun (bun-version: latest)
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, the LICENSE holder, and the `name` field substituted into `rescript.json` |
| **Package manager** | npm / pnpm / yarn / bun. Affects only the `packageManager` Corepack field, the README install commands, and the CI cache key — **all run-time scripts are hardcoded to `bun`** regardless |
| **Validation library** | `zod` ↔ `sury`. Chooses which `src/Validation.res` variant ships and adds `sury` to `rescript.json`'s `dependencies` array (zod has no ReScript-side bs-deps) |

The package-manager choice is intentionally cosmetic. Bun is the recommended PM and is required at runtime; the other PMs are supported solely so the wizard does not refuse a project for the install step.

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs the compiled `.res.mjs` imports | `TemplateVersions.RESCRIPT_RUNTIME` |
| `rescript-x` | Server-side JSX framework with HTMX-first handler API | `TemplateVersions.RESCRIPT_X` |
| `rescript-bun` | ReScript bindings for Bun's runtime APIs (Bun.serve, FormData, etc.) | `TemplateVersions.RESCRIPT_BUN` |
| `zod` *or* `sury` | Validation backend (chosen in the wizard) | `TemplateVersions.ZOD` / `TemplateVersions.SURY` |
| `vite` *(dev)* | Bundler used for client-side assets when needed | `TemplateVersions.VITE` |
| `concurrently` *(dev)* | Pairs `rescript -w` with `bun --watch` for dev | `TemplateVersions.CONCURRENTLY` |

HTMX itself is loaded from a CDN at runtime (pinned to `TemplateVersions.HTMX_CDN` via `src/Layout.res`); it is not an npm dependency.

## Key Files

### `rescript.json`

`rescript-x` requires a specific config shape — `jsx.module = "Hjsx"` plus a set of `compiler-flags` that open `ResX.Globals` and the `RescriptBun.Globals` module. The wizard ships this verbatim because `ProjectFileBuilders.rescriptJson` does not produce that shape:

```json
{
  "name": "my-app",
  "sources": {"dir": "src", "subdirs": true},
  "package-specs": {"module": "esmodule", "in-source": true},
  "suffix": ".res.mjs",
  "jsx": {"module": "Hjsx", "version": 4},
  "dependencies": ["@rescript/core", "rescript-x", "rescript-bun"],
  "compiler-flags": [
    "-open RescriptCore",
    "-open RescriptBun",
    "-open RescriptBun.Globals",
    "-open ResX.Globals"
  ]
}
```

Selecting the sury variant appends `, "sury"` to the `dependencies` array; zod has no ReScript-side bindings to register.

### `src/App.res`

The entry point. `Bun.serve` mounts the res-x handler; the inner `render` function pattern-matches on the request `path` and returns JSX that res-x converts to an HTML `Response`. HTMX endpoints registered via `Handler.handler.hxPost` / `hxGet` are matched by res-x **before** this fallback renderer runs.

```rescript
let server = Bun.serve({
  port,
  development: ResX.BunUtils.isDev,
  fetch: async (request, _server) => {
    await Handler.handler.handleRequest({
      request,
      setupHeaders: () =>
        Headers.make(~init=FromArray([("Content-Type", "text/html")])),
      render: async ({path, requestController, headers: _}) => {
        switch path {
        | list{} =>
          <Layout title="res-x starter">
            <h1> {Hjsx.string("res-x starter")} </h1>
            <Counter />
            <TodoForm />
          </Layout>
        | _ =>
          requestController.setStatus(404)
          <Layout title="Not found">
            <h1> {Hjsx.string("404 — not found")} </h1>
          </Layout>
        }
      },
    })
  },
})
```

### `src/Handler.res`

The per-request context and handler bootstrap. Extend the `context` record with the values you want available via `useContext()` — a database connection, the authenticated user, request metadata. The default ships with a single optional `userId` for illustration.

### `src/Layout.res`

The shared HTML shell. Loads HTMX from `https://unpkg.com/htmx.org@<TemplateVersions.HTMX_CDN>` (pinned, not floating) so the page boots without bundling anything client-side. Swap the `<script src>` to `/htmx.min.js` once you drop a self-hosted copy into `public/`.

### `src/Counter.res`

The minimal HTMX pattern: server-side state in a `ref`, two `hx-post` endpoints, and an `outerHTML` swap on a single `<span>`:

```rescript
let count = ref(0)
let counterId = "counter-value"

@jsx.component
let make = () => {
  let onIncrement = Handler.handler.hxPost(
    "/counter/increment",
    ~securityPolicy=ResX.SecurityPolicy.allow,
    ~handler=async _ => {
      count := count.contents + 1
      <span id={counterId}> {Hjsx.string(Int.toString(count.contents))} </span>
    },
  )

  // ... onDecrement is the mirror image ...

  <section>
    <button
      type_="button"
      hxPost={onIncrement}
      hxSwap={ResX.Htmx.Swap.make(OuterHTML)}
      hxTarget={ResX.Htmx.Target.make(CssSelector(`#${counterId}`))}>
      {Hjsx.string("+")}
    </button>
    // ...
  </section>
}
```

The handler returns the refreshed `<span>` and HTMX swaps it into place — no page reload, no client framework.

### `src/TodoForm.res`

The form-input pattern. Posts to `/todos` with form-encoded data, runs the input through `Validation.parseTodoInput` (implemented with the selected validation library), and either appends the new todo to the list or sets status 400 and re-renders the form with an inline error:

```rescript
let onSubmit = Handler.handler.hxPost(
  "/todos",
  ~securityPolicy=ResX.SecurityPolicy.allow,
  ~handler=async ({request, requestController}) => {
    let formData = await request->Request.formData
    let rawName = formData->ResX.FormDataHelpers.getString("name")->Option.getOr("")
    let rawDescription = formData->ResX.FormDataHelpers.getString("description")->Option.getOr("")
    switch Validation.parseTodoInput(~name=rawName, ~description=rawDescription) {
    | Ok({name, description}) =>
      todos := todos.contents->Array.concat([{name, description}])
      renderList()
    | Error(msg) =>
      requestController.setStatus(400)
      // re-render the form with the error
    }
  },
)
```

The same `outerHTML` swap pattern applies: the handler's return value becomes the new DOM subtree.

### `src/Validation.res`

`parseTodoInput: (~name: string, ~description: string) => result<todoInput, string>` — the runtime contract for the todo form. The signature is identical between the zod and sury variants. The zod variant trims/min/max-validates `name` (1–80 chars) and `description` (≤240 chars) via schema rules.

### `src/__tests__/App.test.mjs`

The smoke test imports `../App.res.mjs` under **`bun:test`**, not vitest. The compiled output dereferences the global `Bun` object via rescript-bun, so running it under Node would crash with `Bun is not defined`:

```js
import { describe, it, expect } from "bun:test"

describe("App module", () => {
  it("compiles to an importable module", async () => {
    const module = await import("../App.res.mjs")
    expect(module).toBeDefined()
  })
})
```

`bun test` ships its own coverage reporter, so no separate vitest devDeps are needed — and the template intentionally omits them.

### `Dockerfile`

A three-stage image based on `oven/bun:1`:

1. `deps` — `bun install --ignore-scripts` against whichever lockfile is present (`bun.lock`, `pnpm-lock.yaml`, etc.)
2. `builder` — `bunx rescript && bun run build` (compile ReScript and any client assets)
3. `runtime` — slim `oven/bun:1-slim` layer that carries `node_modules/`, `src/`, `dist/`, `package.json`, `rescript.json`, drops to the unprivileged `bun` user (uid 1000), exposes `4444`, and runs `bun run src/App.res.mjs`

Managed platforms that speak OCI images (Fly.io, Railway, Render, Google Cloud Run, Scaleway Serverless Containers) can deploy this image directly. For single-binary workflows, swap the runtime `CMD` for `./dist/app` after running `bun run compile` locally.

## npm Scripts

| Script | Description |
| --- | --- |
| `start` | `bun run src/App.res.mjs` — run the compiled server once |
| `dev` | `concurrently "rescript -w" "bun --watch run src/App.res.mjs"` — pair ReScript watcher with Bun's hot-reload |
| `build` | `vite build` — bundle client assets through the res-x Vite plugin |
| `compile` | `bun build --compile src/App.res.mjs --outfile dist/app` — produce a standalone Bun binary |
| `test` | `bun test` — run the bun:test smoke suite |
| `test:coverage` | `bun test --coverage` — same, with Bun's built-in coverage reporter |
| `res:build` | `rescript` — one-shot compile |
| `res:dev` | `rescript -w` — recompile on save |
| `res:clean` | `rescript clean` |

Every script invokes `bun` (or `rescript` directly) regardless of the wizard's package-manager choice. The PM selection only affects how dependencies are installed in the README and the `packageManager` Corepack field — `bun` is required at runtime no matter what.

## Day-Two Recipes

This template is intentionally minimal and Bun-specific — most cross-template recipes do not apply directly. The two patterns most users reach for next:

- **Persistence** — the bundled README walks through promoting the in-memory todo `ref` to `bun:sqlite` storage in `Db.res`, including a wrapper, schema, and `.gitignore` adjustment. Bun ships an embedded SQLite driver, so this is a day-two task rather than a migration.
- **Self-hosting HTMX** — drop `htmx.min.js` into `public/` and swap the `<script src>` in `src/Layout.res` from the unpkg CDN to `/htmx.min.js`. Vite copies `public/` as-is to the production build.

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- **Bun is required at runtime.** Every npm script in `package.json` invokes `bun` directly (`bun run`, `bun --watch run`, `bun test`, `bun build --compile`). Bun 1.3 or later must be installed separately (https://bun.sh) — the README's Prerequisites section calls this out explicitly.
- **The package-manager choice is cosmetic.** Selecting npm / pnpm / yarn only affects the install commands shown in the README and the `packageManager` Corepack field. The wizard does not block other PM choices because they still work for the dependency-install step, but the runtime is always Bun.
- **`bun test`, not vitest.** The compiled `.res.mjs` references the global `Bun` object via rescript-bun. Running the suite under Node would crash with `Bun is not defined`. `bun test` ships its own coverage reporter (`bun test --coverage`), so no vitest devDeps are needed.
- **CI auto-enables `oven-sh/setup-bun`.** When the `bun` PM (or any res-x project) ships, `CommonFiles.ciWorkflow(..., setupBun = true)` injects the `setup-bun@v2` step with `bun-version: latest` so the workflow can `bun install` and `bun test` out of the box.
- **`rescript.json` is shipped verbatim, not generated.** `rescript-x` requires `jsx.module = "Hjsx"` plus four `compiler-flags` that open `ResX.Globals` and `RescriptBun.Globals`. `ProjectFileBuilders.rescriptJson` does not produce that shape, so the template loads it from `res-x/rescript.json` as a resource.
- **HTMX is loaded from `https://unpkg.com/htmx.org@<TemplateVersions.HTMX_CDN>`** (pinned, not floating). Self-host by dropping `htmx.min.js` into `public/` and pointing the `<script src>` at `/htmx.min.js`.
- **The dev port is 4444 for the server and 9000 for Vite.** `App.res` hardcodes 4444; `vite.config.js` configures Vite's dev server on 9000. The Dockerfile's `EXPOSE 4444` matches the server port.
- **State lives in a process-local `ref`** for the counter and todo list, which is convenient for exploring HTMX patterns but evaporates on every restart. The README's Persistence section walks through promoting to `bun:sqlite` when you outgrow the in-memory shape.
- **`bun build --compile` produces a standalone binary** at `dist/app`. After running `bun run compile` locally, you can swap the Dockerfile runtime stage's `CMD` for `["/app/dist/app"]` for a self-contained deployment that does not need `bun` at runtime.
