// Server-side rendering for Inertia pages.
//
// Hono receives a non-Inertia visit, the @hono/inertia middleware builds a
// pageObject, and `Server.res`'s rootView delegates here to fold the
// matching React component into the host page's HTML before sending it
// back. Browsers without JS see the rendered markup; browsers with JS
// hydrate it (`Main.res`) so subsequent Inertia visits stay client-side.
//
// Inertia's React `<App>` component is rendered around the page so the
// `usePage()` hook (used by MainLayout) finds its provider context during
// SSR. `resolveComponent` returns components synchronously, which keeps the
// React tree renderable through `react-dom/server`'s sync `renderToString`.
//
// Adding a new page requires updating the `resolveComponent` switch as
// well as `client/Pages/` and `Routes.res` — the explicit registry
// catches missing pages at compile time rather than at request time.

@module("react-dom/server")
external renderToString: React.element => string = "renderToString"

// `App` is the React component @inertiajs/react uses to wrap a page with the
// `usePage()` provider context. Bound here without the `@react.component`
// PPX so we can pass the component through `initialComponent` /
// `resolveComponent` props directly without ReScript's wrapping layer
// rejecting the dynamic shape.
module InertiaApp = {
  @module("@inertiajs/react") @react.component
  external make: (
    ~initialPage: HonoInertia.pageObject,
    ~initialComponent: 'component,
    ~resolveComponent: string => 'component,
  ) => React.element = "App"
}

// Type-erasing cast at the SSR boundary: `Home.make` and `About.make` carry
// distinct prop types that the type system can't unify in a single registry,
// so they're funnelled through `'component`. Inertia hands the page's typed
// props to whichever component `resolveComponent` selects.
external castComponent: 'a => 'b = "%identity"

let resolveComponent = (name: string) =>
  switch name {
  | "Home" => castComponent(Home.make)
  | "About" => castComponent(About.make)
  | other => Js.Exn.raiseError(`Inertia SSR: unknown page "${other}"`)
  }

/**
 * Result of rendering an Inertia page on the server. `head` collects extra
 * `<head>` fragments (currently empty; reserved for future title/meta DSL),
 * and `body` is the HTML that goes inside `<div id="app">…</div>`.
 */
type rendered = {
  head: array<string>,
  body: string,
}

let renderInertia = (page: HonoInertia.pageObject): rendered => {
  let initialComponent = resolveComponent(page.component)
  let body = renderToString(
    <InertiaApp initialPage=page initialComponent resolveComponent />,
  )
  {head: [], body}
}
