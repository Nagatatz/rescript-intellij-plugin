// Interactive demo: form + useState + fetch. Form input is validated via
// `Validation.parseGreetForm` before the fetch call. Replace /api/greet with
// your own backend, or run the Full-Stack / Monorepo templates for a paired
// server.
@react.component
let make = () => {
  let (name, setName) = React.useState(() => "")
  let (greeting, setGreeting) = React.useState(() => None)
  let (error, setError) = React.useState(() => None)
  let (loading, setLoading) = React.useState(() => false)

  let handleSubmit = async event => {
    ReactEvent.Form.preventDefault(event)
    switch Validation.parseGreetForm(name) {
    | Error(message) =>
      setError(_ => Some(message))
      setGreeting(_ => None)
    | Ok({name: validated}) =>
      setError(_ => None)
      setLoading(_ => true)
      try {
        let message = await Api.greet(validated)
        setGreeting(_ => Some(message))
      } catch {
      | JsExn(err) =>
        setGreeting(_ => Some("Error: " ++ err->JsExn.message->Option.getOr("unknown")))
      }
      setLoading(_ => false)
    }
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
      <button type_="submit" disabled={loading}>
        {React.string(loading ? "Sending..." : "Greet")}
      </button>
    </form>
    {switch error {
    | Some(message) => <p style={{color: "crimson"}}> {React.string(`⚠ ${message}`)} </p>
    | None => React.null
    }}
    {switch greeting {
    | Some(msg) => <p> {React.string(msg)} </p>
    | None => React.null
    }}
  </main>
}