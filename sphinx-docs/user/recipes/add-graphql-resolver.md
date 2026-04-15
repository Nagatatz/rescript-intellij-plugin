---
myst:
  html_meta:
    "keywords": "graphql, yoga, resolver, schema, rescript, hono, drizzle, graphiql"
---

# Add a GraphQL Resolver

The Hono + GraphQL template ships `graphql-yoga` mounted on Hono at `/graphql`. It includes GraphiQL (at the same URL) and a users type backed by Drizzle/SQLite. This recipe extends it with a `posts` type.

## Generated Layout

- `src/schema.graphql` — human-authored SDL (mirrored in typeDefs)
- `src/GraphqlSchema.res` — inlined `typeDefs` + `rootValue` consumed by yoga
- `src/Resolvers/Users.res` — existing users query/mutation resolvers
- `src/Schema.res` — Drizzle users table
- `src/Server.res` — Hono app mounting yoga

## Step 1 — Extend the SDL

Edit `src/schema.graphql` to add the new type:

```graphql
type Post {
  id: Int!
  title: String!
  body: String!
}

extend type Query {
  posts: [Post!]!
  post(id: Int!): Post
}

extend type Mutation {
  createPost(title: String!, body: String!): Post!
}
```

Mirror the change in `src/GraphqlSchema.res`'s `typeDefs` template string — yoga reads that inline SDL at startup.

## Step 2 — Add the Drizzle Table

In `src/Schema.res`:

```rescript
let posts = sqliteTable("posts", {
  "id": intCol("id", {"primaryKey": true, "autoIncrement": true}),
  "title": textCol("title", {"notNull": true}),
  "body": textCol("body", {"notNull": true}),
})
```

```bash
pnpm db:generate
pnpm db:migrate
```

## Step 3 — Write the Resolvers

Create `src/Resolvers/Posts.res`:

```rescript
let listPosts = async (_parent, _args, _ctx, _info) => {
  await Db.db
  ->Db.select({
    "id": Schema.posts["id"],
    "title": Schema.posts["title"],
    "body": Schema.posts["body"],
  })
  ->Db.from(Schema.posts)
  ->Db.allAsync
}

let createPost = async (_parent, args, _ctx, _info) => {
  let inserted =
    await Db.db
    ->Db.insert(Schema.posts)
    ->Db.values({"title": args["title"], "body": args["body"]})
    ->Db.returning
  inserted->Array.get(0)
}
```

## Step 4 — Wire into `rootValue`

In `src/GraphqlSchema.res`:

```rescript
let rootValue = {
  "users": Resolvers.Users.listUsers,
  "user": Resolvers.Users.userById,
  "createUser": Resolvers.Users.createUser,
  "deleteUser": Resolvers.Users.deleteUser,
  "posts": Resolvers.Posts.listPosts,
  "createPost": Resolvers.Posts.createPost,
}
```

## Step 5 — Try It

```bash
pnpm dev
```

Open `http://localhost:4000/graphql` and run:

```graphql
mutation {
  createPost(title: "Hello", body: "World") { id title }
}

query { posts { id title body } }
```

## Step 6 — Regenerate Docs

```bash
pnpm docs:graphql
```

This runs `graphql-markdown` against `src/schema.graphql` and writes `docs/schema.md` (grouped by kind). Commit that file so documentation tracks the schema.

## Patterns for Real Resolvers

- **Field-level resolvers** — put post author resolution inside `Post.author` rather than refetching users in every query
- **Loaders** — use `dataloader` to batch `Post.author` calls; bind with `@module("dataloader")`
- **Context** — forward auth / db handles through yoga's `context` option instead of module-level `Db.db`

## Common Pitfalls

- **Schema drift** — `src/schema.graphql` and the inline `typeDefs` string must agree. Using a single file via `readFileSync` avoids this but requires filesystem access at startup; the template inlines to stay self-contained.
- **Mutation returning null** — check that `returning` is called on the Drizzle builder; `insert(...).values(...)` alone doesn't return rows.

## Related

- [Add a Hono endpoint](add-hono-endpoint.md) — the REST counterpart
- [Set up Drizzle](setup-drizzle.md) — schema migrations
- [Project Templates](../templates/index.md) — the template catalog
