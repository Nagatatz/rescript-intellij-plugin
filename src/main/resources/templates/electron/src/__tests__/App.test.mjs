import { describe, expect, it } from "vitest";

describe("App module", () => {
  it("loads without throwing", async () => {
    await expect(import("../App.res.mjs")).resolves.toBeDefined();
  });
});
