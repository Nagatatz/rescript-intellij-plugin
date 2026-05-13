### Security model

Tauri 2.x exposes the renderer as a real OS WebView, so the security
boundary is the **invoke bridge** — not a Node-style runtime. Four
defaults the template ships keep that boundary tight:

| Setting | Value | Why |
| --- | --- | --- |
| `security.csp` | Explicit (`default-src 'self'` + `ipc:` + `asset:`) | Blocks remote scripts; allows only the Tauri-internal channels |
| Rust `invoke_handler` allowlist | Explicit `generate_handler![greet, get_info, count_with_progress]` | Only listed commands can be called from JS; everything else 404s |
| `src-tauri/capabilities/default.json` | Explicit permission list per plugin | Even with a plugin registered in Rust, the renderer cannot call it unless its permission is granted here |
| ReScript `Validation.parseInfo` | `JSON.t => result<info, string>` | Catches schema drift between Rust structs and ReScript records |

The renderer never imports `@tauri-apps/api` directly. Every cross-
process call goes through `RescriptTauriCore.Core.Raw.invoke(...)` →
Tauri's internal IPC → `#[tauri::command]`, or through a typed plugin
binding (`PluginFs.readFile`, `PluginDialog.openFile`, …). Validate
every payload that re-enters the renderer with `Validation.res` so a
buggy Rust refactor cannot crash the UI silently.

### What the sample exercises

The shipped UI is composed of five panels — each in its own ReScript
module under `src/`:

| Panel | Module | Plugins / APIs |
| --- | --- | --- |
| Plain IPC | `Tauri.res` + `App.res` | `Core.Raw.invoke` |
| System Probe | `SystemInfo.res` | `@rescript-tauri/plugin-os` + core `Path` |
| Hex Inspector | `HexInspector.res` | `plugin-dialog` + `plugin-fs` + `plugin-clipboard-manager` + `plugin-notification` + `plugin-log` |
| Streaming Progress | `Progress.res` (+ Rust `count_with_progress`) | `Core.Channel<ProgressEvent>` (low-level streaming IPC) |
| Window Events | `WindowEvents.res` | `Core.Event.listen` for `tauri://resize|focus|blur|drag-enter|drag-drop` + `Window.setTitle` |

`Main.res` calls `PluginLog.attachConsole()` once at boot, so anything
emitted via `PluginLog.info(...)` in any panel surfaces in the JS
console.

### Naming convention: snake_case commands

Tauri convention is `snake_case` Rust function names that JS calls
verbatim. The shipped commands follow this:

```
greet                  # plain RPC, returns String
get_info               # returns a struct serialised as JSON
count_with_progress    # streaming via Channel<ProgressEvent>
```

Avoid camelCase / kebab-case — Tauri's macros expect snake_case.
Parameter names, however, are auto-converted: a Rust parameter
`on_event` is sent as `onEvent` from JS, which is why
`Tauri.countWithProgress` writes `~args={"onEvent": channel, ...}`.

### Adding a new Tauri command

1. **Define the command in `src-tauri/src/lib.rs`**:
   ```rust
   #[tauri::command]
   fn set_window_title(window: tauri::Window, title: String) -> Result<(), String> {
       window.set_title(&title).map_err(|e| e.to_string())
   }
   ```
2. **Register it in `invoke_handler`**:
   ```rust
   .invoke_handler(tauri::generate_handler![
       greet, get_info, count_with_progress, set_window_title,
   ])
   ```
3. **Bind it in `src/Tauri.res`**:
   ```rescript
   let setWindowTitle = (title: string): promise<unit> =>
     RescriptTauriCore.Core.Raw.invoke(
       "set_window_title",
       ~args={"title": title},
     )
   ```
4. **Validate the response** in `Validation.res` if the handler returns
   data the UI consumes. Fire-and-forget commands (`set_window_title`
   returns `()`) skip this step.

### Adding a new Tauri plugin

1. Add the Rust crate to `src-tauri/Cargo.toml`:
   ```toml
   tauri-plugin-store = "2"
   ```
2. Register it in `lib.rs`:
   ```rust
   .plugin(tauri_plugin_store::Builder::new().build())
   ```
3. Add the matching renderer packages to `package.json` (one
   `@rescript-tauri/plugin-*` binding + its `@tauri-apps/plugin-*`
   peer):
   ```jsonc
   "@rescript-tauri/plugin-store": "^0.1.0",
   "@tauri-apps/plugin-store": "^2.4.0",
   ```
4. List the ReScript binding in `rescript.json` `dependencies` so the
   compiler picks it up.
5. Grant permissions in `src-tauri/capabilities/default.json` — at
   minimum `store:default`, plus any explicit allow rules the plugin's
   docs call out. Missing permissions surface as `tauri_plugin_*::Error`
   responses with names like `store.set not allowed`.

### Streaming IPC with `Core.Channel`

`count_with_progress` is the template's streaming demo. The shape is
the same for every long-running task that wants to push updates back
to the renderer:

- Rust declares a typed channel argument and calls `channel.send(...)`
  whenever it has news. The work runs on a background thread so the
  original `invoke` Promise can resolve immediately.
- ReScript creates the channel via `Core.Channel.make(~decode=...)`,
  attaches `Core.Channel.onMessage` to drain it, and passes the
  channel inside the `~args` payload of `Core.Raw.invoke`.

The decoder runs on every emission, so a Rust enum reshape arrives as
`Error(...)` rather than a silently mis-rendered struct.

### Things that bite

- `tauri::generate_handler!` does **not** auto-discover commands —
  every new `#[tauri::command]` must be added to the list explicitly.
- Rust `Result<T, E>` is the recommended return for fallible commands:
  Tauri serialises `Ok(T)` to a normal response and `Err(E)` to a
  JS-side thrown error. The ReScript side surfaces it through Promise
  rejection (caught with `try/catch` on `JsExn`).
- Commands receive **owned** values by default (`String`, not `&str`).
  Borrowed `&str` works for primitives but fails on owned types like
  `Vec<String>`.
- The CSP `connect-src` whitelist controls `fetch`/XHR, not `invoke`.
  Tauri's IPC uses its own `ipc:` and `http://ipc.localhost` origins —
  both are present in the shipped CSP. Don't remove them.
- The renderer never imports `@tauri-apps/api` directly. Every cross-
  process call goes through `Tauri.res` → `Core.Raw.invoke(...)` or a
  `PluginXxx.*` call. If you reach for `import { invoke } from
  "@tauri-apps/api"`, you almost certainly want a new ReScript binding
  instead.
- **Capabilities are enforced.** A plugin registered in Rust still
  refuses calls from the renderer unless the corresponding permission
  is in `capabilities/default.json`. The Cargo build prints the full
  list of valid permission names on the first mistyped entry.
