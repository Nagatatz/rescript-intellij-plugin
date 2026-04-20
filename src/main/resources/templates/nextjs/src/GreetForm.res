// Client Component (state + fetch). Consumed from app/client/GreetForm.tsx.
@genType @react.component
let make = () => {
  let (name, setName) = React.useState(() => "")
  let (greeting, setGreeting) = React.useState(() => None)

  let handleSubmit = async event => {
    ReactEvent.Form.preventDefault(event)
    let response =
      await Fetch.post(
        "/api/greet",
        JSON.stringifyAny({"name": name})->Option.getOr("{}"),
      )
    let body = await response->Fetch.json
    setGreeting(_ => Some(body["message"]))
  }

  <form onSubmit={event => handleSubmit(event)->ignore}>
    <input
      type_="text"
      placeholder="Your name"
      value={name}
      onChange={e => setName(_ => (e->ReactEvent.Form.target)["value"])}
    />
    <button type_="submit"> {React.string("Greet")} </button>
    {switch greeting {
    | Some(msg) => <p> {React.string(msg)} </p>
    | None => React.null
    }}
  </form>
}