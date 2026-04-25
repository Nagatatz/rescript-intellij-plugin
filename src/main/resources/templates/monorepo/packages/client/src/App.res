// Form + useState + fetch to the Hono server. Demonstrates the
// full client ↔ server loop with a shared Request type.
@react.component
let make = () => {
  let (users, setUsers) = React.useState(() => [])
  let (name, setName) = React.useState(() => "")
  let (email, setEmail) = React.useState(() => "")

  let loadUsers = async () => {
    let fetched = await ApiClient.listUsers()
    setUsers(_ => fetched)
  }

  React.useEffect0(() => {
    loadUsers()->ignore
    None
  })

  let handleSubmit = async event => {
    ReactEvent.Form.preventDefault(event)
    let req: Shared.Api.createUserReq = {name, email}
    let _ = await ApiClient.createUser(req)
    setName(_ => "")
    setEmail(_ => "")
    await loadUsers()
  }

  <main style={{padding: "2rem", fontFamily: "sans-serif"}}>
    <h1> {React.string("{{projectName}}")} </h1>
    <form onSubmit={event => handleSubmit(event)->ignore}>
      <input placeholder="name" value={name} onChange={e => setName(_ => (e->ReactEvent.Form.target)["value"])} />
      <input placeholder="email" value={email} onChange={e => setEmail(_ => (e->ReactEvent.Form.target)["value"])} />
      <button type_="submit"> {React.string("Add user")} </button>
    </form>
    <ul>
      {users
        ->Array.map(u =>
          <li key={u.id->Int.toString}>
            {React.string(u.name ++ " — " ++ u.email)}
          </li>
        )
        ->React.array}
    </ul>
  </main>
}