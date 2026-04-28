// Hono app + Inertia middleware wiring. The HTTP server itself is started
// from `ServerMain.res` so test code can `import` this module without binding
// a port.
//
// Middleware order matters: register `HonoInertia.inertia()` *before* any route
// that calls `c->HonoInertia.render(...)`. Otherwise `render` falls back to
// Hono's HTML renderer and Inertia visits return raw HTML instead of JSON.
let app = Hono.createApp()
app->Hono.use(Logger.logger())
app->Hono.use(HonoInertia.inertia())

// Global error handler: converts uncaught exceptions into a JSON 500 response.
app->Hono.onError((err, ctx) => {
  Console.error(err)
  ctx->Hono.status(500)->Hono.json({"error": "Internal Server Error"})
})

app->Hono.get("/health", ctx => ctx->Hono.json({"status": "ok"}))
Routes.Pages.register(app)

let start = () => {
  HonoNodeServer.serve({fetch: app->HonoNodeServer.honoFetch, port: 3000})
  Console.log("Server on http://localhost:3000 — Inertia pages on /, /about")
}
