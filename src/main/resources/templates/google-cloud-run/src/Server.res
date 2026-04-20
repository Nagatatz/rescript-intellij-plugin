// Request/response types.
type echoPayload = {message: string}

// Cloud Run sets PORT on startup — respect it, fall back to 8080 locally.
@val external processEnv: Dict.t<string> = "process.env"

let port =
  processEnv
  ->Dict.get("PORT")
  ->Option.flatMap(Int.fromString(_))
  ->Option.getOr(8080)

let app = Hono.createApp()

app->Hono.get("/", ctx => ctx->Hono.text("Cloud Run + Hono + ReScript"))

app->Hono.post("/echo", async ctx => {
  let payload: echoPayload = await ctx->Hono.req->Hono.jsonBody
  ctx->Hono.json({"echo": payload.message, "receivedAt": Date.now()->Float.toString})
})

HonoNodeServer.serve(app, {port: port})
Console.log(`Server running on http://localhost:${port->Int.toString}`)