### Build & deploy

```bash
gcloud builds submit --tag gcr.io/PROJECT-ID/{{projectName}}
gcloud run deploy {{projectName}} \\
  --image gcr.io/PROJECT-ID/{{projectName}} \\
  --port 8080 \\
  --allow-unauthenticated
```

### Container layout

The bundled `Dockerfile` is **multi-stage**:

| Stage | Purpose | Contents |
| --- | --- | --- |
| `builder` | Install all deps + compile ReScript | `node_modules` (full), source `.res`, generated `.res.mjs` |
| `runtime` | What ships to Cloud Run | Production deps only + compiled `.res.mjs` |

The runtime stage:

- runs as a **non-root user** (`node` / `bun`, uid 1000) — required by most
  hardened Cloud Run policies and a defence-in-depth measure regardless;
- sets `NODE_ENV=production`;
- pins the base image to the Node major declared in `package.json` /
  `.nvmrc`, so `gcloud run deploy` and local `node --watch` use the same
  runtime;
- never copies `node_modules` from the host — production deps are
  reinstalled inside the runtime stage so the image is reproducible.

### Useful follow-ups

```bash
# Inspect the deployed revision's image digest (pin in CD if you want immutable rollouts):
gcloud run services describe {{projectName}} --format="value(spec.template.spec.containers[0].image)"

# Stream logs:
gcloud run services logs tail {{projectName}}
```
