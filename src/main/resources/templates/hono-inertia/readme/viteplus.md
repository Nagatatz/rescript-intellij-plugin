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

### Caveat — Vite+ is pre-1.0

Vite+ is currently shipping in alpha (0.1.x at the time of writing). The CLI
and config surface are still in flux:

- The `test` / `lint` / `fmt` configuration keys are not yet documented in
  `vite.config.mjs`. Defaults work; configuration may move.
- API changes between 0.1.x patch releases are possible.

If a Vite+ release breaks this template, you can fall back to plain Vite by:

1. Replacing `vite-plus` with `vite` in `vite.config.mjs`
   (`import { defineConfig } from "vite"`).
2. Replacing the `vp ...` scripts in `package.json` with `vite` /
   `vitest run` directly.
3. Adding `vitest`, `eslint`, `prettier` (and their plugins) as devDependencies
   to fill the gaps Vite+ used to cover.

The rest of the template — Hono, Inertia, Drizzle — does not depend on
Vite+ and stays untouched.
