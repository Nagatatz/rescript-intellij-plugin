const { app, BrowserWindow, ipcMain } = require("electron");
const path = require("path");
const os = require("os");

function createWindow() {
  const win = new BrowserWindow({
    width: 800,
    height: 600,
    title: "{{projectName}}",
    webPreferences: {
      preload: path.join(__dirname, "preload.cjs"),
      contextIsolation: true,
      nodeIntegration: false,
    },
  });

  win.loadFile(path.join(__dirname, "dist", "index.html"));
}

// IPC handlers live in the main process and are invoked from the renderer
// via the `electronAPI` object exposed in preload.cjs.
ipcMain.handle("app:getInfo", () => ({
  name: "{{projectName}}",
  electronVersion: process.versions.electron,
  platform: os.platform(),
  arch: os.arch(),
}));

app.whenReady().then(createWindow);

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});