// Interactive demo: form + useState + fetch. Replace /api/greet with your own backend,
// or run the Full-Stack / Monorepo templates for a paired server.
@react.component
let make = () => {
  let (name, setName) = React.useState(() => "")
  let (greeting, setGreeting) = React.useState(() => None)
  let (loading, setLoading) = React.useState(() => false)

  let handleSubmit = async event => {
    ReactEvent.Form.preventDefault(event)
    setLoading(_ => true)
    try {
      let message = await Api.greet(name)
      setGreeting(_ => Some(message))
    } catch {
    | JsExn(err) =>
      setGreeting(_ => Some("Error: " ++ err->JsExn.message->Option.getOr("unknown")))
    }
    setLoading(_ => false)
  }

  <main style={{padding: "2rem", fontFamily: "sans-serif"}}>
    <h1> {React.string("ReScript + Vite+")} </h1>
    <form onSubmit={event => handleSubmit(event)->ignore}>
      <input
        type_="text"
        placeholder="Your name"
        value={name}
        onChange={e => setName(_ => (e->ReactEvent.Form.target)["value"])}
      />
      <button type_="submit" disabled={loading || name == ""}>
        {React.string(loading ? "Sending..." : "Greet")}
      </button>
    </form>
    {switch greeting {
    | Some(msg) => <p> {React.string(msg)} </p>
    | None => React.null
    }}
  </main>
}