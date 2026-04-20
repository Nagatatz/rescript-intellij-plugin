// Bindings for @hono/zod-openapi. createRoute accepts a declarative route spec
// that fuels both runtime routing + OpenAPI 3 generation.
type openapiApp
@module("@hono/zod-openapi") @new external createApp: unit => openapiApp = "OpenAPIHono"
@module("@hono/zod-openapi") external createRoute: 'routeSpec => 'route = "createRoute"
@send external openapiRoute: (openapiApp, 'route, 'handler) => unit = "openapi"
@send external doc: (openapiApp, string, 'meta) => unit = "doc"
