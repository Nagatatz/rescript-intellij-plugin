// Thin wrapper over @rescript-tauri/core's Layer 1 (`Core.Raw.invoke`).
// The renderer receives `JSON.t` from `get_info` — `Validation.parseInfo`
// decides whether the payload matches the expected shape. Migrate to the
// typed `Core.Command` layer once the IPC surface stabilises.

let greet = (name: string): promise<string> =>
  RescriptTauriCore.Core.Raw.invoke("greet", ~args={"name": name})

let getInfoRaw = (): promise<JSON.t> =>
  RescriptTauriCore.Core.Raw.invoke("get_info", ~args=Js.Obj.empty())
