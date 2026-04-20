| File | Purpose |
| --- | --- |
| `src/shared/Shared.res` | Shared domain + wire types (`Shared.Types.*`, `Shared.Api.*`) |
| `src/server/ServerMain.res` | Process entry point |
| `src/server/Server.res` | Hono app + health route |
| `src/server/Routes.res` | Users CRUD handlers (nested as `Routes.Users`) |
| `src/server/Schema.res` | Drizzle SQLite schema |
| `src/server/Db.res` | libsql client + query helpers |
| `src/client/ClientMain.res` | React root render |
| `src/client/App.res` | Users form + list UI |
| `src/client/ApiClient.res` | Fetch wrapper using Shared types |
| `vite.config.mjs` | Vite+ config with /api proxy |
