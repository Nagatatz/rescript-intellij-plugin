| File | Purpose |
| --- | --- |
| `src/Server.res` | App entry point: middleware, routes, docs |
| `src/Routes.res` | Users CRUD handlers (nested as `Routes.Users`) |
| `src/Schema.res` | Drizzle + Zod schemas (single source of truth) |
| `src/Db.res` | libsql client + Drizzle wrapper |
| `src/Logger.res` | Logger middleware binding |
| `src/ZodOpenapi.res` | `@hono/zod-openapi` bindings |
| `src/Scalar.res` | Scalar UI bindings |
| `drizzle.config.ts` | drizzle-kit config for migration generation |
