import { defineConfig } from "vite";
import { reactRouter } from "@react-router/dev/vite";

// React Router v7 (Framework mode) ships its own Vite plugin that owns the SSR
// pipeline; @vitejs/plugin-react is unnecessary because the router plugin
// already configures the React refresh runtime.
export default defineConfig({
  plugins: [reactRouter()],
});
