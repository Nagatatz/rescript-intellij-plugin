---
myst:
  html_meta:
    "keywords": "templates, tanstack start, tanstack router, react, vite, server functions"
---

# TanStack Start

A type-safe full-stack React template powered by TanStack Start (Vite-based) with file-based routing through `@tanstack/react-router`. Sources live under `src/` to match the framework's  convention; the validation library combo is hidden because TanStack Start ships its own data flow primitives.

## Generated layout

```
src/
├── router.tsx
├── routes/
│   ├── __root.tsx
│   └── index.tsx
├── components/
│   └── Greeting.res
├── server/
│   └── Greet.res
└── __tests__/
    └── Greeting.test.mjs
vite.config.ts
tsconfig.json
rescript.json
package.json
```

## Key files

- `src/routes/__root.tsx` — Root layout, wraps `<html>` / `<body>` and renders `<Outlet />`.
- `src/routes/index.tsx` — Home route (`/`) using `createFileRoute`. Loader greets via `Greet.greet`; the page rerenders with the new value when the button fires.
- `src/components/Greeting.res` — ReScript React component consumed from the home route via `Greeting.res.mjs`.
- `src/server/Greet.res` — Server Function bound through `@tanstack/react-start`'s `createServerFn`.
- `vite.config.ts` — Wires `@tanstack/router-plugin` (route tree generation), `@tanstack/react-start/plugin/vite` (SSR pipeline), and `@vitejs/plugin-react` (Fast Refresh).

## npm scripts

| Command | Description |
| --- | --- |
| `dev` | Start Vite dev server with `rescript -w`. |
| `build` | Compile ReScript and run `vite build`. |
| `start` | Run the production server bundle from `.output/`. |
| `test` | Run Vitest. |

## Day-two pointers

- **Add routes** — drop a new `.tsx` file under `src/routes/`; the router plugin regenerates `src/routeTree.gen.ts` automatically.
- **Add Server Functions** — define more `createServerFn(...)` thunks under `src/server/` and import them from any route or component.
- **Layer validation** — the wizard skips the zod/sury combo for this template; install whichever validator you want and call it inside your Server Function handlers.
