// Runtime validation for Todo form input using zod.
//
// `parseTodoInput` takes the two raw form fields as strings and either returns
// a normalized record (description becomes `None` when blank) or an error
// message ready to surface back through the HTMX response.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
} = "z"

@send external parse: (zSchema, 'payload) => 'a = "parse"

type todoInput = {
  name: string,
  description: option<string>,
}

let todoInputSchema = z["object"]({
  "name": z["string"](),
  "description": z["string"](),
})

let parseTodoInput = (~name: string, ~description: string): result<todoInput, string> => {
  let trimmedName = name->String.trim
  let trimmedDescription = description->String.trim
  if trimmedName === "" {
    Error("Name must not be empty")
  } else if String.length(trimmedName) > 80 {
    Error("Name must be 80 characters or fewer")
  } else if String.length(trimmedDescription) > 240 {
    Error("Description must be 240 characters or fewer")
  } else {
    try {
      let _: {..} = parse(
        todoInputSchema,
        {"name": trimmedName, "description": trimmedDescription},
      )
      Ok({
        name: trimmedName,
        description: trimmedDescription === "" ? None : Some(trimmedDescription),
      })
    } catch {
    | Exn.Error(err) => Error(err->Exn.message->Option.getOr("Validation failed"))
    }
  }
}
