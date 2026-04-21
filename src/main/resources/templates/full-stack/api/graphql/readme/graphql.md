### Extending the schema

1. Edit `src/server/schema.graphql` (SDL). Keep it in sync with the `typeDefs`
   string in `src/server/GraphqlSchema.res` — both are read at different times
   (SDL by the Relay compiler, inlined string by graphql-yoga at runtime).
2. Add the matching resolver in `src/server/Resolvers.res` under the appropriate
   type module (for example, a new `type Post` would live under `module Posts`).
3. Wire the resolver into the `rootValue` map in `src/server/GraphqlSchema.res`.

### Adding a client query

1. Create a new module under `src/client/` (or reuse an existing one):

   ```rescript
   module UserByIdQuery = %relay(`
     query UserByIdQuery($id: Int!) {
       user(id: $id) { id name email }
     }
   `)
   ```

2. Run `pnpm relay` (or keep `pnpm relay:watch` running during development).
   The compiler reads the `%relay()` tag and emits
   `src/client/__generated__/UserByIdQuery_graphql.res` with the typed
   variables and response records.
3. Call it with `UserByIdQuery.UserByIdQuery.use(~variables={id: 1}, ())` in
   a component; the surrounding `React.Suspense` boundary shows the fallback
   during fetch.

### Mutations

```rescript
module CreateUserMutation = %relay(`
  mutation CreateUserMutation($name: String!, $email: String!) {
    createUser(name: $name, email: $email) { id name email }
  }
`)

let (commit, isInFlight) = CreateUserMutation.CreateUserMutation.use()
commit(~variables={name: "Alice", email: "alice@example.com"}, ())
```

### Troubleshooting

- **`%relay()` tag not expanding** — check that `rescript.json` has
  `"rescript-relay"` in `bs-dependencies` and `"rescript-relay/ppx"` in
  `ppx-flags`. Rerun `pnpm res:build` after changes.
- **Relay compiler says schema file missing** — `relay.config.js` points at
  `./src/server/schema.graphql`. Verify it exists; if you moved the server
  layout, update the config.
- **Generated files show stale types** — delete `src/client/__generated__/`
  and rerun `pnpm relay`.
