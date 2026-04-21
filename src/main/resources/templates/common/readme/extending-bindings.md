This template ships minimal handwritten bindings to the external JavaScript
libraries it depends on — enough to run, but not exhaustive. When you need more
of a library than what the provided `.res` files cover, add externals in the same
file or in a new module. The patterns below cover the cases you are most likely
to hit first.

### Binding attributes at a glance

| Attribute | Use for |
| --- | --- |
| `@module("pkg")` | Import a value from a JS/npm module |
| `@val` | Reference a global value (`fetch`, `process.argv`, `console`, ...) |
| `@new` | Call a class constructor (`new Foo(...)`) |
| `@send` | Invoke a method on a receiver (first parameter is the receiver) |
| `@get` / `@set` | Read / write an object property |
| `@scope("NativeModules")` | Reach into a nested static namespace |

See the [ReScript binding reference](https://rescript-lang.org/docs/manual/latest/bind-to-js-function)
for the full list and edge cases.

### Pattern: typed fetch wrapper (frontend)

The stock `fetch` binding in `src/Api.res` / `src/Fetch.res` /
`src/client/ApiClient.res` uses polymorphic `'a` for request options and
response bodies. That keeps the seed code flexible but loses type safety. Once a
payload shape stabilizes, replace the polymorphic version with a concrete record:

```rescript
type user = {id: int, name: string, email: string}

@send external jsonAs: response => promise<user> = "json"

let getUser = async id => {
  let response = await fetch(`/api/users/${Int.toString(id)}`, {"method": "GET"})
  await response->jsonAs
}
```

### Pattern: adding a Hono middleware (backend)

`hono/cors` is already pre-bound as `Hono.cors` in the shared `Hono.res` of this
template. For other middlewares (`hono/jwt`, `hono/basic-auth`, `hono/cache`,
`hono/secure-headers`, ...), bind the factory yourself and pass the result to
`Hono.use`:

```rescript
@module("hono/jwt") external jwt: {"secret": string} => Hono.middleware = "jwt"

let app = Hono.createApp()
app->Hono.use(jwt({"secret": "change-me-in-prod"}))
```

The same shape works for any third-party middleware: bind the factory with
`@module`, describe its argument, and hand the return value to `Hono.use` or
`Hono.usePath`.

### Pattern: filtering with drizzle-orm

The stock `src/Db.res` already binds the drizzle-orm essentials — comparison
operators (`eq` / `ne` / `gt` / `gte` / `lt` / `lte` / `inArray` / `like` /
`isNull` / ...), boolean combinators (`and` / `or` / `not`), query chain
builders (`where` / `orderBy` / `limit` / `offset` / `groupBy`), mutation
entry points (`update` / `set` / `deleteFrom`), and ordering helpers
(`asc` / `desc`) — so everyday `WHERE` / `ORDER BY` / `UPDATE` / `DELETE`
queries stay in ReScript:

```rescript
let userById = async id =>
  await Db.db
  ->Db.select({"id": Schema.users["id"], "name": Schema.users["name"]})
  ->Db.from(Schema.users)
  ->Db.where(Db.eq(Schema.users["id"], id))
  ->Db.allAsync
  ->Promise.then(rows => Array.get(rows, 0))

let recentUsers = async () =>
  await Db.db
  ->Db.select({"id": Schema.users["id"], "name": Schema.users["name"]})
  ->Db.from(Schema.users)
  ->Db.orderBy(Db.desc(Schema.users["id"]))
  ->Db.limit(10)
  ->Db.allAsync
```

If you need an operator that isn't in `Db.res` yet, bind it with the same
`@module("drizzle-orm")` / `@send` pattern used inside that file.

### If you need fuller type safety

drizzle-orm keeps the ReScript side polymorphic (`'expr` / `'row`), so
mistyped column references or result shapes won't be caught until runtime.
When that becomes painful, two ReScript-native options are worth a look:

- [`pgtyped-rescript`](https://github.com/zth/pgtyped-rescript) — write raw
  PostgreSQL in `%sql.one` / `%sql.many` tags; argument and row types are
  generated from a running database. **PostgreSQL only.** The README notes
  the API may still change.
- [`rescript-edgedb`](https://github.com/zth/rescript-edgedb) — embed EdgeQL
  in ReScript with full type safety and a dedicated VSCode extension.
  Requires EdgeDB (now Gel) as the backend, so it is a DB-platform swap
  rather than a drop-in replacement for libsql/Postgres.

Both come with a codegen step and are maintained by a single community
author; treat them as signposts, not drop-in substitutes.

### Community binding packages

When handwriting externals becomes tedious, check whether a community package
already covers the surface you need:

- [`@rescript/react-router`](https://www.npmjs.com/package/@rescript/react-router)
  — React Router bindings (stable).
- [`@rescript/webapi`](https://www.npmjs.com/package/@rescript/webapi) — official
  WebAPI bindings. **⚠ Experimental (0.1.x); the API will change before 1.0.**
- [`rescript-schema`](https://www.npmjs.com/package/rescript-schema) — typed
  runtime validation with zero-cost encode/decode.
- [`graphql-ppx`](https://github.com/reasonml-community/graphql-ppx) — compile-time
  typed GraphQL queries (useful alongside the Hono GraphQL template).
- [`rescript-relay`](https://github.com/zth/rescript-relay) — official Relay
  bindings for ReScript. Generates typed query / fragment / mutation artifacts
  from `%relay()` tags via a codegen step. Used by the FULL_STACK GraphQL
  variant for end-to-end typed queries.

Full package index: <https://rescript-lang.org/packages/>.

### Further reading

- [ReScript manual — binding to JS](https://rescript-lang.org/docs/manual/latest/bind-to-js-function)
- [ReScript forum](https://forum.rescript-lang.org/) for questions and
  discussion.
