import { createFileRoute, useRouter } from "@tanstack/react-router";
import { useState } from "react";
import { make as Greeting } from "../components/Greeting.res.mjs";
import { greet } from "../server/Greet.res.mjs";

export const Route = createFileRoute("/")({
  loader: async () => ({ initial: await greet({ data: "{{projectName}}" }) }),
  component: HomeComponent,
});

function HomeComponent() {
  const router = useRouter();
  const { initial } = Route.useLoaderData();
  const [message, setMessage] = useState<string>(initial);

  return (
    <main style={{ padding: "2rem", fontFamily: "system-ui, sans-serif" }}>
      <Greeting name={message} />
      <button
        type="button"
        onClick={async () => {
          const next = await greet({ data: "TanStack Start" });
          setMessage(next);
          router.invalidate();
        }}
      >
        Re-greet from server
      </button>
    </main>
  );
}
