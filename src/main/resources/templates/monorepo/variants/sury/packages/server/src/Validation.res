// HTTP input validation using sury (rescript-struct). Route handlers call
// `parseCreateUserReq` to turn a JSON body into a typed record from the
// `@<project>/shared` package or a human-readable error string.
let createUserReqSchema: S.t<Shared.Api.createUserReq> = S.object(s => {
  Shared.Api.name: s.field("name", S.string),
  email: s.field("email", S.string),
})

let parseCreateUserReq = (json: JSON.t): result<Shared.Api.createUserReq, string> =>
  try Ok(json->S.parseOrThrow(createUserReqSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
