---
myst:
  html_meta:
    "keywords": "hono, graphql, graphql-yoga, graphiql, schema-first, sdl, drizzle, libsql, sqlite, rescript template, validation, zod, sury"
---

# Hono GraphQL

{bdg-info}`Node.js` {bdg-primary}`GraphQL` {bdg-success}`Validation` {bdg-warning}`Schema-first`

A working **GraphQL Yoga** server mounted on **Hono**, backed by SQLite via **libsql + Drizzle ORM**. The wizard generates an end-to-end Users CRUD example — SDL, resolvers, table, and a built-in GraphiQL IDE — so the moment you run `pnpm dev` you can open <http://localhost:4000/graphql> and execute a mutation.

The template is **schema-first by design**: the contract lives in `src/schema.graphql` (SDL) and is mirrored by a `typeDefs` template string in `src/GraphqlSchema.res`. Resolvers are plain ReScript functions in `src/Resolvers.res`, wired into `rootValue` by name. If you outgrow this style, the README contrasts it with code-first builders (Pothos, Nexus, gqtx) so the migration decision stays informed.

## What You Get

```
my-project/
├── rescript.json
├── package.json
├── drizzle.config.ts            # drizzle-kit config (reads Schema.res.mjs)
├── vitest.config.mjs            # registers vitest.setup.mjs
├── vitest.setup.mjs             # pins DATABASE_URL=:memory: before module load
├── src/
│   ├── ServerMain.res           # entry — calls Server.start()
│   ├── Server.res               # Hono app, mounts yoga at /graphql
│   ├── GraphqlSchema.res        # typeDefs + rootValue consumed by yoga
│   ├── Resolvers.res            # module Users { listUsers / userById / createUser / deleteUser }
│   ├── Schema.res               # Drizzle SQLite tables (users)
│   ├── Db.res                   # libsql client + Drizzle helpers
│   ├── Yoga.res                 # graphql-yoga bindings
│   ├── Hono.res                 # Hono bindings
│   ├── HonoNodeServer.res       # @hono/node-server bindings
│   ├── Validation.res           # zod or sury — selected in the wizard
│   ├── schema.graphql           # human-authored SDL (mirror of typeDefs)
│   └── __tests__/Server.test.mjs # vitest hits app.request("/health")
├── README.md                    # Try It / Schema / Database / Project Layout
├── LICENSE                      # MIT, holder = project name
├── .env.example                 # documents DATABASE_URL
├── .nvmrc                       # Node 22
├── .gitignore                   # node_modules, data/, docs/schema.md, drizzle/, .env
├── .editorconfig                # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml           # weekly npm updates
    └── workflows/ci.yml         # install + rescript build + vitest
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, license holder, and `wrangler` `name` field for any Worker derivatives you copy this into |
| **Package manager** | npm / pnpm / yarn / bun. Affects `packageManager` field, README install/run commands, and CI cache key |
| **Validation library** | `zod` ↔ `sury`. Picks `src/Validation.res` and adds the matching dependency. Both expose the same `parseCreateUserInput` signature so resolvers don't branch |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs the compiled `.res.mjs` imports | `TemplateVersions.RESCRIPT_RUNTIME` |
| `hono` | HTTP router that hosts yoga | `TemplateVersions.HONO` |
| `@hono/node-server` | Node adapter for Hono | `TemplateVersions.HONO_NODE_SERVER` |
| `graphql` | Reference implementation (peer of yoga) | `TemplateVersions.GRAPHQL` |
| `graphql-yoga` | Schema executor + GraphiQL UI | `TemplateVersions.GRAPHQL_YOGA` |
| `@libsql/client` | SQLite driver (also speaks Turso `libsql://`) | `TemplateVersions.LIBSQL_CLIENT` |
| `drizzle-orm` | Type-safe SQL builder | `TemplateVersions.DRIZZLE_ORM` |
| `zod` *or* `sury` | Mutation input validation (chosen in the wizard) | `TemplateVersions.ZOD` / `SURY` |
| `drizzle-kit` *(dev)* | Migration generator/runner | `TemplateVersions.DRIZZLE_KIT` |
| `@graphql-markdown/cli` *(dev)* | Renders SDL to Markdown for `pnpm docs:graphql` | `TemplateVersions.GRAPHQL_MARKDOWN` |
| `vitest` *(dev)* | Smoke test runner | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |

## Key Files

### `src/schema.graphql`

The human-authored source of truth. Read by `@graphql-markdown/cli` (and any external GraphQL tool — Apollo Studio, Hasura, codegen) to render `docs/schema.md`:

```graphql
type User {
  id: Int!
  name: String!
  email: String!
}

type Query {
  users: [User!]!
  user(id: Int!): User
}

type Mutation {
  createUser(name: String!, email: String!): User!
  deleteUser(id: Int!): Boolean!
}
```

### `src/GraphqlSchema.res`

Builds the runtime schema yoga executes. The `typeDefs` template string **must mirror `src/schema.graphql`** — the SDL file is what tooling reads, but yoga consumes the inline string so the bundle stays self-contained:

```rescript
let typeDefs = `
  type User { id: Int!, name: String!, email: String! }
  type Query { users: [User!]!, user(id: Int!): User }
  type Mutation {
    createUser(name: String!, email: String!): User!
    deleteUser(id: Int!): Boolean!
  }
`

let schema = Yoga.buildSchema(typeDefs)

let rootValue: {..} = Obj.magic({
  "users": Resolvers.Users.listUsers,
  "user": Resolvers.Users.userById,
  "createUser": Resolvers.Users.createUser,
  "deleteUser": Resolvers.Users.deleteUser,
})
```

`Obj.magic` is intentional — it erases the polymorphic `(parent, args, ctx, info) => 'a` resolver shape so the record can be generalised. The concrete signatures are checked at each resolver definition site in `Resolvers.res`.

### `src/Resolvers.res`

Each GraphQL type gets a nested module so `GraphqlSchema.res` can reference fields as `Resolvers.<Type>.<field>`. The shipped `Users` module shows the full CRUD surface:

```rescript
module Users = {
  let listUsers = async (_parent, _args, _ctx, _info) => ...
  let userById  = async (_parent, args, _ctx, _info) => ...
  let createUser = async (_parent, args, _ctx, _info) => {
    switch Validation.parseCreateUserInput(args->Obj.magic) {
    | Error(msg) => failwith(msg)        // surfaced as a GraphQL error
    | Ok(payload) => ...                 // Drizzle insert + returning
    }
  }
  let deleteUser = async (_parent, args, _ctx, _info) => ...
}
```

`createUser` runs every input through `Validation.parseCreateUserInput` and `failwith`s on error — graphql-yoga catches the throw and translates it into an `errors[]` entry on the response. No extra plumbing.

### `src/Server.res`

Wires Hono, mounts yoga at `/graphql` (GET + POST so GraphiQL works), and exposes `start()` separately so vitest can `import("../Server.res.mjs")` without binding a port:

```rescript
let yoga = Yoga.createYoga({
  "schema": GraphqlSchema.schema,
  "rootValue": GraphqlSchema.rootValue,
})

let app = Hono.createApp()

app->Hono.onError((err, ctx) => {
  Console.error(err)
  ctx->Hono.status(500)->Hono.json({"error": "Internal Server Error"})
})

app->Hono.get("/health", ctx => ctx->Hono.json({"status": "ok"}))
app->Hono.get("/graphql", handleYoga)
app->Hono.post("/graphql", handleYoga)

let start = () => {
  HonoNodeServer.serve({fetch: app->HonoNodeServer.honoFetch, port: 4000})
  Console.log("Server on http://localhost:4000 — GraphiQL at /graphql")
}
```

### `src/ServerMain.res`

A two-line entry point that the npm `start` / `dev` scripts run:

```rescript
Server.start()
```

Kept separate from `Server.res` so `app` stays importable from tests without side effects.

### `src/Schema.res`

Drizzle SQLite table definitions consumed by `db:generate`:

```rescript
let users = sqliteTable("users", {
  "id": intCol("id", {"primaryKey": true, "autoIncrement": true}),
  "name": textCol("name", {"notNull": true}),
  "email": textCol("email", {"notNull": true}),
})
```

### `drizzle.config.ts`

Points drizzle-kit at the **compiled** schema (`Schema.res.mjs`) and reads `DATABASE_URL` from the environment, defaulting to `file:./data/app.db`:

```ts
export default defineConfig({
  schema: "./src/Schema.res.mjs",
  out: "./drizzle",
  dialect: "sqlite",
  dbCredentials: {
    url: process.env.DATABASE_URL ?? "file:./data/app.db",
  },
});
```

### `vitest.config.mjs` + `vitest.setup.mjs`

The setup file pins `DATABASE_URL` to libsql's in-memory store before any module loads:

```js
process.env.DATABASE_URL = ":memory:";
```

This matters because importing `Server.res.mjs` transitively loads `Db → Schema → Resolvers`, and without the override the import chain would try to open `./data/app.db` during test collection.

### `src/Validation.res`

Both variants expose `parseCreateUserInput: JSON.t => result<createUserInput, string>`. The `createUser` resolver `failwith`s on `Error`, which graphql-yoga converts into a GraphQL error.

## npm Scripts

| Script | Description |
| --- | --- |
| `start` | `node src/ServerMain.res.mjs` — run the GraphQL server once |
| `dev` | `node --watch src/ServerMain.res.mjs` — restart on save |
| `test` | `vitest run` — execute the smoke suite |
| `test:coverage` | `vitest run --coverage` — same, with v8 coverage report |
| `docs:graphql` | `graphql-markdown --schema=src/schema.graphql ...` — render `docs/schema.md` from SDL |
| `db:generate` | `drizzle-kit generate` — emit migration SQL from `Schema.res` |
| `db:migrate` | `drizzle-kit migrate` — apply pending migrations to the SQLite file |
| `res:build` | `rescript` — one-shot compile |
| `res:dev` | `rescript -w` — recompile on save |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` |

## Adding a New Type and Resolver

The README walks through this in detail; the workflow boils down to six steps. Use this template's existing `Users` type as a worked example you can copy.

1. **Edit `src/schema.graphql`** with the new type and root fields:

   ```graphql
   type Post { id: Int!, title: String!, authorId: Int! }
   extend type Query { posts: [Post!]! }
   ```

2. **Mirror it in `src/GraphqlSchema.res#typeDefs`** so the runtime schema and the SDL stay in lock-step.
3. **Add a Drizzle table** in `src/Schema.res` and run `pnpm db:generate` + `pnpm db:migrate`.
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
     // existing user resolvers...
     "posts": Resolvers.Posts.listPosts,
   })
   ```

6. **Regenerate docs**: `pnpm docs:graphql` writes `docs/schema.md`.

Mutations follow the same pattern, with one extra step: add a `parsePostInput` to `Validation.res` and `failwith` on `Error` so graphql-yoga can translate the throw into a GraphQL error.

## Testing Resolvers in Isolation

`src/__tests__/Server.test.mjs` covers the HTTP boundary against the `/health` route. For resolver-level tests, hit the schema directly with `graphql/execution`:

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

For pure unit tests, mock `Db.allAsync` with `vi.mock("../Db.res.mjs", ...)` so you exercise resolver logic without spinning up SQLite. Combined with the `vitest.setup.mjs` in-memory DATABASE_URL pin, you can run the entire suite without filesystem I/O.

## Day-Two Recipes

- {doc}`../recipes/add-graphql-resolver` — add a new type + resolver, including the SDL/typeDefs sync workflow
- {doc}`../recipes/setup-drizzle` — Drizzle schema, migrations, and Turso swap
- {doc}`../recipes/add-hono-endpoint` — add a REST route alongside `/graphql` (e.g. webhook intake, health probes)
- {doc}`../recipes/add-openapi-docs` — bolt OpenAPI on top if you grow a parallel REST surface

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- **SDL and typeDefs must agree.** The SDL file (`src/schema.graphql`) is what `graphql-markdown` and external tools read; `typeDefs` in `GraphqlSchema.res` is what yoga executes. They are two copies on purpose — the bundle stays self-contained — but they are easy to drift. A pre-commit hook running `node -e "require('graphql').buildSchema(require('fs').readFileSync('src/schema.graphql','utf-8'))"` catches SDL parse errors before CI does.
- **Schema-first vs code-first.** This template is opinionated toward schema-first because it is friendlier to polyglot teams, public schemas, and external codegen. If you have many contributors and heavy schema churn, the README's *Schema* section walks through swapping `GraphqlSchema.res` for a code-first builder (Pothos, Nexus). Drizzle and Hono stay; only the schema/resolver wiring changes.
- **Resolver-level testing.** `src/__tests__/Server.test.mjs` only covers the HTTP boundary (`app.request("/health")`). The README's *Schema* section shows how to call `graphql({ schema, rootValue, source })` directly from a test — useful when you want to exercise resolver logic without spinning up SQLite. Pair it with `vi.mock("../Db.res.mjs", ...)` for pure unit tests.
- **`Server.res` never calls `serve()` at module top level** — that work lives in `start()` and is invoked from `ServerMain.res`. Tests can therefore `import("../Server.res.mjs")` without binding port 4000.
- **`failwith` in resolvers becomes a GraphQL error.** graphql-yoga catches the throw, walks the path, and emits an `errors[]` entry instead of a 500. Use `failwith` for client-visible validation; reserve `Hono.onError` for unexpected exceptions in non-yoga routes (the global handler returns a JSON 500).
- **Defaults to a local SQLite file.** `DATABASE_URL=file:./data/app.db` is the out-of-the-box wiring. Swap it for a `libsql://` URL (Turso) without touching code — the libsql client speaks both protocols. `.env.example` documents the variable.
- **Node 22 only.** `engines.node` is `>=22` and `.nvmrc` says `22`. Earlier majors are not exercised by CI.
- **Generated artifacts are gitignored:** `data/`, `drizzle/` (migration SQL), `docs/schema.md`, and `.env`.
