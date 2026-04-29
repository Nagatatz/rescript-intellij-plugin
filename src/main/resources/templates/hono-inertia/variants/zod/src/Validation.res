// HTTP input validation using zod. Inertia route handlers call
// `parseGreetForm` to turn a form-style JSON body into a typed record or a
// human-readable error string suitable for re-rendering as a flash prop.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

type greetForm = {name: string}

let greetFormSchema = z["object"]({
  "name": z["string"](),
})

let parseGreetForm = (json: JSON.t): result<greetForm, string> =>
  try {
    let parsed: greetForm = parse(greetFormSchema, json)
    Ok(parsed)
  } catch {
  | JsExn(err) => Error(err->JsExn.message->Option.getOr("Validation failed"))
  }
