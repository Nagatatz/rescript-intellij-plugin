`src/App.res` is annotated `@genType` and consumed from `src/app/page.tsx`, a
**Server Component**. It has no state and no browser-only APIs. The form with
`useState` lives in `src/GreetForm.res`, consumed from `src/app/client/GreetForm.tsx`
which opts in with `"use client"`. Keep stateful ReScript components behind a
`use client` boundary; keep pure rendering components on the server side.