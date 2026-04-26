let app = Hono.createApp()

// CORS is not needed in dev because the client's Vite+ server proxies /api/*
// to this server (see packages/client/vite.config.mjs), keeping browser
// requests same-origin. Uncomment the block below if you remove the proxy,
// point the client at a remote server, or host the client on a separate
// origin in production. Adjust the origin list before deploying.
//
// app->Hono.use(
//   Hono.cors({
//     "origin": "http://localhost:5173",
//     "allowMethods": ["GET", "POST", "PUT", "DELETE"],
//     "credentials": true,
//   }),
// )

// Global error handler: converts uncaught exceptions into a JSON 500 response.
app->Hono.onError((err, ctx) => {
  Console.error(err)
  ctx->Hono.status(500)->Hono.json({"error": "Internal Server Error"})
})

app->Hono.get("/api/hello", ctx =>
  ctx->Hono.json({"message": "Hello from @{{projectName}}/server!"})
)

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

let start = () => {
  HonoNodeServer.serve({fetch: app->HonoNodeServer.honoFetch, port: 3000})
  Console.log("Server running on http://localhost:3000")
}