// File hex-inspector panel. Touches four plugins + one low-level
// Path call:
//
//   plugin-dialog            → native open-file picker
//   plugin-fs                → reads the file as `Uint8Array.t`
//   plugin-clipboard-manager → copies the formatted dump
//   plugin-notification      → toasts on copy (after permission flow)
//   plugin-log               → structured logs forwarded to Rust
//
// The hex render itself is plain ReScript: take the first `previewLen`
// bytes, group by 16, format `offset │ hex │ ascii` lines.

module Dialog = RescriptTauriPluginDialog.PluginDialog
module Fs = RescriptTauriPluginFs.PluginFs
module Clipboard = RescriptTauriPluginClipboardManager.PluginClipboardManager
module Notif = RescriptTauriPluginNotification.PluginNotification
module Log = RescriptTauriPluginLog.PluginLog

let previewLen = 256

// Plugin-fs returns the Stdlib `Uint8Array.t`; bind length / index
// access on it via polymorphic externals so we don't accidentally
// pick up the older `RescriptCore.Uint8Array.t` (the `-open RescriptCore`
// flag in rescript.json puts both into scope).
@get external u8Length: 'a => int = "length"
@get_index external u8Get: ('a, int) => int = ""

let hex2 = byte => {
  let s = Int.toString(byte, ~radix=16)
  String.length(s) == 1 ? "0" ++ s : s
}

let hex8 = n => {
  let s = Int.toString(n, ~radix=16)
  let pad = String.repeat("0", Math.Int.max(0, 8 - String.length(s)))
  pad ++ s
}

let formatDump = (bytes): string => {
  let total = u8Length(bytes)
  let shown = Math.Int.min(total, previewLen)
  let lines = []
  let row = ref(0)
  while row.contents * 16 < shown {
    let offset = row.contents * 16
    let hex = []
    let ascii = []
    let i = ref(0)
    while i.contents < 16 && offset + i.contents < shown {
      let b = u8Get(bytes, offset + i.contents)
      Array.push(hex, hex2(b))
      Array.push(ascii, b >= 0x20 && b < 0x7f ? String.fromCharCode(b) : ".")
      i := i.contents + 1
    }
    while Array.length(hex) < 16 {
      Array.push(hex, "  ")
      Array.push(ascii, " ")
    }
    let hexCol = Array.join(hex, " ")
    let asciiCol = Array.join(ascii, "")
    Array.push(lines, hex8(offset) ++ "  " ++ hexCol ++ "  |" ++ asciiCol ++ "|")
    row := row.contents + 1
  }
  if total > shown {
    Array.push(lines, "… " ++ Int.toString(total - shown) ++ " more bytes")
  }
  Array.join(lines, "\n")
}

type loaded = {path: string, size: int, dump: string}

@react.component
let make = () => {
  let (loaded, setLoaded) = React.useState(() => None)
  let (error, setError) = React.useState(() => None)
  let (busy, setBusy) = React.useState(() => false)
  let (status, setStatus) = React.useState(() => None)

  let handlePick = async _ => {
    setBusy(_ => true)
    setError(_ => None)
    setStatus(_ => None)
    try {
      let picked = await Dialog.openFile(
        ~options={
          title: "Pick any file to peek at",
          filters: [{name: "All files", extensions: ["*"]}],
        },
      )
      switch picked->Nullable.toOption {
      | None => setStatus(_ => Some("Pick cancelled"))
      | Some(path) =>
        let bytes = await Fs.readFile(path)
        let size = u8Length(bytes)
        let _ = await Log.info("read " ++ Int.toString(size) ++ " bytes from " ++ path)
        setLoaded(_ => Some({path: path, size: size, dump: formatDump(bytes)}))
      }
    } catch {
    | JsExn(err) =>
      let msg = err->JsExn.message->Option.getOr("file pick failed")
      let _ = await Log.error("hex-inspector pick failed: " ++ msg)
      setError(_ => Some(msg))
    }
    setBusy(_ => false)
  }

  let handleCopy = async _ =>
    switch loaded {
    | None => ()
    | Some({dump, path}) =>
      try {
        await Clipboard.writeText(dump)
        let granted = await Notif.isPermissionGranted()
        let allowed = if granted {
          true
        } else {
          let requested = await Notif.requestPermission()
          requested == #granted
        }
        if allowed {
          Notif.sendNotification({title: "Hex dump copied", body: "From " ++ path})
        }
        setStatus(_ => Some("Copied " ++ Int.toString(String.length(dump)) ++ " chars"))
      } catch {
      | JsExn(err) => setError(_ => Some(err->JsExn.message->Option.getOr("copy failed")))
      }
    }

  <section style={{padding: "1rem", border: "1px solid #ddd", borderRadius: "6px"}}>
    <h2 style={{marginTop: "0"}}> {React.string("Hex Inspector (plugin-dialog + plugin-fs)")} </h2>
    <div style={{display: "flex", gap: "0.5rem", marginBottom: "0.75rem"}}>
      <button onClick={e => handlePick(e)->ignore} disabled={busy}>
        {React.string(busy ? "Reading…" : "Pick file")}
      </button>
      <button
        onClick={e => handleCopy(e)->ignore}
        disabled={busy || Option.isNone(loaded)}>
        {React.string("Copy + notify")}
      </button>
    </div>
    {switch (loaded, error, status) {
    | (Some(l), _, _) =>
      <>
        <p style={{margin: "0 0 0.25rem"}}>
          <strong> {React.string("Path: ")} </strong>
          <code> {React.string(l.path)} </code>
        </p>
        <p style={{margin: "0 0 0.5rem"}}>
          <strong> {React.string("Size: ")} </strong>
          {React.string(Int.toString(l.size) ++ " bytes")}
        </p>
        <pre
          style={{
            background: "#111",
            color: "#eee",
            padding: "0.75rem",
            borderRadius: "4px",
            overflowX: "auto",
            fontSize: "12px",
          }}>
          {React.string(l.dump)}
        </pre>
        {switch status {
        | Some(s) => <p style={{color: "seagreen"}}> {React.string(s)} </p>
        | None => React.null
        }}
      </>
    | (None, Some(msg), _) =>
      <p style={{color: "crimson"}}> {React.string(msg)} </p>
    | (None, None, Some(s)) =>
      <p> {React.string(s)} </p>
    | (None, None, None) =>
      <p> {React.string("Pick a file to display the first 256 bytes as a hex dump.")} </p>
    }}
  </section>
}
