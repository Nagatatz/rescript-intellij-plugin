// Async Server Component — runs on the server only.
//
// The `async` keyword unlocks server-side data fetching directly inside
// the component (no useEffect, no client waterfall, no API route). Any
// awaited value lands in the rendered HTML; secrets used here never
// reach the browser bundle. Parent layouts can wrap an async Server
// Component in `<Suspense>` to opt into streaming and a `loading.tsx`
// fallback (see `app/loading.tsx`).
import App from "../App.gen";
import GreetForm from "./client/GreetForm";

async function loadServerTimestamp(): Promise<string> {
  // Stand-in for a real fetch / DB / RPC call. Replace with your
  // server-only data source (process.env, Drizzle, Prisma, fetch).
  return new Date().toUTCString();
}

export default async function Page() {
  const serverGeneratedAt = await loadServerTimestamp();
  return (
    <main style={{ padding: "2rem", fontFamily: "sans-serif" }}>
      <App serverGeneratedAt={serverGeneratedAt} />
      <GreetForm />
    </main>
  );
}
