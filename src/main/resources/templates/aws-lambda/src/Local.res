// Local development entry point. Starts a Node HTTP server using
// `@hono/node-server` so the same `Server.app` can be exercised without
// deploying to Lambda. Unused by the esbuild bundle that ships to AWS —
// only `Server.res.mjs` is bundled, so this file (and its dev-only
// dependency `@hono/node-server`) never reach the Lambda artifact.
//
// Caveats vs. real Lambda invocation:
//   - `event.requestContext`, IAM authorizer claims, and other API Gateway
//     fields are absent here. Use SAM CLI (`sam local start-api`) or the
//     Lambda Runtime Interface Emulator if those need to be exercised.
let port = 3000
HonoNodeServer.serve({fetch: Server.app->HonoNodeServer.honoFetch, port})
Console.log("Local Lambda preview on http://localhost:" ++ Int.toString(port))
