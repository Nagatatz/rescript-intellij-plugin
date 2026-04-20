// HTTP input validation for POST /api/greet using zod.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

type greetInput = {name: string}

let greetInputSchema = z["object"]({
  "name": z["string"](),
})

let parseGreetInput = (json: JSON.t): result<greetInput, string> =>
  try {
    let parsed: greetInput = parse(greetInputSchema, json)
    Ok(parsed)
  } catch {
  | Exn.Error(err) => Error(err->Exn.message->Option.getOr("Validation failed"))
  }
