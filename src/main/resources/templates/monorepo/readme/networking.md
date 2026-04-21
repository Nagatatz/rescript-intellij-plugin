The client's Vite+ dev server proxies `/api/*` to the Hono server (see
`packages/client/vite.config.mjs`), so browser requests stay same-origin and
**CORS is not needed in dev**.

A ready-to-uncomment CORS block lives near the top of
`packages/server/src/Server.res` using the pre-bound `Hono.cors` factory.
Uncomment it if you remove the proxy, target a remote server, or host the
client on a separate origin in production. Adjust the origin list before
deploying.
