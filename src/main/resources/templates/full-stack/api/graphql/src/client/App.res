// Root component. `UsersListQuery.use()` suspends until data arrives; the
// outer Suspense boundary in ClientMain.res shows the fallback during the
// initial fetch.
@react.component
let make = () => {
  let data = UsersListQuery.UsersListQuery.use(~variables=(), ())

  <main>
    <h1> {React.string("Users")} </h1>
    <ul>
      {data.users
      ->Array.map(user =>
        <li key={user.id->Int.toString}>
          {React.string(`${user.name} <${user.email}>`)}
        </li>
      )
      ->React.array}
    </ul>
  </main>
}
