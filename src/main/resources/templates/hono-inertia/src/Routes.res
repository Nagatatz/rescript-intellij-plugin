// Inertia route registrations. Each handler renders a React page by name and
// hands typed props to the client. To add a new page:
//   1. Drop a new ReScript module under `src/client/Pages/<Name>.res`.
//   2. Register a route below that calls `c->HonoInertia.render("<Name>", props)`.
module Pages = {
  let register = (app: Hono.app) => {
    app->Hono.get("/", ctx =>
      ctx->HonoInertia.render(
        "Home",
        {
          "title": "Home",
          "message": "Hono renders React pages directly through Inertia.",
        },
      )
    )

    app->Hono.get("/about", ctx =>
      ctx->HonoInertia.render(
        "About",
        {
          "title": "About",
          "stack": ["Hono", "Inertia.js", "@rescript/react", "Vite+"],
        },
      )
    )

    // Sample form-style endpoint: validate the request body, then re-render
    // the Home page with the parsed value as a flash prop. POST stays async
    // because `Hono.jsonBody` returns a promise.
    app->Hono.post("/greet", async ctx => {
      let raw = await ctx->Hono.req->Hono.jsonBody
      switch Validation.parseGreetForm(raw) {
      | Error(message) => ctx->Hono.status(422)->Hono.json({"error": message})
      | Ok({name}) =>
        ctx->HonoInertia.render(
          "Home",
          {
            "title": "Home",
            "message": `Hello, ${name}!`,
          },
        )
      }
    })
  }
}
