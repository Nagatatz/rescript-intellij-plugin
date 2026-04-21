// Builds a graphql-js schema object from the SDL string and bundles the resolvers
// (matching type.field => function shape) consumed by graphql-yoga.

// We inline the SDL rather than reading the file at startup so the bundle stays
// self-contained; keep this in sync with src/server/schema.graphql (and rerun
// relay-compiler after any edit).
let typeDefs = `
  type User { id: Int!, name: String!, email: String! }
  type Query {
    users: [User!]!
    user(id: Int!): User
  }
  type Mutation {
    createUser(name: String!, email: String!): User!
    deleteUser(id: Int!): Boolean!
  }
`

let schema = Yoga.buildSchema(typeDefs)

// Erase the polymorphic resolver signatures so the record type can be generalized.
// graphql-yoga invokes each function with (parent, args, ctx, info); the concrete
// types live in `Resolvers.Users` and are checked where each field is defined.
let rootValue: {..} = Obj.magic({
  "users": Resolvers.Users.listUsers,
  "user": Resolvers.Users.userById,
  "createUser": Resolvers.Users.createUser,
  "deleteUser": Resolvers.Users.deleteUser,
})
