---
myst:
  html_meta:
    "keywords": "drizzle, sqlite, libsql, turso, orm, migrations, rescript"
---

# Set Up Drizzle

The Hono REST, Hono GraphQL, Full-Stack, and Monorepo templates all ship Drizzle ORM with SQLite (via `@libsql/client`). This recipe covers the day-two flow: changing the schema, generating migrations, and pointing at Turso for production.

## What You Get

- `src/Schema.res` — Drizzle table definitions via `@module("drizzle-orm/sqlite-core")` bindings
- `src/Db.res` — `createClient({url: ...})` + `drizzle(client)` + query helpers
- `drizzle.config.ts` — drizzle-kit config pointing at the compiled `Schema.res.mjs`
- `data/app.db` — default local SQLite file (gitignored)
- Scripts — `db:generate`, `db:migrate`

## Adding a Column

Edit `src/Schema.res`:

```rescript
let users = sqliteTable("users", {
  "id": intCol("id", {"primaryKey": true, "autoIncrement": true}),
  "name": textCol("name", {"notNull": true}),
  "email": textCol("email", {"notNull": true}),
  "createdAt": intCol("created_at", {"notNull": true, "default": Date.now()}),
})
```

Regenerate and apply:

```bash
pnpm res:build     # refresh Schema.res.mjs that drizzle-kit reads
pnpm db:generate   # emit migration SQL under ./drizzle/
pnpm db:migrate    # apply pending migrations
```

Commit the generated SQL in `./drizzle/` so collaborators replay the same changes.

## Adding a Table

```rescript
let posts = sqliteTable("posts", {
  "id": intCol("id", {"primaryKey": true, "autoIncrement": true}),
  "title": textCol("title", {"notNull": true}),
  "authorId": intCol("author_id", {"notNull": true}),
})
```

Same two-step: `pnpm db:generate` then `pnpm db:migrate`.

## Querying

The generated `src/Db.res` binds the common verbs you need. Add more via `@send`:

```rescript
@send external where: ('builder, 'expr) => 'builder = "where"
@send external eq: ('col, 'value) => 'expr = "eq"
@send external limit: ('q, int) => 'q = "limit"
```

Example — find by id:

```rescript
let findUser = async id => {
  let rows =
    await Db.db
    ->Db.select({
      "id": Schema.users["id"],
      "name": Schema.users["name"],
    })
    ->Db.from(Schema.users)
    ->Db.where(Db.eq(Schema.users["id"], id))
    ->Db.limit(1)
    ->Db.allAsync
  rows->Array.get(0)
}
```

## Pointing at Turso (Production)

Turso is a managed libsql host — the same client works with a remote URL.

1. `brew install tursodatabase/tap/turso`
2. `turso auth login`
3. `turso db create my-app`
4. `turso db tokens create my-app`
5. Export env vars and run migrations:

```bash
export DATABASE_URL="libsql://<db-name>-<org>.turso.io"
export DATABASE_AUTH_TOKEN="<token-from-step-4>"
pnpm db:migrate
```

Extend `src/Db.res` to read the auth token:

```rescript
let authToken =
  processEnv
  ->Dict.get("DATABASE_AUTH_TOKEN")
  ->Option.getOr("")

let client = createClient({"url": dbUrl, "authToken": authToken})
```

## Drizzle Config Notes

`drizzle.config.ts` reads `src/Schema.res.mjs` — the **compiled** file. If migrations come up empty after a schema edit, ensure ReScript has compiled:

```bash
pnpm res:build && pnpm db:generate
```

Or run `pnpm res:dev` in another terminal so `Schema.res.mjs` stays up to date.

## Switching Dialects

To use PostgreSQL instead, replace:

- `@libsql/client` → `postgres` (or `pg`)
- `drizzle-orm/libsql` → `drizzle-orm/postgres-js`
- `drizzle-orm/sqlite-core` → `drizzle-orm/pg-core`
- `dialect: "sqlite"` → `dialect: "postgresql"` in `drizzle.config.ts`

The route modules (`Routes/Users.res` etc.) remain unchanged because the query helpers are polymorphic.

## Common Pitfalls

- **"No migrations pending" after editing the schema** — `drizzle-kit` reads the compiled `.mjs`. Run `res:build` first.
- **Lock conflicts with watch mode** — stop `res:dev` before `db:migrate` if you hit file-lock errors on Windows.
- **Foreign key constraints** — SQLite doesn't enforce them by default. Call `PRAGMA foreign_keys = ON;` at connection time if you need enforcement.

## Related

- [Add a Hono endpoint](add-hono-endpoint.md) — using the schema from an HTTP route
- [Add a GraphQL resolver](add-graphql-resolver.md) — using the schema from a resolver
- [Project Templates](../templates/index.md) — the template catalog
