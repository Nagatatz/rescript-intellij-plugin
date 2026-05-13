// Streaming demo using `Core.Channel`.
//
// `Tauri.countWithProgress` creates a channel, registers a typed
// decoder, and hands the channel id to the Rust `count_with_progress`
// command. Rust calls `channel.send(...)` from a background thread
// until done; each emission lands in `onEvent` here.

@react.component
let make = () => {
  let (progress, setProgress) = React.useState(() => 0)
  let (target, setTarget) = React.useState(() => 20)
  let (running, setRunning) = React.useState(() => false)
  let (channelId, setChannelId) = React.useState(() => None)
  let (decodeErrors, setDecodeErrors) = React.useState(() => 0)

  let handleStart = async _ => {
    setRunning(_ => true)
    setProgress(_ => 0)
    setDecodeErrors(_ => 0)
    try {
      let id = await Tauri.countWithProgress(
        ~target,
        ~stepMs=80,
        ~onEvent=evt =>
          switch evt {
          | Started({total: _}) => setProgress(_ => 0)
          | Tick({progress}) => setProgress(_ => progress)
          | Finished({total}) =>
            setProgress(_ => total)
            setRunning(_ => false)
          },
        ~onDecodeError=_ => setDecodeErrors(n => n + 1),
      )
      setChannelId(_ => Some(id))
    } catch {
    | _ => setRunning(_ => false)
    }
  }

  let pct = target == 0 ? 0.0 : Int.toFloat(progress) /. Int.toFloat(target) *. 100.0

  <section style={{padding: "1rem", border: "1px solid #ddd", borderRadius: "6px"}}>
    <h2 style={{marginTop: "0"}}>
      {React.string("Streaming Progress (Core.Channel)")}
    </h2>
    <div style={{display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.75rem"}}>
      <label> {React.string("Target: ")} </label>
      <input
        type_="number"
        value={Int.toString(target)}
        disabled={running}
        onChange={event => {
          let v = (event->ReactEvent.Form.target)["value"]
          setTarget(_ => Int.fromString(v)->Option.getOr(target))
        }}
        style={{width: "5rem"}}
      />
      <button onClick={e => handleStart(e)->ignore} disabled={running}>
        {React.string(running ? "Streaming…" : "Start")}
      </button>
    </div>
    <div
      style={{
        height: "1.25rem",
        background: "#eee",
        borderRadius: "3px",
        overflow: "hidden",
        marginBottom: "0.5rem",
      }}>
      <div
        style={{
          height: "100%",
          width: Float.toString(pct) ++ "%",
          background: "#4a90e2",
          transition: "width 80ms linear",
        }}
      />
    </div>
    <p style={{margin: "0", fontSize: "0.875rem", color: "#555"}}>
      {React.string(
        Int.toString(progress) ++ " / " ++ Int.toString(target) ++ " ticks",
      )}
      {switch channelId {
      | Some(id) => React.string("  ·  channel #" ++ Int.toString(id))
      | None => React.null
      }}
      {decodeErrors > 0
        ? React.string("  ·  " ++ Int.toString(decodeErrors) ++ " decode errors")
        : React.null}
    </p>
  </section>
}
