// System probe panel powered by `@rescript-tauri/plugin-os`.
//
// `platform` / `family` / `arch` / `eol` resolve synchronously (no IPC
// round-trip — the plugin caches these on init), while `hostname` and
// `locale` cross the bridge each call.
//
// Bonus low-level bit: `Path.appLogDir` is resolved from `core`'s
// `Path` module to show where the Rust-side `tauri-plugin-log` writes
// files on this OS.

module PluginOs = RescriptTauriPluginOs.PluginOs
module Path = RescriptTauriCore.Path

// Polymorphic variants with no payload are emitted as their tag string
// at runtime, so `%identity` is a zero-cost cast for display purposes.
external pvToString: 'a => string = "%identity"

type state = {
  platform: string,
  family: string,
  arch: string,
  osType: string,
  version: string,
  eol: string,
  exeExtension: string,
  hostname: option<string>,
  locale: option<string>,
  appLogDir: option<string>,
}

@react.component
let make = () => {
  let (state, setState) = React.useState(() => None)
  let (error, setError) = React.useState(() => None)

  React.useEffect0(() => {
    let load = async () => {
      try {
        let hostname = await PluginOs.hostname()
        let locale = await PluginOs.locale()
        let appLogDir = try {
          let dir = await Path.appLogDir()
          Some(dir)
        } catch {
        | _ => None
        }
        setState(_ => Some({
          platform: pvToString(PluginOs.platform()),
          family: pvToString(PluginOs.family()),
          arch: pvToString(PluginOs.arch()),
          osType: pvToString(PluginOs.OsType.get()),
          version: PluginOs.version(),
          eol: PluginOs.eol() == "\n" ? "\\n (LF)" : "\\r\\n (CRLF)",
          exeExtension: PluginOs.exeExtension(),
          hostname: hostname->Nullable.toOption,
          locale: locale->Nullable.toOption,
          appLogDir: appLogDir,
        }))
      } catch {
      | JsExn(err) => setError(_ => Some(err->JsExn.message->Option.getOr("plugin-os failed")))
      }
    }
    load()->ignore
    None
  })

  let row = (label, value) =>
    <React.Fragment key=label>
      <dt> {React.string(label)} </dt>
      <dd> <code> {React.string(value)} </code> </dd>
    </React.Fragment>

  <section style={{padding: "1rem", border: "1px solid #ddd", borderRadius: "6px"}}>
    <h2 style={{marginTop: "0"}}> {React.string("System Probe (plugin-os)")} </h2>
    {switch (state, error) {
    | (Some(s), _) =>
      <dl style={{display: "grid", gridTemplateColumns: "max-content 1fr", gap: "0.25rem 1rem"}}>
        {row("platform", s.platform)}
        {row("family", s.family)}
        {row("arch", s.arch)}
        {row("osType", s.osType)}
        {row("version", s.version)}
        {row("eol", s.eol)}
        {row("exe extension", s.exeExtension == "" ? "(none)" : s.exeExtension)}
        {row("hostname", s.hostname->Option.getOr("(unavailable)"))}
        {row("locale", s.locale->Option.getOr("(unavailable)"))}
        {row("appLogDir", s.appLogDir->Option.getOr("(unavailable)"))}
      </dl>
    | (None, Some(msg)) =>
      <p style={{color: "crimson"}}> {React.string("plugin-os error: " ++ msg)} </p>
    | (None, None) => <p> {React.string("Probing…")} </p>
    }}
  </section>
}
