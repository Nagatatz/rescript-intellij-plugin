---
myst:
  html_meta:
    "keywords": "basic template, rescript, node.js, cli, fs, validation, zod, sury, starter"
---

# Basic

{bdg-info}`Node.js` {bdg-success}`Validation`

The smallest working ReScript + Node.js starter. It is **not** a "Hello World" — in addition to `Console.log`, it demonstrates argv parsing, file I/O via `node:fs/promises`, and validation of a bundled `config.sample.json` through `Validation.res` (zod or sury, chosen in the wizard).

Use this as the reference when you want to understand exactly what minimal scaffolding the plugin produces, or as the starting point for a small Node.js script that would otherwise grow into a one-off.

## What You Get

```
my-project/
├── rescript.json
├── package.json
├── config.sample.json
├── src/
│   ├── App.res                  # entry — parses argv, optionally reads & validates config
│   ├── Args.res                 # tiny argv parser (--flag / --flag=value / positional)
│   ├── Files.res                # fs/promises helpers (readFile / writeFile / exists)
│   ├── Validation.res           # zod or sury — selected in the wizard
│   └── __tests__/App.test.mjs   # vitest smoke test that imports App.res.mjs
├── README.md                    # script docs + Project Layout + Run the App
├── LICENSE                      # MIT, holder = project name
├── .nvmrc                       # Node 24
├── .gitignore                   # node_modules + ReScript build artifacts
├── .editorconfig                # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml           # weekly npm updates
    └── workflows/ci.yml         # install + rescript build + vitest
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, license holder, and the `--name` echoed by `App.res` examples |
| **Package manager** | npm / pnpm / yarn / bun. Affects `packageManager` field, README install/run commands, and CI cache key |
| **Validation library** | `zod` ↔ `sury`. Chooses which `src/Validation.res` variant ships and which dependency is added |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs the compiled `.res.mjs` imports | `TemplateVersions.RESCRIPT_RUNTIME` |
| `zod` *or* `sury` | Validation backend (chosen in the wizard) | `TemplateVersions.ZOD` / `SURY` |
| `vitest` *(dev)* | Smoke test runner | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |

## Key Files

### `src/App.res`

Entry point invoked by `npm start`. It calls `Args.parse(NodeJs.Process.process->NodeJs.Process.argv)` and either prints a greeting or (when `--config <path>` is supplied) reads the file with `Files.readFile` and parses it through `Validation.parseConfig`.

### `src/Args.res` / `src/Files.res`

Two small modules that exist so the template can show *useful* APIs without pulling in a CLI framework:

- `Args.res` — minimal `--flag` / `--flag=value` / positional parser
- `Files.res` — `readFile`, `writeFile`, `exists` wrappers around `node:fs/promises`

Both are intentionally short — they double as a worked example of how to wrap a Node API with `@module` / `@val` bindings.

### `src/Validation.res`

`parseConfig: Js.Json.t => result<config, string>` for the bundled `config.sample.json`. The signature is identical between the zod and sury variants so callers don't need to branch.

### `config.sample.json`

A reference config that exercises the validator:

```json
{
  "name": "demo",
  "version": 1,
  "tags": ["alpha", "beta"]
}
```

## npm Scripts

| Script | Description |
| --- | --- |
| `start` | `node src/App.res.mjs` — run the compiled entry point |
| `test` | `vitest run` — execute the smoke suite |
| `test:coverage` | `vitest run --coverage` — same, with v8 coverage report |
| `res:build` | `rescript` — one-shot compile |
| `res:dev` | `rescript -w` — recompile on save |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` |

## Day-Two Recipes

This template is intentionally close to the metal — there's no framework to extend. Common follow-ups:

- Add a CLI subcommand → graduate to the **CLI Tool** template (or copy its `Commands/` pattern)
- Add HTTP → graduate to **Hono (Node.js)**
- Publish to npm → graduate to **npm Library**

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- `node` engine is pinned to `>=24` and `.nvmrc` says `24`. Earlier majors are not exercised by CI.
- The smoke test only verifies that `import("../App.res.mjs")` resolves — it does not exercise `Files.res` or `Validation.res`. Add domain-specific tests as you grow the project.
- The `config.sample.json` filename is deliberate: `.gitignore` keeps `config.json` (the real, possibly-secret one) out of the repo while letting the *sample* travel with the source.
