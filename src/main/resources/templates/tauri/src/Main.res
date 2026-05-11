switch ReactDOM.querySelector("#root") {
| Some(rootEl) =>
  ReactDOM.Client.Root.render(ReactDOM.Client.createRoot(rootEl), <App />)
| None => Console.error("Could not find root element")
}
