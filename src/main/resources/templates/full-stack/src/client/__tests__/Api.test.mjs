import { describe, expect, it } from "vitest";

describe("client Api module", () => {
  it("loads without throwing", async () => {
    await expect(import("../Api.res.mjs")).resolves.toBeDefined();
  });
});
