Persistence uses SQLite via `@libsql/client` and Drizzle ORM. Defaults to
`./data/app.db`; set `DATABASE_URL` (e.g. a Turso `libsql://` URL) to swap.

```bash
{{cmdDbGenerate}}
{{cmdDbMigrate}}
```
