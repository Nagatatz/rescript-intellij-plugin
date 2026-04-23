// Runtime validation for `init` subcommand options using zod. `Commands.Init.run`
// collects --name / --dir into a JSON object and calls `parseInitOptions` to
// reject malformed input instead of silently scaffolding with bad values.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

type initOptions = {name: string, dir: string}

let initOptionsSchema = z["object"]({
  "name": z["string"](),
  "dir": z["string"](),
})

let parseInitOptions = (json: JSON.t): result<initOptions, string> =>
  try {
    let parsed: initOptions = parse(initOptionsSchema, json)
    Ok(parsed)
  } catch {
  | Exn.Error(err) => Error(err->Exn.message->Option.getOr("Invalid init options"))
  }
