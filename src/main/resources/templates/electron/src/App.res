// Interactive demo: click the button to invoke the main process via IPC
// and render the returned info.
@react.component
let make = () => {
  let (info, setInfo) = React.useState(() => None)
  let (loading, setLoading) = React.useState(() => false)

  let handleClick = async _ => {
    setLoading(_ => true)
    let result = await Electron.getInfo()
    setInfo(_ => Some(result))
    setLoading(_ => false)
  }

  <main style={{padding: "2rem", fontFamily: "sans-serif"}}>
    <h1> {React.string("ReScript + Electron")} </h1>
    <button onClick={event => handleClick(event)->ignore} disabled={loading}>
      {React.string(loading ? "Loading..." : "Get system info")}
    </button>
    {switch info {
    | Some(i) =>
      <dl>
        <dt> {React.string("Name")} </dt> <dd> {React.string(i.name)} </dd>
        <dt> {React.string("Electron")} </dt> <dd> {React.string(i.electronVersion)} </dd>
        <dt> {React.string("Platform")} </dt> <dd> {React.string(i.platform)} </dd>
        <dt> {React.string("Arch")} </dt> <dd> {React.string(i.arch)} </dd>
      </dl>
    | None => React.null
    }}
  </main>
}