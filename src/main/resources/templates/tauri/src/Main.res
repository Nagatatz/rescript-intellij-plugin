module Log = RescriptTauriPluginLog.PluginLog

// Forward frontend logs to the JS console so anything emitted via
// `PluginLog.info(...)` (e.g. in HexInspector) is visible during dev.
let _ = Log.attachConsole()

switch ReactDOM.querySelector("#root") {
| Some(rootEl) =>
  ReactDOM.Client.Root.render(ReactDOM.Client.createRoot(rootEl), <App />)
| None => Console.error("Could not find root element")
}
