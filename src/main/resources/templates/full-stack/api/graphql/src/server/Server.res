// Hono app wiring for the GraphQL variant. graphql-yoga handles /graphql;
// Hono serves health endpoints and is the extension point for any REST surface
// you want to keep alongside GraphQL.
let yoga = Yoga.createYoga({
  "schema": GraphqlSchema.schema,
  "rootValue": GraphqlSchema.rootValue,
})

let app = Hono.createApp()

// CORS is not needed in dev because Vite proxies /graphql and /api/* to this
// server (see vite.config.mjs), keeping browser requests same-origin. Uncomment
// the block below if you remove the proxy, point the client at a remote server,
// or host the client on a separate origin in production. Adjust the origin
// list before deploying.
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

app->Hono.get("/api/health", ctx => ctx->Hono.json({"status": "ok"}))

// Forward /graphql (GET + POST) to yoga's Fetch-compatible handler. GraphiQL
// ships automatically at the same URL.
let handleYoga = ctx => {
  let request = ctx->Hono.req
  yoga(request)
}
app->Hono.get("/graphql", handleYoga)
app->Hono.post("/graphql", handleYoga)

let start = () => {
  HonoNodeServer.serve({fetch: app->HonoNodeServer.honoFetch, port: 3000})
  Console.log("Server on http://localhost:3000 — GraphiQL at /graphql, health at /api/health")
}
