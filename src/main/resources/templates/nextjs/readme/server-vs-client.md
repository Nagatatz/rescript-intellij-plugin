`src/App.res` is annotated `@genType` and consumed from `src/app/page.tsx`, an
**async Server Component**. `page.tsx` runs server-only data fetching (the
template ships a stand-in `loadServerTimestamp()` — replace with your real
fetch / DB / RPC call) and threads the result down to `App.res` as a prop.
That prop reaches the rendered HTML without any client-side waterfall.

The form with `useState` lives in `src/GreetForm.res`, consumed from
`src/app/client/GreetForm.tsx` which opts in with `"use client"`. Keep
stateful ReScript components behind a `use client` boundary; keep pure or
async-data-fetching components on the server side.

### Async Server Component pattern

```tsx
// app/page.tsx
export default async function Page() {
  const data = await loadFromDatabase();   // server-only
  return <App data={data} />;              // ReScript via App.gen
}
```

```rescript
// src/App.res
@genType @react.component
let make = (~data: string) => <p>{React.string(data)}</p>
```

When the server work is slow, wrap the boundary in `<Suspense>`:

```tsx
import { Suspense } from "react";
<Suspense fallback={<Skeleton />}><App data={...} /></Suspense>
```

The bundled `app/loading.tsx` is the implicit Suspense fallback for the
route segment — Next.js auto-wraps `page.tsx` with it. Edit it to match
your design system.
