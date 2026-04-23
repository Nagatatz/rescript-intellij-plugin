// Runtime validation for IPC responses using sury. The renderer sits behind
// contextIsolation, but the main process can still ship malformed payloads
// during a bad refactor — `parseInfo` turns those into a human-readable
// error before the UI assumes the shape.
type info = {name: string, electronVersion: string, platform: string, arch: string}

let infoSchema: S.t<info> = S.object(s => {
  name: s.field("name", S.string),
  electronVersion: s.field("electronVersion", S.string),
  platform: s.field("platform", S.string),
  arch: s.field("arch", S.string),
})

let parseInfo = (json: JSON.t): result<info, string> =>
  try Ok(json->S.parseOrThrow(infoSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
