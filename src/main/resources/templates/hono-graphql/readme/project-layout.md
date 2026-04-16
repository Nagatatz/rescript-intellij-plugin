| File | Purpose |
| --- | --- |
| `src/Server.res` | Hono app that mounts yoga at /graphql |
| `src/GraphqlSchema.res` | `typeDefs` + `rootValue` consumed by yoga |
| `src/Resolvers/Users.res` | Users query + mutation resolvers |
| `src/Schema.res` | Drizzle SQLite table definitions |
| `src/Db.res` | libsql client + Drizzle helpers |
| `src/schema.graphql` | Human-authored SDL (mirror of typeDefs) |
| `drizzle.config.ts` | drizzle-kit config for migrations |
