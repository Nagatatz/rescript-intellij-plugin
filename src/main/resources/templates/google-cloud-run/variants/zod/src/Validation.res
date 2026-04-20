// HTTP input validation using zod. Route handlers call `parseEchoPayload`
// to turn a JSON body into a typed record or a human-readable error string.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

type echoPayload = {message: string}

let echoPayloadSchema = z["object"]({
  "message": z["string"](),
})

let parseEchoPayload = (json: JSON.t): result<echoPayload, string> =>
  try {
    let parsed: echoPayload = parse(echoPayloadSchema, json)
    Ok(parsed)
  } catch {
  | Exn.Error(err) => Error(err->Exn.message->Option.getOr("Validation failed"))
  }
