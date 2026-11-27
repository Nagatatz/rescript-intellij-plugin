// Runtime validation for Todo form input using sury (rescript-struct).
//
// The schema owns the trim and min/max rules, so `parseTodoInput` only
// needs to collapse the (already-trimmed) description to `None` when the
// user left it blank. Any schema failure surfaces through `S.Exn` with a
// human-readable message ready for the HTMX response.

type rawInput = {
  name: string,
  description: string,
}

let rawInputSchema: S.t<rawInput> = S.object(s => {
  name: s.field(
    "name",
    S.string
    ->S.trim
    ->S.minLength(1, ~message="Name must not be empty")
    ->S.maxLength(80, ~message="Name must be 80 characters or fewer"),
  ),
  description: s.field(
    "description",
    S.string
    ->S.trim
    ->S.maxLength(240, ~message="Description must be 240 characters or fewer"),
  ),
})

type todoInput = {
  name: string,
  description: option<string>,
}

let parseTodoInput = (~name: string, ~description: string): result<todoInput, string> => {
  let payload = {
    "name": name,
    "description": description,
  }
  try {
    let data = payload->S.parseOrThrow(~to=rawInputSchema)
    Ok({
      name: data.name,
      description: data.description === "" ? None : Some(data.description),
    })
  } catch {
  | S.Exn(err) => Error(err.message)
  }
}
