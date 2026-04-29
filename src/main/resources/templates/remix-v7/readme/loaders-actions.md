React Router v7's data layer has two halves. **Loaders** run on the server
before render and return data that the route receives via `loaderData`.
**Actions** run when the route receives a non-GET request (form submit,
fetcher call) and let you mutate state, then optionally redirect or
return data alongside the next render.

This template ships a ReScript loader in `app/loaders/HomeLoader.res`.
The home route imports it through `HomeLoader.res.mjs` and re-exports
the result. Add an action by exporting `export async function action(args)`
in the route file and posting from a `<Form method="post">`.
