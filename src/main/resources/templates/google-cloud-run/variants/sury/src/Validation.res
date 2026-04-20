// HTTP input validation using sury (rescript-struct). Route handlers call
// `parseEchoPayload` to turn a JSON body into a typed record or a
// human-readable error string.
type echoPayload = {message: string}

let echoPayloadSchema: S.t<echoPayload> = S.object(s => {
  message: s.field("message", S.string),
})

let parseEchoPayload = (json: JSON.t): result<echoPayload, string> =>
  try Ok(json->S.parseOrThrow(echoPayloadSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
