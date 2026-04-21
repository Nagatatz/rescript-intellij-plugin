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

The stock `src/Db.res` does not bind `eq` / `and` / `or`, so `SELECT ... WHERE`
queries need one more external:

```rescript
@module("drizzle-orm") external eq: ('col, 'val) => 'expr = "eq"
@send external where: ('q, 'expr) => 'q = "where"

let userById = async id =>
  await Db.db
  ->Db.select({"id": Schema.users["id"], "name": Schema.users["name"]})
  ->Db.from(Schema.users)
  ->where(eq(Schema.users["id"], id))
  ->Db.allAsync
```

`and`, `or`, `inArray`, and the rest of `drizzle-orm`'s expression helpers follow
the same shape.

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

Full package index: <https://rescript-lang.org/packages/>.

### Further reading

- [ReScript manual — binding to JS](https://rescript-lang.org/docs/manual/latest/bind-to-js-function)
- [ReScript forum](https://forum.rescript-lang.org/) for questions and
  discussion.
