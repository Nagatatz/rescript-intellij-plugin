// Relay environment. The fetcher sends operations to /graphql; Vite+'s dev
// proxy (see vite.config.mjs) forwards that to the Hono server on :3000 so
// browser requests stay same-origin.
type response
@send external json: response => promise<'a> = "json"
@val external fetch: (string, 'opts) => promise<response> = "fetch"

let fetchQuery = (operation, variables, _cacheConfig, _uploads) => {
  let response = fetch(
    "/graphql",
    {
      "method": "POST",
      "headers": {
        "Accept": "application/json",
        "Content-Type": "application/json",
      },
      "body": JSON.stringifyAny({"query": operation["text"], "variables": variables})
        ->Option.getOr("{}"),
    },
  )
  response->Promise.then(r => r->json)
}

let network = RescriptRelay.Network.makePromiseBased(~fetchFunction=fetchQuery, ())

let environment = RescriptRelay.Environment.make(
  ~network,
  ~store=RescriptRelay.Store.make(RescriptRelay.RecordSource.make()),
  (),
)
