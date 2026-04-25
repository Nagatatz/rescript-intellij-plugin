| File | Purpose |
| --- | --- |
| `src/app/page.tsx` | **Async** Server Component — fetches server-only data and threads it into `App.res` |
| `src/app/loading.tsx` | Suspense fallback rendered while `page.tsx` resolves |
| `src/app/client/GreetForm.tsx` | Client wrapper around ReScript form |
| `src/app/api/greet/route.ts` | POST /api/greet Route Handler |
| `src/App.res` | ReScript server-rendered component (accepts a `serverGeneratedAt` prop) |
| `src/GreetForm.res` | ReScript client component (state + fetch) |
| `src/Fetch.res` | Fetch wrapper shared by clients |