// Entry point — wires `Bun.serve` to the res-x Handler. The render function
// performs path-based routing and returns JSX that res-x converts to an HTML
// Response. HTMX endpoints registered via `Handler.handler.hxPost` / `hxGet`
// are matched by res-x before this fallback renderer runs.

let port = 4444

let server = Bun.serve({
  port,
  development: ResX.BunUtils.isDev,
  fetch: async (request, _server) => {
    await Handler.handler.handleRequest({
      request,
      setupHeaders: () =>
        Headers.make(~init=FromArray([("Content-Type", "text/html")])),
      render: async ({path, requestController, headers: _}) => {
        switch path {
        | list{} =>
          <Layout title="res-x starter">
            <h1> {Hjsx.string("res-x starter")} </h1>
            <Counter />
            <TodoForm />
          </Layout>
        | _ =>
          requestController.setStatus(404)
          <Layout title="Not found">
            <h1> {Hjsx.string("404 — not found")} </h1>
          </Layout>
        }
      },
    })
  },
})

let portString = server->Bun.Server.port->Int.toString
Console.log(`Listening on http://localhost:${portString}`)

if ResX.BunUtils.isDev {
  ResX.BunUtils.runDevServer(~port)
}
