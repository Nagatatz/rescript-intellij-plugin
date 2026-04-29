---
myst:
  html_meta:
    "keywords": "templates, astro, react islands, ssg, ssr, content site"
---

# Astro

A content-focused Astro site that mixes static markup with React Islands. Astro pages are authored in `.astro`; interactive React components live alongside them as ReScript modules and hydrate on demand via `client:load` / `client:visible`. SSR is enabled through `@astrojs/node` so the template runs end-to-end with `astro dev`, `astro build`, and `astro preview`.

```{note}
The plugin does not provide language support for `.astro` files themselves. Use the official Astro VS Code extension or your editor's HTML/JSX modes for those; ReScript files (`.res`) get the full plugin experience as usual.
```

## Generated layout

```
src/
├── pages/
│   └── index.astro
├── components/
│   ├── Counter.res
│   └── StaticGreeting.res
└── __tests__/
    └── Counter.test.mjs
astro.config.mjs
tsconfig.json
rescript.json
package.json
```

## Key files

- `src/pages/index.astro` — Home page. Embeds `StaticGreeting` (rendered statically) and `Counter` (hydrated via `client:load`).
- `src/components/Counter.res` — Interactive React Island written in ReScript.
- `src/components/StaticGreeting.res` — Static React component — Astro renders it on the server and ships zero JS for it.
- `astro.config.mjs` — Wires `@astrojs/react` and the `@astrojs/node` adapter. Defaults to `output: "server"`.

## npm scripts

| Command | Description |
| --- | --- |
| `dev` | Start Astro dev server with `rescript -w`. |
| `build` | Compile ReScript and run `astro build`. |
| `preview` | Serve the built site locally. |
| `test` | Run Vitest. |

## Day-two pointers

- **Hydration directives** — pick `client:load`, `client:idle`, `client:visible`, `client:media`, or `client:only="react"` per Island depending on how eager you want the JS to ship.
- **Static-only output** — drop `output` (or set it to `"static"`) and remove `@astrojs/node` for a pure static deploy.
- **Per-route prerender** — export `export const prerender = true` from a page to opt back into static rendering inside an SSR site.
