// Runtime validation for `config.json` using zod. `App.res` calls
// `parseConfig` to fail fast on a malformed config instead of crashing
// somewhere downstream.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
  "number": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

type config = {greeting: string, repeat: float}

let configSchema = z["object"]({
  "greeting": z["string"](),
  "repeat": z["number"](),
})

let parseConfig = (json: JSON.t): result<config, string> =>
  try {
    let parsed: config = parse(configSchema, json)
    Ok(parsed)
  } catch {
  | JsExn(err) => Error(err->JsExn.message->Option.getOr("Invalid config"))
  }
