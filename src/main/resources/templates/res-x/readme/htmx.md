This template loads HTMX from a CDN (`https://unpkg.com/htmx.org@{{htmxVersion}}`)
via `src/Layout.res` so you can launch without bundling anything client-side.

When you are ready to self-host:

1. Drop `htmx.min.js` into the top-level `public/` folder (Vite copies it
   as-is to the production build).
2. Swap the `<script src>` in `Layout.res` to `/htmx.min.js`.

HTMX attributes are emitted through typed helpers exposed by `rescript-x`:

- `hxPost={handler.hxPost(...)}` — type-checks against the handler signature
  so you cannot point a swap at a missing endpoint.
- `hxSwap={ResX.Htmx.Swap.make(OuterHTML)}` — produces valid `hx-swap`
  values without stringly-typed mistakes.
- `hxTarget={ResX.Htmx.Target.make(CssSelector("#counter-value"))}` — the
  target selector is checked at the type level before reaching the browser.

See the [HTMX reference](https://htmx.org/reference/) for the full attribute
list; every one of them has a matching `hx*` prop in res-x.
