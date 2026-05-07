Persistence uses **PostgreSQL** via `postgres` (postgres-js) and Drizzle ORM.
A `compose.yaml` ships with the template so the database runs locally without
a host install:

```bash
docker compose up -d
{{cmdDbMigrate}}
```

The default `DATABASE_URL` is `postgres://app:dev@localhost:5432/app`, matching
the credentials in `compose.yaml`. Override it for staging / production via
`.env` or the deployment platform's environment variables.

```bash
{{cmdDbGenerate}}
{{cmdDbMigrate}}
```
