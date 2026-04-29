The shipped `astro.config.mjs` enables `output: "server"` and uses
`@astrojs/node` so every page is server-rendered on each request — that's
what makes `astro preview` work without a separate static deploy step.

For a fully static site:

1. Remove `output` (or set it to `"static"`).
2. Drop `@astrojs/node` from dependencies.
3. Replace `adapter: node({ mode: "standalone" })` with the static-only
   integrations you need.

Hybrid output is also available — pages opt into prerender per-route by
exporting `export const prerender = true`. Refer to the Astro docs for
the latest matrix of rendering modes.
