// HTTP input validation using sury (rescript-struct). Route handlers call
// `parseCreateOrderPayload` to turn a JSON body into a typed record or a
// human-readable error string.
type createOrderPayload = {productId: string, quantity: int}

let createOrderPayloadSchema: S.t<createOrderPayload> = S.object(s => {
  productId: s.field("productId", S.string),
  quantity: s.field("quantity", S.int),
})

let parseCreateOrderPayload = (json: JSON.t): result<createOrderPayload, string> =>
  try Ok(json->S.parseOrThrow(createOrderPayloadSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
