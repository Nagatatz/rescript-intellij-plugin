// Low-level Tauri window-event subscription.
//
// `Event.listen` registers a renderer-side callback for one of the
// predefined `tauri://...` events emitted by the OS / window manager —
// resize, focus, blur, drag-drop. Each callback receives the raw JSON
// payload plus a Tauri event id, so we keep a rolling log of the last
// 20 emissions for inspection.
//
// The "Set window title" button mutates the host OS window through
// `Window.getCurrent().setTitle(...)`, a direct hop into the native
// process — a useful contrast to the IPC-command style elsewhere.

module Event = RescriptTauriCore.Event
module Window = RescriptTauriCore.Window

let formatPayload = (json: JSON.t) => JSON.stringify(json)

let now = () => {
  let d = Date.make()
  Date.toLocaleTimeString(d)
}

type entry = {at: string, name: string, payload: string}

let pushEntry = (entries, entry) => {
  let combined = Array.concat([entry], entries)
  combined->Array.slice(~start=0, ~end=20)
}

@react.component
let make = () => {
  let (entries, setEntries) = React.useState(() => [])
  let (title, setTitle) = React.useState(() => "tauri-app")
  let (status, setStatus) = React.useState(() => None)

  React.useEffect0(() => {
    let unlistens = ref([])
    let subscribe = async () => {
      let mkEvent = name => Event.make(~name, ~decode=json => Ok(json))
      let names = [
        ("tauri://resize", Event.TauriEvent.windowResized),
        ("tauri://focus", Event.TauriEvent.windowFocus),
        ("tauri://blur", Event.TauriEvent.windowBlur),
        ("tauri://drag-enter", Event.TauriEvent.dragEnter),
        ("tauri://drag-drop", Event.TauriEvent.dragDrop),
      ]
      for i in 0 to Array.length(names) - 1 {
        switch Array.get(names, i) {
        | Some((label, tauriEvt)) =>
          let evt = mkEvent((tauriEvt :> string))
          let unlisten = await evt->Event.listen(result =>
            switch result {
            | Ok(e) =>
              setEntries(entries =>
                pushEntry(entries, {at: now(), name: label, payload: formatPayload(e.payload)})
              )
            | Error(_) => ()
            }
          )
          unlistens := Array.concat([unlisten], unlistens.contents)
        | None => ()
        }
      }
    }
    subscribe()->ignore
    Some(
      () =>
        unlistens.contents->Array.forEach(unlisten => unlisten()),
    )
  })

  let handleSetTitle = async _ => {
    try {
      let win = Window.getCurrent()
      await win->Window.setTitle(title)
      setStatus(_ => Some("Title set"))
    } catch {
    | JsExn(err) =>
      setStatus(_ => Some("setTitle failed: " ++ err->JsExn.message->Option.getOr("?")))
    }
  }

  <section style={{padding: "1rem", border: "1px solid #ddd", borderRadius: "6px"}}>
    <h2 style={{marginTop: "0"}}>
      {React.string("Window Events (Core.Event.listen + Window.setTitle)")}
    </h2>
    <div style={{display: "flex", gap: "0.5rem", marginBottom: "0.75rem"}}>
      <input
        type_="text"
        value={title}
        onChange={event => {
          let v = (event->ReactEvent.Form.target)["value"]
          setTitle(_ => v)
        }}
        style={{flex: "1"}}
      />
      <button onClick={e => handleSetTitle(e)->ignore}>
        {React.string("Set window title")}
      </button>
    </div>
    {switch status {
    | Some(s) => <p style={{margin: "0 0 0.5rem", fontSize: "0.875rem"}}> {React.string(s)} </p>
    | None => React.null
    }}
    <p style={{margin: "0 0 0.25rem", fontSize: "0.875rem", color: "#555"}}>
      {React.string("Resize the window or drag a file onto it — events appear below.")}
    </p>
    <ul
      style={{
        margin: "0",
        padding: "0.5rem 1rem",
        background: "#f6f6f6",
        borderRadius: "4px",
        maxHeight: "12rem",
        overflowY: "auto",
        fontFamily: "monospace",
        fontSize: "12px",
      }}>
      {if Array.length(entries) == 0 {
        <li style={{listStyle: "none", color: "#888"}}> {React.string("(no events yet)")} </li>
      } else {
        entries
        ->Array.mapWithIndex((e, i) =>
          <li key={Int.toString(i)} style={{listStyleType: "none"}}>
            <span style={{color: "#888"}}> {React.string(e.at)} </span>
            <span style={{color: "#444", margin: "0 0.5rem"}}> {React.string(e.name)} </span>
            <span> {React.string(e.payload)} </span>
          </li>
        )
        ->React.array
      }}
    </ul>
  </section>
}
