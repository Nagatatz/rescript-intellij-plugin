// HTTP input validation for POST /api/greet using sury (rescript-struct).
type greetInput = {name: string}

let greetInputSchema: S.t<greetInput> = S.object(s => {
  name: s.field("name", S.string),
})

let parseGreetInput = (json: JSON.t): result<greetInput, string> =>
  try Ok(json->S.parseOrThrow(greetInputSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
