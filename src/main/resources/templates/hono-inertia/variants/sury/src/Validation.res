// HTTP input validation using sury (rescript-struct). Inertia route handlers
// call `parseGreetForm` to turn a form-style JSON body into a typed record or
// a human-readable error string suitable for re-rendering as a flash prop.
type greetForm = {name: string}

let greetFormSchema: S.t<greetForm> = S.object(s => {
  name: s.field("name", S.string),
})

let parseGreetForm = (json: JSON.t): result<greetForm, string> =>
  try Ok(json->S.parseOrThrow(greetFormSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
