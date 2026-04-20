// graphql-yoga bindings: buildSchema + createYoga. Yoga returns a Fetch-compatible
// handler that Hono's context.req can pass through directly.
@module("graphql") external buildSchema: string => 'schema = "buildSchema"

type yogaHandler<'response> = Hono.request => promise<'response>

@module("graphql-yoga")
external createYoga: {
  "schema": 'schema,
  "rootValue": 'resolvers,
} => yogaHandler<'response> = "createYoga"
