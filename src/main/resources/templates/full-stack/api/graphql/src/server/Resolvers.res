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

  let userById = async (_parent, args, _ctx, _info) => {
    let rows =
      await Db.db
      ->Db.select({
        "id": Schema.users["id"],
        "name": Schema.users["name"],
        "email": Schema.users["email"],
      })
      ->Db.from(Schema.users)
      ->Db.where(Db.eq(Schema.users["id"], args["id"]))
      ->Db.allAsync
    rows->Array.get(0)
  }

  let createUser = async (_parent, args, _ctx, _info) => {
    switch Validation.parseCreateUserReq(args->Obj.magic) {
    | Error(msg) => failwith(msg)
    | Ok(payload) =>
      let inserted =
        await Db.db
        ->Db.insert(Schema.users)
        ->Db.values({"name": payload.name, "email": payload.email})
        ->Db.returning
      inserted->Array.get(0)
    }
  }

  let deleteUser = async (_parent, args, _ctx, _info) => {
    let deleted =
      await Db.db
      ->Db.deleteFrom(Schema.users)
      ->Db.where(Db.eq(Schema.users["id"], args["id"]))
      ->Db.returning
    deleted->Array.length > 0
  }
}
