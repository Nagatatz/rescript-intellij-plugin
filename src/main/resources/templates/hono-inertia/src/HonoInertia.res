// Bindings for the @hono/inertia middleware.
//
// `inertia()` returns a Hono middleware that overrides the context's `render`
// method. After the middleware is registered, route handlers should call
// `c->HonoInertia.render("PageName", props)` to return an Inertia response —
// the same `render` then negotiates between an HTML host page (initial load)
// and a JSON page object (subsequent Inertia visits).
@module("@hono/inertia")
external inertia: unit => Hono.middleware = "inertia"

// Inertia-flavoured `c.render(component, props)`. Returns the same response
// shape as Hono's text/json helpers so route handlers can `await` it.
@send
external render: (Hono.context, string, 'props) => promise<'response> = "render"
