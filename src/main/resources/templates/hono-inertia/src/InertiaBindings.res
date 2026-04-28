// Minimal bindings for @inertiajs/react v3. Only the surface this template
// actually uses is bound; extend as needed when adding features.

type pageProps = Js.Dict.t<Js.Json.t>

// The Inertia page envelope sent by the server. Component-specific props are
// untyped here; pages cast `usePage()` results to their own typed shape.
type page<'props> = {
  component: string,
  props: 'props,
  url: string,
  version: option<string>,
}

type setupArgs = {
  el: Dom.element,
  @as("App")
  app: React.component<pageProps>,
  props: pageProps,
}

type appOptions = {
  resolve: string => promise<{..}>,
  setup: setupArgs => unit,
}

@module("@inertiajs/react")
external createInertiaApp: appOptions => unit = "createInertiaApp"

module Link = {
  @module("@inertiajs/react") @react.component
  external make: (
    ~href: string,
    ~method: string=?,
    ~replace: bool=?,
    ~preserveState: bool=?,
    ~preserveScroll: bool=?,
    ~children: React.element=?,
  ) => React.element = "Link"
}

@module("@inertiajs/react")
external usePage: unit => page<'props> = "usePage"
