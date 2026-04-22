// Runtime validation for Todo form input using sury (rescript-struct).
//
// `parseTodoInput` takes the two raw form fields as strings and either returns
// a normalized record (description becomes `None` when blank) or an error
// message ready to surface back through the HTMX response.
type todoInput = {
  name: string,
  description: option<string>,
}

type rawInput = {
  name: string,
  description: string,
}

let rawInputSchema: S.t<rawInput> = S.object(s => {
  name: s.field("name", S.string),
  description: s.field("description", S.string),
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
    let payload = {
      "name": trimmedName,
      "description": trimmedDescription,
    }
    try {
      let _: rawInput = payload->Obj.magic->S.parseOrThrow(rawInputSchema)
      Ok({
        name: trimmedName,
        description: trimmedDescription === "" ? None : Some(trimmedDescription),
      })
    } catch {
    | S.Error(err) => Error(err.message)
    }
  }
}
