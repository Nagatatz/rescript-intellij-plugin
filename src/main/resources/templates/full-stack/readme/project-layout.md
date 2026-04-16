| File | Purpose |
| --- | --- |
| `src/shared/Types.res` | Domain types (user, etc.) |
| `src/shared/Api.res` | Wire types for /api/* |
| `src/server/Main.res` | Process entry point |
| `src/server/Server.res` | Hono app + health route |
| `src/server/Routes/Users.res` | Users CRUD handlers |
| `src/server/Schema.res` | Drizzle SQLite schema |
| `src/server/Db.res` | libsql client + query helpers |
| `src/client/Main.res` | React root render |
| `src/client/App.res` | Users form + list UI |
| `src/client/Api.res` | Fetch wrapper using Shared types |
| `vite.config.mjs` | Vite+ config with /api proxy |
