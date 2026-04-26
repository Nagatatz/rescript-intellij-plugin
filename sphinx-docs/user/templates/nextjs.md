---
myst:
  html_meta:
    "keywords": "next.js, app router, server components, route handler, gentype, react, rescript, validation, zod, sury"
---

# Next.js

{bdg-info}`SSR` {bdg-success}`React 19` {bdg-primary}`genType`

A Next.js 16 application with the App Router, ReScript components surfaced through `genType`, and a fully ReScript-implemented `POST /api/greet` Route Handler. The template demonstrates the canonical RSC layering: an async **Server Component** that fetches data on the server, a **Client Component** for interactivity, and a **Route Handler** for server-side endpoints — each backed by ReScript via genType bindings.

Pick this template when you need server-side rendering, server-side data fetching, SEO, or App Router primitives (layouts, loading fallbacks, route handlers). If you only need a client-only React SPA, the **Vite+ + React** template is lighter.

## What You Get

```
my-project/
├── rescript.json                  # JSX automatic + genType + @rescript/react in bs-deps
├── package.json                   # private, "concurrently" wires rescript -w + next dev
├── next.config.mjs                # default config — extend as needed
├── tsconfig.json                  # Next.js defaults + path mapping
├── rescript-modules.d.ts          # ambient declare module "*.res.mjs"
├── src/
│   ├── App.res                    # @genType server-rendered component (takes serverGeneratedAt prop)
│   ├── GreetForm.res              # @genType client component (state + fetch)
│   ├── Fetch.res                  # tiny POST helper shared by clients
│   ├── NextServer.res             # bindings for next/server (NextRequest, NextResponse.json)
│   ├── __tests__/
│   │   └── App.test.mjs           # smoke test that App.res.mjs exposes a component
│   └── app/
│       ├── page.tsx               # async Server Component — calls App.gen + GreetForm
│       ├── loading.tsx            # Suspense fallback
│       ├── client/
│       │   └── GreetForm.tsx      # "use client" wrapper around GreetForm.gen
│       └── api/
│           └── greet/
│               ├── route.ts       # one-line shim: re-exports post as POST
│               ├── GreetRoute.res # the actual handler body
│               └── Validation.res # zod or sury — selected in the wizard
├── README.md                      # script docs + RSC + Route Handlers + Project Layout
├── LICENSE                        # MIT, holder = project name
├── .nvmrc                         # Node 24
├── .gitignore                     # node_modules + ReScript build + .next/ + out/ + .env*.local
├── .editorconfig                  # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml             # weekly npm updates
    └── workflows/ci.yml           # install + rescript build + vitest
```

There is **no** `pages/` directory. This is App Router only.

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, the LICENSE holder, and the `<h1>` text in `App.res` |
| **Package manager** | npm / pnpm / yarn / bun. Affects `packageManager` field, README install commands, and CI cache key |
| **Validation library** | `zod` ↔ `sury`. Chooses which `src/app/api/greet/Validation.res` variant ships — the validator runs inside the API route on the parsed JSON body |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `next` | Next.js framework | `TemplateVersions.NEXTJS` |
| `react` / `react-dom` | React 19 runtime | `TemplateVersions.REACT` / `TemplateVersions.REACT_DOM` |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs the compiled `.res.mjs` imports at runtime | `TemplateVersions.RESCRIPT_RUNTIME` |
| `@rescript/react` | React bindings (hooks, components, events) | `TemplateVersions.RESCRIPT_REACT` |
| `zod` *or* `sury` | API body validation (chosen in the wizard) | `TemplateVersions.ZOD` / `TemplateVersions.SURY` |
| `concurrently` *(dev)* | Runs `rescript -w` and `next dev` together under a single process | `TemplateVersions.CONCURRENTLY` |
| `typescript` *(dev)* | Required by Next.js + by genType for the emitted `.gen.tsx` / `.gen.d.ts` | `TemplateVersions.TYPESCRIPT` |
| `@types/react` / `@types/react-dom` *(dev)* | React typings | `TemplateVersions.REACT_TYPES` / `TemplateVersions.REACT_DOM_TYPES` |
| `@types/node` *(dev)* | Node typings consumed by `next.config.mjs` and route handlers | `TemplateVersions.NODE_TYPES` |
| `vitest` *(dev)* | Smoke test runner | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |

## Key Files

### `rescript.json`

Three template-specific bits:

- `bs-dependencies` includes `@rescript/react` so JSX type-checks
- `jsx.mode = "automatic"` so JSX desugars to `react/jsx-runtime`
- `gentypeconfig` is enabled — every `@genType` annotation produces a matching `.gen.tsx` (for components) or `.gen.d.ts` next to the `.res.mjs` output

genType is on by default because the App Router consumes ReScript components from `.tsx` files. Without it, TypeScript would only see the untyped runtime modules.

### `src/app/page.tsx` (Server Component)

The default export of every App Router route file is a React component. Marking it `async` opts into server-side data fetching with no `useEffect`, no client waterfall, and no API round-trip:

```tsx
import App from "../App.gen";
import GreetForm from "./client/GreetForm";

async function loadServerTimestamp(): Promise<string> {
  // Replace with your server-only data source (process.env, Drizzle, Prisma, fetch).
  return new Date().toUTCString();
}

export default async function Page() {
  const serverGeneratedAt = await loadServerTimestamp();
  return (
    <main style={{ padding: "2rem", fontFamily: "sans-serif" }}>
      <App serverGeneratedAt={serverGeneratedAt} />
      <GreetForm />
    </main>
  );
}
```

`App` is imported from `App.gen` — the genType-generated `.tsx` file, *not* the original `App.res`. That means TypeScript sees real types for the ReScript component's props.

### `src/App.res` (ReScript server-renderable component)

A pure component that accepts a `serverGeneratedAt` string prop and renders it. No state, no client-only APIs:

```rescript
@genType @react.component
let make = (~serverGeneratedAt: string) => {
  <section>
    <h1> {React.string("Welcome to <projectName>")} </h1>
    <p>{React.string("This block is a Server Component. The form below is a Client Component.")}</p>
    <p style={{color: "#666", fontSize: "0.875rem"}}>
      {React.string("Rendered on the server at " ++ serverGeneratedAt)}
    </p>
  </section>
}
```

The `@genType @react.component` pair is the canonical annotation for "expose this React component to TypeScript callers." Drop the `@genType` and the component becomes ReScript-only.

### `src/app/client/GreetForm.tsx` + `src/GreetForm.res`

The Client Component is a two-file sandwich:

```tsx
// app/client/GreetForm.tsx
"use client";

import GreetFormRescript from "../../GreetForm.gen";

export default function GreetForm() {
  return <GreetFormRescript />;
}
```

The `"use client"` directive is the *boundary*: it tells Next.js that everything imported from this file (transitively) belongs in the browser bundle. The actual ReScript component (`src/GreetForm.res`) uses `React.useState`, attaches event handlers, and calls `Fetch.post` — all of which require running on the client.

```rescript
// src/GreetForm.res
@genType @react.component
let make = () => {
  let (name, setName) = React.useState(() => "")
  let (greeting, setGreeting) = React.useState(() => None)

  let handleSubmit = async event => {
    ReactEvent.Form.preventDefault(event)
    let response = await Fetch.post("/api/greet", JSON.stringifyAny({"name": name})->Option.getOr("{}"))
    let body = await response->Fetch.json
    setGreeting(_ => Some(body["message"]))
  }
  ...
}
```

The rule of thumb: keep stateful or event-driven ReScript components behind a `"use client"` boundary. Keep pure or async-data-fetching components on the server side.

### `src/app/api/greet/route.ts` + `GreetRoute.res` (Route Handler)

Next.js requires the file to be named `route.ts` / `route.js` / `route.mjs`. ReScript requires module filenames to start with an *uppercase* letter. The template resolves the conflict with a one-line re-export shim:

```ts
// route.ts
export { post as POST } from "./GreetRoute.res.mjs";
```

The handler body lives in `GreetRoute.res`:

```rescript
let post = async (req: NextServer.nextRequest): NextServer.nextResponse => {
  let raw = try await NextServer.reqJson(req) catch {
  | _ => JSON.Object(Dict.make())
  }
  switch Validation.parseGreetInput(raw) {
  | Ok(input) =>
    NextServer.jsonResponse(
      JSON.Object(Dict.fromArray([("message", JSON.String("Hello, " ++ input.name ++ "!"))])),
    )
  | Error(msg) =>
    NextServer.jsonResponseWithInit(
      JSON.Object(Dict.fromArray([("error", JSON.String(msg))])),
      {"status": 400},
    )
  }
}
```

The validate-then-respond ordering matters: untyped HTTP bodies are exactly the surface where `Validation` earns its keep. Bad input returns `400` with `{ "error": "..." }`; valid input returns `200` with the greeting.

### `src/NextServer.res`

Minimal bindings to `next/server`. Just enough for the POST handler:

```rescript
type nextRequest
type nextResponse

@send external reqJson: nextRequest => promise<JSON.t> = "json"

@module("next/server") @scope("NextResponse")
external jsonResponse: JSON.t => nextResponse = "json"

@module("next/server") @scope("NextResponse")
external jsonResponseWithInit: (JSON.t, {..}) => nextResponse = "json"
```

Extend this file as you add more Route Handlers — keeping the bindings centralized prevents `@module("next/server")` externals from sprawling across the project.

### `src/app/api/greet/Validation.res`

`parseGreetInput: JSON.t => result<greetInput, string>` — runs against the parsed HTTP body. The signature is identical between the zod and sury variants so `GreetRoute.res` does not branch on which is loaded.

When you add a new endpoint that takes JSON, ship a sibling `Validation.res` and run it before doing any work. Returning a `result` keeps the contract honest at the HTTP boundary.

### `rescript-modules.d.ts`

```ts
declare module "*.res.mjs";
```

An ambient declaration so TypeScript stops complaining about the runtime entry points (`route.ts`'s re-export of `./GreetRoute.res.mjs` would otherwise be untyped). The `.gen.tsx` / `.gen.d.ts` files genType emits already carry real types for component props — this declaration covers the runtime-only modules.

## npm Scripts

| Script | Description |
| --- | --- |
| `dev` | `concurrently "rescript -w" "next dev"` — watch ReScript and run the dev server together |
| `build` | `rescript && next build` — compile ReScript first, then build Next.js |
| `start` | `next start` — run the production Next.js server |
| `test` | `vitest run` — execute the smoke suite |
| `test:coverage` | `vitest run --coverage` — same, with v8 coverage report |
| `res:build` | `rescript` — one-shot ReScript compile |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` |
| `res:dev` | `rescript -w` — recompile on save (also wired into `dev`) |

`build` puts the ReScript compile *before* `next build` deliberately. Next.js reads the `.res.mjs` and `.gen.tsx` outputs during its own bundling pass, so they must exist on disk first.

## Day-Two Recipes

- {doc}`../recipes/create-react-component` — adding new ReScript components and exposing them via genType
- {doc}`../recipes/convert-from-typescript` — porting an existing `.tsx` page into a `.res` component
- {doc}`../recipes/optimize-imports` — keeping `App.res` and `GreetForm.res` tidy as the app grows
- {doc}`../recipes/find-dead-code` — running `reanalyze` to catch unused components and bindings

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- **App Router only.** There is no `pages/` directory and no `getServerSideProps`. If you need pages-router primitives, this template is the wrong starting point.
- **ReScript files compile to `.res.mjs` then are imported from TSX.** Always remember the chain: `App.res` → `App.res.mjs` (runtime) → `App.gen.tsx` (genType-typed wrapper) → `app/page.tsx` (consumer). Edit `App.res`; consume from `*.gen.tsx`.
- **Route Handler filename collision.** `route.ts` is a Next.js requirement; `GreetRoute.res` is a ReScript requirement. Keep the shim pattern (one-line re-export) for every new endpoint rather than inlining handler logic in `route.ts`.
- **Validation runs on the server.** The API route is the trust boundary — anything coming over HTTP is unsafe until `Validation.parseGreetInput` returns `Ok`. Server-side checks must not be skipped even when the client form already validates the same field; the client is just UX, not security.
- **`concurrently` quoting.** `dev` is wired as `concurrently "rescript -w" "next dev"` with literal escaped quotes. If you add a third process, follow the same quoting style or `concurrently` will silently merge the args.
- **Server Components cannot use `useState`.** If you try to call a hook inside a non-`"use client"` ReScript component imported from `page.tsx`, Next.js will fail at build time with a Server Component error. Move the stateful component behind a `"use client"` wrapper like `app/client/GreetForm.tsx`.
- **`@rescript/runtime` must be a direct dependency** (it is, by default). pnpm's strict layout hides transitive deps from user code, and `.res.mjs` files import from `@rescript/runtime/lib/es6/...` at runtime — without the direct dep, both the dev server and `next build` will throw `Cannot find module '@rescript/runtime/...'`.
- **`.gitignore` adds `.next/`, `out/`, and `.env*.local`** on top of the ReScript defaults so the Next.js build cache, the static export output, and local env files are not committed.
- **Vitest does not exercise the API route.** The bundled smoke test only checks that `App.res.mjs` exposes a function. Add a test that imports `GreetRoute.res.mjs` and feeds it a fake `nextRequest` when you start adding business logic to the handler.
