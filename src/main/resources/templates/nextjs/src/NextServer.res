// Minimal ReScript bindings for Next.js's `next/server` Route Handler API.
// Covers what the generated POST /api/greet handler needs; extend as you add
// more Route Handlers.
type nextRequest
type nextResponse

@send external reqJson: nextRequest => promise<JSON.t> = "json"

@module("next/server") @scope("NextResponse")
external jsonResponse: JSON.t => nextResponse = "json"

@module("next/server") @scope("NextResponse")
external jsonResponseWithInit: (JSON.t, {..}) => nextResponse = "json"
