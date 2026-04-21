This template lives in **one** `package.json`, with three source roots that ReScript
compiles together:

- `src/shared/` — types used by both sides (wire format, domain records)
- `src/server/` — Hono + Drizzle (SQLite) API on port 3000
- `src/client/` — Vite+/React UI that fetches `/api/*` through a dev proxy

Run `npm run dev` (or `pnpm dev` / `yarn dev`) to boot both processes via
`concurrently`. The client dev server proxies `/api/*` to the server so you
never need to configure CORS locally.

If you later remove the proxy, point the client at a remote server, or host
the client on a separate origin, a ready-to-uncomment CORS block lives near
the top of `src/server/Server.res` using the pre-bound `Hono.cors` factory.
