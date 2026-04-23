// Runtime validation for new-todo input using sury. `App.res` calls
// `parseDraftTodo` to reject blank / too-long entries before they hit
// the list state.
type draftTodo = {text: string}

let draftTodoSchema: S.t<draftTodo> = S.object(s => {
  text: s.field("text", S.string),
})

let parseDraftTodo = (text: string): result<draftTodo, string> => {
  let trimmed = text->String.trim
  if trimmed == "" {
    Error("Todo cannot be empty")
  } else if trimmed->String.length > 120 {
    Error("Todo cannot exceed 120 characters")
  } else {
    let payload = Dict.fromArray([("text", JSON.Encode.string(trimmed))])->JSON.Encode.object
    try Ok(payload->S.parseOrThrow(draftTodoSchema)) catch {
    | S.Error(err) => Error(err.message)
    }
  }
}
