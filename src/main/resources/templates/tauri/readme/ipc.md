### Security model

Tauri 2.x exposes the renderer as a real OS WebView, so the security
boundary is the **invoke bridge** — not a Node-style runtime. Three
defaults the template ships keep that boundary tight:

| Setting | Value | Why |
| --- | --- | --- |
| `security.csp` | Explicit (`default-src 'self'` + `ipc:` + `asset:`) | Blocks remote scripts; allows only the Tauri-internal channels |
| Rust `invoke_handler` allowlist | Explicit `generate_handler![greet, get_info]` | Only listed commands can be called from JS; everything else 404s |
| ReScript `Validation.parseInfo` | `JSON.t => result<info, string>` | Catches schema drift between Rust structs and ReScript records |

The renderer never imports `@tauri-apps/api` directly. Every cross-process
call goes through `RescriptTauriCore.Core.Raw.invoke(...)` → Tauri's
internal IPC → `#[tauri::command]`. Validate every payload that re-enters
the renderer with `Validation.res` so a buggy Rust refactor cannot crash
the UI silently.

### Naming convention: snake_case commands

Tauri convention is `snake_case` Rust function names that JS calls
verbatim. The shipped commands (`greet`, `get_info`) follow this:

```
greet              # plain RPC, returns String
get_info           # returns a struct serialised as JSON
window_set_title   # window-scoped action (recommended prefix)
fs_read_user_file  # filesystem action
db_list_todos      # data layer
```

Avoid camelCase / kebab-case — Tauri's macros expect snake_case. Avoid
bare verbs like `save` or `get` — they collide as the surface grows.

### Adding a new Tauri command

1. **Define the command in `src-tauri/src/main.rs`**:
   ```rust
   #[tauri::command]
   fn set_window_title(window: tauri::Window, title: String) -> Result<(), String> {
       window.set_title(&title).map_err(|e| e.to_string())
   }
   ```
2. **Register it in `invoke_handler`**:
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
4. **Validate the response** in `Validation.res` if the handler returns
   data the UI consumes. Fire-and-forget commands (`set_window_title`
   returns `()`) skip this step.

### Things that bite

- `tauri::generate_handler!` does **not** auto-discover commands — every
  new `#[tauri::command]` must be added to the list explicitly.
- Rust `Result<T, E>` is the recommended return for fallible commands:
  Tauri serialises `Ok(T)` to a normal response and `Err(E)` to a JS-side
  thrown error. The ReScript side surfaces it through Promise rejection.
- Commands receive **owned** values by default (`String`, not `&str`).
  Borrowed `&str` works for primitives but fails on owned types like
  `Vec<String>`.
- The CSP `connect-src` whitelist controls `fetch`/XHR, not `invoke`.
  Tauri's IPC uses its own `ipc:` and `http://ipc.localhost` origins —
  both are present in the shipped CSP. Don't remove them.
- The renderer never imports `@tauri-apps/api` directly. Every cross-
  process call goes through `Tauri.res` → `Core.Raw.invoke(...)`. If you
  find yourself wanting `import { invoke } from "@tauri-apps/api"`, you
  almost certainly want a new ReScript binding instead.
