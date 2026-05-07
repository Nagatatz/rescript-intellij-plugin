Persistence uses **MySQL** via the `mysql2` driver with **Drizzle ORM**. A
`compose.yaml` ships with the template so the database runs locally without
a host install:

```bash
docker compose up -d
{{cmdDbMigrate}}
{{cmdRunDev}}
```

The default `DATABASE_URL` is `mysql://root:dev@localhost:3306/app`, matching
the credentials in `compose.yaml`. Override it via `.env` or the deployment
platform's environment variables for production.

Migrations:

```bash
{{cmdDbGenerate}}
{{cmdDbMigrate}}
```

Schema lives in `src/Schema.res`; update it and re-run `db:generate` to diff.
