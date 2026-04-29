```
.
├── index.html              # Inertia HTML host page (Vite entry)
├── vite.config.mjs         # Vite+ config (react + inertiaPages plugins)
├── drizzle.config.ts       # Drizzle migration config
├── src/
│   ├── Hono.res            # Hono framework bindings
│   ├── HonoInertia.res     # @hono/inertia middleware bindings
│   ├── HonoNodeServer.res  # @hono/node-server bindings
│   ├── InertiaBindings.res # @inertiajs/react bindings (Link / usePage / createInertiaApp)
│   ├── Logger.res          # hono/logger
│   ├── Schema.res          # Drizzle SQLite schema
│   ├── Db.res              # libsql + Drizzle wiring
│   ├── Validation.res      # zod or sury input validation
│   ├── Routes.res          # Inertia route handlers
│   ├── Server.res          # App + middleware composition
│   ├── ServerMain.res      # `node` entrypoint
│   └── client/
│       ├── Main.res        # Inertia client bootstrap
│       ├── pages.js        # `import.meta.glob` resolver shim (JS — see frontend.md)
│       ├── MainLayout.res  # Shared layout
│       └── Pages/
│           ├── Home.res
│           └── About.res
└── src/__tests__/
    └── Server.test.mjs     # Vitest smoke tests for the Hono routes
```

### Why split `Server.res` and `ServerMain.res`?

`Server.res` exports the `app` value but does **not** bind a port, so test
code can `import { app } from "../Server.res.mjs"` and exercise routes via
`app.request(...)` without the side effect of starting an HTTP listener.
`ServerMain.res` is the production entry point that calls `Server.start()`.
