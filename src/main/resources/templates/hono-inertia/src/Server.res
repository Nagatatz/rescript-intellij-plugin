// Hono app + Inertia middleware wiring. The HTTP server itself is started
// from `ServerMain.res` so test code can `import` this module without binding
// a port.
//
// Middleware order matters: register `HonoInertia.inertia(...)` *before* any
// route that calls `c->HonoInertia.render(...)`. Otherwise `render` falls
// back to Hono's HTML renderer and Inertia visits return raw HTML instead of
// JSON.

// `serializePage` only escapes `/`, so embedding into a single-quoted
// attribute also requires escaping `'` to `&#39;`. JSON itself never produces
// `'`, but user-provided prop values can.
let escapeApostrophes = (s: string): string =>
  s->String.replaceAll("'", "&#39;")

// HTML host page returned for non-Inertia visits. The Inertia client mounts
// into `#app` and reads the page object from the `data-page` attribute.
let rootView: HonoInertia.rootView = (page, _ctx) => {
  let pageJson = page->HonoInertia.serializePage->escapeApostrophes
  `<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Hono Inertia</title>
  </head>
  <body>
    <div id="app" data-page='${pageJson}'></div>
    <script type="module" src="/src/client/Main.res.mjs"></script>
  </body>
</html>`
}

let app = Hono.createApp()
app->Hono.use(Logger.logger())
app->Hono.use(HonoInertia.inertia({rootView: rootView}))

// Global error handler: converts uncaught exceptions into a JSON 500 response.
app->Hono.onError((err, ctx) => {
  Console.error(err)
  ctx->Hono.status(500)->Hono.json({"error": "Internal Server Error"})
})

app->Hono.get("/health", ctx => ctx->Hono.json({"status": "ok"}))
Routes.Pages.register(app)

let start = () => {
  HonoNodeServer.serve({fetch: app->HonoNodeServer.honoFetch, port: 3000})
  Console.log("Server on http://localhost:3000 — Inertia pages on /, /about")
}
