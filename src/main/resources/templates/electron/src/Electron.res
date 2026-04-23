// Bindings over the `window.electronAPI` object exposed by preload.cjs. The
// renderer receives `JSON.t` — `Validation.parseInfo` decides whether the
// payload matches the expected shape.
@val external electronAPI: {"getInfo": unit => promise<JSON.t>} = "electronAPI"

let getInfoRaw = (): promise<JSON.t> => electronAPI["getInfo"]()
