// Hono app wiring. Routes live under src/server/Routes/ for easy growth.
let app = Hono.createApp()

// CORS is not needed in dev because Vite proxies /api/* to this server
// (see vite.config.mjs), keeping browser requests same-origin. Uncomment the
// block below if you remove the proxy, point the client at a remote server,
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
Routes.Users.register(app)

let start = () => {
  HonoNodeServer.serve(app, {port: 3000})
  Console.log("Server on http://localhost:3000 — try /api/health")
}