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

let parseCreateUserReq = (json: JSON.t): result<Api.createUserReq, string> =>
  try {
    let parsed: Api.createUserReq = parse(createUserReqSchema, json)
    Ok(parsed)
  } catch {
  | Exn.Error(err) => Error(err->Exn.message->Option.getOr("Validation failed"))
  }
