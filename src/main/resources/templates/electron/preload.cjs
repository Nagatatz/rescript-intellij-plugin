// Exposes a typed, minimal API to the renderer via contextBridge so the
// renderer does not need Node integration enabled.
const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("electronAPI", {
  getInfo: () => ipcRenderer.invoke("app:getInfo"),
});