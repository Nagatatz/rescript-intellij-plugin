// Suspense fallback rendered while async Server Components in this
// segment resolve. Next.js automatically wraps `page.tsx` in a
// `<Suspense>` boundary that uses this file. Replace with a real
// skeleton or spinner once you decide on a design system.
export default function Loading() {
  return (
    <main style={{ padding: "2rem", fontFamily: "sans-serif" }}>
      <p style={{ color: "#666" }}>Loading…</p>
    </main>
  );
}
