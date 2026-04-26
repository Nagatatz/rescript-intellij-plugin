---
myst:
  html_meta:
    "keywords": "electron template, rescript, desktop, ipc, contextBridge, vite-plus, react, validation, zod, sury"
---

# Electron

{bdg-info}`Desktop` {bdg-primary}`React` {bdg-success}`Validation`

A working Electron desktop app whose renderer is a ReScript + React UI bundled by Vite+. The shipped sample is not a "Hello World" — it shows the full request/response loop between the renderer and the main process, including the safety net you actually need in production: every IPC payload that re-enters the renderer is parsed through `Validation.res` (zod or sury) so a malformed reply from `main.cjs` becomes a UI error, not a `TypeError` deep inside React.

Pick this template if you want to ship a desktop app and start from a stack that already wires `contextIsolation: true`, `nodeIntegration: false`, and `contextBridge.exposeInMainWorld`. The renderer is the same Vite-driven React surface as the **Vite + React** template, so React knowledge transfers directly; the new ground is Electron's two-process model and the IPC contract sitting between them.

## What You Get

```
my-project/
├── rescript.json                  # JSX enabled, depends on @rescript/react + validation lib
├── package.json                   # type: "module", electron + vite-plus dev deps
├── main.cjs                       # main process — BrowserWindow + ipcMain.handle("app:getInfo")
├── preload.cjs                    # contextBridge.exposeInMainWorld("electronAPI", { ... })
├── index.html                     # renderer entry — loads /src/Main.res.mjs as a module
├── vite.config.mjs                # vite-plus + @vitejs/plugin-react, base: "./"
├── src/
│   ├── Main.res                   # ReactDOM.Client.createRoot(rootEl) → <App />
│   ├── App.res                    # Button → invoke IPC → validate → render result/error
│   ├── Electron.res               # @val external electronAPI bindings over window.electronAPI
│   ├── Validation.res             # zod or sury — parses the IPC response into typed `info`
│   └── __tests__/App.test.mjs     # vitest smoke test that imports App.res.mjs
├── README.md                      # IPC security model + "About Vite+" section
├── LICENSE                        # MIT, holder = project name
├── .nvmrc                         # Node 24
├── .gitignore                     # node_modules + ReScript output + dist/, out/, .vite/
├── .editorconfig                  # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml             # weekly npm updates
    └── workflows/ci.yml           # install + rescript build + vitest
```

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, the `BrowserWindow` title (`main.cjs` substitutes `{{projectName}}`), the `<title>` in `index.html`, and the LICENSE holder |
| **Package manager** | npm / pnpm / yarn / bun. Sets the `packageManager` field, the README install/run snippets, and the CI cache key |
| **Validation library** | `zod` ↔ `sury`. Selects which `src/Validation.res` variant ships and which of `zod` / `sury` is added to `dependencies` |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs imported by compiled `.res.mjs` | `TemplateVersions.RESCRIPT_RUNTIME` |
| `@rescript/react` | React bindings (JSX-enabled `rescript.json`) | `TemplateVersions.RESCRIPT_REACT` |
| `react` / `react-dom` | Renderer UI library | `TemplateVersions.REACT` / `REACT_DOM` |
| `zod` *or* `sury` | Validation backend (IPC payload parser) | `TemplateVersions.ZOD` / `SURY` |
| `electron` *(dev)* | Desktop runtime + bundler-side types | `TemplateVersions.ELECTRON` |
| `vite-plus` *(dev)* | Vite-based bundler used to produce `dist/` | `TemplateVersions.VITE_PLUS` |
| `@voidzero-dev/vite-plus-core` *(dev)* | Vite+ core runtime (peer of `vite-plus`) | `TemplateVersions.VITE_PLUS_CORE` |
| `vite` *(dev)* | Direct `vite` pin used by `@vitejs/plugin-react` | `TemplateVersions.VITE` |
| `@vitejs/plugin-react` *(dev)* | React refresh + JSX transform plugin | `TemplateVersions.VITEJS_PLUGIN_REACT` |
| `vitest` *(dev)* | Smoke test runner | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |

`electron-builder` is **not** included by default — see the Notes section for why and when to add it.

## Key Files

### `main.cjs`

The Electron main process. CommonJS (`.cjs`) because Electron's `app`/`BrowserWindow`/`ipcMain` modules are CJS-only at the time the template was authored; using `.cjs` keeps the file working regardless of the `"type": "module"` in `package.json`.

It does three things:

1. Creates a single `BrowserWindow` with hardened defaults — `contextIsolation: true`, `nodeIntegration: false`, and a `preload: path.join(__dirname, "preload.cjs")`.
2. Loads `dist/index.html` (the production bundle Vite+ produced).
3. Registers an `ipcMain.handle("app:getInfo", ...)` handler that returns app + system info.

The `app:getInfo` channel demonstrates the recommended `domain:action` naming convention (see the README's "Renderer ↔ Main IPC" section). Avoid bare verbs like `getInfo` — they collide as the surface grows.

### `preload.cjs`

The single bridge across the contextIsolation boundary. Uses `contextBridge.exposeInMainWorld` to publish a small `electronAPI` object on `window`:

```js
contextBridge.exposeInMainWorld("electronAPI", {
  getInfo: () => ipcRenderer.invoke("app:getInfo"),
});
```

Anything **not** in this object is invisible to the renderer. Adding a new channel always means editing this file plus `main.cjs` plus `src/Electron.res` — the README ships a step-by-step walkthrough of all three layers.

### `src/Electron.res`

Thin ReScript binding over the exposed object. The shipped binding is intentionally typed as `JSON.t` — *not* the eventual `info` record — because validation lives one layer further in:

```rescript
@val external electronAPI: {"getInfo": unit => promise<JSON.t>} = "electronAPI"

let getInfoRaw = (): promise<JSON.t> => electronAPI["getInfo"]()
```

The `Raw` suffix is a deliberate convention: it signals "this value has *not* been validated yet" so callers know they need to run it through `Validation.res` before destructuring.

### `src/Validation.res`

`parseInfo: JSON.t => result<info, string>`. The signature is identical between the zod and sury variants, so the renderer's call site does not branch on which library shipped:

```rescript
type info = {name: string, electronVersion: string, platform: string, arch: string}
let parseInfo: JSON.t => result<info, string>
```

Why validate at all? The renderer sits behind contextIsolation, but the *main process* can still ship malformed payloads during a bad refactor. `parseInfo` turns those into a human-readable error before the UI assumes the shape.

### `src/App.res`

The interactive demo. A button click runs:

```rescript
let raw = await Electron.getInfoRaw()
switch Validation.parseInfo(raw) {
| Ok(result) => setInfo(_ => Some(result))
| Error(message) => setError(_ => Some(message))
}
```

The error branch renders a red `IPC validation error: ...` banner. Mutate `main.cjs` to return an unexpected payload and you can watch the validator catch it without the React tree blowing up.

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

Minimal Vite+ config — `defineConfig` from `vite-plus` (not `vite`), the React plugin, and `base: "./"`. The relative `base` matters: Electron loads `dist/index.html` via `file://`, so absolute asset URLs would 404.

### `index.html`

The renderer entry HTML. Hosts the React root and pulls in `/src/Main.res.mjs` as an ES module — the file the ReScript compiler emits next to `Main.res`.

## npm Scripts

| Script | Description |
| --- | --- |
| `dev` | `vp dev` — Vite+ dev server for the renderer (open in a browser to iterate on UI without launching Electron) |
| `build` | `vp build` — bundle the renderer to `dist/` |
| `electron` | `electron .` — launch Electron against an already-built `dist/` |
| `start` | `vp build && electron .` — production-like one-shot: build then launch |
| `test` | `vp test` — Vitest under the Vite+ runner |
| `test:coverage` | `vp test --coverage` — same, with v8 coverage |
| `res:build` | `rescript` — one-shot ReScript compile |
| `res:dev` | `rescript -w` — recompile on save |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` |

## Adding a New IPC Channel

Adding a channel always touches three layers — the README ships a step-by-step walkthrough and the shape is worth memorizing because forgetting any layer is silent (`undefined is not a function` in the renderer).

1. **Define the handler in `main.cjs`**:

   ```js
   ipcMain.handle("window:setTitle", (_event, title) => {
     BrowserWindow.getFocusedWindow()?.setTitle(String(title));
   });
   ```

2. **Expose it from `preload.cjs`**:

   ```js
   contextBridge.exposeInMainWorld("electronAPI", {
     getInfo: () => ipcRenderer.invoke("app:getInfo"),
     setWindowTitle: (title) => ipcRenderer.invoke("window:setTitle", title),
   });
   ```

3. **Bind it in `src/Electron.res`** (extend the externals object so ReScript sees the new method):

   ```rescript
   @val external electronAPI: {
     "getInfo": unit => promise<JSON.t>,
     "setWindowTitle": string => promise<unit>,
   } = "electronAPI"

   let setWindowTitle = (title: string) => electronAPI["setWindowTitle"](title)
   ```

4. **Validate the response** in `Validation.res` if the handler returns data the UI consumes — a stray `null` or shape change in main is far easier to debug as a parse error than as a `TypeError` deep inside React. For fire-and-forget channels (`setWindowTitle` returns `unit`) you can skip this step.

## Security Model

The bundled `BrowserWindow` is configured with the modern hardened defaults:

| Setting | Value | Why |
| --- | --- | --- |
| `contextIsolation` | `true` | Keeps preload globals out of the page's `window` |
| `nodeIntegration` | `false` | Renderer cannot `require("fs")`; no Node escape from a compromised dep |
| Preload bridge | `contextBridge.exposeInMainWorld` | Only the surface you opt-in to crosses the boundary |

The renderer never imports Electron directly. Every cross-process call goes through `window.electronAPI.*` → `ipcRenderer.invoke(...)` → `ipcMain.handle(...)`. Validate every payload that re-enters the renderer with `Validation.res` so a buggy main-process change cannot crash the UI silently.

## Day-Two Recipes

- {doc}`../recipes/create-react-component` — same React conventions apply to the renderer
- {doc}`../recipes/optimize-imports` — keep `src/Electron.res` and `src/Validation.res` tidy as the IPC surface grows
- {doc}`../recipes/find-dead-code` — useful for pruning IPC channels that no renderer still calls

For ReScript-side editor workflows once the project is open, see {doc}`../features/index`.

## Notes

- **Electron is not hot-reloaded.** `vp dev` updates the renderer in your browser, but the running Electron window does not auto-refresh on `main.cjs` / `preload.cjs` changes. Stop and re-run `npm start` after touching the main-process files.
- **`electron-builder` is intentionally absent.** Packaging is opinionated (signing identities, icons, target platforms, auto-updater) and varies wildly per project, so the template stops at "runs locally". Add `electron-builder` (or Forge / Tauri-style alternatives) when you decide on a release strategy.
- **The renderer never imports Electron directly.** Every cross-process call goes through `window.electronAPI.*` → `ipcRenderer.invoke(...)` → `ipcMain.handle(...)`. If you find yourself wanting `require("electron")` in the renderer, you almost certainly want a new IPC channel instead.
- **`ipcRenderer.send` is fire-and-forget.** The shipped pattern uses `invoke`/`handle` exclusively — request/response with a typed return value the validator can inspect. Stick to it unless you have a streaming use case.
- **Functions, class instances, and DOM nodes cannot cross the bridge.** Stringify or convert to plain objects before returning from `ipcMain.handle`.
- **Channel names use `domain:action`.** The shipped `app:getInfo` is the canonical example; the `app:*` prefix is reserved for app-lifecycle channels — pick a different domain (`fs:*`, `db:*`, `window:*`) for feature work.
- **The smoke test is intentionally tiny.** `App.test.mjs` only verifies that `import("../App.res.mjs")` resolves; it does not boot Electron or exercise IPC. Add domain tests as you grow the renderer.
- **`vite-plus` is pre-1.0.** The version pin is `^0.1.x`; expect minor breaking changes between releases until 1.0 lands. Vite+ bundles its own `vite` internally — the direct `vite` dependency is only there so `@vitejs/plugin-react` resolves a peer.
