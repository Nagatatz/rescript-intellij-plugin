// Hono app wiring. Routes live under src/server/Routes/ for easy growth.
let app = Hono.createApp()

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