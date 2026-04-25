// HTTP input validation using zod. Route handlers call `parseCreateUserReq`
// to turn a JSON body into a typed record from the `@<project>/shared` package
// or a human-readable error string.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

let createUserReqSchema = z["object"]({
  "name": z["string"](),
  "email": z["string"](),
})

let parseCreateUserReq = (json: JSON.t): result<Shared.Api.createUserReq, string> =>
  try {
    let parsed: Shared.Api.createUserReq = parse(createUserReqSchema, json)
    Ok(parsed)
  } catch {
  | JsExn(err) => Error(err->JsExn.message->Option.getOr("Validation failed"))
  }
