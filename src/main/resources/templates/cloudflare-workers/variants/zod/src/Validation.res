// HTTP input validation using zod. Route handlers call `parseGreetingPayload`
// to turn a JSON body into a typed record or a human-readable error string.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

type greetingPayload = {name: string}

let greetingPayloadSchema = z["object"]({
  "name": z["string"](),
})

let parseGreetingPayload = (json: JSON.t): result<greetingPayload, string> =>
  try {
    let parsed: greetingPayload = parse(greetingPayloadSchema, json)
    Ok(parsed)
  } catch {
  | JsExn(err) => Error(err->JsExn.message->Option.getOr("Validation failed"))
  }
