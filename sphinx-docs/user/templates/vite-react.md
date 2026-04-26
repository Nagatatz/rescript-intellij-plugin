---
myst:
  html_meta:
    "keywords": "vite, vite plus, react, jsx, rescript-react, single-page, spa, fetch, validation, zod, sury"
---

# Vite+ + React

{bdg-warning}`Vite+ pre-1.0` {bdg-info}`SPA` {bdg-success}`React 19`

A single-page React application powered by ReScript and the Vite+ toolchain. Vite+ (`vite-plus`) is a unified wrapper over Vite, Vitest, Oxlint, Oxfmt, and Rolldown — one CLI (`vp`) handles dev, build, preview, and tests. The template ships a working greet form (input + `useState` + `fetch`) backed by an offline fallback, plus a `vp test` smoke suite.

Pick this template when you want a React SPA with the modern unified toolchain. It is the fastest way to get a `.res` + JSX project running in the browser. If Vite+ blocks you (it is pre-1.0), the README and this page describe a clean fallback to vanilla Vite — you are not locked in.

## What You Get

```
my-project/
├── rescript.json                  # JSX mode "automatic", @rescript/react in bs-deps
├── package.json                   # ESM, "private": true, "vp" scripts
├── index.html                     # <script type="module" src="/src/Main.res.mjs">
├── vite.config.mjs                # imports defineConfig from "vite-plus" + react()
├── src/
│   ├── Main.res                   # ReactDOM.Client.createRoot + <App />
│   ├── App.res                    # form + useState + fetch + Validation
│   ├── Api.res                    # fetch("/api/greet") + offline fallback
│   ├── Validation.res             # zod or sury — selected in the wizard
│   └── __tests__/
│       └── App.test.mjs           # smoke test that App is a function component
├── README.md                      # script docs + About Vite+ section
├── LICENSE                        # MIT, holder = project name
├── .nvmrc                         # Node 24
├── .gitignore                     # node_modules + ReScript build + dist/ + .vite/
├── .editorconfig                  # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml             # weekly npm updates
    └── workflows/ci.yml           # install + rescript build + vp test
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, the LICENSE holder, and the `<title>` in `index.html` |
| **Package manager** | npm / pnpm / yarn / bun. Affects `packageManager` field, README install commands, and CI cache key |
| **Validation library** | `zod` ↔ `sury`. Chooses which `src/Validation.res` variant ships — the validator runs inside the form's submit handler |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs the compiled `.res.mjs` imports at runtime | `TemplateVersions.RESCRIPT_RUNTIME` |
| `@rescript/react` | React bindings (hooks, components, events) | `TemplateVersions.RESCRIPT_REACT` |
| `react` / `react-dom` | React 19 runtime | `TemplateVersions.REACT` / `TemplateVersions.REACT_DOM` |
| `zod` *or* `sury` | Form input validation (chosen in the wizard) | `TemplateVersions.ZOD` / `TemplateVersions.SURY` |
| `vite-plus` *(dev)* | Unified Vite/Vitest/Oxlint wrapper exposing the `vp` CLI | `TemplateVersions.VITE_PLUS` |
| `@voidzero-dev/vite-plus-core` *(dev)* | Vite+ core engine | `TemplateVersions.VITE_PLUS_CORE` |
| `vite` *(dev)* | Direct dep so the documented Vite+ → Vite fallback works without changing dependencies | `TemplateVersions.VITE` |
| `@vitejs/plugin-react` *(dev)* | React Fast Refresh + JSX | `TemplateVersions.VITEJS_PLUGIN_REACT` |
| `vitest` *(dev)* | Smoke test runner (also driven through `vp test`) | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |

## Key Files

### `rescript.json`

Three template-specific bits:

- `bs-dependencies` includes `@rescript/react` so JSX type-checks
- `jsx.mode = "automatic"` so JSX desugars to `react/jsx-runtime` rather than `React.createElement`
- The validation library (`@rescript/zod` shim or `rescript-sury`) is appended depending on the wizard pick

You should rarely need to touch any of those — they map 1:1 onto modern React + ReScript conventions.

### `index.html`

The Vite entry document. Loads `/src/Main.res.mjs` as a module — Vite resolves the path through its dev server (in `dev`) or rewrites it to a hashed bundle (in `build`).

### `src/Main.res`

The bootstrap. Three lines: find `#root`, create a React root, render `<App />`:

```rescript
switch ReactDOM.querySelector("#root") {
| Some(rootEl) =>
  ReactDOM.Client.Root.render(ReactDOM.Client.createRoot(rootEl), <App />)
| None => Console.error("Could not find root element")
}
```

### `src/App.res`

The interactive demo. A controlled `<input>` + a submit button + four pieces of `useState` (name, greeting, error, loading). The submit handler validates the input through `Validation.parseGreetForm`, then calls `Api.greet`:

```rescript
let handleSubmit = async event => {
  ReactEvent.Form.preventDefault(event)
  switch Validation.parseGreetForm(name) {
  | Error(message) =>
    setError(_ => Some(message))
    setGreeting(_ => None)
  | Ok({name: validated}) =>
    setError(_ => None)
    setLoading(_ => true)
    try {
      let message = await Api.greet(validated)
      setGreeting(_ => Some(message))
    } catch {
    | JsExn(err) =>
      setGreeting(_ => Some("Error: " ++ err->JsExn.message->Option.getOr("unknown")))
    }
    setLoading(_ => false)
  }
}
```

The validate-then-fetch ordering matters: validation is cheap and synchronous, so you check it before paying for a network round-trip. Errors render inline (`<p style={{color: "crimson"}}>`) and disable the submit button while the request is in flight.

### `src/Api.res`

A thin `fetch` wrapper aimed at `/api/greet`. The notable detail is the *offline fallback*: when no backend is available the function still returns a usable greeting so the form does not appear broken on first run.

```rescript
let greet = async (name: string): string => {
  try {
    let response = await fetch("/api/greet", {...})
    if response->ok {
      let body = await response->json
      body["message"]
    } else {
      `Hello, ${name}! (offline fallback — no backend at /api/greet)`
    }
  } catch {
  | _ => `Hello, ${name}! (offline fallback — fetch failed)`
  }
}
```

When you wire a real backend (graduate to **Full-Stack** or **Monorepo**, or stand up the **Hono (REST)** template), drop the fallback branches.

### `src/Validation.res`

`parseGreetForm: string => result<greetForm, string>` — runs before the fetch call. The zod and sury variants both check that the trimmed name is non-empty and ≤ 80 characters. Bad input lights up the inline error message; valid input proceeds to `Api.greet`.

### `src/__tests__/App.test.mjs`

The smoke test imports the *compiled* `App.res.mjs` and asserts on the exported component:

```js
import { describe, expect, it } from "vitest";
import { make as App } from "../App.res.mjs";

describe("App", () => {
  it("is a function component", () => {
    expect(typeof App).toBe("function");
  });
});
```

This is a minimal sanity check that the component is reachable. It does not render the form. To assert on DOM behavior, add `@testing-library/react` and a JSDOM environment to the dev dependencies, then write tests that render `<App />` and click the submit button.

### `vite.config.mjs`

Two lines plus boilerplate:

```js
import { defineConfig } from "vite-plus";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
});
```

Note the `defineConfig` import comes from `vite-plus`, not `vite`. To fall back to vanilla Vite, swap that import (see Notes below).

## npm Scripts

| Script | Description |
| --- | --- |
| `dev` | `vp dev` — start the Vite+ dev server with HMR |
| `build` | `vp build` — produce a production bundle in `dist/` |
| `preview` | `vp preview` — serve the built `dist/` for manual smoke testing |
| `test` | `vp test` — run Vitest through the Vite+ CLI |
| `test:coverage` | `vp test --coverage` — same, with v8 coverage report |
| `res:build` | `rescript` — one-shot ReScript compile |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` |
| `res:dev` | `rescript -w` — recompile on save (run alongside `vp dev`) |

The recommended development workflow is two terminals: `pnpm res:dev` in one to keep `.res.mjs` outputs current, `pnpm dev` in the other for the Vite+ HMR server. Vite+ picks up the recompiled modules via its file watcher.

## Falling Back to Vanilla Vite

When pre-1.0 Vite+ misbehaves (most often a `vite/internal` resolution failure during `vp build`), the migration off Vite+ is two edits and zero new dependencies:

1. In `vite.config.mjs`, swap the import:

   ```diff
   - import { defineConfig } from "vite-plus";
   + import { defineConfig } from "vite";
   ```

2. In `package.json`, replace the `vp` scripts with their vanilla equivalents:

   ```jsonc
   "scripts": {
     "dev":     "vite",
     "build":   "vite build",
     "preview": "vite preview",
     "test":    "vitest run",
     "test:coverage": "vitest run --coverage"
   }
   ```

The `vite` and `vitest` packages are already in `devDependencies`. You can leave `vite-plus` and `@voidzero-dev/vite-plus-core` installed (they are inert when the config does not import them) or remove them with `pnpm remove vite-plus @voidzero-dev/vite-plus-core`.

When Vite+ ships a stable release, the migration back is the same edit in reverse — there is no `vite.config` syntax difference between the two for a typical React project.

## Day-Two Recipes

- {doc}`../recipes/create-react-component` — adding a new ReScript React component and wiring it into `App.res`
- {doc}`../recipes/convert-from-typescript` — porting an existing `.tsx` component into a `.res` component
- {doc}`../recipes/optimize-imports` — keeping `App.res` and `Main.res` tidy as the SPA grows
- {doc}`../recipes/find-dead-code` — running `reanalyze` to catch unused components

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- **Vite+ is pre-1.0.** `TemplateVersions.VITE_PLUS` pins the `0.1.x` range. APIs, defaults, and CLI flags may shift before the stable release. Track the [Vite+ changelog](https://vite.plus) when bumping.
- **Known incompatibility:** the current pre-1.0 Vite+ does not always resolve `vite/internal` cleanly when paired with `@vitejs/plugin-react`, so `vp build` may fail. The fallback is one edit: in `vite.config.mjs`, replace `import { defineConfig } from "vite-plus"` with `import { defineConfig } from "vite"`, then swap the npm scripts (`dev` / `build` / `preview` / `test`) from `vp` to `vite` / `vite build` / `vite preview` / `vitest run`. The template already declares a direct `vite` dependency so this swap requires no `npm install`.
- React 19 is the default. `@rescript/react` and the `@types/react` family are pinned to matching majors in `TemplateVersions.kt`; bump them together.
- `rescript.json`'s `jsx.mode` is set to `"automatic"`. If you set it back to `"classic"`, you also need to import `React` at the top of every `.res` file that uses JSX.
- The Vitest smoke test only verifies that `import { make as App } from "../App.res.mjs"` returns a function. It does not actually render the form. Add `@testing-library/react` and a JSDOM environment when you need DOM assertions.
- The form intentionally calls `Validation` *before* the network. This keeps the contract simple: the network only sees inputs that already passed local checks. Server-side validation is still required — see the **Hono (REST)** or **Full-Stack** templates for the matching backend pattern.
- `.gitignore` adds `dist/` and `.vite/` on top of the ReScript defaults so build artifacts and Vite's local cache do not get committed.
- `package.json` declares `"private": true`. Flip it to `false` (and add a real `version` and `license`) only when you actually want to publish the SPA itself to npm — which is unusual for an end-user app.
- The `vp` CLI is a thin wrapper over Vitest for `vp test`. If you ever need Vitest-only flags that Vite+ does not surface, run `vitest run` directly — the `vitest` and `@vitest/coverage-v8` deps are already installed.
