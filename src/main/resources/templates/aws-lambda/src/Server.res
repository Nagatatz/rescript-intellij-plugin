// Response type produced by POST /orders.
type createOrderResponse = {orderId: string, productId: string, quantity: int}

let app = Hono.createApp()

// Enable CORS if API Gateway fronts this Lambda for a browser client:
//   app->Hono.use(Hono.cors({"origin": "https://your-app.example"}))

app->Hono.get("/", ctx => ctx->Hono.text("Lambda + Hono + ReScript"))

// Example GET with a path param
app->Hono.get("/orders/:id", ctx => {
  let id = ctx->Hono.req->Hono.paramAt("id")
  ctx->Hono.json({"orderId": id, "status": "pending"})
})

// Example POST with JSON body validated by Validation.res (zod or sury variant).
app->Hono.post("/orders", async ctx => {
  let raw = await ctx->Hono.req->Hono.jsonBody
  switch Validation.parseCreateOrderPayload(raw) {
  | Error(msg) => ctx->Hono.status(400)->Hono.json({"error": msg})
  | Ok(payload) =>
    let response: createOrderResponse = {
      orderId: "ord_" ++ Date.now()->Float.toString,
      productId: payload.productId,
      quantity: payload.quantity,
    }
    ctx->Hono.status(201)->Hono.json(response)
  }
})

// Top-level lets become named ESM exports automatically — equivalent to
// `export const handler = ...` on the JS side, which is what AWS Lambda
// looks up at invocation time. Doing it via a real binding (rather than
// `%%raw`) ensures the compiler emits the `HonoLambda` import.
let handler = HonoLambda.handle(app)
