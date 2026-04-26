// Cloud Run sets PORT on startup — respect it, fall back to 8080 locally.
@val external processEnv: Dict.t<string> = "process.env"

let port =
  processEnv
  ->Dict.get("PORT")
  ->Option.flatMap(Int.fromString(_))
  ->Option.getOr(8080)

let app = Hono.createApp()

// Enable CORS if this service is called from a browser on another origin:
//   app->Hono.use(Hono.cors({"origin": "https://your-app.example"}))

app->Hono.get("/", ctx => ctx->Hono.text("Cloud Run + Hono + ReScript"))

app->Hono.post("/echo", async ctx => {
  let raw = await ctx->Hono.req->Hono.jsonBody
  switch Validation.parseEchoPayload(raw) {
  | Error(msg) => ctx->Hono.status(400)->Hono.json({"error": msg})
  | Ok(payload) =>
    ctx->Hono.json({"echo": payload.message, "receivedAt": Date.now()->Float.toString})
  }
})

let start = () => {
  HonoNodeServer.serve({fetch: app->HonoNodeServer.honoFetch, port})
  Console.log(`Server running on http://localhost:${port->Int.toString}`)
}