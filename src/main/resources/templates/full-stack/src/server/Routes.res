// Route registrations. New endpoint groups should become siblings of `Users`
// below so `Server.res` can call them as `Routes.<Group>.register(app)`.
module Users = {
  // Users CRUD wired against the Drizzle `users` table.
  let register = app => {
    app->Hono.get("/api/users", async ctx => {
      let rows =
        await Db.db
        ->Db.select({
          "id": Schema.users["id"],
          "name": Schema.users["name"],
          "email": Schema.users["email"],
        })
        ->Db.from(Schema.users)
        ->Db.allAsync
      ctx->Hono.json(rows)
    })

    app->Hono.post("/api/users", async ctx => {
      let raw = await ctx->Hono.req->Hono.jsonBody
      switch Validation.parseCreateUserReq(raw) {
      | Error(msg) => ctx->Hono.status(400)->Hono.json({"error": msg})
      | Ok(payload) =>
        let inserted =
          await Db.db
          ->Db.insert(Schema.users)
          ->Db.values({"name": payload.name, "email": payload.email})
          ->Db.returning
        ctx->Hono.status(201)->Hono.json(inserted->Array.get(0))
      }
    })
  }
}
