// Mount yoga under /graphql. GraphiQL ships automatically at the same URL.
let yoga = Yoga.createYoga({
  "schema": GraphqlSchema.schema,
  "rootValue": GraphqlSchema.rootValue,
})

let app = Hono.createApp()

// Enable CORS if GraphiQL or your client is served from a different origin:
//   app->Hono.use(Hono.cors({"origin": "https://your-app.example"}))

// Global error handler: converts uncaught exceptions into a JSON 500 response.
app->Hono.onError((err, ctx) => {
  Console.error(err)
  ctx->Hono.status(500)->Hono.json({"error": "Internal Server Error"})
})

app->Hono.get("/", ctx => ctx->Hono.text("Hono GraphQL + ReScript — open /graphql"))
app->Hono.get("/health", ctx => ctx->Hono.json({"status": "ok"}))

// Forward /graphql (GET + POST) to yoga's Fetch-compatible handler.
let handleYoga = ctx => {
  let request = ctx->Hono.req
  yoga(request)
}
app->Hono.get("/graphql", handleYoga)
app->Hono.post("/graphql", handleYoga)

let start = () => {
  HonoNodeServer.serve({fetch: app->HonoNodeServer.honoFetch, port: 4000})
  Console.log("Server on http://localhost:4000 — GraphiQL at /graphql")
}