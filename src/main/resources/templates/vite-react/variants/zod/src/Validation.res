// Form input validation for the greet form using zod. `App.res` calls
// `parseGreetForm` on submit to catch empty / oversized names before they
// hit the network.
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

let parseGreetForm = (name: string): result<greetForm, string> => {
  let trimmed = name->String.trim
  if trimmed == "" {
    Error("Name cannot be empty")
  } else if trimmed->String.length > 80 {
    Error("Name cannot exceed 80 characters")
  } else {
    let payload = Dict.fromArray([("name", JSON.Encode.string(trimmed))])->JSON.Encode.object
    try {
      let parsed: greetForm = parse(greetFormSchema, payload)
      Ok(parsed)
    } catch {
    | JsExn(err) => Error(err->JsExn.message->Option.getOr("Invalid form input"))
    }
  }
}
