Persistence lives in the **server workspace** (`packages/server/`). It uses
**SQLite** via `@libsql/client` with **Drizzle ORM**. The DB file defaults to
`packages/server/data/app.db` — set `DATABASE_URL` in `packages/server/.env`
(template ships `packages/server/.env.example`) to swap in a Turso `libsql://`
URL or another libsql-compatible host.

Schema lives in `packages/server/src/Schema.res`. Update it and run the
migration scripts inside the server workspace:

```bash
{{cmdDbGenerate}}
{{cmdDbMigrate}}
```

These commands run `drizzle-kit` in the server workspace via the package
manager's filter (`pnpm --filter`, `yarn workspace`, `npm run -w`, or
`bun --filter`).
