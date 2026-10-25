This template uses [Vite+](https://vite.plus) (`vite-plus`) for the renderer build.
`package.json` declares `vite` as an npm alias of `@voidzero-dev/vite-plus-core`, which
Vite+ requires. To use classic Vite instead, point `vite` back at the regular package,
replace `vite-plus` with `vite` in `vite.config.mjs`, and adjust the scripts.