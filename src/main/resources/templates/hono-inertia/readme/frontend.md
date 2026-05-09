### Adding a new page

1. Create `src/client/Pages/<Name>.res` with `@react.component let make = (~prop1, ~prop2) => ...`.
2. Wrap the body in `<MainLayout> ... </MainLayout>` so it inherits the shared chrome.
3. Register a route in `src/Routes.res`:

   ```rescript
   app->Hono.get("/the/path", ctx =>
     ctx->HonoInertia.render("<Name>", { ... })
   )
   ```

4. Add a switch arm to `src/Ssr.res` so the page can be server-rendered:

   ```rescript
   | "<Name>" =>
     let props: {"prop1": string, "prop2": int} = castProps(page.props)
     renderToString(<Name prop1={props["prop1"]} prop2={props["prop2"]} />)
   ```

`src/client/pages.js` already discovers the new component for the **client**
side through `import.meta.glob` — but the **server** side renders synchronously
and needs the page to be listed in `Ssr.res` explicitly. Forgetting Step 4
turns into a build error rather than a runtime 500, so the type checker keeps
the registry honest.

### Server-side rendering

Non-Inertia visits (the first request, search-engine crawlers, OGP fetchers)
get a **fully rendered page** from the server: `Server.res`'s `rootView`
delegates to `Ssr.renderInertia`, which calls `react-dom/server`'s
`renderToString` on the matching page component and embeds the resulting HTML
inside `<div id="app" data-page='…'>…</div>`. Subsequent Inertia visits
(`X-Inertia: true`) keep their existing JSON-only behaviour — `@hono/inertia`
short-circuits before `rootView` runs, so there is no SSR overhead on
client-side navigation.

The browser entry (`src/client/Main.res`) calls `hydrateRoot` to attach React
to the SSR-rendered DOM rather than throwing it away with a fresh
`createRoot`, so the first paint is the real page and the framework only adds
event listeners on top.

### Navigating between pages

Use `<InertiaBindings.Link href="/about">` instead of plain `<a>` tags so
Inertia can intercept the click, fetch the page object as JSON, and swap the
component without a full page reload. See `MainLayout.res` for an example.

### Reading current page state

`InertiaBindings.usePage()` returns the active page envelope:

```rescript
let page = InertiaBindings.usePage()
Console.log(page.url)      // current URL
Console.log(page.component) // active page name, e.g. "Home"
```

### Why a JS shim for `pages.js`?

ReScript 12's `Js.import` resolves the import path **statically** — the
compiler tracks the path at compile time, so `import("./Pages/" ++ name)` is
not allowed. `import.meta.glob` is a Vite-only extension that lets us build a
table of lazy loaders at build time. Implementing the resolver in plain JS
keeps the rest of the project pure ReScript.
