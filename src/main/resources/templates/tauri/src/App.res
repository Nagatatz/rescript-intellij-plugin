// Interactive demo: click the button to invoke a Tauri command, validate
// the response shape through `Validation.parseInfo`, and render either the
// typed result or the validation error. The greeting input demonstrates a
// fire-and-forget command that returns `string` directly.
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
    | Ok(result) =>
      setInfo(_ => Some(result))
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

  <main style={{padding: "2rem", fontFamily: "sans-serif"}}>
    <h1> {React.string("ReScript + Tauri")} </h1>
    <div style={{display: "flex", gap: "0.75rem", marginBottom: "1rem"}}>
      <button onClick={event => handleGreet(event)->ignore} disabled={loading}>
        {React.string("Say hello")}
      </button>
      <button onClick={event => handleGetInfo(event)->ignore} disabled={loading}>
        {React.string(loading ? "Loading..." : "Get app info")}
      </button>
    </div>
    {switch greeting {
    | Some(g) => <p> {React.string(g)} </p>
    | None => React.null
    }}
    {switch (info, error) {
    | (Some(i), _) =>
      <dl>
        <dt> {React.string("Name")} </dt> <dd> {React.string(i.name)} </dd>
        <dt> {React.string("Version")} </dt> <dd> {React.string(i.version)} </dd>
        <dt> {React.string("Platform")} </dt> <dd> {React.string(i.platform)} </dd>
        <dt> {React.string("Arch")} </dt> <dd> {React.string(i.arch)} </dd>
      </dl>
    | (None, Some(message)) =>
      <p style={{color: "crimson"}}> {React.string(`IPC validation error: ${message}`)} </p>
    | (None, None) => React.null
    }}
  </main>
}
