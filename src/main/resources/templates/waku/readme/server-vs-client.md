React Server Components let you mix two execution environments in a single
component tree:

- **Server Components** render on the server (or at build time), can read
  databases / filesystem / env vars, and ship zero JS for their own UI.
  Files under `src/pages/` are Server Components by default in Waku.
- **Client Components** ship as JavaScript and run in the browser, where
  they can use hooks (`useState`, `useEffect`, etc.) and respond to user
  events. Their file must begin with the directive `"use client"`.

ReScript does not emit `"use client"` at the top of compiled `.res.mjs`
files, so this template ships a thin TSX wrapper (`CounterClient.tsx`)
that contains the directive and re-exports the ReScript `make` function.
This is the canonical pattern: keep the wrapper as small as possible, put
all UI logic in the `.res` file.
