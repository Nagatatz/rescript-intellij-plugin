// Interactive demo: click the button to invoke the main process via IPC,
// validate the response shape through `Validation.parseInfo`, and render
// either the typed result or the validation error.
@react.component
let make = () => {
  let (info, setInfo) = React.useState(() => None)
  let (error, setError) = React.useState(() => None)
  let (loading, setLoading) = React.useState(() => false)

  let handleClick = async _ => {
    setLoading(_ => true)
    setError(_ => None)
    let raw = await Electron.getInfoRaw()
    switch Validation.parseInfo(raw) {
    | Ok(result) =>
      setInfo(_ => Some(result))
    | Error(message) =>
      setInfo(_ => None)
      setError(_ => Some(message))
    }
    setLoading(_ => false)
  }

  <main style={{padding: "2rem", fontFamily: "sans-serif"}}>
    <h1> {React.string("ReScript + Electron")} </h1>
    <button onClick={event => handleClick(event)->ignore} disabled={loading}>
      {React.string(loading ? "Loading..." : "Get system info")}
    </button>
    {switch (info, error) {
    | (Some(i), _) =>
      <dl>
        <dt> {React.string("Name")} </dt> <dd> {React.string(i.name)} </dd>
        <dt> {React.string("Electron")} </dt> <dd> {React.string(i.electronVersion)} </dd>
        <dt> {React.string("Platform")} </dt> <dd> {React.string(i.platform)} </dd>
        <dt> {React.string("Arch")} </dt> <dd> {React.string(i.arch)} </dd>
      </dl>
    | (None, Some(message)) =>
      <p style={{color: "crimson"}}> {React.string(`IPC validation error: ${message}`)} </p>
    | (None, None) => React.null
    }}
  </main>
}