// Server Function bound through @tanstack/react-start. The `createServerFn`
// helper wraps the body so the function only ever runs on the server, and
// returns a thunk that the client can call as if it were local.
@module("@tanstack/react-start") external createServerFn: 'config => 'fn = "createServerFn"

type input = {data: string}

let greet = createServerFn({"method": "GET"})({
  "validator": (payload: input) => payload,
  "handler": (ctx: input) => `Hello, ${ctx.data}!`,
})
