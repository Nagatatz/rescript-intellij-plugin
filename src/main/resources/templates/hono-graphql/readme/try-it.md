After `pnpm dev`, visit <http://localhost:4000/graphql> for the built-in
GraphiQL IDE. Try:

```graphql
mutation {
  createUser(name: "Ada", email: "ada@example.com") { id name }
}

query { users { id name email } }
```
