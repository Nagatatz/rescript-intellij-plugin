// Users list + add-user form. Uses Shared.Types.user so the server
// and client cannot drift out of sync.
@react.component
let make = () => {
  let (users, setUsers) = React.useState(() => [])
  let (name, setName) = React.useState(() => "")
  let (email, setEmail) = React.useState(() => "")

  let refresh = async () => {
    let fetched = await Api.listUsers()
    setUsers(_ => fetched)
  }

  React.useEffect0(() => {
    refresh()->ignore
    None
  })

  let handleSubmit = async event => {
    ReactEvent.Form.preventDefault(event)
    let req: Shared.Api.createUserReq = {name, email}
    let _ = await Api.createUser(req)
    setName(_ => "")
    setEmail(_ => "")
    await refresh()
  }

  <main style={ReactDOM.Style.make(~padding="2rem", ~fontFamily="sans-serif", ())}>
    <h1> {React.string("{{projectName}}")} </h1>
    <form onSubmit={handleSubmit}>
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