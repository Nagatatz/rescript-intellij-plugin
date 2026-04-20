// POST /api/greet handler. Parses the JSON body via `Validation.parseGreetInput`
// (zod or sury, selected at Wizard creation time) and returns 400 + error JSON
// on failure. The sibling `route.ts` re-exports `post` as `POST` to satisfy
// Next.js's file-system routing convention.
let post = async (req: NextServer.nextRequest): NextServer.nextResponse => {
  let raw = try await NextServer.reqJson(req) catch {
  | _ => JSON.Object(Dict.make())
  }
  switch Validation.parseGreetInput(raw) {
  | Ok(input) =>
    NextServer.jsonResponse(
      JSON.Object(
        Dict.fromArray([("message", JSON.String("Hello, " ++ input.name ++ "!"))]),
      ),
    )
  | Error(msg) =>
    NextServer.jsonResponseWithInit(
      JSON.Object(Dict.fromArray([("error", JSON.String(msg))])),
      {"status": 400},
    )
  }
}
