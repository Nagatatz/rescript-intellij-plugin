TanStack Start runs Server Functions through `createServerFn`. The wrapper
guarantees that the body only executes on the server and exposes an
`async` thunk that the client may call directly — there is no manual REST
or RPC plumbing. This template bundles a sample function in
`app/server/Greet.res` that returns a greeting; the home route loader and
its button both invoke it without leaving the React tree.

To add a new function, drop a `.res` file under `app/server/`, define a
`createServerFn(...)` thunk, then import it from any route or component.
The Vite plugin wires the boundary automatically.
