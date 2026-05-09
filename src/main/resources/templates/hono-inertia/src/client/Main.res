// Inertia client entry. `createInertiaApp` swaps the `<App />` rendered into
// `#app` whenever the user navigates between Inertia routes. Page resolution
// is delegated to `pages.js`, which uses Vite's `import.meta.glob` (something
// ReScript 12's static `Js.import` cannot do directly).
//
// The mount point is hydrated rather than freshly created because
// `src/Ssr.res` already rendered the initial page on the server and wrote
// the markup into `<div id="app">…</div>`. Hydration re-uses that DOM and
// attaches event listeners without throwing the SSR HTML away.
@module("./pages.js")
external resolvePage: string => promise<{..}> = "resolvePage"

@module("react-dom/client")
external hydrateRoot: (Dom.element, React.element) => unit = "hydrateRoot"

InertiaBindings.createInertiaApp({
  resolve: resolvePage,
  setup: ({el, app, props}) => {
    hydrateRoot(el, React.createElement(app, props))
  },
})
