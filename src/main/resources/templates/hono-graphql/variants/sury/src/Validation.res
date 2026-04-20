// HTTP input validation using sury (rescript-struct). GraphQL mutation
// resolvers (and any REST routes added later) call `parseCreateUserInput` to
// turn a JSON-shaped value into a typed record or a human-readable error string.
type createUserInput = {name: string, email: string}

let createUserInputSchema: S.t<createUserInput> = S.object(s => {
  name: s.field("name", S.string),
  email: s.field("email", S.string),
})

let parseCreateUserInput = (json: JSON.t): result<createUserInput, string> =>
  try Ok(json->S.parseOrThrow(createUserInputSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
