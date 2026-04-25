### Schema-first by design

This template is **schema-first**: the GraphQL contract lives in
`src/schema.graphql` (SDL) and is mirrored by the `typeDefs` template
string in `src/GraphqlSchema.res`. Resolvers are plain ReScript
functions in `src/Resolvers.res`, wired into `rootValue` by name.

The trade-off vs **code-first** (Pothos, TypeGraphQL, Nexus, gqtx):

| | Schema-first (this template) | Code-first |
| --- | --- | --- |
| Source of truth | `.graphql` SDL file | TypeScript / ReScript builder calls |
| Tooling friction | Lower — works with any GraphQL editor, codegen, doc generator | Requires the framework's plugin / codegen step |
| Type safety end-to-end | Manual — `typeDefs` and resolvers must agree | Automatic — schema is derived from typed builders |
| Refactoring | Edit two files in sync (SDL + `typeDefs`) | One place |
| Best fit | Small/medium APIs, polyglot teams, public schemas | Large APIs with many contributors, heavy schema churn |

If you outgrow schema-first (typically: many contributors, heavy schema
churn, or you want compile-time guarantees that resolvers cover every
field), swap `GraphqlSchema.res` for a code-first builder. Drizzle and
Hono stay; only the schema/resolver wiring changes.

### Adding a new type + resolver

1. **Edit `src/schema.graphql`** with the new type and root fields:
   ```graphql
   type Post { id: Int!, title: String!, authorId: Int! }
   extend type Query { posts: [Post!]! }
   ```
2. **Mirror it in `src/GraphqlSchema.res#typeDefs`** — keep the two in
   sync (the `docs:graphql` script will warn if they diverge by
   regenerating docs from the SDL only).
3. **Add a Drizzle table** in `src/Schema.res` and run
   `{{cmdDbGenerate}}` + `{{cmdDbMigrate}}`.
4. **Add a resolver module** in `src/Resolvers.res`:
   ```rescript
   module Posts = {
     let listPosts = async (_p, _a, _c, _i) =>
       await Db.db
       ->Db.select({"id": Schema.posts["id"], "title": Schema.posts["title"], "authorId": Schema.posts["authorId"]})
       ->Db.from(Schema.posts)
       ->Db.allAsync
   }
   ```
5. **Wire it into `rootValue`** in `GraphqlSchema.res`:
   ```rescript
   let rootValue: {..} = Obj.magic({
     // ...existing user resolvers...
     "posts": Resolvers.Posts.listPosts,
   })
   ```
6. **Regenerate docs**: `{{cmdDocsGraphql}}` writes `docs/schema.md`.

### Testing resolvers in isolation

`src/__tests__/Server.test.mjs` covers the HTTP boundary. For
resolver-level tests, hit the schema directly with `graphql/execution`:

```js
import { graphql } from "graphql";
import { schema, rootValue } from "../GraphqlSchema.res.mjs";

it("listUsers returns rows from Db.allAsync", async () => {
  const result = await graphql({
    schema,
    rootValue,
    source: `query { users { id name } }`,
  });
  expect(result.errors).toBeUndefined();
  expect(Array.isArray(result.data.users)).toBe(true);
});
```

For pure unit tests, mock `Db.allAsync` with `vi.mock("../Db.res.mjs", ...)`
so you exercise resolver logic without spinning up SQLite.

### Keeping SDL and typeDefs in sync

The SDL file is the human-authored source of truth used by
`graphql-markdown` and external tooling (Apollo Studio, Hasura, etc).
`typeDefs` is the runtime input to `Yoga.buildSchema`. They must match.
A pre-commit hook running `node -e "require('graphql').buildSchema(require('fs').readFileSync('src/schema.graphql', 'utf-8'))"`
catches SDL parse errors before they reach CI.
