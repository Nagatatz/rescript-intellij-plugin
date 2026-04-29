Astro pages are static by default. To opt a React component into client-side
hydration, add a `client:*` directive when you embed it:

- `client:load` — hydrate immediately on page load.
- `client:idle` — hydrate during browser idle time.
- `client:visible` — hydrate when the component scrolls into view.
- `client:media` — hydrate when a media query matches.
- `client:only="react"` — render only on the client (no SSR).

ReScript components compile to `.res.mjs`; import them in the Astro page
frontmatter (`import { make as Counter } from "../components/Counter.res.mjs"`)
and embed them like any other React component. The static `StaticGreeting`
in this template ships zero JS; the `Counter` Island hydrates with `client:load`.
