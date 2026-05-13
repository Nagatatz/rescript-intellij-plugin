// Thin wrappers over @rescript-tauri/core's IPC layers.
//
// `greet` / `getInfoRaw` stay on the Layer 1 (`Core.Raw.invoke`)
// surface — the renderer still owns response shape validation via
// `Validation.parseInfo`.
//
// `countWithProgress` exercises the lower-level streaming layer:
// a `Core.Channel<progressEvent>` is created up-front, handed to the
// Rust handler as an argument, and the Rust side calls `channel.send`
// repeatedly until the work finishes.

module Core = RescriptTauriCore.Core

let greet = (name: string): promise<string> =>
  Core.Raw.invoke("greet", ~args={"name": name})

let getInfoRaw = (): promise<JSON.t> =>
  Core.Raw.invoke("get_info", ~args=Object.make())

type progressEvent =
  | Started({total: int})
  | Tick({progress: int})
  | Finished({total: int})

// `tag = "kind"` + `rename_all = "camelCase"` on the Rust side means
// every payload looks like `{ "kind": "started" | "tick" | "finished",
// ... }`. Decode it explicitly here so a Rust enum reshape surfaces as
// `Error(...)` in `Channel.onMessage` instead of silently mis-rendering.
let decodeProgress = (json: JSON.t): result<progressEvent, string> => {
  let intField = (dict, key) =>
    switch dict->Dict.get(key) {
    | Some(JSON.Number(n)) => Some(Float.toInt(n))
    | _ => None
    }
  switch json->JSON.Decode.object {
  | None => Error("expected object")
  | Some(dict) =>
    switch dict->Dict.get("kind") {
    | Some(JSON.String("started")) =>
      switch intField(dict, "total") {
      | Some(total) => Ok(Started({total: total}))
      | None => Error("started.total missing")
      }
    | Some(JSON.String("tick")) =>
      switch intField(dict, "progress") {
      | Some(progress) => Ok(Tick({progress: progress}))
      | None => Error("tick.progress missing")
      }
    | Some(JSON.String("finished")) =>
      switch intField(dict, "total") {
      | Some(total) => Ok(Finished({total: total}))
      | None => Error("finished.total missing")
      }
    | _ => Error("unknown progress kind")
    }
  }
}

// Returns the channel id so callers can correlate Rust-side logs with
// the channel that received them — purely diagnostic.
let countWithProgress = (
  ~target: int,
  ~stepMs: int,
  ~onEvent: progressEvent => unit,
  ~onDecodeError: string => unit,
): promise<int> => {
  let channel = Core.Channel.make(~decode=decodeProgress)
  channel->Core.Channel.onMessage(result =>
    switch result {
    | Ok(evt) => onEvent(evt)
    | Error(msg) => onDecodeError(msg)
    }
  )
  Core.Raw.invoke(
    "count_with_progress",
    ~args={"onEvent": channel, "target": target, "stepMs": stepMs},
  )->Promise.then(_ => Promise.resolve(Core.Channel.id(channel)))
}
