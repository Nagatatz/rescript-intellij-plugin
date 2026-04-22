The project ships a multi-stage `Dockerfile` based on the official
`oven/bun:1` image so deployments stay consistent between local Docker,
CI registries, and managed Bun-friendly platforms.

```bash
docker build -t my-res-x .
docker run --rm -p 4444:4444 my-res-x
```

The build pipeline resolves in three layers:

1. `deps` — runs `bun install --ignore-scripts` against whichever
   lockfile is present (pnpm or bun).
2. `builder` — runs `bunx rescript` then `bun run build` so the server
   modules and any client assets under `dist/` are ready.
3. `runtime` — the slim layer that only carries `node_modules/`, the
   compiled `src/`, and `dist/` and launches `bun run src/App.res.mjs`.

Managed platforms that speak OCI images (Fly.io, Railway, Render,
Google Cloud Run, Scaleway Serverless Containers) can deploy the same
image directly. For single-binary workflows, swap the `runtime` stage's
`CMD` for `./dist/app` after running `bun run compile` locally.
