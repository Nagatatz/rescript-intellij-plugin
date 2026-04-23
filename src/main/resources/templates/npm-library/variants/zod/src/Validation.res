// Runtime validation for public API inputs using zod. `Index.greetChecked` calls
// `parseGreetInput` so TS/JS consumers that pass untyped payloads get an error
// result instead of a TypeError deep inside the library.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

@genType
type greetInput = {name: string}

let greetInputSchema = z["object"]({
  "name": z["string"](),
})

@genType
let parseGreetInput = (json: JSON.t): result<greetInput, string> =>
  try {
    let parsed: greetInput = parse(greetInputSchema, json)
    Ok(parsed)
  } catch {
  | Exn.Error(err) => Error(err->Exn.message->Option.getOr("Invalid greet input"))
  }
