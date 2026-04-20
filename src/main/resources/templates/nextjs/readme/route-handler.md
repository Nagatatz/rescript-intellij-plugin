The POST /api/greet endpoint is fully ReScript:

- `src/app/api/greet/GreetRoute.res` contains the handler body
- `src/app/api/greet/Validation.res` runs schema validation (zod or sury)
- `src/app/api/greet/route.ts` is a one-line shim that re-exports the `post`
  function as `POST` so Next.js's file-system routing picks it up

`src/NextServer.res` provides minimal bindings for `next/server`
(`NextRequest`, `NextResponse.json`). Extend it as you add more Route Handlers
rather than hand-writing `@module("next/server")` externals in each file.

On validation failure the handler returns HTTP 400 with `{ "error": "..." }`.
