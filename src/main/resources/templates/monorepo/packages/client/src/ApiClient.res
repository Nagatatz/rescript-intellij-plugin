// Fetch wrapper. Routes go through /api/* and are proxied by Vite+ to the Hono
// server during development.
type response
@send external json: response => promise<'a> = "json"
@val external fetch: (string, 'opts) => promise<response> = "fetch"

// `async` already wraps the return type in a promise, so the annotation is
// the resolved value type — not `promise<...>`.
let listUsers = async (): array<Shared.Types.user> => {
  let response = await fetch("/api/users", Obj.magic())
  await response->json
}

let createUser = async (payload: Shared.Api.createUserReq): Shared.Api.createUserRes => {
  let response = await fetch("/api/users", {
    "method": "POST",
    "headers": {"Content-Type": "application/json"},
    "body": JSON.stringifyAny(payload)->Option.getOr("{}"),
  })
  await response->json
}