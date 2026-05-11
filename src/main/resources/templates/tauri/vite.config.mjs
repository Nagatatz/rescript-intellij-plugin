import { defineConfig } from "vite-plus";
import react from "@vitejs/plugin-react";

// Tauri opens a single page (the file:// shell on prod, the dev URL on
// `tauri dev`). The renderer is purely client-side here — server-side
// rendering and code-splitting choices belong to the Rust process.
export default defineConfig({
  plugins: [react()],
  // Prevent Vite from obscuring Rust errors emitted on stderr.
  clearScreen: false,
  server: {
    port: 5173,
    strictPort: true,
  },
  // Relative base so the production bundle works when Tauri loads it via file://.
  base: "./",
});
