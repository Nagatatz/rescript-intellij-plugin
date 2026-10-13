This template uses **Vite+** (`vite-plus`), VoidZero's unified toolchain that
collapses Vite, Vitest, Oxlint, Oxfmt, and Rolldown into a single CLI.

### Common commands

| Command         | What it does                                                       |
|-----------------|--------------------------------------------------------------------|
| `vp dev`        | Start the dev server (Vite + the React HMR plugin)                 |
| `vp build`      | Produce a production bundle into `dist/`                           |
| `vp preview`    | Serve the production build locally                                 |
| `vp test`       | Run the Vitest suite (uses `src/__tests__/` by default)            |
| `vp test --coverage` | Vitest with the V8 coverage provider                          |
| `vp check`      | Lint + format-check + typecheck in one pass (Oxlint + Oxfmt)       |

### The `vite` alias

Vite+ requires the `vite` package to resolve to its own core build, so
`package.json` declares `vite` as an npm alias of
`@voidzero-dev/vite-plus-core`. Keep that alias when upgrading; `vp migrate`
re-aligns it if it drifts.

If a Vite+ release breaks this template, you can fall back to plain Vite by:

1. Pointing `vite` back at the regular package in `package.json` and replacing
   `vite-plus` with `vite` in `vite.config.mjs`
   (`import { defineConfig } from "vite"`).
2. Replacing the `vp ...` scripts in `package.json` with `vite` /
   `vitest run` directly.
3. Adding `vitest`, `eslint`, `prettier` (and their plugins) as devDependencies
   to fill the gaps Vite+ used to cover.

The rest of the template — Hono, Inertia, Drizzle — does not depend on
Vite+ and stays untouched.
