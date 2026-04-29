Routes live under `app/routes/`. The `@tanstack/router-plugin` Vite
plugin scans that directory and rewrites `app/routeTree.gen.ts` whenever
files change. Naming conventions:

- `__root.tsx` – root layout, always rendered.
- `index.tsx` – `/` route.
- `about.tsx` – `/about` route.
- `posts/$postId.tsx` – `/posts/:postId` dynamic route.
- `posts.tsx` plus `posts/index.tsx` – nested layout + index.

ReScript components live in `app/components/`. They expose a `make`
function via `@react.component`; TSX routes import them through their
compiled `.res.mjs` artifact, e.g. `import { make as Greeting } from
"../components/Greeting.res.mjs"`.
