This template uses [Vite+](https://vite.plus) (`vite-plus`), a unified toolchain
bundling Vite, Vitest, Oxlint, Oxfmt, and Rolldown behind the `vp` CLI.

Vite+ requires the `vite` package to resolve to its own core build, so `package.json`
declares `vite` as an npm alias of `@voidzero-dev/vite-plus-core`. Keep that alias when
upgrading; `vp migrate` re-aligns it if it drifts. To fall back to classic Vite, point
`vite` back at the regular package, import `defineConfig` from `vite` in
`vite.config.mjs`, and switch the npm scripts to `vite` / `vite build` / `vitest run`.