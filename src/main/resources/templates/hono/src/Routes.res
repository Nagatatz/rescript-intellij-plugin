// Route registrations. New endpoint groups should become siblings of `Users`
// below so `Server.res` can call them as `Routes.<Group>.register(app)`.
module Users = {
  // Users CRUD routes. Wired into the OpenAPI app so they appear in /docs.
  let register = (app: Hono.app) => {
    app->Hono.get("/users", async ctx => {
      let rows =
        await Db.db
        ->Db.select({"id": Schema.users["id"]})
        ->Db.from(Schema.users)
        ->Db.allAsync
      ctx->Hono.json(rows)
    })

    app->Hono.get("/users/:id", ctx => {
      let id = ctx->Hono.req->Hono.paramAt("id")
      ctx->Hono.json({"id": id, "placeholder": true})
    })

    app->Hono.post("/users", async ctx => {
      let raw = await ctx->Hono.req->Hono.jsonBody
      switch Validation.parseCreateUserInput(raw) {
      | Error(msg) => ctx->Hono.status(400)->Hono.json({"error": msg})
      | Ok(payload) =>
        let inserted =
          await Db.db
          ->Db.insert(Schema.users)
          ->Db.values({"name": payload.name, "email": payload.email})
          ->Db.returning
        ctx->Hono.status(201)->Hono.json(inserted)
      }
    })

    app->Hono.put("/users/:id", async ctx => {
      let id = ctx->Hono.req->Hono.paramAt("id")
      let _ = await ctx->Hono.req->Hono.jsonBody
      ctx->Hono.json({"id": id, "updated": true})
    })

    app->Hono.deleteRoute("/users/:id", ctx => {
      let id = ctx->Hono.req->Hono.paramAt("id")
      ctx->Hono.status(204)->Hono.json({"id": id})
    })
  }
}
