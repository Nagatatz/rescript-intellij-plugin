Routes are declared explicitly in `app/routes.ts` using the helpers from
`@react-router/dev/routes`. The DX is similar to Remix v2 but the file
layout is up to you — there is no automatic file-system convention. The
shipped configuration mounts `routes/home.tsx` at `/`.

Add nested layouts with `layout("routes/dashboard.tsx", [...])`, dynamic
segments with `route(":id", "routes/show.tsx")`, and parallel index
routes with `index("routes/landing.tsx")`. Run `pnpm typecheck` (or the
equivalent for your package manager) after changes to regenerate the
typed `+types/...` modules consumed by each route component.
