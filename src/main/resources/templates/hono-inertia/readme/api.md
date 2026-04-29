| Method | Path     | Behavior                                                              |
|--------|----------|-----------------------------------------------------------------------|
| GET    | `/`      | Renders the `Home` page with `{title, message}` props                 |
| GET    | `/about` | Renders the `About` page with `{title, stack[]}` props                |
| POST   | `/greet` | Validates the JSON body via `Validation.parseGreetForm`, re-renders Home with a flash |
| GET    | `/health`| Plain JSON `{status:"ok"}` (not Inertia — useful for liveness probes) |

Inertia visits set the `X-Inertia` header on the request and receive a JSON
page object back; the same routes return the HTML host page when accessed
without that header (e.g. by typing the URL in the browser).
