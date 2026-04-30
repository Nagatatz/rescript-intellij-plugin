// Vite+ unified config. The `vp` CLI (vp dev / vp build / vp test / vp check)
// reads this file plus the project package.json to wire Vitest, Oxlint, and
// Oxfmt with sensible defaults — no separate vitest.config / .eslintrc /
// .prettierrc files are needed.
//
// The Hono Inertia type-safety plugin is intentionally omitted: it generates
// a TypeScript-only `pages.gen.ts` that ReScript does not consume, since
// `c.render` is already typed through HonoInertia.res externals.
import { defineConfig } from "vite-plus";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Forward Inertia GET/POST requests to the Hono server during dev.
      // The Hono dev server is expected on http://localhost:3000 (see
      // ServerMain.res). Static assets and HMR continue to be served by
      // Vite+ directly.
      "^/(?!@vite|src|node_modules|@id|@fs).*": {
        target: "http://localhost:3000",
        changeOrigin: true,
      },
    },
  },
});
