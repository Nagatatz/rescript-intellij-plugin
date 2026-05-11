# Manual Test Projects

Two small ReScript projects you can open inside `./gradlew runIde` to
exercise the user-facing features added since `v0.1.13` by hand. They
intentionally avoid production-grade structure — each file isolates a
single feature so you can pop the relevant tool window, fire the
relevant `Alt+Enter`, and immediately see the behaviour.

## Layout

```
manual-test-projects/
├── README.md             ← this file
├── main/                 ← open this for most features
│   ├── package.json
│   ├── rescript.json
│   └── src/
│       ├── HoogleSearchSamples.res     ← Hoogle-style type signature search
│       ├── VariantSamples.res          ← variants + incomplete switch
│       ├── VariantUsage.res            ← cross-file variant references
│       ├── NarrowingSamples.res        ← type narrowing inlay hints (LSP)
│       ├── FlowSamples.res             ← Variant Flow Diagram
│       ├── ImpactSamples.res           ← Type Impact Preview
│       ├── CoverageSamples.res         ← Type Coverage Heat Map
│       ├── InteropSamples.res          ← JS Interop Risk Map
│       ├── LegacyReason.re             ← Reason migration source
│       ├── LegacyReason.rei            ← Reason migration interface
│       └── ExampleNotebook.resnb       ← ReScript Notebook
└── monorepo/             ← open this root for monorepo detection
    ├── pnpm-workspace.yaml
    ├── package.json
    └── packages/ui/
        ├── rescript.json
        └── src/Hello.res
```

## Setup

```bash
# Build the plugin once
./gradlew clean buildPlugin

# Install the test project's ReScript toolchain so LSP-driven features work
cd manual-test-projects/main
npm install
cd ../..

# Open one of the test projects in a sandboxed IDE
./gradlew runIde
# Then File → Open → manual-test-projects/main  (or monorepo)
```

`runIde` reuses the existing sandbox, so installed dependencies survive
restarts. Use `./gradlew clean runIde` only when you need a fresh
sandbox (e.g. after bumping `pluginVersion`).

## Feature → File → Steps

| Feature | File | What to do |
|---------|------|------------|
| **Hoogle Type Signature Search** | `main/src/HoogleSearchSamples.res` | `Shift+Shift` → switch to **ReScript Types** tab. Try queries: `string => int`, `(int, int) => int`, `array<'a> => option<'a>`, `=> option<'a>`, `option<'a> => 'a`. Confirm matches rank EXACT > TVAR_MATCH > PARTIAL. |
| **Add Missing Switch Arms** (intention) | `main/src/VariantSamples.res` | Place caret inside the body of `render`'s `switch` → `Alt+Enter` → "Add missing switch arms". `Loaded(_)` and `Failed(_)` should be inserted. |
| **Rename Variant Constructor** (intention) | `main/src/VariantSamples.res` + `VariantUsage.res` | Caret on `Loading` (in either file) → `Alt+Enter` → "Rename variant constructor". Confirm dialog shows 4 occurrences across 2 files; undo with one `Ctrl+Z`. |
| **Type Narrowing inlay** | `main/src/NarrowingSamples.res` | Needs LSP running (after `npm install`). Each `switch` arm should display an inlay hint indicating the narrowed type of the scrutinee. |
| **Variant Flow Diagram** | `main/src/FlowSamples.res` | Right tool window → **ReScript Switch Flow** → place caret inside the `switch` → diagram renders four branches. Toolbar: Copy Mermaid / Copy DOT. |
| **Type Impact Preview** | `main/src/ImpactSamples.res` | Right tool window → **ReScript Type Impact** → place caret on the `user` type declaration → references list populates (typeRef + constructor + field-access classifications). |
| **Type Coverage Heat Map** | `main/src/CoverageSamples.res` | Bottom tool window → **ReScript Type Coverage**. `CoverageSamples.res` should appear near the top of the "low coverage" sort because most `let`s are inferred. Color: red for < 30%. |
| **JS Interop Risk Map** | `main/src/InteropSamples.res` | Right tool window → **ReScript Interop Risk**. Should list `%raw`, `external`, `Obj.magic`, `@module`, `@bs.module` with HIGH/MEDIUM/LOW colour-coded risk. |
| **Reason Migration Pilot** | `main/src/LegacyReason.re` + `.rei` | Right tool window → **ReScript Migration Pilot** → checkbox the two files → Convert. Files should be rewritten to `.res` / `.resi`. Requires `rescript` CLI in `PATH` (e.g. via `npx`). |
| **ReScript Notebook** | `main/src/ExampleNotebook.resnb` | Double-click in Project view to open. The cell-based editor should appear with a markdown cell + two code cells. Evaluate each cell; output is written back into the file. Toolbar → Export to Markdown. |
| **Monorepo `rescript.json` detection** | `monorepo/` | Open `monorepo/` as the project root → ReScript status bar widget reports the workspace package(s). Settings → ReScript → confirm `packageRoots` was auto-populated from `pnpm-workspace.yaml`. |

## Features not exercised by these fixtures

These produce fresh projects through the **New Project** wizard, so
there is nothing to commit — generate them on demand to test:

- Project Wizard new templates: **TanStack Start**, **Remix RR v7**,
  **Astro**, **Waku** (Validation combo should be hidden for all four)
- Project Wizard new **Database** axis (libSQL / Postgres / MySQL) on
  Hono, Hono GraphQL, Hono + Inertia, monorepo, full-stack
- **Hono + Inertia SSR** — generate the template, `npm install && npm
  run dev`, then `curl http://localhost:5173` should return prerendered
  HTML before hydration

## Regenerating compiled output

```bash
cd manual-test-projects/main
npx rescript build      # or `npm run build`
```

Compiled `.bs.js` / `lib/` artifacts are git-ignored.
