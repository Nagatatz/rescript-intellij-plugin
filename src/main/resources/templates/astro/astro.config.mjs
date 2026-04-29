import { defineConfig } from "astro/config";
import react from "@astrojs/react";
import node from "@astrojs/node";

// SSR via the Node adapter keeps the template runnable with `astro preview`
// after `astro build`. For a fully static site, drop `output: "server"` and
// remove the @astrojs/node dependency.
export default defineConfig({
  output: "server",
  adapter: node({ mode: "standalone" }),
  integrations: [react()],
});
