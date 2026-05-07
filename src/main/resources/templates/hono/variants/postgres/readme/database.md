Persistence uses **PostgreSQL** via the `postgres` driver (postgres-js) with
**Drizzle ORM**. A `compose.yaml` ships with the template so the database
runs locally without a host install:

```bash
docker compose up -d
{{cmdDbMigrate}}
{{cmdRunDev}}
```

The default `DATABASE_URL` is `postgres://app:dev@localhost:5432/app`, matching
the credentials in `compose.yaml`. Override it via `.env` or the deployment
platform's environment variables for production.

Migrations:

```bash
{{cmdDbGenerate}}
{{cmdDbMigrate}}
```

Schema lives in `src/Schema.res`; update it and re-run `db:generate` to diff.
