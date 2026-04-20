// Request/response types.
type createOrderPayload = {productId: string, quantity: int}
type createOrderResponse = {orderId: string, productId: string, quantity: int}

let app = Hono.createApp()

app->Hono.get("/", ctx => ctx->Hono.text("Lambda + Hono + ReScript"))

// Example GET with a path param
app->Hono.get("/orders/:id", ctx => {
  let id = ctx->Hono.req->Hono.paramAt("id")
  ctx->Hono.json({"orderId": id, "status": "pending"})
})

// Example POST with JSON body.
app->Hono.post("/orders", async ctx => {
  let payload: createOrderPayload = await ctx->Hono.req->Hono.jsonBody
  let response: createOrderResponse = {
    orderId: "ord_" ++ Date.now()->Float.toString,
    productId: payload.productId,
    quantity: payload.quantity,
  }
  ctx->Hono.status(201)->Hono.json(response)
})

%%raw("export const handler = HonoLambda.handle(app)")