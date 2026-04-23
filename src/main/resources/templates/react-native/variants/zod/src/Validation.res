// Runtime validation for new-todo input using zod. `App.res` calls
// `parseDraftTodo` to reject blank / too-long entries before they hit
// the list state.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

type draftTodo = {text: string}

let draftTodoSchema = z["object"]({
  "text": z["string"](),
})

let parseDraftTodo = (text: string): result<draftTodo, string> => {
  let trimmed = text->String.trim
  if trimmed == "" {
    Error("Todo cannot be empty")
  } else if trimmed->String.length > 120 {
    Error("Todo cannot exceed 120 characters")
  } else {
    let payload = Dict.fromArray([("text", JSON.Encode.string(trimmed))])->JSON.Encode.object
    try {
      let parsed: draftTodo = parse(draftTodoSchema, payload)
      Ok(parsed)
    } catch {
    | Exn.Error(err) => Error(err->Exn.message->Option.getOr("Invalid todo"))
    }
  }
}
