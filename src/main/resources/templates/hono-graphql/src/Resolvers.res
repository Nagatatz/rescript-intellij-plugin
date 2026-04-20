// GraphQL resolvers. Group each type's resolvers into a nested module so
// `GraphqlSchema.res` can reference them as `Resolvers.<Type>.<field>`.
module Users = {
  // Each field gets a resolver function matching the signature graphql-yoga
  // expects: (parent, args, ctx, info) => value | Promise<value>.
  let listUsers = async (_parent, _args, _ctx, _info) => {
    await Db.db
    ->Db.select({
      "id": Schema.users["id"],
      "name": Schema.users["name"],
      "email": Schema.users["email"],
    })
    ->Db.from(Schema.users)
    ->Db.allAsync
  }

  let userById = async (_parent, _args, _ctx, _info) => {
    // TODO: implement with drizzle-orm `eq` filter once the binding is added.
    let users = await listUsers(_parent, _args, _ctx, _info)
    users->Array.get(0)
  }

  let createUser = async (_parent, args, _ctx, _info) => {
    let inserted =
      await Db.db
      ->Db.insert(Schema.users)
      ->Db.values({"name": args["name"], "email": args["email"]})
      ->Db.returning
    inserted->Array.get(0)
  }

  let deleteUser = async (_parent, _args, _ctx, _info) => {
    // Placeholder: wire to drizzle `delete(...).where(eq(users.id, args.id))`.
    true
  }
}
