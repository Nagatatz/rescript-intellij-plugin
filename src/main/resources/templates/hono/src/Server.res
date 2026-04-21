// Top-level Hono app with logger middleware, Users routes, OpenAPI spec, and Scalar UI.
let app = Hono.createApp()
app->Hono.use(Logger.logger())

// Enable CORS if this service is called from a browser:
//   app->Hono.use(Hono.cors({"origin": "https://your-app.example"}))

// Global error handler: converts uncaught exceptions into a JSON 500 response.
app->Hono.onError((err, ctx) => {
  Console.error(err)
  ctx->Hono.status(500)->Hono.json({"error": "Internal Server Error"})
})

app->Hono.get("/", ctx => ctx->Hono.text("Hono + SQLite + ReScript"))
app->Hono.get("/health", ctx => ctx->Hono.json({"status": "ok"}))
Routes.Users.register(app)

// Serve the OpenAPI spec + Scalar UI. In a production setup, guard behind auth.
app->Hono.get("/openapi.json", ctx =>
  ctx->Hono.json({
    "openapi": "3.1.0",
    "info": {"title": "API", "version": "0.1.0"},
    "paths": Dict.make(),
  })
)
app->Hono.get("/docs", Scalar.apiReference({"spec": {"url": "/openapi.json"}}))

HonoNodeServer.serve(app, {port: 3000})
Console.log("Server on http://localhost:3000 — docs at /docs")