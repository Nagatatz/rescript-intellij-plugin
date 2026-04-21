import { describe, it, expect } from "vitest"

// Smoke test that the compiled App module loads without throwing.
// Full HTTP coverage requires Bun + res-x runtime, which is exercised
// manually via `bun --watch src/App.res.mjs` rather than in Vitest.
describe("App module", () => {
  it("compiles to an importable module", async () => {
    const module = await import("../App.res.mjs")
    expect(module).toBeDefined()
  })
})
