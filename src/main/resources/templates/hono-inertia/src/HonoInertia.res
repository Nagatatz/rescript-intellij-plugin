// Bindings for the @hono/inertia middleware (v0.2.x).
//
// `inertia(options)` returns a Hono middleware that overrides the context's
// `render` method. After registration, route handlers should call
// `c->HonoInertia.render("PageName", props)` to return an Inertia response —
// the same `render` then negotiates between an HTML host page (initial load)
// and a JSON page object (subsequent Inertia visits).

// Inertia page envelope sent to the client. Mirrors @hono/inertia's
// `PageObject` shape; `version` is nullable per the JSON wire format.
type pageObject = {
  component: string,
  props: Js.Json.t,
  url: string,
  version: Nullable.t<string>,
}

// Renders the initial HTML host page for non-Inertia visits. Receives the
// current page object plus the Hono context, and returns a complete HTML
// string. The middleware uses a minimal default when this is omitted.
type rootView = (pageObject, Hono.context) => string

// Options accepted by `inertia(...)`. Both fields are optional.
type options = {
  version?: Nullable.t<string>,
  rootView?: rootView,
}

@module("@hono/inertia")
external inertia: options => Hono.middleware = "inertia"

// Inertia-flavoured `c.render(component, props)`. The middleware decides
// HTML vs JSON based on the `X-Inertia` request header; the return value is
// a Hono `Response`, returned synchronously.
@send
external render: (Hono.context, string, 'props) => 'response = "render"

// Escapes a page object for embedding inside a `data-page` attribute or a
// `<script type="application/json">` element. Mirrors @inertiajs/core's
// escape (only `/` → `\/`); callers embedding into a single-quoted attribute
// must additionally escape `'` themselves.
@module("@hono/inertia")
external serializePage: pageObject => string = "serializePage"
