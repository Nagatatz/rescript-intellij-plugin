The starter ships two example components that demonstrate the core patterns
you will reach for when building a res-x application.

**Counter** (`src/Counter.res`) keeps its value in a Bun process-local `ref`
and exposes `hx-post` endpoints for `/counter/increment` and
`/counter/decrement`. Each handler returns the refreshed `<span>` and HTMX
swaps it into place via `hx-swap="outerHTML"` — no page reload.

**Todo form** (`src/TodoForm.res`) posts to `/todos` with form-encoded input,
runs it through `Validation.parseTodoInput` ({{validationLib}} schemas), and
either re-renders the list or sends back the form with a status 400 and an
inline error. Extending the form with new fields means adding a field to the
schema and surfacing the new input.

Routing lives in `src/App.res`: every path not claimed by an HTMX handler
falls through to the render function, which pattern-matches on `path` and
returns JSX that res-x converts to an HTML response.
