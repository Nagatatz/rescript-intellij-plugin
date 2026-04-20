// HTTP input validation using zod. GraphQL mutation resolvers (and any REST
// routes added later) call `parseCreateUserInput` to turn a JSON-shaped value
// into a typed record or a human-readable error string.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
  "number": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

type createUserInput = {name: string, email: string}

let createUserInputSchema = z["object"]({
  "name": z["string"](),
  "email": z["string"](),
})

let parseCreateUserInput = (json: JSON.t): result<createUserInput, string> =>
  try {
    let parsed: createUserInput = parse(createUserInputSchema, json)
    Ok(parsed)
  } catch {
  | Exn.Error(err) => Error(err->Exn.message->Option.getOr("Validation failed"))
  }
