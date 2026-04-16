The GraphQL schema is defined once in `src/schema.graphql` and mirrored by the
`typeDefs` template string in `src/GraphqlSchema.res`. Update both when you add
new types, then regenerate human docs:

```bash
pnpm docs:graphql
```
