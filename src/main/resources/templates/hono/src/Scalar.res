// @scalar/hono-api-reference mounts an interactive API explorer.
@module("@scalar/hono-api-reference")
external apiReference: 'opts => (Hono.context => 'a) = "apiReference"
