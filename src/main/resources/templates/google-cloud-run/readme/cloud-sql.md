Connect to Cloud SQL via the `@google-cloud/cloud-sql-connector` or a
standard Postgres driver with the Cloud SQL Auth Proxy.

```bash
pnpm add pg @google-cloud/cloud-sql-connector
```

```rescript
@module("pg") @new external makePool: 'opts => 'pool = "Pool"
@send external queryAsync: ('pool, string, array<'a>) => promise<'rows> = "query"
```

Pass the `DATABASE_URL` as an environment variable via `--set-env-vars` on deploy.