This template lives in **one** `package.json` with three source roots that ReScript
compiles together:

- `src/shared/` — types used by both sides (wire-format records, domain types)
- `src/server/` — Hono + Drizzle (SQLite) server with graphql-yoga mounted at `/graphql`
- `src/client/` — Vite+/React UI that talks to the server via `rescript-relay`
  (see `src/client/RelayEnvironment.res`); queries live in `%relay()` tags and
  are type-checked against `src/server/schema.graphql` by the Relay compiler

Run `npm run dev` (or `pnpm dev` / `yarn dev`) to boot ReScript, the Relay
compiler watcher, the Hono server, and the Vite+ client concurrently. The client
dev server proxies `/api/*` and `/graphql` to the Hono server, so browser
requests stay same-origin and **CORS is not needed in dev**.

A ready-to-uncomment CORS block lives near the top of `src/server/Server.res`
using the pre-bound `Hono.cors` factory. Uncomment it if you remove the proxy,
target a remote server, or host the client on a separate origin in production.

The Relay compiler step (`pnpm relay` / `pnpm relay:watch`) must run at least
once before the first build; it emits `src/client/__generated__/*_graphql.res`
files from the `%relay()` tags plus `src/server/schema.graphql`. Those generated
files are gitignored — rerun the compiler after editing a query or the SDL.
