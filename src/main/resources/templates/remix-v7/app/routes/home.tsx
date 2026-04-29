import type { Route } from "./+types/home";
import { make as Greet } from "../components/Greet.res.mjs";
import { homeLoader } from "../loaders/HomeLoader.res.mjs";

export function meta(_: Route.MetaArgs) {
  return [{ title: "{{projectName}}" }, { name: "description", content: "ReScript + React Router v7" }];
}

export async function loader(_: Route.LoaderArgs) {
  return homeLoader({ project: "{{projectName}}" });
}

export default function Home({ loaderData }: Route.ComponentProps) {
  return (
    <main style={{ padding: "2rem", fontFamily: "system-ui, sans-serif" }}>
      <Greet name={loaderData.name} />
      <p>Loaders run on the server; the value above came from HomeLoader.res.</p>
    </main>
  );
}
