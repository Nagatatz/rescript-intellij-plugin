Waku is a small, opinionated framework focused on RSC. Highlights:

- File-system routing via `src/pages/`. Each `index.tsx`, `[slug].tsx`,
  and nested `pages/<route>/index.tsx` becomes a route.
- `getConfig()` per page declares whether to render `"static"` (built
  ahead of time) or `"dynamic"` (rendered per request).
- Server Functions: any function exported from a server file can be
  called from a Client Component by importing it; Waku rewires the call
  into an RPC at build time.

Refer to the Waku docs (https://waku.gg) for the latest configuration
conventions and the available render modes.
