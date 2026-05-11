// Runtime validation for Tauri command responses using sury. Even though
// commands are typed in Rust, the JS-side payload arriving over the IPC
// bridge is still `JSON.t` until proven otherwise — `parseInfo` turns
// shape drift between Rust and ReScript into a human-readable error.
type info = {name: string, version: string, platform: string, arch: string}

let infoSchema: S.t<info> = S.object(s => {
  name: s.field("name", S.string),
  version: s.field("version", S.string),
  platform: s.field("platform", S.string),
  arch: s.field("arch", S.string),
})

let parseInfo = (json: JSON.t): result<info, string> =>
  try Ok(json->S.parseOrThrow(infoSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
