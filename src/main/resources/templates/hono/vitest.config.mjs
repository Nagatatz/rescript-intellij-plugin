import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    // Run before any test file's static imports execute, so `Db.res`'s
    // top-level `createClient(...)` sees the in-memory libsql URL instead
    // of opening the on-disk `./data/app.db`.
    setupFiles: ["./vitest.setup.mjs"],
  },
});
