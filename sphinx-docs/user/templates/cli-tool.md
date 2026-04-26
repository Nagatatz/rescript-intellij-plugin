---
myst:
  html_meta:
    "keywords": "cli, command line, subcommand, dispatcher, args, npm bin, vitest, validation, zod, sury, rescript"
---

# CLI Tool

{bdg-info}`Node.js` {bdg-success}`Testing`

A minimal but production-shaped command-line tool: a `bin/` shim that satisfies npm's `bin` contract, a top-level dispatcher (`Cli.res`), per-subcommand modules (`Commands.Greet`, `Commands.Init`), and a small flag-parsing helper (`Args.res`) that you own end to end. No `commander`, `yargs`, or `clipanion` — when you need more, the helpers are short enough to extend.

Pick this template when you are building anything `npx`-able, an internal tool, a code generator, or the early scaffold of a CLI that will eventually grow subcommands. The structure mirrors the way `git`, `docker`, and the `rescript` binary itself are organized: one entry point that dispatches to per-command modules.

## What You Get

```
my-project/
├── rescript.json
├── package.json                   # ESM, "bin" object pointing at bin/cli.mjs
├── bin/
│   └── cli.mjs                    # #!/usr/bin/env node — imports Cli.res.mjs
├── src/
│   ├── Cli.res                    # entry — dispatches argv to subcommands
│   ├── Args.res                   # positional + named flag helpers
│   ├── Commands.res               # Commands.Greet, Commands.Init nested modules
│   ├── Validation.res             # zod or sury — selected in the wizard
│   └── __tests__/
│       └── Args.test.mjs          # hasFlag + namedValue suites
├── README.md                      # script docs + Usage + Project Layout + Install Locally
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
| **Project name** | Becomes the npm `name`, the `bin` key (so `npm link` exposes the binary under that name), the LICENSE holder, and the program name printed by `--help` |
| **Package manager** | npm / pnpm / yarn / bun. Affects `packageManager` field, README install/run commands, and CI cache key |
| **Validation library** | `zod` ↔ `sury`. Chooses which `src/Validation.res` variant ships — the validator runs inside `init` to check `--name` / `--dir` |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs the compiled `.res.mjs` imports at runtime | `TemplateVersions.RESCRIPT_RUNTIME` |
| `zod` *or* `sury` | Validation backend (chosen in the wizard) | `TemplateVersions.ZOD` / `TemplateVersions.SURY` |
| `vitest` *(dev)* | Smoke test runner | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |

Notably absent: any third-party CLI framework. `Args.res` is ~25 lines, `Cli.res` is the dispatcher, and you can extend either without learning a DSL.

## Key Files

### `bin/cli.mjs`

The canonical npm `bin` shim. The file must start with `#!/usr/bin/env node` so Unix resolves the right interpreter when the binary is invoked directly. Everything else lives in ReScript:

```js
#!/usr/bin/env node
import "../src/Cli.res.mjs";
```

`package.json` maps the project name to this file:

```json
"bin": {
  "<projectName>": "./bin/cli.mjs"
}
```

The `bin` field is an *object* (not a bare string) on purpose — when you add a second binary later you can drop another entry alongside without restructuring.

### `src/Cli.res`

The entry. Reads `process.argv`, splits off the subcommand name, and dispatches:

```rescript
let rec run = () => {
  let allArgs = Args.positional()
  let (subcommand, remaining) = Args.splitSubcommand(allArgs)

  switch subcommand {
  | Some("greet") => Commands.Greet.run(remaining)
  | Some("init") => Commands.Init.run(remaining)
  | Some("--help") | Some("-h") | None => printUsage()
  | Some(cmd) =>
    Console.error(`Unknown subcommand: ${cmd}`)
    printUsage()
  }
}
```

To add a new subcommand: add a new module under `Commands.res`, then add a new pattern arm to the `switch` above. The unknown-command branch falls through to `printUsage` so misspelled inputs get a useful error rather than silent exit zero.

### `src/Args.res`

Four small helpers, all written against `process.argv`:

| Function | Purpose |
| --- | --- |
| `positional()` | Returns argv with the node binary and script path stripped |
| `splitSubcommand(args)` | Returns `(Some(first), rest)` or `(None, [])` |
| `hasFlag(args, flag)` | `true` if `flag` appears anywhere in `args` |
| `namedValue(args, flag)` | The value following `flag`, or `None` if absent |

`hasFlag` and `namedValue` are `@genType`-annotated so JS-side tests can call them directly (which is exactly what `Args.test.mjs` does). When you outgrow this — e.g. you need short-flag clustering, `--flag=value` syntax, or subcommand-specific help — extend `Args.res` rather than reaching for a framework. The whole module is short enough to read in one sitting.

### `src/Commands.res`

Subcommands are nested modules so the dispatcher can write `Commands.<Name>.run(args)`. The template ships two:

- **`Commands.Greet`** — `greet <name> [--shout]`. Demonstrates a positional argument plus a boolean flag (`hasFlag`).
- **`Commands.Init`** — `init --name <project-name> --dir <path>`. Demonstrates a subcommand that *validates its options* before doing any work:

```rescript
module Init = {
  let run = (args: array<string>) => {
    let raw =
      Dict.fromArray([
        ("name", args->Args.namedValue("--name")->Option.map(JSON.Encode.string)->Option.getOr(JSON.Encode.null)),
        ("dir", args->Args.namedValue("--dir")->Option.map(JSON.Encode.string)->Option.getOr(JSON.Encode.null)),
      ])->JSON.Encode.object
    switch Validation.parseInitOptions(raw) {
    | Error(message) => Console.error(`init: ${message}`)
    | Ok({name, dir}) =>
      Console.log(`Initializing new "<projectName>"-style project "${name}" at ${dir}...`)
      Console.log("(In a real CLI, this would create files on disk.)")
    }
  }
}
```

The pattern — collect raw args into a `JSON.t`, hand it to `Validation`, branch on the `result` — generalizes to every subcommand that takes structured options. It also keeps the validation library swappable: change zod ↔ sury in the wizard and only `Validation.res` differs.

### `src/__tests__/Args.test.mjs`

The smoke test imports the *compiled* `Args.res.mjs` and exercises the two flag helpers:

```js
import { describe, expect, it } from "vitest";
import { hasFlag, namedValue } from "../Args.res.mjs";

describe("hasFlag", () => {
  it("returns true when the flag is present", () => {
    expect(hasFlag(["--shout", "Alice"], "--shout")).toBe(true);
  });
  it("returns false when the flag is absent", () => {
    expect(hasFlag(["Alice"], "--shout")).toBe(false);
  });
});

describe("namedValue", () => {
  it("returns the value following the flag", () => {
    expect(namedValue(["--out", "dist"], "--out")).toEqual("dist");
  });
  it("returns undefined when the flag is missing", () => {
    expect(namedValue(["Alice"], "--out")).toBeUndefined();
  });
});
```

This is the canonical guard that the argv-parsing helpers behave identically across ReScript bumps. When you extend `Args.res` (e.g. add `--flag=value` parsing), add a matching describe block here. The suite imports the compiled `.res.mjs` so it doubles as a check that the source still compiles.

### `src/Validation.res`

`parseInitOptions: JSON.t => result<initOptions, string>`. Validates the `--name` and `--dir` options that `Commands.Init` collects from argv. The signature is identical between the zod and sury variants so `Commands.res` does not need to know which is loaded.

When you add a new subcommand that takes structured options, define a new `parseFooOptions` here and call it from the command body. Returning a `result` keeps the contract honest: the dispatcher either gets validated data back or prints `<command>: <error>` to stderr.

## npm Scripts

| Script | Description |
| --- | --- |
| `build` | `rescript` — one-shot compile |
| `start` | `node bin/cli.mjs` — run the CLI once locally (without `npm link`) |
| `test` | `vitest run` — execute the smoke suite |
| `test:coverage` | `vitest run --coverage` — same, with v8 coverage report |
| `res:build` | `rescript` — alias kept for parity with other templates |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` |
| `res:dev` | `rescript -w` — recompile on save |

To pass arguments through `start`, use the package-manager pass-through: `pnpm start -- greet Alice --shout`.

## Usage Examples

After running the build (`pnpm build`), invoke the CLI through `node bin/cli.mjs` or `pnpm start --`:

```bash
pnpm start -- greet Alice
pnpm start -- greet Alice --shout
pnpm start -- init --name my-project --dir ./projects/my-project
pnpm start -- --help
```

`init` validates the `--name` / `--dir` options through `Validation.parseInitOptions`. Missing or malformed options produce an `init: <message>` error on stderr and exit non-zero (Node's default for an uncaught error from `Console.error` will not change exit code automatically — wrap with `process.exit(1)` if you need a hard failure).

## Installing Locally

Two paths to test the CLI on your `$PATH`:

1. **`npm link`** — links the package globally so you can call the binary by its name from any directory:

   ```bash
   pnpm build
   npm link
   <projectName> greet Alice
   ```

   `npm unlink -g <projectName>` reverses it. (`pnpm` users may prefer `pnpm link --global` instead.)

2. **`npx`** from a tarball — run without installing globally:

   ```bash
   pnpm build
   npm pack
   npx ./<projectName>-1.0.0.tgz greet Alice
   ```

When you are ready to publish, `npm publish` ships the package and consumers can `npx <projectName>` directly.

## Day-Two Recipes

- {doc}`../recipes/find-dead-code` — running `reanalyze` so removed subcommands or helpers do not linger
- {doc}`../recipes/optimize-imports` — keeping `Cli.res` and `Commands.res` tidy as the dispatcher grows
- {doc}`../recipes/debug-rescript` — attaching a debugger to `node bin/cli.mjs` to step through subcommand bodies

For ReScript-side editor workflows once the project is open, see the {doc}`../features/index`.

## Notes

- `node` engine is pinned to `>=24` and `.nvmrc` says `24`. Earlier majors are not exercised by CI.
- The `bin` key in `package.json` is the *project name*, so the binary is exposed as that name after `npm link` (or after publish + global install). Plan accordingly when picking the project name in the wizard — it ends up on user `$PATH`s.
- `bin/cli.mjs` is intentionally a one-line `import` of the compiled ReScript output. Do *not* hand-edit it to add logic — extend `Cli.res` instead. The shim's only job is to provide the shebang and resolve the entry module.
- To install the CLI globally for local testing: `pnpm build && npm link`. Then run the binary by its project name from anywhere. `npm unlink` reverses it.
- Adding a second binary: append a new entry to `package.json#bin` and ship a sibling wrapper under `bin/` (e.g. `bin/<name>-init.mjs`). Each wrapper still needs the `#!/usr/bin/env node` shebang.
- The `Args.test.mjs` suite imports the *compiled* `Args.res.mjs`, so it doubles as a guard that the `.res` source still compiles. Run `pnpm res:dev` in a side terminal during development so saves recompile before Vitest re-executes.
- The `init` body intentionally encodes argv into JSON and then validates it. That asymmetry exists so the same validator can be reused later by an HTTP / RPC entry point that already speaks JSON — you do not have to re-implement the option contract per surface.
- There is no `prepare` script. Unlike the npm Library template, this scaffold assumes you publish a built CLI rather than expecting consumers to compile on install. If you want the same `prepare` behavior, add `"prepare": "rescript"` to the scripts block.
