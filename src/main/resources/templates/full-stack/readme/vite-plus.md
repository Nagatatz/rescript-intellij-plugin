The client uses [Vite+](https://vite.plus) (`vite-plus`) for the build/test toolchain.
Its `package.json` declares `vite` as an npm alias of `@voidzero-dev/vite-plus-core`, which
Vite+ requires. To fall back to classic Vite, point `vite` back at the regular package,
replace `vite-plus` with `vite` in `vite.config.mjs`, and swap the `vp` scripts for `vite`.
