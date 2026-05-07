Run the Hono app on Node.js for fast iteration without deploying to Lambda.
Use two terminals:

```bash
# Terminal 1 — keep ReScript sources compiled
{{cmdResDev}}

# Terminal 2 — run the local server with file watching
{{cmdDev}}
```

The server listens on `http://localhost:3000`. Routes mirror the deployed
Lambda exactly because both entry points share `Server.app`.

`@hono/node-server` is a `devDependency` only; it is **not** included in the
esbuild bundle (which entry-points `src/Server.res.mjs`), so the Lambda
artifact stays the same size.

### When local-only is not enough

Plain `node` cannot reproduce API Gateway fields such as
`event.requestContext`, IAM authorizer claims, or path stage prefixes. If
you need that level of fidelity, use one of:

- **AWS SAM CLI** — `sam local start-api` runs the function in a Docker
  container that emulates API Gateway.
- **Lambda Runtime Interface Emulator (RIE)** — the official lightweight
  emulator embedded in the AWS-provided base images.
