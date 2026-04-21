// Mounts the app with a RescriptRelay environment provider and a React.Suspense
// boundary that catches data-fetch suspensions from queries.
switch ReactDOM.querySelector("#root") {
| Some(root) =>
  ReactDOM.Client.createRoot(root)->ReactDOM.Client.Root.render(
    <RescriptRelay.Context.Provider environment={RelayEnvironment.environment}>
      <React.Suspense fallback={React.string("Loading...")}>
        <App />
      </React.Suspense>
    </RescriptRelay.Context.Provider>,
  )
| None => Console.error("No #root element found")
}
