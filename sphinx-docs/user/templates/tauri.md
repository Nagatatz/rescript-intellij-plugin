---
myst:
  html_meta:
    "keywords": "tauri template, rescript, desktop, rust, ipc, invoke, vite-plus, react, validation, zod, sury, rescript-tauri"
---

# Tauri

{bdg-info}`Desktop` {bdg-primary}`Rust` {bdg-success}`Validation`

A working Tauri 2.x desktop app whose renderer is a ReScript + React UI bundled by Vite+ and whose process is a tiny Rust crate under `src-tauri/`. The shipped sample is not "Hello World" — it shows the full request/response loop between the renderer and Rust: a `greet(name)` command that returns a string, and a `get_info()` command that returns a struct which the renderer parses through `Validation.res` (zod or sury) before assuming the shape. Drift between the Rust struct and the ReScript record surfaces as a UI error, not a `TypeError` deep inside React.

Pick this template if you want to ship a desktop app and start from a stack that already wires Tauri's CSP, snake_case command allowlist, and a `Validation.res` boundary. The renderer is the same Vite-driven React surface as the **Vite + React** and **Electron** templates, so React knowledge transfers directly; the new ground is the Rust process plus the `tauri::generate_handler!` allowlist.

## What You Get

```
my-project/
├── rescript.json                  # JSX enabled, depends on @rescript/react + @rescript-tauri/core
├── package.json                   # type: "module", @tauri-apps/cli + vite-plus dev deps
├── index.html                     # renderer entry — loads /src/Main.res.mjs as a module
├── vite.config.mjs                # vite-plus + @vitejs/plugin-react, base: "./", strictPort 5173
├── src/
│   ├── Main.res                   # ReactDOM.Client.createRoot(rootEl) → <App />
│   ├── App.res                    # Buttons → greet / get_info → validate → render result/error
│   ├── Tauri.res                  # @rescript-tauri/core bindings (Core.Raw.invoke wrappers)
│   ├── Validation.res             # zod or sury — parses get_info response into typed `info`
│   └── __tests__/App.test.mjs     # vitest smoke test that imports App.res.mjs
├── src-tauri/
│   ├── Cargo.toml                 # tauri + serde + serde_json + tauri-build
│   ├── build.rs                   # tauri_build::build()
│   ├── tauri.conf.json            # CSP, devUrl, frontendDist, beforeDev/Build (PM-aware)
│   ├── .gitignore                 # target/ and gen/
│   └── src/main.rs                # greet / get_info commands + invoke_handler allowlist
├── README.md                      # IPC security model + Production Bundling section
├── LICENSE                        # MIT, holder = project name
├── .nvmrc                         # Node 24
├── .gitignore                     # node_modules + ReScript output + dist/ + src-tauri/target/
├── .editorconfig                  # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml             # weekly npm updates
    └── workflows/ci.yml           # install + rescript build + vp build + vitest
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, the Cargo crate `name`, the Tauri `productName` + `identifier` + window title, and the LICENSE holder |
| **Package manager** | npm / pnpm / yarn / bun. Sets the `packageManager` field, the README install/run snippets, the CI cache key, **and** the `beforeDevCommand` / `beforeBuildCommand` strings in `tauri.conf.json` |
| **Validation library** | `zod` ↔ `sury`. Selects which `src/Validation.res` variant ships and which of `zod` / `sury` is added to `dependencies` |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs imported by compiled `.res.mjs` | `TemplateVersions.RESCRIPT_RUNTIME` |
| `@rescript/react` | React bindings (JSX-enabled `rescript.json`) | `TemplateVersions.RESCRIPT_REACT` |
| `@rescript-tauri/core` | ReScript bindings over `@tauri-apps/api` (Core.Raw.invoke, Event, Window, …) | `TemplateVersions.RESCRIPT_TAURI_CORE` |
| `@tauri-apps/api` | Tauri's JS SDK (peer of `@rescript-tauri/core`) | `TemplateVersions.TAURI_APPS_API` |
| `react` / `react-dom` | Renderer UI library | `TemplateVersions.REACT` / `REACT_DOM` |
| `zod` *or* `sury` | Validation backend (IPC payload parser) | `TemplateVersions.ZOD` / `SURY` |
| `@tauri-apps/cli` *(dev)* | `tauri dev` / `tauri build` driver | `TemplateVersions.TAURI_APPS_CLI` |
| `vite-plus` *(dev)* | Vite-based bundler used for the renderer | `TemplateVersions.VITE_PLUS` |
| `@voidzero-dev/vite-plus-core` *(dev)* | Vite+ core runtime (peer of `vite-plus`) | `TemplateVersions.VITE_PLUS_CORE` |
| `vite` *(dev)* | Direct `vite` pin used by `@vitejs/plugin-react` | `TemplateVersions.VITE` |
| `@vitejs/plugin-react` *(dev)* | React refresh + JSX transform plugin | `TemplateVersions.VITEJS_PLUGIN_REACT` |
| `vitest` *(dev)* | Smoke test runner | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |

Rust dependencies (`src-tauri/Cargo.toml`) pin `tauri = "2"`, `tauri-build = "2"`, `serde`, and `serde_json` only — extend per command surface as needed.

`tauri-plugin-*` crates are **not** included by default. Add the ReScript bindings (`@rescript-tauri/plugin-dialog`, `@rescript-tauri/plugin-fs`, etc. — published alongside `@rescript-tauri/core`) together with their Rust plugin crate when you need them.

## Key Files

### `src-tauri/src/main.rs`

The Tauri main process. Defines two `#[tauri::command]` functions:

- `greet(name: &str) -> String` — plain RPC for the "Say hello" button
- `get_info() -> AppInfo` — returns `{ name, version, platform, arch }` serialised via `serde::Serialize`

The `invoke_handler` macro registers both into the IPC allowlist:

```rust
.invoke_handler(tauri::generate_handler![greet, get_info])
```

Adding a new command without updating this list silently 404s on the JS side. The command name shape is `snake_case` Rust function names — that is exactly what `Core.Raw.invoke` calls from JS.

### `src-tauri/tauri.conf.json`

The Tauri 2.x config. Notable fields:

- `build.beforeDevCommand` and `build.beforeBuildCommand` — substituted at scaffold time with the package-manager-specific dev / build invocation (`pnpm dev` vs `npm run dev` vs `yarn dev` vs `bun run dev`). `tauri dev` runs the renderer dev server before opening the window; `tauri build` runs `vp build` first to produce `dist/`.
- `build.devUrl: "http://localhost:5173"` — pairs with `vite.config.mjs`'s `strictPort: true`. Change both together if you need a different port.
- `build.frontendDist: "../dist"` — Tauri reads the Vite+ output from this path on `tauri build`.
- `app.security.csp` — `default-src 'self'` plus `asset:` / `https://asset.localhost` (for `Core.Raw.convertFileSrc`) and `ipc:` / `http://ipc.localhost` (for Tauri's invoke transport on Windows / macOS). **Production apps must define an explicit CSP** — see [Tauri's CSP guidance](https://v2.tauri.app/security/csp/).
- `bundle.icon` — **intentionally omitted** so `tauri dev` runs on a fresh checkout without an icon source. Generate icons with `tauri icon path/to/source.png` and add the produced list back when you're ready for `tauri build`.

### `src/Tauri.res`

Thin ReScript binding over `@rescript-tauri/core`. The shipped wrappers are intentionally typed as `JSON.t` for `get_info` — *not* the eventual `info` record — because validation lives one layer further in:

```rescript
let greet = (name: string): promise<string> =>
  RescriptTauriCore.Core.Raw.invoke("greet", ~args={"name": name})

let getInfoRaw = (): promise<JSON.t> =>
  RescriptTauriCore.Core.Raw.invoke("get_info", ~args=Js.Obj.empty())
```

The `Raw` suffix on `getInfoRaw` is a deliberate convention: it signals "this value has *not* been validated yet" so callers know they need to run it through `Validation.res` before destructuring.

### `src/Validation.res`

`parseInfo: JSON.t => result<info, string>`. The signature is identical between the zod and sury variants, so the renderer's call site does not branch on which library shipped:

```rescript
type info = {name: string, version: string, platform: string, arch: string}
let parseInfo: JSON.t => result<info, string>
```

Why validate at all? The renderer can trust Tauri's allowlist to reject *unknown* commands, but the *struct shape* coming back from Rust is still just JSON until proven otherwise. Add a field to `AppInfo` on the Rust side and forget to add it to `parseInfo`? The validator catches it as a parse error before React ever sees `undefined`.

### `src/App.res`

The interactive demo. Two buttons:

- **Say hello** runs `Tauri.greet("ReScript")` and prints the returned string.
- **Get app info** runs `Tauri.getInfoRaw()`, validates the result, and either renders a `<dl>` of the typed record or a red `IPC validation error: ...` banner.

Mutate `get_info` in `main.rs` to return an unexpected payload — drop a field, rename one, change a type — and you can watch the validator catch it without the React tree blowing up.

### `src/Main.res`

The renderer entry the `index.html` `<script type="module">` loads. Mounts `<App />` onto `#root`:

```rescript
switch ReactDOM.querySelector("#root") {
| Some(rootEl) =>
  ReactDOM.Client.Root.render(ReactDOM.Client.createRoot(rootEl), <App />)
| None => Console.error("Could not find root element")
}
```

### `vite.config.mjs`

Minimal Vite+ config — `defineConfig` from `vite-plus` (not `vite`), the React plugin, `base: "./"`, `server.strictPort: true`, `server.port: 5173`, and `clearScreen: false`. The relative `base` keeps the production bundle working when Tauri loads `dist/index.html` via `file://`. `strictPort` makes Vite fail fast if 5173 is taken — silently sliding to 5174 would leave `tauri.conf.json`'s `devUrl` pointing at the wrong place.

## npm Scripts

| Script | Description |
| --- | --- |
| `tauri:dev` | `tauri dev` — full app: starts the renderer dev server (via `beforeDevCommand`) and launches the Tauri window |
| `tauri:build` | `tauri build` — production binary, after running `beforeBuildCommand` to bundle the renderer (requires Rust + platform deps) |
| `tauri` | `tauri` — passthrough for `tauri icon`, `tauri info`, `tauri signer`, etc. |
| `dev` | `vp dev` — Vite+ dev server only (open in a browser to iterate on UI without launching Tauri) |
| `build` | `vp build` — bundle the renderer to `dist/` |
| `test` | `vp test` — Vitest under the Vite+ runner |
| `test:coverage` | `vp test --coverage` — same, with v8 coverage |
| `res:build` | `rescript` — one-shot ReScript compile |
| `res:dev` | `rescript -w` — recompile on save |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` |

## Adding a New Tauri Command

Adding a command always touches three layers — the README ships a step-by-step walkthrough and the shape is worth memorizing because forgetting any layer is silent (`"command set_window_title not found"` in the renderer).

1. **Define the command in `src-tauri/src/main.rs`**:
   ```rust
   #[tauri::command]
   fn set_window_title(window: tauri::Window, title: String) -> Result<(), String> {
       window.set_title(&title).map_err(|e| e.to_string())
   }
   ```
2. **Register it in `invoke_handler`** (still in `main.rs`):
   ```rust
   .invoke_handler(tauri::generate_handler![greet, get_info, set_window_title])
   ```
3. **Bind it in `src/Tauri.res`**:
   ```rescript
   let setWindowTitle = (title: string): promise<unit> =>
     RescriptTauriCore.Core.Raw.invoke(
       "set_window_title",
       ~args={"title": title},
     )
   ```
4. **Validate the response** in `Validation.res` if the handler returns data the UI consumes. Fire-and-forget commands (`set_window_title` returns `()`) skip this step.

## Production Bundling

`tauri build` needs a Rust toolchain (`rustup default stable`) plus platform-specific deps — see the [Tauri 2 prerequisites guide](https://v2.tauri.app/start/prerequisites/). Linux needs `webkit2gtk` + `libsoup`; Windows needs the MSVC C++ build tools; macOS needs Xcode CLI tools.

Before the first `tauri build`, generate the icon set:

```bash
tauri icon path/to/source.png
# Writes 32x32.png / 128x128.png / icon.icns / icon.ico into src-tauri/icons/
```

Then re-enable `bundle.icon` in `tauri.conf.json` with the produced filenames. Signing and auto-update are out of scope for the starter — see the README's "Production Bundling" section for the recipe pointers (Developer ID notarisation on macOS, `signtool` on Windows, the Tauri updater plugin).

CI doesn't run `tauri build` in this template — the matrix would need per-OS runners plus signing identities, which varies wildly per project. Add the workflow yourself once your distribution strategy is settled.

## Day-Two Recipes

- {doc}`../recipes/create-react-component` — same React conventions apply to the renderer
- {doc}`../recipes/optimize-imports` — keep `src/Tauri.res` and `src/Validation.res` tidy as the command surface grows
- {doc}`../recipes/find-dead-code` — useful for pruning commands no renderer still calls

For ReScript-side editor workflows once the project is open, see {doc}`../features/index`.

## Notes

- **Tauri commands use `snake_case`.** Rust function names map verbatim to the JS call name — `greet`, `get_info`, `set_window_title`. The macros do not auto-translate to camelCase.
- **`invoke_handler` is the allowlist.** Adding `#[tauri::command]` to a function does nothing on its own — it must also appear in `generate_handler![...]`. Forgetting this is silent (the JS call rejects with "command X not found").
- **The renderer never imports `@tauri-apps/api` directly.** Every cross-process call goes through `Tauri.res` → `RescriptTauriCore.Core.Raw.invoke(...)` → `#[tauri::command]`. If you find yourself wanting `import { invoke } from "@tauri-apps/api"`, you almost certainly want a new ReScript binding instead.
- **Functions, class instances, and DOM nodes cannot cross the bridge.** Tauri serialises arguments and return values as JSON — primitives, owned strings, plain structs (`serde::Serialize`), arrays, and maps only.
- **The CSP `connect-src` whitelist controls `fetch`/XHR, not `invoke`.** Tauri's IPC uses its own `ipc:` and `http://ipc.localhost` origins — both are present in the shipped CSP. Don't remove them.
- **`@rescript-tauri/core` is pre-1.0 (0.1.x).** Expect minor breaking changes between releases until 1.0 ships. The version pin is `^0.1.x` so patch updates flow through without auto-adopting breaking 0.minor bumps.
- **`vite-plus` is also pre-1.0.** The version pin is `^0.1.x`; the README documents the swap-to-classic-Vite fallback if a release breaks the bundler workflow.
- **The smoke test is intentionally tiny.** `App.test.mjs` only verifies that `import("../App.res.mjs")` resolves; it does not boot Tauri or exercise IPC. Add domain tests as you grow the renderer.
