// Inertia client entry. `createInertiaApp` swaps the `<App />` rendered into
// `#app` whenever the user navigates between Inertia routes. Page resolution
// is delegated to `pages.js`, which uses Vite's `import.meta.glob` (something
// ReScript 12's static `Js.import` cannot do directly).
@module("./pages.js")
external resolvePage: string => promise<{..}> = "resolvePage"

InertiaBindings.createInertiaApp({
  resolve: resolvePage,
  setup: ({el, app, props}) => {
    let root = ReactDOM.Client.createRoot(el)
    root->ReactDOM.Client.Root.render(React.createElement(app, props))
  },
})
