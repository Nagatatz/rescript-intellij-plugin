// Relay query module. The %relay tag is expanded by rescript-relay's PPX
// (enabled via rescript.json's `ppx-flags`) at compile time; the Relay
// compiler (`pnpm relay`) separately generates the typed artifact in
// src/client/__generated__/UsersListQuery_graphql.res.
module UsersListQuery = %relay(`
  query UsersListQuery {
    users {
      id
      name
      email
    }
  }
`)
