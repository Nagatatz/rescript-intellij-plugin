import { describe, it, expect } from "bun:test"

// Smoke test that the compiled App module loads under Bun. The runtime
// references the global `Bun` object via rescript-bun, so this suite has
// to run under `bun test` rather than vitest (which executes under Node).
describe("App module", () => {
  it("compiles to an importable module", async () => {
    const module = await import("../App.res.mjs")
    expect(module).toBeDefined()
  })
})
