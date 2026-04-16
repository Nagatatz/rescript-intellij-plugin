The server serves OpenAPI docs out of the box:

- `/openapi.json` — raw spec (OpenAPI 3.1)
- `/docs` — Scalar UI explorer

Route specs live alongside the handlers using `createRoute` from
`@hono/zod-openapi`, so request/response validation and the spec never drift.
Generate TypeScript/Swift/Python SDKs from the spec with `openapi-generator` if needed.
