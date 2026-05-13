// Sample shell composing the panels exercised in this template:
//
//   • Greet/Info       → @rescript-tauri/core (Raw.invoke)
//   • SystemInfo       → @rescript-tauri/plugin-os + core Path
//   • HexInspector     → plugin-dialog + plugin-fs + plugin-clipboard-manager
//                        + plugin-notification + plugin-log
//   • Progress         → Core.Channel streaming (low-level IPC)
//   • WindowEvents     → Core.Event.listen + Window.setTitle
//
// `Main.res` kicks off `PluginLog.attachConsole()` once at boot so any
// `Log.info(...)` calls in the panels surface in the JS console too.

@react.component
let make = () => {
  let (info, setInfo) = React.useState(() => None)
  let (greeting, setGreeting) = React.useState(() => None)
  let (error, setError) = React.useState(() => None)
  let (loading, setLoading) = React.useState(() => false)

  let handleGetInfo = async _ => {
    setLoading(_ => true)
    setError(_ => None)
    let raw = await Tauri.getInfoRaw()
    switch Validation.parseInfo(raw) {
    | Ok(result) => setInfo(_ => Some(result))
    | Error(message) =>
      setInfo(_ => None)
      setError(_ => Some(message))
    }
    setLoading(_ => false)
  }

  let handleGreet = async _ => {
    setLoading(_ => true)
    let result = await Tauri.greet("ReScript")
    setGreeting(_ => Some(result))
    setLoading(_ => false)
  }

  <main
    style={{
      padding: "2rem",
      fontFamily: "sans-serif",
      display: "flex",
      flexDirection: "column",
      gap: "1rem",
      maxWidth: "880px",
      margin: "0 auto",
    }}>
    <header>
      <h1 style={{margin: "0 0 0.25rem"}}> {React.string("ReScript + Tauri Sample")} </h1>
      <p style={{margin: "0", color: "#555"}}>
        {React.string(
          "Exercises @rescript-tauri/core plus six @rescript-tauri/plugin-* bindings and a few low-level IPC primitives.",
        )}
      </p>
    </header>
    <section style={{padding: "1rem", border: "1px solid #ddd", borderRadius: "6px"}}>
      <h2 style={{marginTop: "0"}}> {React.string("Plain IPC (Core.Raw.invoke)")} </h2>
      <div style={{display: "flex", gap: "0.75rem", marginBottom: "0.5rem"}}>
        <button onClick={event => handleGreet(event)->ignore} disabled={loading}>
          {React.string("Say hello")}
        </button>
        <button onClick={event => handleGetInfo(event)->ignore} disabled={loading}>
          {React.string(loading ? "Loading..." : "Get app info")}
        </button>
      </div>
      {switch greeting {
      | Some(g) => <p style={{margin: "0.25rem 0"}}> {React.string(g)} </p>
      | None => React.null
      }}
      {switch (info, error) {
      | (Some(i), _) =>
        <dl style={{margin: "0.25rem 0"}}>
          <dt> {React.string("Name")} </dt> <dd> {React.string(i.name)} </dd>
          <dt> {React.string("Version")} </dt> <dd> {React.string(i.version)} </dd>
          <dt> {React.string("Platform")} </dt> <dd> {React.string(i.platform)} </dd>
          <dt> {React.string("Arch")} </dt> <dd> {React.string(i.arch)} </dd>
        </dl>
      | (None, Some(message)) =>
        <p style={{color: "crimson"}}>
          {React.string(`IPC validation error: ${message}`)}
        </p>
      | (None, None) => React.null
      }}
    </section>
    <SystemInfo />
    <HexInspector />
    <Progress />
    <WindowEvents />
  </main>
}
