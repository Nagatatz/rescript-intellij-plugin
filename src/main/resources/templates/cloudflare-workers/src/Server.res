// env is the bindings object Workers injects. We narrow it for our KV binding.
type env = {"GREETINGS": Kv.namespace}

let app = Hono.createApp()

// Enable CORS if this Worker is called from a browser on another origin:
//   app->Hono.use(Hono.cors({"origin": "https://your-app.example"}))

app->Hono.get("/", ctx => ctx->Hono.text("Workers + Hono + ReScript"))

app->Hono.post("/greetings", async ctx => {
  let env: env = %raw("ctx.env")
  let raw = await ctx->Hono.req->Hono.jsonBody
  switch Validation.parseGreetingPayload(raw) {
  | Error(msg) => ctx->Hono.status(400)->Hono.json({"error": msg})
  | Ok(payload) =>
    await env["GREETINGS"]->Kv.put(payload.name, Date.now()->Float.toString)
    ctx->Hono.status(201)->Hono.json({"ok": true, "name": payload.name})
  }
})

app->Hono.get("/greetings", async ctx => {
  let env: env = %raw("ctx.env")
  let result = await env["GREETINGS"]->Kv.list
  ctx->Hono.json({"names": result.keys->Array.map(k => k["name"])})
})

%%raw("export default app")