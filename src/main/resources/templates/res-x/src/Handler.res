// Per-request context shared across components and HTMX handlers.
// Extend this record with fields you want available via `useContext()` —
// e.g. a database connection, authenticated user, or request metadata.
type context = {userId: option<string>}

let handler = ResX.Handlers.make(~requestToContext=async _request => {
  userId: None,
})

let useContext = () => handler.useContext()
