// Fetch wrapper. Routes go through /api/* and are proxied by Vite+ to the Hono
// server during development.
type response
@send external json: response => promise<'a> = "json"
@val external fetch: (string, 'opts) => promise<response> = "fetch"

let listUsers = async (): promise<array<Shared.Types.user>> => {
  let response = await fetch("/api/users", Obj.magic())
  await response->json
}

let createUser = async (payload: Shared.Api.createUserReq): promise<Shared.Api.createUserRes> => {
  let response = await fetch("/api/users", {
    "method": "POST",
    "headers": {"Content-Type": "application/json"},
    "body": JSON.stringifyAny(payload)->Option.getOr("{}"),
  })
  await response->json
}