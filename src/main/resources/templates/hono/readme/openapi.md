The server serves OpenAPI docs out of the box:

- `/openapi.json` — raw spec (OpenAPI 3.1)
- `/docs` — Scalar UI explorer

The default `/openapi.json` is a stub. Populate it by generating an OpenAPI document
from your schema module (zod variant ships `@hono/zod-openapi` bindings in
`ZodOpenapi.res`; sury variant can generate a JSON Schema via `S.toJSONSchema`
and assemble the document in `Server.res`). Generate TypeScript/Swift/Python SDKs
from the spec with `openapi-generator` if needed.
