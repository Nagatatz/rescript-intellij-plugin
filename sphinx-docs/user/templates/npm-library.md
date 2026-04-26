---
myst:
  html_meta:
    "keywords": "npm library, rescript, gentype, typescript bindings, vitest, abortcontroller, validation, zod, sury, publish"
---

# npm Library

{bdg-info}`Node.js` {bdg-success}`Testing` {bdg-primary}`genType`

A publishable npm package written in ReScript with first-class TypeScript bindings via `genType`. The template ships a small but realistic API surface — a synchronous greeting, an async `fetchWithTimeout` (AbortController), and two list helpers — so you have something to compile, test, and publish on day one.

Pick this template when you intend to release the project to npm (public or private registry) and want JS/TS consumers to enjoy real `.d.ts` types generated from your ReScript sources. The template assumes ESM-only consumers; if you need CJS interop you can layer it on top, but the defaults target modern bundlers and Node 24+.

## What You Get

```
my-project/
├── rescript.json
├── package.json                   # ESM, "main" / "types" / "exports" / "files" set
├── tsconfig.json                  # strict, declaration: true (for genType .d.ts)
├── src/
│   ├── Index.res                  # public entry — re-exports + greetChecked
│   ├── ListUtils.res              # chunk + partitionMap (pure helpers)
│   ├── Fetcher.res                # fetchWithTimeout (AbortController + setTimeout)
│   ├── Validation.res             # zod or sury — selected in the wizard
│   └── __tests__/
│       ├── Index.test.mjs         # greet smoke test (imports .res.mjs)
│       ├── ListUtils.test.mjs     # chunk + partitionMap suites
│       └── Fetcher.test.mjs       # fetchWithTimeout with vi.stubGlobal("fetch")
├── README.md                      # script docs + API Surface table + Publish flow
├── LICENSE                        # MIT, holder = project name
├── .nvmrc                         # Node 24
├── .gitignore                     # node_modules + ReScript build artifacts
├── .editorconfig                  # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml             # weekly npm updates
    └── workflows/ci.yml           # install + rescript build + vitest
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, the LICENSE holder, and the suffix in `Index.res`'s default greeting |
| **Package manager** | npm / pnpm / yarn / bun. Affects `packageManager` field, README install commands, and CI cache key |
| **Validation library** | `zod` ↔ `sury`. Chooses which `src/Validation.res` variant ships and which dependency is added |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs the compiled `.res.mjs` imports at runtime | `TemplateVersions.RESCRIPT_RUNTIME` |
| `zod` *or* `sury` | Validation backend (chosen in the wizard) | `TemplateVersions.ZOD` / `TemplateVersions.SURY` |
| `vitest` *(dev)* | Per-module test runner | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |
| `typescript` *(dev)* | Required by `tsc --noEmit` and by genType for `.d.ts` checking | `TemplateVersions.TYPESCRIPT` |

## Key Files

### `src/Index.res`

The public entry point. Every export the package surfaces lives here, either as a definition or as a re-export from another module. Each is annotated `@genType` so genType can emit a matching declaration in `Index.gen.d.ts`.

```rescript
/** Returns a greeting addressed to [name]. */
@genType
let greet = (name: string) => {
  `Hello from <projectName>, ${name}!`
}

/**
 * Validates an untyped JSON input before greeting. Returns an [Error] message
 * for TS/JS consumers that pass a malformed payload instead of throwing.
 */
@genType
let greetChecked = (input: JSON.t) =>
  switch Validation.parseGreetInput(input) {
  | Ok({name}) => Ok(greet(name))
  | Error(message) => Error(message)
  }

@genType let chunk = ListUtils.chunk
@genType let fetchWithTimeout = Fetcher.fetchWithTimeout
```

`greetChecked` is the canonical pattern for "I don't trust my caller": run the JSON through `Validation.parseGreetInput` and return a `result` rather than throwing across the FFI boundary. This is what makes the library safe to consume from plain JavaScript, where the type checker won't catch a malformed argument.

### `src/ListUtils.res`

Two generic helpers that double as worked examples of `Array` operations:

- `chunk(xs, ~size)` — splits an array into fixed-size buckets; the last bucket may be shorter.
- `partitionMap(xs, f)` — runs `f` on each element and routes `Ok` / `Error` results into a tuple of two arrays.

Both are written with mutable `ref` accumulators — straightforward to read, fast at runtime, and a useful demonstration that ReScript's stdlib lets you combine functional and imperative styles.

### `src/Fetcher.res`

`fetchWithTimeout` shows the canonical Promise + AbortController pattern in ReScript:

```rescript
@val external fetch: (string, 'opts) => promise<'response> = "fetch"

type abortController
@new external makeAbortController: unit => abortController = "AbortController"
@get external signal: abortController => 'signal = "signal"
@send external abort: abortController => unit = "abort"

@genType
let fetchWithTimeout = async (url: string, ~timeoutMs: int): promise<'response> => {
  let controller = makeAbortController()
  let timer = setTimeout(() => controller->abort, timeoutMs)
  try {
    let response = await fetch(url, {"signal": controller->signal})
    clearTimeout(timer)
    response
  } catch {
  | err =>
    clearTimeout(timer)
    raise(err)
  }
}
```

The `clearTimeout` call lives in *both* the success and the failure branch so the timer never leaks. Copy this shape when you write any abortable async helper — it generalizes cleanly to streaming, retries, and cancellation tokens.

### `src/Validation.res`

`parseGreetInput: JSON.t => result<greetInput, string>`, validating the *public-API arguments that JS/TS consumers pass*. The signature is identical between the zod and sury variants, so callers and types do not need to branch on which backend was chosen.

When you add a new public function that accepts untyped input, define a new `parseFooInput` here and call it from `Index.res` before doing any work. Returning a `result` keeps the contract honest: the JS side either gets a value back or a readable error message — never an exception across the FFI boundary.

### `src/__tests__/Index.test.mjs`

The smoke test imports the *compiled* `Index.res.mjs` and asserts on the exported `greet`:

```js
import { describe, expect, it } from "vitest";
import { greet } from "../Index.res.mjs";

describe("greet", () => {
  it("returns a greeting containing the supplied name", () => {
    expect(greet("world")).toContain("world");
  });
});
```

This is the canonical guard that the public API is reachable from a JS consumer's perspective — if `greet` ever stops being exported (or `@genType` is dropped), the suite fails. When you add a new public function, follow the same pattern: import it from `Index.res.mjs`, exercise it, assert on the runtime value.

### `src/__tests__/ListUtils.test.mjs`

Per-module Vitest suite for the helpers. Notable: it asserts on the ReScript-generated variant runtime tag directly:

```js
const [oks, errs] = partitionMap([1, 2, 3], (n) =>
  n % 2 === 0 ? { TAG: "Ok", _0: n * 10 } : { TAG: "Error", _0: `odd: ${n}` }
);
```

`{ TAG: "Ok", _0: ... }` is the shape ReScript emits for polymorphic variants in ESM mode. Keep this in mind when you write JS-side tests against ReScript-generated unions — you call the function with raw variant payloads, not constructor functions.

### `src/__tests__/Fetcher.test.mjs`

Async test that stubs the global `fetch` via Vitest's `vi.stubGlobal`:

```js
import { afterEach, describe, expect, it, vi } from "vitest";
import { fetchWithTimeout } from "../Fetcher.res.mjs";

afterEach(() => vi.restoreAllMocks());

describe("fetchWithTimeout", () => {
  it("returns the response when fetch resolves in time", async () => {
    const fake = { ok: true };
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(fake));
    await expect(fetchWithTimeout("https://example.test", 100)).resolves.toBe(fake);
  });
});
```

The `afterEach(() => vi.restoreAllMocks())` is mandatory — without it, a stubbed `fetch` from one test leaks into the next and breaks any suite that does not also stub. When you add new fetch-driven modules, follow the same pattern.

### `package.json` entry points

The publish-related fields are wired so consumers can `import { greet } from "<pkg>"` and get real types:

```json
{
  "type": "module",
  "main": "./src/Index.res.mjs",
  "types": "./src/Index.gen.d.ts",
  "exports": {
    ".": {
      "types": "./src/Index.gen.d.ts",
      "import": "./src/Index.res.mjs"
    }
  },
  "files": [
    "src/**/*.res.mjs",
    "src/**/*.res",
    "src/**/*.gen.d.ts",
    "src/**/*.gen.tsx"
  ]
}
```

The `files` allowlist is deliberately tight: only compiled JS, the matching `.gen.d.ts` types, the optional `.gen.tsx` (for React component bindings), and the original `.res` sources travel in the published tarball. Everything else — tests, configs, `node_modules` — is excluded by default.

### `prepare` script

`scripts.prepare = "rescript"` is wired so a fresh `npm install` of your library inside another project will compile the ReScript sources before the package is consumed. Combined with the `files` allowlist above, this means consumers do not need to install `rescript` themselves — they just `import` and use.

## npm Scripts

| Script | Description |
| --- | --- |
| `build` | `rescript` — one-shot compile |
| `clean` | `rescript clean` — remove generated `.res.mjs` |
| `test` | `vitest run` — execute the per-module suites |
| `test:coverage` | `vitest run --coverage` — same, with v8 coverage report |
| `prepare` | `rescript` — runs on `npm install` of the published tarball |
| `res:build` | `rescript` — alias kept for parity with other templates |
| `res:clean` | `rescript clean` — alias kept for parity with other templates |
| `res:dev` | `rescript -w` — recompile on save |

`prepare` and `build` are both `rescript` today. They are kept separate so you can later wire `build` to a wider pipeline (bundling, minification, copying assets) without breaking the `prepare` contract npm relies on.

## Publishing Flow

The README ships a four-step recipe that maps onto the `Publish` section:

1. Bump the version in `package.json`.
2. Run `pnpm build` (or the equivalent for your package manager) to regenerate the `.res.mjs` and `.gen.d.ts` artifacts that the `files` allowlist depends on.
3. Run `pnpm test` to confirm Vitest passes.
4. Run `npm publish --access public` (drop `--access public` for a private scoped package).

The `prepare` script means consumers who install your tarball will *also* run `rescript` — so even if the publish artifacts are stale, the consumer's `node_modules` ends up with a fresh compile. Treat this as a safety net, not a substitute for running `pnpm build` before `npm publish`.

If you publish under an npm scope (`@your-org/my-lib`), update the `name` in `package.json` and the README install snippet at the top. The `bin` field is not used by this template (libraries usually do not ship binaries), so there is nothing to rename there.

## Day-Two Recipes

- {doc}`../recipes/convert-from-typescript` — porting an existing `.ts` module into a `.res` module that lives next to your other library files
- {doc}`../recipes/find-dead-code` — running `reanalyze` to confirm every export is actually reachable before publishing
- {doc}`../recipes/optimize-imports` — keeping `Index.res` tidy as the surface grows

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- `node` engine is pinned to `>=24` and `.nvmrc` says `24`. Earlier majors are not exercised by CI.
- The Vitest suites import the *compiled* `.res.mjs` outputs, not the `.res` sources. CI runs `rescript` before `vitest run` to make sure the artifacts exist; locally, run `pnpm res:dev` in a side terminal so saves recompile and Vitest's watcher picks up the change.
- `Fetcher.test.mjs` stubs the global `fetch` via `vi.stubGlobal` and uses `afterEach(() => vi.restoreAllMocks())`. When you add new fetch-driven modules, follow the same pattern so suites stay isolated.
- `ListUtils.test.mjs` exercises the ReScript variant runtime tag (`{ TAG: "Ok", _0: ... }`) directly. That is the shape ReScript emits for polymorphic variants — keep it in mind if you write JS-side tests against ReScript-generated unions.
- `tsconfig.json` ships with `strict: true` and `declaration: true` so genType's `.d.ts` output round-trips through `tsc --noEmit` without errors. Do not relax those flags casually — the published `.d.ts` files are the only contract your TS consumers see.
- The `prepare` script means consumers who install your *unpublished* tarball (`npm pack && npm install ./pkg.tgz`) also get a fresh ReScript compile. This is occasionally surprising on CI machines without `node_modules`; if it bites you, override with `--ignore-scripts`.
- `.github/dependabot.yml` opens weekly PRs against `package-ecosystem: npm`. Review the ReScript / `@rescript/runtime` bumps together — they are pinned to the same major in `TemplateVersions.kt` for a reason.
