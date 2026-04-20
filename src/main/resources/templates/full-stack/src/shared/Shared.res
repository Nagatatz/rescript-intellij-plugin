// Types shared between the server and the client. The nested module layout
// lets both sides reference values as `Shared.Types.*` / `Shared.Api.*`,
// keeping the dependency direction obvious (client/server both depend on
// shared; shared depends on neither).
module Types = {
  type user = {id: int, name: string, email: string}
}

module Api = {
  // Wire format for the /api/users endpoints. Keep server and client
  // in sync by editing just this file.
  type createUserReq = {name: string, email: string}
  type createUserRes = {id: int, name: string, email: string}
}
