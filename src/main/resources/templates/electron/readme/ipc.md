### Security model

The bundled `BrowserWindow` is configured with the **modern hardened
defaults**:

| Setting | Value | Why |
| --- | --- | --- |
| `contextIsolation` | `true` | Keeps preload globals out of the page's `window` |
| `nodeIntegration` | `false` | Renderer cannot `require("fs")`; no Node escape from a compromised dep |
| Preload bridge | `contextBridge.exposeInMainWorld` | Only the surface you opt-in to crosses the boundary |

The renderer never imports Electron directly. Every cross-process call
goes through `window.electronAPI.*` → `ipcRenderer.invoke(...)` →
`ipcMain.handle(...)`. Validate every payload that re-enters the
renderer with `Validation.res` so a buggy main-process change can't
crash the UI silently.

### Naming convention: `domain:action`

The shipped `app:getInfo` channel demonstrates the recommended naming
scheme: a short domain prefix + `:` + a verb-shaped action.

```
app:getInfo          # app metadata
window:setTitle      # window-related actions
fs:readUserFile      # filesystem actions
db:listTodos         # data layer
```

Avoid bare verbs (`getInfo`, `save`) — they collide as the surface
grows and obscure intent in logs. Avoid filenames in channel names
(`Settings.ts:save`) — IPC contracts are public to the renderer and
should outlive any single file.

### Adding a new IPC channel

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
3. **Bind it in `src/Electron.res`** (extend the externals object so
   ReScript sees the new method):
   ```rescript
   @val external electronAPI: {
     "getInfo": unit => promise<JSON.t>,
     "setWindowTitle": string => promise<unit>,
   } = "electronAPI"

   let setWindowTitle = (title: string) => electronAPI["setWindowTitle"](title)
   ```
4. **Validate the response** in `Validation.res` if the handler returns
   data the UI consumes — a stray `null` or shape change in main is far
   easier to debug as a parse error than as a `TypeError` deep inside
   React.

### Things that bite

- `ipcRenderer.send` is fire-and-forget — use `invoke`/`handle` for
  request/response. The shipped pattern uses `invoke` exclusively.
- Functions, class instances, and DOM nodes cannot cross the bridge.
  Stringify, plain objects only.
- `app:*` is conventionally reserved for app-lifecycle channels; pick
  a different domain for feature work.
