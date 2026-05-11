// Runtime validation for Tauri command responses using zod. Even though
// commands are typed in Rust, the JS-side payload arriving over the IPC
// bridge is still `JSON.t` until proven otherwise — `parseInfo` turns
// shape drift between Rust and ReScript into a human-readable error.
type zSchema

@module("zod")
external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
} = "z"

@send external parse: (zSchema, JSON.t) => 'a = "parse"

type info = {name: string, version: string, platform: string, arch: string}

let infoSchema = z["object"]({
  "name": z["string"](),
  "version": z["string"](),
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
