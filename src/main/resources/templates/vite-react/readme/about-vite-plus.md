This template uses [Vite+](https://vite.plus) (`vite-plus`), an early-access toolchain
bundling Vite, Vitest, Oxlint, Oxfmt, and Rolldown. Vite+ is **pre-1.0** — APIs may
change before stable release.

> **Known issue:** the current pre-1.0 Vite+ does not resolve `vite/internal` cleanly
> when paired with `@vitejs/plugin-react`, so `vp build` may fail. As a fallback,
> replace `vite-plus` with `vite` in `vite.config.mjs` and switch the npm scripts to
> `vite` / `vite build`. The migration path back to Vite+ is straightforward once the
> stable release lands.