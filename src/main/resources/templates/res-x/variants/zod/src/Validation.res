// Runtime validation for Todo form input using zod v4.
//
// The schema owns the length and trim rules, so `parseTodoInput` only needs
// to decide whether the (already-trimmed) description should collapse to
// `None` when blank. zod's `safeParse` returns a tagged result; we surface
// the first issue's message back through the HTMX response.
type zSchema

@module("zod")
external z: {
  "string": unit => zSchema,
  "object": Dict.t<zSchema> => zSchema,
} = "z"

@send external trim: zSchema => zSchema = "trim"
@send external min: (zSchema, int, string) => zSchema = "min"
@send external max: (zSchema, int, string) => zSchema = "max"

type zIssue = {
  path: array<string>,
  message: string,
}

type zError = {issues: array<zIssue>}

type rawInput = {
  name: string,
  description: string,
}

type safeParseResult = {
  success: bool,
  data?: rawInput,
  error?: zError,
}

@send external safeParse: (zSchema, 'input) => safeParseResult = "safeParse"

type todoInput = {
  name: string,
  description: option<string>,
}

let todoInputSchema = z["object"](
  Dict.fromArray([
    (
      "name",
      z["string"]()
      ->trim
      ->min(1, "Name must not be empty")
      ->max(80, "Name must be 80 characters or fewer"),
    ),
    (
      "description",
      z["string"]()
      ->trim
      ->max(240, "Description must be 240 characters or fewer"),
    ),
  ]),
)

let parseTodoInput = (~name: string, ~description: string): result<todoInput, string> => {
  let result = todoInputSchema->safeParse({"name": name, "description": description})
  switch (result.data, result.error) {
  | (Some(data), _) =>
    Ok({
      name: data.name,
      description: data.description === "" ? None : Some(data.description),
    })
  | (None, Some(err)) =>
    err.issues
    ->Array.get(0)
    ->Option.map(issue => issue.message)
    ->Option.getOr("Validation failed")
    ->Error
  | (None, None) => Error("Validation failed")
  }
}
