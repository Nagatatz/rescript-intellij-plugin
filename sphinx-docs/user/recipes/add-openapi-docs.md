---
myst:
  html_meta:
    "keywords": "openapi, scalar, hono, zod, rescript, api docs, swagger"
---

# Add OpenAPI Docs

The Hono REST template ships with [Scalar UI](https://github.com/scalar/scalar) at `/docs` and an OpenAPI 3.1 spec at `/openapi.json`, powered by `@hono/zod-openapi`. This recipe explains how the pieces fit together and how to enrich the generated spec.

## What You Get

- `src/ZodOpenapi.res` — ReScript bindings over `@hono/zod-openapi`
- `src/Scalar.res` — bindings over `@scalar/hono-api-reference`
- `src/Server.res` — wires `/openapi.json` and `/docs`
- `src/Routes/Users.res` — users CRUD exposed through the spec

Open `http://localhost:3000/docs` after `pnpm dev` to see the interactive UI.

## How It Wires Together

```
Zod schema ──► @hono/zod-openapi createRoute ──► app.openapi(route, handler)
                                                      │
                                                      ├─► /openapi.json (spec)
                                                      └─► Route handler (validated input/output)

/openapi.json ──► Scalar.apiReference ──► /docs (interactive UI)
```

## Define a Zod Schema

```rescript
// src/Schemas.res
let createUser = ZodOpenapi.object({
  "name": ZodOpenapi.string(~minLength=1, ()),
  "email": ZodOpenapi.string(~format="email", ()),
})

let user = ZodOpenapi.object({
  "id": ZodOpenapi.number(),
  "name": ZodOpenapi.string(),
  "email": ZodOpenapi.string(~format="email", ()),
})
```

## Attach to a Route

```rescript
let route = ZodOpenapi.createRoute({
  "method": "post",
  "path": "/users",
  "request": {
    "body": {
      "content": {
        "application/json": {"schema": Schemas.createUser},
      },
    },
  },
  "responses": {
    "201": {
      "description": "Created",
      "content": {
        "application/json": {"schema": Schemas.user},
      },
    },
    "400": {"description": "Validation error"},
  },
})

app->ZodOpenapi.openapi(route, async ctx => {
  let body = ctx->ZodOpenapi.validBody
  /* body is typed from Schemas.createUser */
})
```

## Describe the API

Add metadata so Scalar UI renders a title, description, and server list:

```rescript
// src/Server.res
app->ZodOpenapi.doc("/openapi.json", {
  "openapi": "3.1.0",
  "info": {
    "title": "My API",
    "version": "1.0.0",
    "description": "User management service.",
  },
  "servers": [
    {"url": "http://localhost:3000", "description": "Local"},
    {"url": "https://api.example.com", "description": "Production"},
  ],
})
```

## Group Routes with Tags

Tags control how Scalar UI groups endpoints in the sidebar:

```rescript
let route = ZodOpenapi.createRoute({
  "method": "get",
  "path": "/users",
  "tags": ["Users"],
  /* ... */
})
```

## Security

Wire bearer-token auth into the spec:

```rescript
app->ZodOpenapi.registerComponent("securitySchemes", "BearerAuth", {
  "type": "http",
  "scheme": "bearer",
  "bearerFormat": "JWT",
})

let protectedRoute = ZodOpenapi.createRoute({
  /* ... */
  "security": [{"BearerAuth": []}],
})
```

Scalar UI renders an "Authorize" button so visitors can paste their token.

## Exporting the Spec

`curl http://localhost:3000/openapi.json > openapi.json` dumps the spec to disk. Wire this into CI to validate that the spec stays stable across commits, or feed it to a client generator (`openapi-generator`, `orval`, `@hey-api/openapi-ts`).

## Switching UIs

Scalar is the default, but the spec at `/openapi.json` works with any OpenAPI renderer:

- [Swagger UI](https://github.com/swagger-api/swagger-ui)
- [Redoc](https://github.com/Redocly/redoc)
- [Stoplight Elements](https://stoplight.io/open-source/elements)

Swap `Scalar.apiReference` in `src/Server.res` for the equivalent handler.

## Common Pitfalls

- **Route not showing in `/docs`** — ensure you used `app.openapi(route, handler)` rather than `app.get(...)`. Plain Hono routes don't appear in the spec.
- **Scalar UI blank** — check the browser console. Usually `/openapi.json` returned HTML (error page) instead of JSON. Visit it directly to see the actual error.
- **Zod schema mismatch** — when a response handler returns a shape Zod can't coerce, you'll see a 500 with a validation error. Use `ZodOpenapi.string(~format="email", ())` style helpers to match the runtime shape.

## Related

- [Add a Hono endpoint](add-hono-endpoint.md) — adding routes end-to-end
- [Set up Drizzle](setup-drizzle.md) — persisting validated payloads
- [Project Templates](../templates/index.md) — the template catalog
