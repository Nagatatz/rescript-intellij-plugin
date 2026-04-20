// HTTP input validation using zod. Route handlers call `parseCreateOrderPayload`
// to turn a JSON body into a typed record or a human-readable error string.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
  "number": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

type createOrderPayload = {productId: string, quantity: int}

let createOrderPayloadSchema = z["object"]({
  "productId": z["string"](),
  "quantity": z["number"](),
})

let parseCreateOrderPayload = (json: JSON.t): result<createOrderPayload, string> =>
  try {
    let parsed: createOrderPayload = parse(createOrderPayloadSchema, json)
    Ok(parsed)
  } catch {
  | Exn.Error(err) => Error(err->Exn.message->Option.getOr("Validation failed"))
  }
