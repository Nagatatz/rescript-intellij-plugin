import { defineConfig } from "vite"
import resXVitePlugin from "rescript-x/res-x-vite-plugin.mjs"

export default defineConfig({
  plugins: [
    resXVitePlugin({
      clientDirs: ["client"],
    }),
  ],
  server: {
    port: 9000,
  },
})
