// Vite+ unified config. The `vp` CLI (vp dev / vp build / vp test / vp check)
// reads this file plus the project package.json to wire Vitest, Oxlint, and
// Oxfmt with sensible defaults — no separate vitest.config / .eslintrc /
// .prettierrc files are needed.
import { defineConfig } from "vite-plus";
import react from "@vitejs/plugin-react";
import { inertiaPages } from "@hono/inertia/vite";

export default defineConfig({
  plugins: [react(), inertiaPages()],
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
