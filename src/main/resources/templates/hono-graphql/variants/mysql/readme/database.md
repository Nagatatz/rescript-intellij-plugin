Persistence uses **MySQL** via the `mysql2` driver and Drizzle ORM. A
`compose.yaml` ships with the template so the database runs locally without
a host install:

```bash
docker compose up -d
{{cmdDbMigrate}}
```

The default `DATABASE_URL` is `mysql://root:dev@localhost:3306/app`, matching
the credentials in `compose.yaml`. Override it for staging / production via
`.env` or the deployment platform's environment variables.

```bash
{{cmdDbGenerate}}
{{cmdDbMigrate}}
```
