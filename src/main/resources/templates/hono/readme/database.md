Persistence uses **SQLite** via `@libsql/client` with **Drizzle ORM**. The DB file
lives at `./data/app.db` by default (set `DATABASE_URL` to override, e.g. a Turso
`libsql://` URL for production).

Migrations:

```bash
{{cmdDbGenerate}}
{{cmdDbMigrate}}
```

Schema lives in `src/Schema.res`; update it and re-run `db:generate` to diff.
