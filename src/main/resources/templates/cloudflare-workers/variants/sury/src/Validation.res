// HTTP input validation using sury (rescript-struct). Route handlers call
// `parseGreetingPayload` to turn a JSON body into a typed record or a
// human-readable error string.
type greetingPayload = {name: string}

let greetingPayloadSchema: S.t<greetingPayload> = S.object(s => {
  name: s.field("name", S.string),
})

let parseGreetingPayload = (json: JSON.t): result<greetingPayload, string> =>
  try Ok(json->S.parseOrThrow(greetingPayloadSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
