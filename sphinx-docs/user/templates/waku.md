---
myst:
  html_meta:
    "keywords": "templates, waku, react server components, rsc, daishi kato"
---

# Waku

A minimal React Server Components app powered by Waku (by Daishi Kato). The template ships both halves of the RSC boundary in ReScript: a Server Component (`Greet.res`, no JS shipped) and a Client Component (`Counter.res`, made client-side via a thin TSX `"use client"` wrapper).

## Generated layout

```
src/
├── pages/
│   └── index.tsx
├── components/
│   ├── Greet.res
│   ├── Counter.res
│   └── CounterClient.tsx
└── __tests__/
    └── Greet.test.mjs
tsconfig.json
rescript.json
package.json
```

## Key files

- `src/pages/index.tsx` — Server Component (Waku's default for `src/pages/`). Imports the ReScript `Greet` directly and the `CounterClient` Client wrapper.
- `src/components/Greet.res` — Server Component body. Renders to HTML on the server; ships zero JS for itself.
- `src/components/Counter.res` — Client Component body, written in ReScript with `useState`.
- `src/components/CounterClient.tsx` — Thin TSX wrapper that owns the `"use client"` directive and re-exports the ReScript `make` function. ReScript cannot emit `"use client"` itself, so this small file is the canonical workaround.

## npm scripts

| Command | Description |
| --- | --- |
| `dev` | Start `waku dev` with `rescript -w`. |
| `build` | Compile ReScript and run `waku build`. |
| `start` | Run the production server. |
| `test` | Run Vitest. |

## Day-two pointers

- **Add Server Components** — drop another `.res` file under `src/components/` and import it from any page or other Server Component. No client wrapper required.
- **Add Client Components** — write the body in ReScript, then add a `<Name>Client.tsx` wrapper that begins with `"use client"` and re-exports `make`. Keep the wrapper as small as possible.
- **Server Functions** — when stable in your version of Waku, expose any function from a server file and call it from a Client Component; Waku rewires the call into an RPC at build time.
