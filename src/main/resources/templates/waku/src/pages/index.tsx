import { make as Greet } from "../components/Greet.res.mjs";
import CounterClient from "../components/CounterClient";

// This file is a Server Component (Waku's default for files under src/pages).
// It can fetch data on the server and embed Client Components by importing
// modules that begin with `"use client"`.
export default function HomePage() {
  return (
    <main style={{ padding: "2rem", fontFamily: "system-ui, sans-serif" }}>
      <Greet name="{{projectName}}" />
      <p>The greeting above is a Server Component — no JS shipped for it.</p>
      <CounterClient initial={0} />
    </main>
  );
}

export const getConfig = async () => ({ render: "static" });
