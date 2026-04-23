// Runtime validation for `init` subcommand options using sury. `Commands.Init.run`
// collects --name / --dir into a JSON object and calls `parseInitOptions` to
// reject malformed input instead of silently scaffolding with bad values.
type initOptions = {name: string, dir: string}

let initOptionsSchema: S.t<initOptions> = S.object(s => {
  name: s.field("name", S.string),
  dir: s.field("dir", S.string),
})

let parseInitOptions = (json: JSON.t): result<initOptions, string> =>
  try Ok(json->S.parseOrThrow(initOptionsSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
