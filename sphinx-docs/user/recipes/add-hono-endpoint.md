---
myst:
  html_meta:
    "keywords": "hono, rest api, endpoint, zod, openapi, rescript, drizzle"
---

# Add a Hono Endpoint

The Hono REST template (and Full-Stack / Monorepo variants) ship a users CRUD. This recipe walks through adding a new endpoint end-to-end: route handler, Drizzle storage, Zod validation, and OpenAPI docs.

## What You Get Out of the Box

The generated project already wires:

- `src/Server.res` — Hono app with logger, `/openapi.json`, `/docs` (Scalar UI)
- `src/Routes/Users.res` — GET/POST/PUT/DELETE for `/users`
- `src/Schema.res` — Drizzle `users` table
- `src/Db.res` — libsql client + common query helpers
- `src/ZodOpenapi.res` — Zod bindings for `@hono/zod-openapi`

Our goal: add a `/posts` endpoint that lists posts and creates one.

## Step 1 — Add the Table to `Schema.res`

```rescript
let posts = sqliteTable("posts", {
  "id": intCol("id", {"primaryKey": true, "autoIncrement": true}),
  "title": textCol("title", {"notNull": true}),
  "body": textCol("body", {"notNull": true}),
  "authorId": intCol("author_id", {"notNull": true}),
})
```

Generate and apply the migration:

```bash
pnpm db:generate
pnpm db:migrate
```

## Step 2 — Write the Route Module

Create `src/Routes/Posts.res`:

```rescript
let register = app => {
  app->Hono.get("/posts", async ctx => {
    let rows =
      await Db.db
      ->Db.select({
        "id": Schema.posts["id"],
        "title": Schema.posts["title"],
        "body": Schema.posts["body"],
        "authorId": Schema.posts["authorId"],
      })
      ->Db.from(Schema.posts)
      ->Db.allAsync
    ctx->Hono.json(rows)
  })

  app->Hono.post("/posts", async ctx => {
    let payload = await ctx->Hono.req->Hono.jsonBody
    let inserted =
      await Db.db
      ->Db.insert(Schema.posts)
      ->Db.values({
        "title": payload["title"],
        "body": payload["body"],
        "authorId": payload["authorId"],
      })
      ->Db.returning
    ctx->Hono.status(201)->Hono.json(inserted->Array.get(0))
  })
}
```

## Step 3 — Register in `Server.res`

```rescript
Routes.Users.register(app)
Routes.Posts.register(app)
```

## Step 4 — Add Zod Validation

Lift the payload shape to a shared Zod schema:

```rescript
let createPostSchema = ZodOpenapi.object({
  "title": ZodOpenapi.string(~minLength=1, ()),
  "body": ZodOpenapi.string(),
  "authorId": ZodOpenapi.number(),
})
```

Validate before hitting the database:

```rescript
app->Hono.post("/posts", async ctx => {
  let raw = await ctx->Hono.req->Hono.jsonBody
  switch createPostSchema->ZodOpenapi.safeParse(raw) {
  | {success: true, data} =>
    /* insert data */
  | {success: false, error} =>
    ctx->Hono.status(400)->Hono.json({"error": error})
  }
})
```

## Step 5 — Expose in OpenAPI

The template already wires `/openapi.json` and `/docs`. Routes registered via `app->Hono.get(...)` appear automatically. For richer metadata (tags, response schemas), switch to `@hono/zod-openapi`'s `createRoute` helper — see the Hono docs for the full pattern.

## Step 6 — Test

```bash
# Terminal 1
pnpm dev

# Terminal 2
curl -X POST http://localhost:3000/posts \
  -H 'Content-Type: application/json' \
  -d '{"title":"Hello","body":"World","authorId":1}'

curl http://localhost:3000/posts
```

Open `http://localhost:3000/docs` to exercise the new endpoint in Scalar UI.

## Common Pitfalls

- **Forgetting to run migrations** — `db:generate` creates the SQL file, `db:migrate` applies it. If your insert errors with "no such table", you missed step 2.
- **Mixing records and dicts** — Drizzle's row shape is a dict (accessed with `payload["title"]`). Convert to a ReScript record at the boundary if you want stricter typing downstream.
- **CORS during frontend development** — use the Vite+ dev proxy (Full-Stack / Monorepo templates set this up automatically) instead of configuring CORS on Hono.

## Related

- [Set up Drizzle](setup-drizzle.md) — schema, migrations, Turso
- [Add OpenAPI docs](add-openapi-docs.md) — richer schema metadata
- [Project Templates](../templates/index.md) — the template catalog
