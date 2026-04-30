### Adding a new page

1. Create `src/client/Pages/<Name>.res` with `@react.component let make = (~prop1, ~prop2) => ...`.
2. Wrap the body in `<MainLayout> ... </MainLayout>` so it inherits the shared chrome.
3. Register a route in `src/Routes.res`:

   ```rescript
   app->Hono.get("/the/path", ctx =>
     ctx->HonoInertia.render("<Name>", { ... })
   )
   ```

`src/client/pages.js` discovers the new file automatically through
`import.meta.glob`; no central registry needs editing.

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
