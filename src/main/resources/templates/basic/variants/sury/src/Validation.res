// Runtime validation for `config.json` using sury. `App.res` calls
// `parseConfig` to fail fast on a malformed config instead of crashing
// somewhere downstream.
type config = {greeting: string, repeat: float}

let configSchema: S.t<config> = S.object(s => {
  greeting: s.field("greeting", S.string),
  repeat: s.field("repeat", S.float),
})

let parseConfig = (json: JSON.t): result<config, string> =>
  try Ok(json->S.parseOrThrow(configSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
