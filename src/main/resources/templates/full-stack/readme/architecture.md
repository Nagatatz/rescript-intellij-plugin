This template lives in **one** `package.json`, with three source roots that ReScript
compiles together:

- `src/shared/` — types used by both sides (wire format, domain records)
- `src/server/` — Hono + Drizzle (SQLite) API on port 3000
- `src/client/` — Vite+/React UI that fetches `/api/*` through a dev proxy

Run `npm run dev` (or `pnpm dev` / `yarn dev`) to boot **three** processes
in parallel via `concurrently`:

- `rescript -w` — recompiles `.res` files on save (covers shared / server / client)
- `node --watch src/server/ServerMain.res.mjs` — restarts the API on rebuilt output
- `vp dev` — Vite+ client dev server, proxies `/api/*` to the API (no CORS needed)

Edits to a `.res` file in any source root flow through to the running
processes without a manual rebuild step. If you start `dev` before any
ReScript output exists, the server may briefly fail until `rescript -w`
emits its first `.res.mjs` — re-run `dev` or wait one cycle.

If you later remove the proxy, point the client at a remote server, or host
the client on a separate origin, a ready-to-uncomment CORS block lives near
the top of `src/server/Server.res` using the pre-bound `Hono.cors` factory.
