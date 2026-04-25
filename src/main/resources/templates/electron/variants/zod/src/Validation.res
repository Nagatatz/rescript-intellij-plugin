// Runtime validation for IPC responses using zod. The renderer sits behind
// contextIsolation, but the main process can still ship malformed payloads
// during a bad refactor — `parseInfo` turns those into a human-readable
// error before the UI assumes the shape.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

type info = {name: string, electronVersion: string, platform: string, arch: string}

let infoSchema = z["object"]({
  "name": z["string"](),
  "electronVersion": z["string"](),
  "platform": z["string"](),
  "arch": z["string"](),
})

let parseInfo = (json: JSON.t): result<info, string> =>
  try {
    let parsed: info = parse(infoSchema, json)
    Ok(parsed)
  } catch {
  | JsExn(err) => Error(err->JsExn.message->Option.getOr("Invalid IPC payload"))
  }
