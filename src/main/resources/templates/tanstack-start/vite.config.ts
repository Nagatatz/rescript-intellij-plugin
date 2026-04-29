import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { tanstackStart } from "@tanstack/react-start/plugin/vite";
import { tanstackRouter } from "@tanstack/router-plugin/vite";

// TanStack Start ships with its own SSR + routing pipeline. Order matters:
// the router plugin must come before tanstackStart() so the generated route
// tree is available when Start wires the SSR entry, and react() runs last
// so Vite picks up JSX in TSX files (ReScript already compiles to plain JS).
export default defineConfig({
  plugins: [
    tanstackRouter({
      target: "react",
      autoCodeSplitting: true,
      routesDirectory: "src/routes",
      generatedRouteTree: "src/routeTree.gen.ts",
    }),
    tanstackStart(),
    react(),
  ],
});
