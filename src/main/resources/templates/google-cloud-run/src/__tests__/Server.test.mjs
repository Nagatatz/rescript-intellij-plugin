import { describe, expect, it } from "vitest";

describe("Server module", () => {
  it("loads without throwing", async () => {
    await expect(import("../Server.res.mjs")).resolves.toBeDefined();
  });
});
